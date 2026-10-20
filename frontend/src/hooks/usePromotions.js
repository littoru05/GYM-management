import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import * as promotionService from '../services/promotionService'

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
      toast.success(`Đã thêm voucher "${data.code}" thành công!`)
    },
    onError: (error) => {
      toast.error(error.message || 'Thêm voucher thất bại. Vui lòng thử lại.')
    },
  })
}

export function useUpdatePromotion() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => promotionService.updatePromotion(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['promotions'] })
      toast.success(`Cập nhật voucher "${data.code}" thành công!`)
    },
    onError: (error) => {
      toast.error(error.message || 'Cập nhật voucher thất bại. Vui lòng thử lại.')
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
      toast.success(`${statusText} voucher "${data.code}"!`)
    },
    onError: (error) => {
      toast.error(error.message || 'Thay đổi trạng thái voucher thất bại.')
    },
  })
}

export function useDeletePromotion() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id) => promotionService.deletePromotion(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['promotions'] })
      toast.success('Đã xóa voucher thành công!')
    },
    onError: (error) => {
      toast.error(error.message || 'Xóa voucher thất bại. Vui lòng thử lại.')
    },
  })
}
