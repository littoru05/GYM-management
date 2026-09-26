import axiosClient from '../api/axiosClient'

export function getPackages({ status = '' } = {}) {
  return axiosClient.get('/memberships', { params: { status: status || undefined } }).then((res) => res.data)
}

export function getPackage(id) {
  return axiosClient.get(`/memberships/${id}`).then((res) => res.data)
}

export function createPackage(payload) {
  return axiosClient.post('/memberships', payload).then((res) => res.data)
}

export function updatePackage(id, payload) {
  return axiosClient.put(`/memberships/${id}`, payload).then((res) => res.data)
}

export function deletePackage(id) {
  return axiosClient.delete(`/memberships/${id}`).then((res) => res.data)
}
