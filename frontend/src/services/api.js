import axiosClient from '../api/axiosClient.js'

// Toggle this flag (or read from env) once a real Spring Boot backend is available.
// When false, services connect directly to the Spring Boot REST backend via axiosClient.
export const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

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
