import axiosClient from '../api/axiosClient.js'
import { PROMOTION_STATUS } from '../utils/constants.js'

/**
 * Tính toán trạng thái hiệu lực động của Voucher (ACTIVE, INACTIVE, UPCOMING, EXPIRED, USED_UP)
 */
export function getEffectivePromotionStatus(promo) {
  if (!promo) return PROMOTION_STATUS.INACTIVE
  if (promo.status === 'INACTIVE') return PROMOTION_STATUS.INACTIVE

  const today = new Date().toISOString().slice(0, 10)
  const usageLimit = Number(promo.usageLimit) || 0
  const usedCount = Number(promo.usedCount) || 0

  if (usageLimit > 0 && usedCount >= usageLimit) {
    return PROMOTION_STATUS.USED_UP
  }
  if (promo.startDate && today < promo.startDate) {
    return PROMOTION_STATUS.UPCOMING
  }
  if (promo.endDate && today > promo.endDate) {
    return PROMOTION_STATUS.EXPIRED
  }
  return PROMOTION_STATUS.ACTIVE
}

/**
 * Lấy danh sách Voucher / Khuyến mãi từ Backend (UC13)
 * Kết nối REST API: GET /api/admin/promotions
 */
export async function getPromotions({
  search = '',
  keyword = '',
  status = '',
  discountType = '',
  page = 0,
  size = 100,
} = {}) {
  const queryKeyword = (keyword || search || '').trim()
  const queryStatus = status && status !== 'ALL' ? status : undefined

  try {
    const response = await axiosClient.get('/admin/promotions', {
      params: {
        page: Math.max(0, page),
        size: size || 100,
        keyword: queryKeyword || undefined,
        search: queryKeyword || undefined,
        status: queryStatus,
      },
    })

    const resData = response.data?.data || response.data
    const rawList = resData?.content || (Array.isArray(resData) ? resData : [])
    let result = rawList.map((p) => ({
      ...p,
      effectiveStatus: getEffectivePromotionStatus(p),
    }))

    // Filter by discountType in client if needed
    if (discountType && discountType !== 'ALL') {
      result = result.filter((p) => p.discountType === discountType)
    }

    return result
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || err.response.data?.error || 'Lỗi khi tải danh sách khuyến mãi')
    }
    // Return empty array if network unavailable
    return []
  }
}

/**
 * Lấy chi tiết Voucher theo ID (UC13)
 * Kết nối REST API: GET /api/admin/promotions/{id}
 */
export async function getPromotionById(id) {
  try {
    const response = await axiosClient.get(`/admin/promotions/${id}`)
    const data = response.data?.data || response.data
    return {
      ...data,
      effectiveStatus: getEffectivePromotionStatus(data),
    }
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không tìm thấy thông tin khuyến mãi')
    }
    throw err
  }
}

/**
 * Tạo mới Voucher / Khuyến mãi (UC13)
 * Kết nối REST API: POST /api/admin/promotions
 */
export async function createPromotion(payload) {
  try {
    const requestBody = {
      code: (payload.code || '').trim().toUpperCase(),
      name: (payload.name || '').trim(),
      description: payload.description ? payload.description.trim() : null,
      discountType: payload.discountType,
      discountValue: Number(payload.discountValue),
      startDate: payload.startDate,
      endDate: payload.endDate,
      usageLimit: Number(payload.usageLimit),
      status: payload.status || 'ACTIVE',
      minOrderAmount: payload.minOrderAmount ? Number(payload.minOrderAmount) : null,
      maxDiscountAmount: payload.maxDiscountAmount ? Number(payload.maxDiscountAmount) : null,
    }

    const response = await axiosClient.post('/admin/promotions', requestBody)
    const created = response.data?.data || response.data
    return {
      ...created,
      effectiveStatus: getEffectivePromotionStatus(created),
    }
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || err.response.data?.error || 'Không thể tạo mới khuyến mãi')
    }
    throw err
  }
}

/**
 * Cập nhật Voucher / Khuyến mãi (UC13)
 * Kết nối REST API: PUT /api/admin/promotions/{id}
 */
export async function updatePromotion(id, payload) {
  try {
    const requestBody = {
      code: payload.code ? payload.code.trim().toUpperCase() : undefined,
      name: payload.name ? payload.name.trim() : undefined,
      description: payload.description !== undefined ? payload.description.trim() : undefined,
      discountType: payload.discountType,
      discountValue: payload.discountValue !== undefined ? Number(payload.discountValue) : undefined,
      startDate: payload.startDate,
      endDate: payload.endDate,
      usageLimit: payload.usageLimit !== undefined ? Number(payload.usageLimit) : undefined,
      status: payload.status,
      minOrderAmount: payload.minOrderAmount !== undefined ? Number(payload.minOrderAmount) : undefined,
      maxDiscountAmount: payload.maxDiscountAmount !== undefined ? Number(payload.maxDiscountAmount) : undefined,
    }

    const response = await axiosClient.put(`/admin/promotions/${id}`, requestBody)
    const updated = response.data?.data || response.data
    return {
      ...updated,
      effectiveStatus: getEffectivePromotionStatus(updated),
    }
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || err.response.data?.error || 'Không thể cập nhật khuyến mãi')
    }
    throw err
  }
}

/**
 * Chuyển đổi trạng thái kích hoạt / tạm ngưng của Voucher (UC13)
 * Gọi GET và PUT /api/admin/promotions/{id} với status mới
 */
export async function togglePromotionStatus(id) {
  try {
    const current = await getPromotionById(id)
    const nextStatus = current.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    const response = await axiosClient.put(`/admin/promotions/${id}`, {
      ...current,
      status: nextStatus,
    })
    const updated = response.data?.data || response.data
    return {
      ...updated,
      effectiveStatus: getEffectivePromotionStatus(updated),
    }
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không thể thay đổi trạng thái khuyến mãi')
    }
    throw err
  }
}

/**
 * Xóa Voucher / Khuyến mãi (UC13)
 * Kết nối REST API: DELETE /api/admin/promotions/{id}
 */
export async function deletePromotion(id) {
  try {
    await axiosClient.delete(`/admin/promotions/${id}`)
    return { success: true, id }
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không thể xóa khuyến mãi')
    }
    throw err
  }
}

/**
 * Lấy danh sách Voucher đang hoạt động (dùng cho POS Checkout và Selector)
 * Kết nối REST API: GET /api/promotions/active
 */
export async function getActivePromotions() {
  try {
    const response = await axiosClient.get('/promotions/active')
    const list = response.data?.data || response.data || []
    return (Array.isArray(list) ? list : []).map((p) => ({
      ...p,
      effectiveStatus: getEffectivePromotionStatus(p),
    }))
  } catch {
    return []
  }
}

/**
 * Kiểm tra và áp dụng mã giảm giá
 * Kết nối REST API: POST /api/promotions/apply
 */
export async function applyPromotion(code, orderAmount = 0) {
  try {
    const response = await axiosClient.post('/promotions/apply', {
      code: (code || '').trim().toUpperCase(),
      orderAmount: Number(orderAmount) || 0,
    })
    return response.data?.data || response.data
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Mã giảm giá không hợp lệ')
    }
    throw err
  }
}

/**
 * Validate promotion code helper
 */
export async function validatePromotionCode(code, orderAmount = 0) {
  try {
    const res = await applyPromotion(code, orderAmount)
    const promoData = res?.data || res
    return { valid: true, promotion: promoData, data: res }
  } catch (err) {
    return { valid: false, reason: err.message }
  }
}
