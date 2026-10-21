import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import * as promotionService from '../services/promotionService'
import { showErrorToast, showSuccessToast } from '../utils/toastService'

export function usePromotionsQuery({ search = '', status = '', discountType = '' } = {}) {
  return useQuery({
    queryKey: ['promotions', { search, status, discountType }],
    queryFn: () => promotionService.getPromotions({ search, status, discountType }),
  })
}

export function useCreatePromotion() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload) => promotionService.createPromotion(payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['promotions'] })
      showSuccessToast(`Đã thêm voucher "${data.code}" thành công!`)
    },
    onError: (error) => {
      showErrorToast(error, 'Thêm voucher thất bại. Vui lòng thử lại.')
    },
  })
}

export function useUpdatePromotion() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => promotionService.updatePromotion(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['promotions'] })
      showSuccessToast(`Cập nhật voucher "${data.code}" thành công!`)
    },
    onError: (error) => {
      showErrorToast(error, 'Cập nhật voucher thất bại. Vui lòng thử lại.')
    },
  })
}

export function useTogglePromotionStatus() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id) => promotionService.togglePromotionStatus(id),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['promotions'] })
      const statusText = data.status === 'ACTIVE' ? 'Đã kích hoạt' : 'Đã tạm ngưng'
      showSuccessToast(`${statusText} voucher "${data.code}"!`)
    },
    onError: (error) => {
      showErrorToast(error, 'Thay đổi trạng thái voucher thất bại.')
    },
  })
}

export function useDeletePromotion() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id) => promotionService.deletePromotion(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['promotions'] })
      showSuccessToast('Đã xóa voucher thành công!')
    },
    onError: (error) => {
      showErrorToast(error, 'Xóa voucher thất bại. Vui lòng thử lại.')
    },
  })
}
