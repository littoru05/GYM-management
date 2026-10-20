import axiosClient from '../api/axiosClient.js'

/**
 * Lấy danh sách nhân viên có phân trang, tìm kiếm và bộ lọc (UC10)
 * Kết nối REST API: GET /api/admin/staffs
 */
export async function getStaffList({
  page = 0,
  size = 10,
  keyword = '',
  search = '',
  role = '',
  status = '',
} = {}) {
  const queryKeyword = (keyword || search || '').trim()

  try {
    const response = await axiosClient.get('/admin/staffs', {
      params: {
        page: Math.max(0, page),
        size: size || 10,
        keyword: queryKeyword || undefined,
        role: role || undefined,
        status: status || undefined,
      },
    })
    return response.data?.data || response.data
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || err.response.data?.error || 'Lỗi khi tải danh sách nhân viên')
    }
    // Return empty Page structure if network unavailable
    return {
      content: [],
      totalElements: 0,
      totalPages: 0,
      number: page,
      size,
    }
  }
}

/**
 * Lấy chi tiết nhân viên theo ID (UC10)
 * Kết nối REST API: GET /api/admin/staffs/{id}
 */
export async function getStaffById(id) {
  try {
    const response = await axiosClient.get(`/admin/staffs/${id}`)
    return response.data?.data || response.data
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không tìm thấy nhân viên')
    }
    throw err
  }
}

/**
 * Tạo mới tài khoản nhân viên (UC10)
 * Kết nối REST API: POST /api/admin/staffs
 */
export async function createStaff(payload) {
  try {
    const response = await axiosClient.post('/admin/staffs', payload)
    return response.data?.data || response.data
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không thể tạo nhân viên mới')
    }
    throw err
  }
}

/**
 * Cập nhật thông tin nhân viên (UC10)
 * Kết nối REST API: PUT /api/admin/staffs/{id}
 */
export async function updateStaff(id, payload) {
  try {
    const response = await axiosClient.put(`/admin/staffs/${id}`, payload)
    return response.data?.data || response.data
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không thể cập nhật nhân viên')
    }
    throw err
  }
}

/**
 * Chuyển đổi trạng thái khóa/mở khóa tài khoản nhân viên (UC10)
 * Kết nối REST API: PATCH /api/admin/staffs/{id}/toggle-status
 */
export async function toggleStaffLock(id) {
  try {
    const response = await axiosClient.patch(`/admin/staffs/${id}/toggle-status`)
    return response.data?.data || response.data
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không thể thay đổi trạng thái tài khoản')
    }
    throw err
  }
}

/**
 * Đặt lại mật khẩu nhân viên (UC10)
 * Kết nối REST API: PUT /api/admin/staffs/{id}
 */
export async function changeStaffPassword(id, password) {
  try {
    const response = await axiosClient.put(`/admin/staffs/${id}`, { password })
    return response.data?.data || response.data
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không thể đặt lại mật khẩu')
    }
    throw err
  }
}

/**
 * Cập nhật trạng thái tài khoản nhân viên (UC10)
 * Kết nối REST API: PUT /api/admin/staffs/{id}
 */
export async function updateStaffStatus(id, status) {
  try {
    const response = await axiosClient.put(`/admin/staffs/${id}`, { status })
    return response.data?.data || response.data
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không thể cập nhật trạng thái nhân viên')
    }
    throw err
  }
}

/**
 * Vô hiệu hóa tài khoản nhân viên (UC10)
 * Kết nối REST API: DELETE /api/admin/staffs/{id}
 */
export async function deleteStaff(id) {
  try {
    const response = await axiosClient.delete(`/admin/staffs/${id}`)
    return response.data?.data || response.data
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Không thể vô hiệu hóa nhân viên')
    }
    throw err
  }
}

