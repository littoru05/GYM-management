import { promotions as mockPromotions } from '../data/mockData.js'
import { mockRequest, generateId, USE_MOCK, axiosClient } from './api.js'
import { PROMOTION_STATUS } from '../utils/constants.js'

const STORAGE_KEY = 'gym_promotions_data'

function loadPromotionStore() {
  if (typeof window !== 'undefined' && window.localStorage) {
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved) {
        const parsed = JSON.parse(saved)
        if (Array.isArray(parsed) && parsed.length > 0) {
          return parsed
        }
      }
    } catch (e) {
      console.error('Error loading promotions from localStorage', e)
    }
  }
  return [...mockPromotions]
}

function savePromotionStore(data) {
  if (typeof window !== 'undefined' && window.localStorage) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
    } catch (e) {
      console.error('Error saving promotions to localStorage', e)
    }
  }
}

let promotionsStore = loadPromotionStore()

export function getEffectivePromotionStatus(promo) {
  const today = new Date().toISOString().slice(0, 10)
  if (promo.status === 'INACTIVE') return PROMOTION_STATUS.INACTIVE
  if (Number(promo.usedCount) >= Number(promo.usageLimit)) return PROMOTION_STATUS.USED_UP
  if (promo.startDate && today < promo.startDate) return PROMOTION_STATUS.UPCOMING
  if (promo.endDate && today > promo.endDate) return PROMOTION_STATUS.EXPIRED
  return PROMOTION_STATUS.ACTIVE
}

export async function getPromotions({ search = '', status = '', discountType = '' } = {}) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.get('/admin/promotions', {
        params: {
          keyword: search || undefined,
          search: search || undefined,
          status: status && status !== 'ALL' ? status : undefined,
          size: 100,
        },
      })
      const resData = response.data?.data || response.data
      const list = Array.isArray(resData) ? resData : (resData?.content || [])
      let result = list.map((p) => ({
        ...p,
        effectiveStatus: getEffectivePromotionStatus(p),
      }))

      if (discountType && discountType !== 'ALL') {
        result = result.filter((p) => p.discountType === discountType)
      }

      return result
    } catch {
      // Fallback to mock data if backend endpoint is not yet reachable
    }
  }

  return mockRequest(() => {
    let result = promotionsStore.map((p) => ({
      ...p,
      effectiveStatus: getEffectivePromotionStatus(p),
    }))

    if (search && search.trim()) {
      const q = search.trim().toLowerCase()
      result = result.filter(
        (p) =>
          (p.code && p.code.toLowerCase().includes(q)) ||
          (p.name && p.name.toLowerCase().includes(q)) ||
          (p.description && p.description.toLowerCase().includes(q))
      )
    }

    if (status && status !== 'ALL') {
      result = result.filter((p) => p.effectiveStatus === status || p.status === status)
    }

    if (discountType && discountType !== 'ALL') {
      result = result.filter((p) => p.discountType === discountType)
    }

    return result
  })
}

export async function getPromotionById(id) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.get(`/admin/promotions/${id}`)
      const promo = response.data?.data || response.data
      return {
        ...promo,
        effectiveStatus: getEffectivePromotionStatus(promo),
      }
    } catch {
      // Fallback to mock
    }
  }

  return mockRequest(() => {
    const promo = promotionsStore.find((p) => p.id === id)
    if (!promo) throw new Error('Không tìm thấy voucher')
    return {
      ...promo,
      effectiveStatus: getEffectivePromotionStatus(promo),
    }
  })
}

export async function createPromotion(payload) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.post('/admin/promotions', payload)
      const data = response.data?.data || response.data
      return {
        ...data,
        effectiveStatus: getEffectivePromotionStatus(data),
      }
    } catch (err) {
      if (err.response?.status !== 404 && err.response?.status !== 502) {
        throw err
      }
    }
  }

  return mockRequest(() => {
    const normalizedCode = (payload.code || '').trim().toUpperCase()
    const duplicate = promotionsStore.some(
      (p) => p.code.trim().toUpperCase() === normalizedCode
    )
    if (duplicate) {
      throw new Error(`Mã voucher "${normalizedCode}" đã tồn tại. Vui lòng chọn mã khác.`)
    }

    const newPromo = {
      id: generateId('V'),
      code: normalizedCode,
      name: payload.name.trim(),
      description: payload.description?.trim() || '',
      discountType: payload.discountType,
      discountValue: Number(payload.discountValue),
      startDate: payload.startDate,
      endDate: payload.endDate,
      usageLimit: Number(payload.usageLimit),
      usedCount: 0,
      status: payload.status || 'ACTIVE',
      createdAt: new Date().toISOString(),
    }

    promotionsStore = [newPromo, ...promotionsStore]
    savePromotionStore(promotionsStore)
    return {
      ...newPromo,
      effectiveStatus: getEffectivePromotionStatus(newPromo),
    }
  })
}

export async function updatePromotion(id, payload) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.put(`/admin/promotions/${id}`, payload)
      const data = response.data?.data || response.data
      return {
        ...data,
        effectiveStatus: getEffectivePromotionStatus(data),
      }
    } catch (err) {
      if (err.response?.status !== 404 && err.response?.status !== 502) {
        throw err
      }
    }
  }

  return mockRequest(() => {
    const index = promotionsStore.findIndex((p) => p.id === id)
    if (index === -1) throw new Error('Không tìm thấy voucher để cập nhật')

    const normalizedCode = payload.code ? payload.code.trim().toUpperCase() : promotionsStore[index].code
    if (payload.code) {
      const duplicate = promotionsStore.some(
        (p) => p.id !== id && p.code.trim().toUpperCase() === normalizedCode
      )
      if (duplicate) {
        throw new Error(`Mã voucher "${normalizedCode}" đã được sử dụng bởi voucher khác.`)
      }
    }

    const updated = {
      ...promotionsStore[index],
      ...payload,
      code: normalizedCode,
      name: payload.name ? payload.name.trim() : promotionsStore[index].name,
      description: payload.description !== undefined ? payload.description.trim() : promotionsStore[index].description,
      discountType: payload.discountType || promotionsStore[index].discountType,
      discountValue: payload.discountValue !== undefined ? Number(payload.discountValue) : promotionsStore[index].discountValue,
      startDate: payload.startDate || promotionsStore[index].startDate,
      endDate: payload.endDate || promotionsStore[index].endDate,
      usageLimit: payload.usageLimit !== undefined ? Number(payload.usageLimit) : promotionsStore[index].usageLimit,
      status: payload.status || promotionsStore[index].status,
      updatedAt: new Date().toISOString(),
    }

    promotionsStore[index] = updated
    savePromotionStore(promotionsStore)
    return {
      ...updated,
      effectiveStatus: getEffectivePromotionStatus(updated),
    }
  })
}

export async function togglePromotionStatus(id) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.patch(`/admin/promotions/${id}/toggle-status`)
      const data = response.data?.data || response.data
      return {
        ...data,
        effectiveStatus: getEffectivePromotionStatus(data),
      }
    } catch (err) {
      if (err.response?.status !== 404 && err.response?.status !== 502) {
        throw err
      }
    }
  }

  return mockRequest(() => {
    const index = promotionsStore.findIndex((p) => p.id === id)
    if (index === -1) throw new Error('Không tìm thấy voucher')

    const current = promotionsStore[index]
    const nextStatus = current.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    promotionsStore[index] = { ...current, status: nextStatus, updatedAt: new Date().toISOString() }
    savePromotionStore(promotionsStore)
    return {
      ...promotionsStore[index],
      effectiveStatus: getEffectivePromotionStatus(promotionsStore[index]),
    }
  })
}

export async function deletePromotion(id) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.delete(`/admin/promotions/${id}`)
      return response.data?.data || response.data
    } catch (err) {
      if (err.response?.status !== 404 && err.response?.status !== 502) {
        throw err
      }
    }
  }

  return mockRequest(() => {
    const exists = promotionsStore.some((p) => p.id === id)
    if (!exists) throw new Error('Không tìm thấy voucher để xóa')

    promotionsStore = promotionsStore.filter((p) => p.id !== id)
    savePromotionStore(promotionsStore)
    return { success: true }
  })
}

export async function getActivePromotions() {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.get('/promotions/active')
      const list = response.data?.data || response.data
      return (Array.isArray(list) ? list : []).map((p) => ({
        ...p,
        effectiveStatus: getEffectivePromotionStatus(p),
      }))
    } catch {
      // Fallback
    }
  }

  return mockRequest(() => {
    return promotionsStore
      .map((p) => ({ ...p, effectiveStatus: getEffectivePromotionStatus(p) }))
      .filter((p) => p.effectiveStatus === PROMOTION_STATUS.ACTIVE)
  })
}

export async function validatePromotionCode(code, orderAmount = 0) {
  if (!USE_MOCK) {
    try {
      if (orderAmount > 0) {
        const response = await axiosClient.post('/promotions/apply', {
          code,
          orderAmount,
        })
        const data = response.data?.data || response.data
        return { valid: true, promotion: data }
      } else {
        const activeList = await getActivePromotions()
        const found = activeList.find(
          (p) => p.code?.toLowerCase() === (code || '').trim().toLowerCase()
        )
        if (found) {
          return { valid: true, promotion: found }
        }
        return { valid: false, reason: 'Mã voucher không tồn tại hoặc đã hết hạn' }
      }
    } catch (err) {
      if (err.response?.status !== 404 && err.response?.status !== 502) {
        return { valid: false, reason: err.response?.data?.message || 'Mã voucher không hợp lệ' }
      }
    }
  }

  return mockRequest(() => {
    const promo = promotionsStore.find(
      (p) => p.code.toLowerCase() === (code || '').trim().toLowerCase()
    )
    if (!promo) return { valid: false, reason: 'Mã voucher không tồn tại' }
    const effectiveStatus = getEffectivePromotionStatus(promo)
    if (effectiveStatus !== PROMOTION_STATUS.ACTIVE) {
      const reasons = {
        [PROMOTION_STATUS.INACTIVE]: 'Voucher đang tạm ngưng áp dụng',
        [PROMOTION_STATUS.UPCOMING]: 'Voucher chưa đến ngày bắt đầu',
        [PROMOTION_STATUS.EXPIRED]: 'Voucher đã hết hạn sử dụng',
        [PROMOTION_STATUS.USED_UP]: 'Voucher đã hết lượt sử dụng tối đa',
      }
      return { valid: false, reason: reasons[effectiveStatus] || 'Voucher không khả dụng' }
    }
    return { valid: true, promotion: promo }
  })
}
