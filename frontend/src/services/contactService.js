import axiosClient from '../api/axiosClient'
import { USE_MOCK, mockRequest, generateId } from './api'
import { contactLeads as initialContactLeads } from '../data/mockData'

let mockLeads = [...(initialContactLeads || [])]

export function createContactLead(payload) {
  if (USE_MOCK) {
    return mockRequest(() => {
      const newLead = {
        id: generateId('lead_'),
        name: payload.name,
        email: payload.email,
        phone: payload.phone,
        message: payload.message || '',
        status: 'PENDING',
        isRead: false,
        createdAt: new Date().toISOString(),
      }
      mockLeads.unshift(newLead)
      return newLead
    }, 1000)
  }
  return axiosClient.post('/leads', payload).then((res) => res.data)
}

