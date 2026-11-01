import axiosClient from '../api/axiosClient'
import { USE_MOCK, mockRequest, generateId } from './api'
import { memberships as initialPackages } from '../data/mockData'

let mockPackages = [...(initialPackages || [])]

export function getPackages({ status = '' } = {}) {
  if (USE_MOCK) {
    return mockRequest(() => {
      if (!status) return [...mockPackages]
      return mockPackages.filter((p) => p.status === status)
    }, 300)
  }
  return axiosClient.get('/memberships', { params: { status: status || undefined } }).then((res) => res.data)
}

export function getPackage(id) {
  if (USE_MOCK) {
    return mockRequest(() => {
      const found = mockPackages.find((p) => String(p.id) === String(id))
      if (!found) throw new Error('Không tìm thấy gói tập')
      return { ...found }
    }, 200)
  }
  return axiosClient.get(`/memberships/${id}`).then((res) => res.data)
}

export function createPackage(payload) {
  if (USE_MOCK) {
    return mockRequest(() => {
      const newPkg = {
        id: generateId('PKG_'),
        name: payload.name,
        durationMonths: Number(payload.durationMonths),
        price: Number(payload.price),
        description: payload.description || '',
        status: payload.status || 'ACTIVE',
        benefits: payload.benefits || ['Tập gym không giới hạn'],
        popular: false,
      }
      mockPackages.push(newPkg)
      return newPkg
    }, 1000)
  }
  return axiosClient.post('/memberships', payload).then((res) => res.data)
}

export function updatePackage(id, payload) {
  if (USE_MOCK) {
    return mockRequest(() => {
      const index = mockPackages.findIndex((p) => String(p.id) === String(id))
      if (index === -1) throw new Error('Không tìm thấy gói tập để cập nhật')
      mockPackages[index] = {
        ...mockPackages[index],
        ...payload,
        ...(payload.durationMonths !== undefined ? { durationMonths: Number(payload.durationMonths) } : {}),
        ...(payload.price !== undefined ? { price: Number(payload.price) } : {}),
      }
      return { ...mockPackages[index] }
    }, 1000)
  }
  return axiosClient.put(`/memberships/${id}`, payload).then((res) => res.data)
}

export function deletePackage(id) {
  if (USE_MOCK) {
    return mockRequest(() => {
      mockPackages = mockPackages.filter((p) => String(p.id) !== String(id))
      return { success: true }
    }, 500)
  }
  return axiosClient.delete(`/memberships/${id}`).then((res) => res.data)
}

