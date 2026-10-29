import toast from 'react-hot-toast'

/**
 * Từ điển ánh xạ các mã lỗi kỹ thuật, exception class name hoặc chuỗi tiếng Anh từ Backend
 * sang thông điệp tiếng Việt thân thiện, rõ ràng cho người dùng.
 */
const TECHNICAL_ERROR_DICTIONARY = [
  // 1. Nghiệp vụ Voucher / Khuyến mãi
  {
    patterns: [
      /voucher.*(not found|hết hạn|không tồn tại|invalid|expired)/i,
      /mã voucher không tồn tại/i,
      /mã giảm giá không tồn tại/i,
      /VOUCHER_NOT_FOUND/i,
      /VOUCHER_EXPIRED/i,
    ],
    message: 'Mã voucher không tồn tại hoặc đã hết hạn',
  },
  {
    patterns: [/voucher.*(used|đã sử dụng|exceeded|hết lượt)/i],
    message: 'Mã voucher đã hết lượt sử dụng hoặc không thỏa điều kiện áp dụng',
  },

  // 2. Nghiệp vụ Kho hàng / Tồn kho / Sản phẩm
  {
    patterns: [
      /InsufficientStockException/i,
      /INSUFFICIENT_STOCK/i,
      /không đủ.*tồn kho/i,
      /số lượng tồn kho không đủ/i,
      /out of stock/i,
    ],
    message: 'Số lượng tồn kho không đủ',
  },
  {
    patterns: [/sku.*(duplicate|tồn tại|trùng)/i, /mã sku.*đã tồn tại/i],
    message: 'Mã SKU sản phẩm đã tồn tại trong hệ thống',
  },

  // 3. Nghiệp vụ Lọc ngày / Báo cáo
  {
    patterns: [
      /khoảng thời gian lọc không hợp lệ/i,
      /ngày bắt đầu.*ngày kết thúc/i,
      /startDate.*endDate/i,
      /INVALID_DATE_RANGE/i,
      /start.*date.*end.*date/i,
    ],
    message: 'Khoảng thời gian lọc không hợp lệ',
  },

  // 4. Trùng lặp dữ liệu / Vi phạm ràng buộc
  {
    patterns: [/DuplicateResourceException/i, /duplicat.*key/i, /trùng lặp/i],
    message: 'Dữ liệu bị trùng lặp hoặc đã tồn tại trong hệ thống.',
  },
  {
    patterns: [/DataIntegrityViolationException/i, /constraint.*violation/i],
    message: 'Dữ liệu không hợp lệ hoặc vi phạm ràng buộc toàn vẹn trong hệ thống.',
  },
  {
    patterns: [/foreign key/i, /cannot delete/i, /đang được sử dụng/i],
    message: 'Không thể xóa vì dữ liệu đang được liên kết trong hệ thống.',
  },

  // 5. Xác thực & Phân quyền
  {
    patterns: [/BadCredentialsException/i, /bad credentials/i, /sai mật khẩu/i, /mật khẩu không chính xác/i],
    message: 'Email hoặc mật khẩu không chính xác.',
  },
  {
    patterns: [/LockedException/i, /tài khoản.*khóa/i],
    message: 'Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.',
  },
  {
    patterns: [/DisabledException/i, /vô hiệu hóa/i],
    message: 'Tài khoản của bạn đã bị ngừng hoạt động.',
  },
  {
    patterns: [/AccessDeniedException/i, /forbidden/i, /không có quyền/i],
    message: 'Bạn không có quyền thực hiện thao tác này.',
  },

  // 6. Mạng & Hạ tầng (ALB / Timeout / Disconnected)
  {
    patterns: [
      /ECONNABORTED/i,
      /ETIMEDOUT/i,
      /timeout/i,
      /quá thời gian/i,
      /hết thời gian chờ/i,
    ],
    message: 'Hết thời gian chờ phản hồi từ máy chủ (Timeout). Đường truyền mạng tới ALB bị gián đoạn hoặc phản hồi chậm. Vui lòng thử lại.',
  },
  {
    patterns: [
      /ERR_NETWORK/i,
      /Network Error/i,
      /mất kết nối mạng/i,
      /đường truyền mạng bị gián đoạn/i,
      /mất kết nối máy chủ/i,
      /ECONNRESET/i,
      /ERR_CONNECTION_REFUSED/i,
    ],
    message: 'Mất kết nối mạng (ERR_NETWORK). Vui lòng kiểm tra lại đường truyền internet.',
  },
  {
    patterns: [/502 Bad Gateway/i, /503 Service Unavailable/i, /504 Gateway Timeout/i, /Bad Gateway/i],
    message: 'Cổng kết nối máy chủ (ALB) phản hồi lỗi hoặc tạm thời không khả dụng. Vui lòng thử lại sau.',
  },
]

/**
 * Trích xuất và chuyển đổi mã lỗi kỹ thuật từ backend thành thông điệp tiếng Việt rõ ràng.
 *
 * @param {any} error - Đối tượng lỗi từ Axios, Error thông thường, hoặc chuỗi thông báo
 * @param {string} [fallbackMessage] - Thông điệp dự phòng nếu không trích xuất được
 * @returns {string} Thông điệp lỗi tiếng Việt dễ hiểu
 */
export function formatErrorMessage(error, fallbackMessage = 'Thao tác thất bại. Vui lòng thử lại.') {
  if (!error) return fallbackMessage

  // Nếu tham số truyền vào trực tiếp là string
  if (typeof error === 'string') {
    for (const item of TECHNICAL_ERROR_DICTIONARY) {
      if (item.patterns.some((pattern) => pattern.test(error))) {
        return item.message
      }
    }
    return error
  }

  // 0. Kiểm tra error.code trực tiếp (ví dụ: ECONNABORTED, ERR_NETWORK, ETIMEDOUT)
  if (error.code && typeof error.code === 'string') {
    for (const item of TECHNICAL_ERROR_DICTIONARY) {
      if (item.patterns.some((pattern) => pattern.test(error.code))) {
        return item.message
      }
    }
  }

  // 1. Kiểm tra validation errors map từ Backend (MethodArgumentNotValidException)
  // Backend trả về: { status: 400, message: "...", errors: { field1: "msg1", field2: "msg2" } }
  const errorsMap = error.response?.data?.errors
  if (errorsMap && typeof errorsMap === 'object' && Object.keys(errorsMap).length > 0) {
    const errorDetails = Object.values(errorsMap)
      .filter(Boolean)
      .join('; ')
    if (errorDetails) {
      return `Dữ liệu không hợp lệ: ${errorDetails}`
    }
  }

  // 2. Lấy thông điệp chính từ response backend
  const rawBackendMessage =
    error.response?.data?.message ||
    error.response?.data?.error ||
    error.userMessage ||
    error.message

  if (rawBackendMessage && typeof rawBackendMessage === 'string') {
    // Đối chiếu với từ điển chuyển đổi lỗi kỹ thuật
    for (const item of TECHNICAL_ERROR_DICTIONARY) {
      if (item.patterns.some((pattern) => pattern.test(rawBackendMessage))) {
        return item.message
      }
    }

    // Lọc bỏ thông điệp kỹ thuật mặc định của Axios
    if (
      !rawBackendMessage.startsWith('Request failed with status code') &&
      !rawBackendMessage.includes('status code 400') &&
      !rawBackendMessage.includes('status code 500')
    ) {
      return rawBackendMessage
    }
  }

  // 3. Fallback theo HTTP Status Code
  const status = error.response?.status
  if (status === 400) return fallbackMessage || 'Dữ liệu gửi lên không hợp lệ, vui lòng kiểm tra lại.'
  if (status === 403) return 'Bạn không có quyền thực hiện thao tác này.'
  if (status === 404) return 'Không tìm thấy tài nguyên yêu cầu.'
  if (status === 409) return 'Dữ liệu đã tồn tại hoặc vi phạm trùng lặp trong hệ thống.'
  if (status === 502) return 'Cổng kết nối máy chủ (ALB) phản hồi lỗi (502 Bad Gateway). Vui lòng thử lại sau.'
  if (status === 503) return 'Dịch vụ máy chủ tạm thời không khả dụng (503 Service Unavailable). Vui lòng thử lại sau.'
  if (status === 504) return 'Hết thời gian chờ phản hồi từ máy chủ (504 Gateway Timeout). Vui lòng thử lại.'
  if (status >= 500) return 'Máy chủ gặp sự cố xử lý (500). Vui lòng thử lại sau.'

  return fallbackMessage
}

/**
 * Hiển thị Toast đỏ cảnh báo lỗi nghiệp vụ với thông điệp tiếng Việt thân thiện
 *
 * @param {any} errorOrMessage - Lỗi kỹ thuật hoặc message cần hiển thị
 * @param {string} [fallbackMessage] - Thông điệp dự phòng
 * @param {Object} [options] - Cấu hình thêm cho react-hot-toast
 * @returns {string} Toast ID
 */
export function showErrorToast(errorOrMessage, fallbackMessage, options = {}) {
  const message = formatErrorMessage(errorOrMessage, fallbackMessage)
  return toast.error(message, {
    duration: 5000,
    ...options,
  })
}

/**
 * Hiển thị Toast xanh cho các thao tác thành công (tạo mới, cập nhật, xóa, v.v.)
 *
 * @param {string} message - Thông điệp thành công tiếng Việt
 * @param {Object} [options] - Cấu hình thêm cho react-hot-toast
 * @returns {string} Toast ID
 */
export function showSuccessToast(message, options = {}) {
  return toast.success(message, {
    duration: 3500,
    ...options,
  })
}

export { toast }
