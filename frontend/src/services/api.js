import axiosClient from '../api/axiosClient.js'

// Toggle this flag via VITE_USE_MOCK in .env.
// When false (default), services communicate with the real backend REST API.
export const USE_MOCK = import.meta.env?.VITE_USE_MOCK === 'true'

/**
 * Wraps mock data access in a Promise so calling code behaves the same
 * whether it talks to mock data or a real REST endpoint later.
 */
export function mockRequest(factory, delay = 300) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      try {
        resolve(factory())
      } catch (error) {
        reject(error)
      }
    }, delay)
  })
}

export function generateId(prefix) {
  return `${prefix}${Date.now()}${Math.floor(Math.random() * 1000)}`
}

export { axiosClient }
