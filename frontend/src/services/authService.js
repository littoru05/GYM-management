import axiosClient from '../api/axiosClient'
import { USE_MOCK, mockRequest } from './api'

export async function login(email, password) {
  const normalizedEmail = (email || '').trim().toLowerCase()

  // Chế độ Mock dữ liệu nội bộ (chỉ bật khi VITE_USE_MOCK=true)
  if (USE_MOCK) {
    return mockRequest(() => {
      if (normalizedEmail.includes('admin')) {
        return {
          id: 'S001',
          code: 'S001',
          name: 'Admin Quản Trị',
          email: email,
          phone: '0912345678',
          role: 'ADMIN',
          status: 'ACTIVE',
          token: 'mock-jwt-token-admin',
        }
      }
      return {
        id: 'S002',
        code: 'S002',
        name: 'Nhân viên lễ tân',
        email: email,
        phone: '0987654321',
        role: 'STAFF',
        status: 'ACTIVE',
        token: 'mock-jwt-token-staff',
      }
    }, 200)
  }

  // Chế độ kết nối API thực tế tới Backend
  const candidateEmails = [normalizedEmail]
  if (normalizedEmail.endsWith('@gym.com')) {
    candidateEmails.push(normalizedEmail.replace('@gym.com', '@gym.local'))
  } else if (normalizedEmail.endsWith('@gym.local')) {
    candidateEmails.push(normalizedEmail.replace('@gym.local', '@gym.com'))
  }

  let lastError = null
  for (const candidate of candidateEmails) {
    try {
      const res = await axiosClient.post('/auth/login', { email: candidate, password })
      if (res?.data) {
        return res.data
      }
    } catch (err) {
      lastError = err
      // Phản hồi xác thực từ server (401 Bad Credentials, 403 Locked, 400 Validation)
      // Ném lỗi ngay để form đăng nhập hiển thị thông báo chính xác
      if (err.response?.status === 401 || err.response?.status === 403 || err.response?.status === 400) {
        throw err
      }
    }
  }

  if (lastError) {
    throw lastError
  }
}

export async function logout() {
  try {
    const res = await axiosClient.post('/auth/logout')
    return res.data
  } catch {
    return { success: true }
  }
}
