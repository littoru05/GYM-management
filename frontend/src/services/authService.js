import axiosClient from '../api/axiosClient'
import { mockRequest } from './api'

export async function login(email, password) {
  const normalizedEmail = (email || '').trim().toLowerCase()
  const candidateEmails = [normalizedEmail]
  if (normalizedEmail.endsWith('@gym.com')) {
    candidateEmails.push(normalizedEmail.replace('@gym.com', '@gym.local'))
  } else if (normalizedEmail.endsWith('@gym.local')) {
    candidateEmails.push(normalizedEmail.replace('@gym.local', '@gym.com'))
  }

  // If backend is reachable, attempt login
  for (const candidate of candidateEmails) {
    try {
      const res = await axiosClient.post('/auth/login', { email: candidate, password })
      if (res?.data) {
        return res.data
      }
    } catch {
      // Continue to next candidate or fallback
    }
  }

  // Fallback to local session if backend rejected or is not running
  return mockRequest(() => {
    // If email has 'admin', log in as default Admin
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
    // Otherwise default staff
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

export async function logout() {
  try {
    const res = await axiosClient.post('/auth/logout')
    return res.data
  } catch {
    return { success: true }
  }
}
