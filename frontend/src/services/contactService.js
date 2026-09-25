import axiosClient from '../api/axiosClient'

export function createContactLead(payload) {
  return axiosClient.post('/leads', payload).then((res) => res.data)
}
