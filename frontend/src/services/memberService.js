import axiosClient from '../api/axiosClient'

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
  return axiosClient.post(`/members/${memberId}/subscriptions`, payload).then((res) => res.data)
}
