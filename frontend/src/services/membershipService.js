import { memberships as mockMemberships } from '../data/mockData'
import { mockRequest, generateId } from './api'

let membershipsStore = [...mockMemberships]

export function getMemberships() {
  return mockRequest(() => [...membershipsStore])
}

export function getActiveMemberships() {
  return mockRequest(() => membershipsStore.filter((m) => m.status === 'ACTIVE'))
}

export function getMembershipById(id) {
  return mockRequest(() => membershipsStore.find((m) => m.id === id) || null)
}

export function createMembership(payload) {
  return mockRequest(() => {
    const newMembership = { id: generateId('MS'), status: 'ACTIVE', popular: false, ...payload }
    membershipsStore = [...membershipsStore, newMembership]
    return newMembership
  })
}

export function updateMembership(id, payload) {
  return mockRequest(() => {
    const index = membershipsStore.findIndex((m) => m.id === id)
    if (index === -1) throw new Error('Không tìm thấy gói tập')
    membershipsStore[index] = { ...membershipsStore[index], ...payload }
    return membershipsStore[index]
  })
}

export function deleteMembership(id) {
  return mockRequest(() => {
    membershipsStore = membershipsStore.filter((m) => m.id !== id)
    return { success: true }
  })
}

export function toggleMembershipStatus(id) {
  return mockRequest(() => {
    const index = membershipsStore.findIndex((m) => m.id === id)
    if (index === -1) throw new Error('Không tìm thấy gói tập')
    membershipsStore[index] = {
      ...membershipsStore[index],
      status: membershipsStore[index].status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE',
    }
    return membershipsStore[index]
  })
}
