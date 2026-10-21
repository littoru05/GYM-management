// Verification script for axiosClient interceptors
import assert from 'node:assert'

globalThis.document = {
  createElement: () => {
    const el = { parentNode: null, appendChild() {}, firstChild: null, data: '' }
    el.firstChild = el
    return el
  },
  head: { appendChild() {} },
  querySelector: () => null,
}

// 1. Mock browser window & storage environment
globalThis.window = {
  location: {
    origin: 'http://localhost:3000',
    pathname: '/admin/dashboard',
    href: 'http://localhost:3000/admin/dashboard',
  },
  navigator: {
    onLine: true,
  },
  localStorage: {
    _data: {},
    getItem(k) {
      return this._data[k] || null
    },
    setItem(k, v) {
      this._data[k] = String(v)
    },
    removeItem(k) {
      delete this._data[k]
    },
    clear() {
      this._data = {}
    },
  },
  sessionStorage: {
    _data: {},
    getItem(k) {
      return this._data[k] || null
    },
    setItem(k, v) {
      this._data[k] = String(v)
    },
    removeItem(k) {
      delete this._data[k]
    },
    clear() {
      this._data = {}
    },
  },
}

// Import useAuthStore & axiosClient
const { useAuthStore } = await import('./src/store/useAuthStore.js')
const { default: axiosClient, getStoredToken } = await import('./src/api/axiosClient.js')

console.log('--- STARTING INTERCEPTOR UNIT TESTS ---')

// TEST 1: Request Interceptor - extracts token from Zustand store
{
  useAuthStore.getState().setAuth({ id: 1, name: 'Admin' }, 'zustand-jwt-token-123')
  const token = getStoredToken()
  assert.strictEqual(token, 'zustand-jwt-token-123', 'Should get token from Zustand store')

  const config = { headers: {} }
  const requestHandler = axiosClient.interceptors.request.handlers[0].fulfilled
  const updatedConfig = await requestHandler(config)
  assert.strictEqual(
    updatedConfig.headers.Authorization,
    'Bearer zustand-jwt-token-123',
    'Authorization header must match Bearer zustand-jwt-token-123'
  )
  console.log('✓ PASS: Request Interceptor extracts token from Zustand store')
}

// TEST 2: Request Interceptor - extracts fallback token from localStorage
{
  useAuthStore.getState().clearAuth()
  globalThis.window.localStorage.setItem('token', 'localstorage-jwt-456')
  const token = getStoredToken()
  assert.strictEqual(token, 'localstorage-jwt-456', 'Should get fallback token from localStorage')

  const config = { headers: {} }
  const requestHandler = axiosClient.interceptors.request.handlers[0].fulfilled
  const updatedConfig = await requestHandler(config)
  assert.strictEqual(
    updatedConfig.headers.Authorization,
    'Bearer localstorage-jwt-456',
    'Authorization header must match Bearer localstorage-jwt-456'
  )
  globalThis.window.localStorage.clear()
  console.log('✓ PASS: Request Interceptor falls back to localStorage token')
}

// TEST 3: Request Interceptor - no token attached when unauthenticated
{
  useAuthStore.getState().clearAuth()
  globalThis.window.localStorage.clear()
  globalThis.window.sessionStorage.clear()
  assert.strictEqual(getStoredToken(), null, 'No token in storage')

  const config = { headers: {} }
  const requestHandler = axiosClient.interceptors.request.handlers[0].fulfilled
  const updatedConfig = await requestHandler(config)
  assert.strictEqual(updatedConfig.headers.Authorization, undefined, 'No Authorization header attached')
  console.log('✓ PASS: Request Interceptor attaches nothing when unauthenticated')
}

// TEST 4: Response Interceptor - catches 401 Unauthorized, clears token and triggers redirect
{
  useAuthStore.getState().setAuth({ id: 1, name: 'Admin' }, 'expired-token')
  globalThis.window.localStorage.setItem('token', 'expired-token')
  globalThis.window.location.pathname = '/admin/reports'
  globalThis.window.location.href = 'http://localhost:3000/admin/reports'

  const responseErrorHandler = axiosClient.interceptors.response.handlers[0].rejected
  const error401 = {
    response: {
      status: 401,
      data: { message: 'JWT expired' },
    },
    config: { url: '/admin/reports/revenue' },
  }

  let rejected = false
  try {
    await responseErrorHandler(error401)
  } catch (err) {
    rejected = true
    assert.strictEqual(err.response.status, 401)
  }

  assert.strictEqual(rejected, true, 'Interceptor must re-reject the 401 error')
  assert.strictEqual(useAuthStore.getState().accessToken, null, 'Zustand accessToken must be cleared')
  assert.strictEqual(useAuthStore.getState().isAuthenticated, false, 'Auth state must be unauthenticated')
  assert.strictEqual(globalThis.window.localStorage.getItem('token'), null, 'localStorage token must be cleared')
  assert.strictEqual(
    globalThis.window.sessionStorage.getItem('auth_redirect_reason'),
    'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.',
    'Session storage redirect reason must be set'
  )

  // Wait 200ms for debounce redirect
  await new Promise((resolve) => setTimeout(resolve, 250))
  assert.strictEqual(globalThis.window.location.href, '/login', 'Must redirect to /login')

  console.log('✓ PASS: Response Interceptor catches 401, clears auth, sets redirect reason & redirects to /login')
}

// TEST 5: Response Interceptor - 401 on /auth/login does NOT redirect
{
  globalThis.window.location.pathname = '/login'
  globalThis.window.location.href = 'http://localhost:3000/login'
  globalThis.window.sessionStorage.clear()

  const responseErrorHandler = axiosClient.interceptors.response.handlers[0].rejected
  const errorLogin401 = {
    response: {
      status: 401,
      data: { message: 'Bad credentials' },
    },
    config: { url: '/auth/login' },
  }

  let rejected = false
  try {
    await responseErrorHandler(errorLogin401)
  } catch {
    rejected = true
  }

  assert.strictEqual(rejected, true, 'Interceptor must re-reject the login 401 error')
  assert.strictEqual(globalThis.window.location.href, 'http://localhost:3000/login', 'Href remains on /login')
  assert.strictEqual(
    globalThis.window.sessionStorage.getItem('auth_redirect_reason'),
    null,
    'Login 401 must not set session expiration reason'
  )
  console.log('✓ PASS: Response Interceptor handles login 401 without session expired redirect')
}

// TEST 6: Response Interceptor - catches ERR_NETWORK
{
  const responseErrorHandler = axiosClient.interceptors.response.handlers[0].rejected
  const errorNetwork = {
    code: 'ERR_NETWORK',
    message: 'Network Error',
    request: {},
    config: { url: '/members' },
  }

  let rejected = false
  try {
    await responseErrorHandler(errorNetwork)
  } catch (err) {
    rejected = true
    assert.strictEqual(
      err.userMessage,
      'Mất kết nối mạng (ERR_NETWORK). Vui lòng kiểm tra lại đường truyền internet.'
    )
  }

  assert.strictEqual(rejected, true, 'Interceptor must re-reject the network error')
  console.log('✓ PASS: Response Interceptor catches ERR_NETWORK and attaches clear userMessage')
}

// TEST 7: Response Interceptor - catches 403 Forbidden and 500 Server Error
{
  const responseErrorHandler = axiosClient.interceptors.response.handlers[0].rejected

  // 403
  let rejected403 = false
  try {
    await responseErrorHandler({
      response: { status: 403, data: { message: 'Quyền bị từ chối' } },
      config: { url: '/admin/staffs' },
    })
  } catch (err) {
    rejected403 = true
    assert.strictEqual(err.response.status, 403)
  }
  assert.strictEqual(rejected403, true)

  // 500
  let rejected500 = false
  try {
    await responseErrorHandler({
      response: { status: 500, data: { message: 'Lỗi máy chủ nội bộ' } },
      config: { url: '/pos/checkout' },
    })
  } catch (err) {
    rejected500 = true
    assert.strictEqual(err.response.status, 500)
  }
  assert.strictEqual(rejected500, true)

  console.log('✓ PASS: Response Interceptor catches 403 Forbidden and 500 Internal Server Error')
}

console.log('\n--- ALL INTERCEPTOR UNIT TESTS PASSED SUCCESSFULLY! ---')
