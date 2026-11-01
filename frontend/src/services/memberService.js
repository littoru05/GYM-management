import axiosClient from '../api/axiosClient'
import { USE_MOCK, mockRequest, generateId } from './api'

export function getMembers({ page = 0, size = 10, keyword = '', status = '' } = {}) {
  return axiosClient
    .get('/members', { params: { page, size, keyword: keyword || undefined, status: status || undefined } })
    .then((res) => res.data)
}

export function getMember(id) {
  return axiosClient.get(`/members/${id}`).then((res) => res.data)
}

export function createMember(payload) {
  return axiosClient.post('/members', payload).then((res) => res.data)
}

export function updateMember(id, payload) {
  return axiosClient.put(`/members/${id}`, payload).then((res) => res.data)
}

export function deleteMember(id) {
  return axiosClient.delete(`/members/${id}`).then((res) => res.data)
}

export function registerSubscription(memberId, payload) {
  const data =
    typeof memberId === 'object' && memberId !== null
      ? memberId
      : { memberId, ...payload }

  if (USE_MOCK) {
    return mockRequest(() => {
      return {
        id: generateId('SUB_'),
        memberId: data.memberId,
        membershipId: data.membershipId,
        startDate: data.startDate,
        status: 'ACTIVE',
        paidPrice: data.paidPrice,
        createdAt: new Date().toISOString(),
      }
    }, 1000)
  }

  return axiosClient.post('/subscriptions', data).then((res) => res.data)
}

