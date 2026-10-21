import axiosClient from '../api/axiosClient'

/**
 * Kiểm tra tính hợp lệ của mã voucher khi mua gói tập hoặc thanh toán
 * @param {Object} params
 * @param {string} params.code Mã voucher
 * @param {number} [params.membershipId] ID gói tập
 * @param {number} [params.orderAmount] Giá trị gói tập trước khi giảm
 * @param {number} [params.memberId] ID hội viên
 * @returns {Promise<Object>} Thông tin chiết khấu, số tiền giảm và giá cuối cùng
 */
export async function validateVoucher({ code, membershipId, orderAmount, memberId }) {
  if (!code || !code.trim()) {
    throw new Error('Vui lòng nhập mã voucher')
  }

  const payload = {
    code: code.trim().toUpperCase(),
    membershipId: membershipId ? Number(membershipId) : undefined,
    orderAmount: orderAmount !== undefined && orderAmount !== null ? Number(orderAmount) : undefined,
    memberId: memberId ? Number(memberId) : undefined,
  }

  try {
    const response = await axiosClient.post('/vouchers/validate', payload)
    return response.data?.data || response.data
  } catch (err) {
    const msg = err.response?.data?.message || err.message || 'Mã voucher không hợp lệ'
    throw new Error(msg)
  }
}
