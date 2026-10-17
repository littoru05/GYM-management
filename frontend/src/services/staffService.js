import { staff as mockStaff } from '../data/mockData'
import { mockRequest, USE_MOCK, axiosClient } from './api'
import { STAFF_STATUS } from '../utils/constants'

const STORAGE_KEY = 'gym_staff_data'

function loadStaffStore() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      const parsed = JSON.parse(saved)
      if (Array.isArray(parsed) && parsed.length > 0) {
        return parsed
      }
    }
  } catch (e) {
    console.error('Error loading staff from localStorage', e)
  }
  return [...mockStaff]
}

function saveStaffStore(data) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } catch (e) {
    console.error('Error saving staff to localStorage', e)
  }
}

let staffStore = loadStaffStore()

export async function getStaffList({ search = '', role = '', status = '' } = {}) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.get('/admin/staffs', {
        params: { search, role, status },
      })
      return response.data?.data || response.data
    } catch {
      // Fallback to mock data if backend route is not yet implemented
    }
  }

  return mockRequest(() => {
    let filtered = [...staffStore]

    if (search && search.trim()) {
      const q = search.trim().toLowerCase()
      filtered = filtered.filter(
        (s) =>
          (s.name && s.name.toLowerCase().includes(q)) ||
          (s.email && s.email.toLowerCase().includes(q)) ||
          (s.phone && s.phone.includes(q)) ||
          (s.id && s.id.toLowerCase().includes(q))
      )
    }

    if (role && role.trim()) {
      filtered = filtered.filter((s) => s.role === role)
    }

    if (status && status.trim()) {
      filtered = filtered.filter((s) => s.status === status)
    }

    return filtered
  }, 200)
}

export async function getStaffById(id) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.get(`/admin/staffs/${id}`)
      return response.data?.data || response.data
    } catch {
      // Fallback
    }
  }

  return mockRequest(() => {
    const item = staffStore.find((s) => s.id === id)
    if (!item) throw new Error('Không tìm thấy nhân viên')
    return item
  }, 150)
}

export async function createStaff(payload) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.post('/admin/staffs', payload)
      return response.data?.data || response.data
    } catch {
      // Fallback
    }
  }

  return mockRequest(() => {
    const maxNum = staffStore.reduce((max, s) => {
      const num = parseInt(s.id.replace(/\D/g, ''), 10)
      return Number.isNaN(num) ? max : Math.max(max, num)
    }, 0)
    const nextId = `S${String(maxNum + 1).padStart(3, '0')}`

    const newStaff = {
      id: nextId,
      status: STAFF_STATUS.ACTIVE,
      createdDate: new Date().toISOString().slice(0, 10),
      lastLogin: null,
      ...payload,
    }

    staffStore = [newStaff, ...staffStore]
    saveStaffStore(staffStore)
    return newStaff
  }, 300)
}

export async function updateStaff(id, payload) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.put(`/admin/staffs/${id}`, payload)
      return response.data?.data || response.data
    } catch {
      // Fallback
    }
  }

  return mockRequest(() => {
    const index = staffStore.findIndex((s) => s.id === id)
    if (index === -1) throw new Error('Không tìm thấy nhân viên')

    staffStore[index] = {
      ...staffStore[index],
      ...payload,
    }
    saveStaffStore(staffStore)
    return staffStore[index]
  }, 250)
}

export async function toggleStaffLock(id) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.patch(`/admin/staffs/${id}/toggle-lock`)
      return response.data?.data || response.data
    } catch {
      // Fallback
    }
  }

  return mockRequest(() => {
    const index = staffStore.findIndex((s) => s.id === id)
    if (index === -1) throw new Error('Không tìm thấy nhân viên')

    const newStatus =
      staffStore[index].status === STAFF_STATUS.ACTIVE ? STAFF_STATUS.LOCKED : STAFF_STATUS.ACTIVE

    staffStore[index] = {
      ...staffStore[index],
      status: newStatus,
    }
    saveStaffStore(staffStore)
    return staffStore[index]
  }, 200)
}

export async function updateStaffStatus(id, status) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.patch(`/admin/staffs/${id}/status`, { status })
      return response.data?.data || response.data
    } catch {
      // Fallback
    }
  }

  return mockRequest(() => {
    const index = staffStore.findIndex((s) => s.id === id)
    if (index === -1) throw new Error('Không tìm thấy nhân viên')

    staffStore[index] = {
      ...staffStore[index],
      status,
    }
    saveStaffStore(staffStore)
    return staffStore[index]
  }, 200)
}

export async function changeStaffPassword(id, password) {
  if (!USE_MOCK) {
    try {
      const response = await axiosClient.post(`/admin/staffs/${id}/reset-password`, { password })
      return response.data?.data || response.data
    } catch {
      // Fallback
    }
  }

  return mockRequest(() => {
    return { success: true, id, message: 'Đặt lại mật khẩu thành công' }
  }, 200)
}
