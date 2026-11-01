import axiosClient from '../api/axiosClient'

export function checkIn(phone) {
  return axiosClient.post('/check-ins', { phone }).then((res) => res.data)
}

export function getTodayCheckIns() {
  return axiosClient.get('/check-ins/today').then((res) => res.data)
}
