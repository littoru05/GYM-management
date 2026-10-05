import axiosClient from '../api/axiosClient'

// Bảng kho lọc/tìm kiếm phía client nên lấy trọn danh sách trong một trang
const ALL_PRODUCTS_SIZE = 1000

export function getProducts() {
  return axiosClient
    .get('/products', { params: { page: 0, size: ALL_PRODUCTS_SIZE } })
    .then((res) => res.data.content)
}

export function getCategories() {
  return axiosClient.get('/categories/all', { params: { status: 'ACTIVE' } }).then((res) => res.data)
}

export function createProduct(payload) {
  return axiosClient.post('/products', payload).then((res) => res.data)
}

// payload: { quantity, cost?, note? }
export function importStock(id, payload) {
  return axiosClient.post(`/products/${id}/import`, payload).then((res) => res.data)
}

// payload: { actualStock, reason }
export function adjustStock(id, payload) {
  return axiosClient.post(`/products/${id}/adjust`, payload).then((res) => res.data)
}
