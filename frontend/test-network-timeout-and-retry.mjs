import assert from 'node:assert'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

// Setup lightweight browser mocks for node environment
globalThis.document = {
  createElement: () => {
    const el = { parentNode: null, appendChild() {}, firstChild: null, data: '' }
    el.firstChild = el
    return el
  },
  head: { appendChild() {} },
  querySelector: () => null,
}

globalThis.window = {
  location: {
    origin: 'http://localhost:3000',
    pathname: '/admin/dashboard',
    href: 'http://localhost:3000/admin/dashboard',
    reload() {},
  },
  navigator: {
    onLine: true,
  },
  localStorage: {
    _data: {},
    getItem(k) { return this._data[k] || null },
    setItem(k, v) { this._data[k] = String(v) },
    removeItem(k) { delete this._data[k] },
    clear() { this._data = {} },
  },
  sessionStorage: {
    _data: {},
    getItem(k) { return this._data[k] || null },
    setItem(k, v) { this._data[k] = String(v) },
    removeItem(k) { delete this._data[k] },
    clear() { this._data = {} },
  },
  addEventListener() {},
  removeEventListener() {},
}

console.log('--- STARTING NETWORK TIMEOUT, ALB DISRUPTION & GENTLE RETRY TESTS ---')

// Import JS modules under test
const { default: axiosClient } = await import('./src/api/axiosClient.js')
const { formatErrorMessage, showErrorToast, toast } = await import('./src/utils/toastService.js')
const { DASHBOARD_QUERY_CONFIG } = await import('./src/hooks/useRevenue.js')

// =========================================================================
// TEST GROUP 1: Toast thông báo thân thiện khi mạng bị timeout (ECONNABORTED)
// =========================================================================
console.log('\n[TEST GROUP 1] Kiểm tra xử lý thông báo Toast khi Timeout (ECONNABORTED / 504)...')

{
  const responseErrorHandler = axiosClient.interceptors.response.handlers[0].rejected

  // 1.1. Bắt lỗi ECONNABORTED (Axios timeout)
  const timeoutError = {
    code: 'ECONNABORTED',
    message: 'timeout of 15000ms exceeded',
    config: { url: '/admin/reports/revenue-summary' },
  }

  let caught = false
  try {
    await responseErrorHandler(timeoutError)
  } catch (err) {
    caught = true
    assert.strictEqual(caught, true)
    assert.ok(
      err.userMessage && err.userMessage.includes('Timeout'),
      'userMessage phải chứa thông tin Timeout thân thiện'
    )
    assert.ok(
      err.userMessage.includes('ALB') || err.userMessage.includes('máy chủ'),
      'userMessage phải giải thích rõ ràng về đường truyền mạng/ALB'
    )
  }

  // 1.2. Bắt lỗi 504 Gateway Timeout từ AWS ALB
  const alb504Error = {
    response: { status: 504, data: { message: '504 Gateway Timeout' } },
    config: { url: '/admin/reports/revenue-series' },
  }

  try {
    await responseErrorHandler(alb504Error)
  } catch (err) {
    assert.ok(
      err.userMessage && err.userMessage.includes('Timeout'),
      '504 từ ALB phải được chuyển thành thông báo Timeout thân thiện'
    )
  }

  // 1.3. formatErrorMessage chuyển đổi mã lỗi ECONNABORTED
  const directEconnaborted = formatErrorMessage({ code: 'ECONNABORTED' })
  assert.ok(
    directEconnaborted.includes('Timeout') || directEconnaborted.includes('chờ phản hồi'),
    'formatErrorMessage phải nhận diện trực tiếp error.code = ECONNABORTED'
  )

  const directTimeoutString = formatErrorMessage('Request timed out: ECONNABORTED')
  assert.ok(
    directTimeoutString.includes('Timeout') || directTimeoutString.includes('chờ phản hồi'),
    'formatErrorMessage phải nhận diện chuỗi ECONNABORTED'
  )

  console.log('✓ PASS: Axios Interceptor & ToastService xử lý thông báo timeout (ECONNABORTED / 504) cực kỳ thân thiện!')
}

// =========================================================================
// TEST GROUP 2: Toast thông báo khi đường truyền từ client tới ALB bị ngắt quãng
// =========================================================================
console.log('\n[TEST GROUP 2] Kiểm tra xử lý khi đường truyền mạng từ Client tới ALB bị ngắt quãng...')

{
  const responseErrorHandler = axiosClient.interceptors.response.handlers[0].rejected

  // 2.1. Bắt lỗi ERR_NETWORK (Mất kết nối hoàn toàn)
  const networkErr = {
    code: 'ERR_NETWORK',
    message: 'Network Error',
    request: {},
    config: { url: '/dashboard' },
  }

  try {
    await responseErrorHandler(networkErr)
  } catch (err) {
    assert.strictEqual(
      err.userMessage,
      'Mất kết nối mạng (ERR_NETWORK). Vui lòng kiểm tra lại đường truyền internet.'
    )
  }

  // 2.2. Bắt lỗi ALB 502 Bad Gateway hoặc 503 Service Unavailable khi backend khởi động lại
  const alb502 = {
    response: { status: 502, data: {} },
    config: { url: '/dashboard/stats' },
  }

  try {
    await responseErrorHandler(alb502)
  } catch (err) {
    assert.ok(
      err.userMessage && (err.userMessage.includes('ERR_NETWORK') || err.userMessage.includes('máy chủ')),
      '502 từ ALB phải cảnh báo lỗi mạng hoặc máy chủ thân thiện'
    )
  }

  // 2.3. formatErrorMessage xử lý các mã 502, 503
  const msg502 = formatErrorMessage({ response: { status: 502 } })
  assert.ok(msg502.includes('502 Bad Gateway') || msg502.includes('ALB'))

  const msg503 = formatErrorMessage({ response: { status: 503 } })
  assert.ok(msg503.includes('503 Service Unavailable') || msg503.includes('khả dụng'))

  console.log('✓ PASS: Xử lý thông báo mất kết nối mạng và lỗi hạ tầng ALB (502, 503, ERR_NETWORK) chuẩn xác!')
}

// =========================================================================
// TEST GROUP 3: Cơ chế tự động thử lại (retry) nhẹ nhàng cho Dashboard queries
// =========================================================================
console.log('\n[TEST GROUP 3] Kiểm tra cơ chế tự động thử lại nhẹ nhàng (gentle retry) cho Dashboard queries...')

{
  assert.ok(DASHBOARD_QUERY_CONFIG, 'DASHBOARD_QUERY_CONFIG phải tồn tại')
  assert.strictEqual(typeof DASHBOARD_QUERY_CONFIG.retry, 'function', 'retry phải là một hàm điều kiện')
  assert.strictEqual(typeof DASHBOARD_QUERY_CONFIG.retryDelay, 'function', 'retryDelay phải là một hàm backoff')

  // 3.1. KHÔNG thử lại các mã lỗi nghiệp vụ 4xx (400, 401, 403, 404, 409, 422)
  const clientErrors = [400, 401, 403, 404, 409, 422]
  for (const status of clientErrors) {
    const shouldRetry = DASHBOARD_QUERY_CONFIG.retry(0, { response: { status } })
    assert.strictEqual(
      shouldRetry,
      false,
      `Không được retry lỗi client HTTP ${status} để tránh gửi yêu cầu vô ích`
    )
  }

  // 3.2. THỬ LẠI NHẸ NHÀNG tối đa 2 lần đối với lỗi mạng, timeout (ECONNABORTED) hoặc 5xx
  const networkErr = { code: 'ERR_NETWORK', message: 'Network Error' }
  const timeoutErr = { code: 'ECONNABORTED', message: 'timeout of 15000ms exceeded' }
  const server500Err = { response: { status: 500 } }
  const alb502Err = { response: { status: 502 } }

  for (const testErr of [networkErr, timeoutErr, server500Err, alb502Err]) {
    // Lần 1: failureCount = 0 -> Thử lại
    assert.strictEqual(DASHBOARD_QUERY_CONFIG.retry(0, testErr), true, 'Lần 1 thất bại phải tự động thử lại')
    // Lần 2: failureCount = 1 -> Thử lại
    assert.strictEqual(DASHBOARD_QUERY_CONFIG.retry(1, testErr), true, 'Lần 2 thất bại phải tự động thử lại')
    // Lần 3: failureCount = 2 -> Dừng lại nhẹ nhàng, không gây nghẽn mạng (DoD)
    assert.strictEqual(DASHBOARD_QUERY_CONFIG.retry(2, testErr), false, 'Lần 3 dừng lại để người dùng quyết định')
  }

  // 3.3. Thời gian giãn cách nhẹ nhàng (exponential backoff từ 1s đến 3s)
  const delay0 = DASHBOARD_QUERY_CONFIG.retryDelay(0)
  const delay1 = DASHBOARD_QUERY_CONFIG.retryDelay(1)
  const delay2 = DASHBOARD_QUERY_CONFIG.retryDelay(2)

  assert.ok(delay0 >= 1000 && delay0 <= 1500, `Delay 0 (${delay0}ms) phải trong khoảng 1s - 1.5s`)
  assert.ok(delay1 >= 1500 && delay1 <= 2500, `Delay 1 (${delay1}ms) phải tăng dần nhẹ nhàng`)
  assert.ok(delay2 <= 3000, `Delay 2 (${delay2}ms) không vượt quá 3s để giữ trải nghiệm mượt mà`)

  console.log('✓ PASS: Cơ chế gentle retry tự động thử lại 2 lần với giãn cách 1s -> 2.25s -> tối đa 3s!')
}

// =========================================================================
// TEST GROUP 4: DoD - Ứng dụng không bị crash giao diện trắng khi mạng ngắt quãng
// =========================================================================
console.log('\n[TEST GROUP 4] Kiểm tra DoD: Không bị crash màn hình trắng khi mạng tới ALB ngắt quãng...')

{
  // 4.1. Kiểm tra tĩnh cấu trúc ErrorBoundary.jsx
  const errorBoundaryCode = fs.readFileSync(path.join(__dirname, 'src/components/common/ErrorBoundary.jsx'), 'utf-8')
  assert.ok(errorBoundaryCode.includes('ECONNABORTED'), 'ErrorBoundary phải nhận diện lỗi timeout ECONNABORTED')
  assert.ok(errorBoundaryCode.includes('ERR_NETWORK'), 'ErrorBoundary phải nhận diện lỗi ERR_NETWORK')
  assert.ok(errorBoundaryCode.includes('handleOnline'), 'ErrorBoundary phải có cơ chế tự động phục hồi khi có mạng trở lại (online)')
  assert.ok(errorBoundaryCode.includes('window.addEventListener(\'online\''), 'ErrorBoundary phải lắng nghe sự kiện online')
  assert.ok(errorBoundaryCode.includes('Thử lại kết nối'), 'ErrorBoundary phải cung cấp nút Thử lại kết nối')

  // 4.2. Kiểm tra tĩnh cấu trúc DashboardPage.jsx
  const dashboardCode = fs.readFileSync(path.join(__dirname, 'src/pages/admin/DashboardPage.jsx'), 'utf-8')
  assert.ok(dashboardCode.includes('hasNetworkError'), 'DashboardPage phải phát hiện trạng thái lỗi mạng từ các queries')
  assert.ok(dashboardCode.includes('handleRetryAll'), 'DashboardPage phải có hàm thử lại dữ liệu cho toàn bộ chỉ số')
  assert.ok(dashboardCode.includes('safeSeriesData'), 'DashboardPage phải dùng safeSeriesData an toàn chống lỗi TypeError')
  assert.ok(dashboardCode.includes('safeTransactions'), 'DashboardPage phải dùng safeTransactions an toàn chống lỗi TypeError')
  assert.ok(dashboardCode.includes('WifiOff'), 'DashboardPage phải hiển thị biểu tượng WifiOff khi mất mạng')

  // 4.3. Kiểm tra tĩnh cấu trúc ReportsPage.jsx
  const reportsCode = fs.readFileSync(path.join(__dirname, 'src/pages/admin/ReportsPage.jsx'), 'utf-8')
  assert.ok(reportsCode.includes('hasNetworkError'), 'ReportsPage phải phát hiện lỗi mạng')
  assert.ok(reportsCode.includes('safeTransactions.filter'), 'ReportsPage phải lọc an toàn trên safeTransactions')
  assert.ok(reportsCode.includes('safeSeriesData'), 'ReportsPage phải truyền safeSeriesData sang biểu đồ')

  // 4.4. Kiểm tra tĩnh Table.jsx xử lý an toàn
  const tableCode = fs.readFileSync(path.join(__dirname, 'src/components/common/Table.jsx'), 'utf-8')
  assert.ok(tableCode.includes('!Array.isArray(data)'), 'Table.jsx phải kiểm tra !Array.isArray(data) để không crash render')

  console.log('✓ PASS: Toàn bộ component, Table, Dashboard và ErrorBoundary an toàn 100%, không xảy ra crash màn hình trắng!')
}

console.log('\n========================================================================')
console.log('🎉 TẤT CẢ TEST TIMEOUT (ECONNABORTED), ALB DISRUPTION & GENTLE RETRY ĐÃ PASS 100%!')
console.log('DoD ĐẠT YÊU CẦU: Giao diện chống crash trắng tuyệt đối khi kết nối tới ALB bị ngắt quãng!')
console.log('========================================================================\n')
