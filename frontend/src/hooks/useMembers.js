import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import * as memberService from '../services/memberService'
import { showErrorToast, showSuccessToast } from '../utils/toastService'

export function useMembersQuery({ page, size, keyword, status }) {
  return useQuery({
    queryKey: ['members', { page, size, keyword, status }],
    queryFn: () => memberService.getMembers({ page, size, keyword, status }),
    placeholderData: (previousData) => previousData,
  })
}

export function useCreateMember() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload) => memberService.createMember(payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['members'] })
      showSuccessToast(`Thêm hội viên "${data?.name || ''}" thành công!`)
    },
    onError: (error) => {
      showErrorToast(error, 'Thêm hội viên thất bại. Vui lòng kiểm tra lại thông tin.')
    },
  })
}

export function useUpdateMember() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => memberService.updateMember(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['members'] })
      showSuccessToast(`Cập nhật thông tin hội viên "${data?.name || ''}" thành công!`)
    },
    onError: (error) => {
      showErrorToast(error, 'Cập nhật hội viên thất bại. Vui lòng thử lại.')
    },
  })
}

export function useRegisterSubscription() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ memberId, payload }) => memberService.registerSubscription(memberId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members'] })
      queryClient.invalidateQueries({ queryKey: ['revenue-summary'] })
      queryClient.invalidateQueries({ queryKey: ['revenue-series'] })
      queryClient.invalidateQueries({ queryKey: ['reports'] })
      queryClient.invalidateQueries({ queryKey: ['transactions'] })
      queryClient.invalidateQueries({ queryKey: ['membership-statistics'] })
      showSuccessToast('Đăng ký gói tập cho hội viên thành công!')
    },
    onError: (error) => {
      showErrorToast(error, 'Đăng ký gói tập thất bại. Vui lòng thử lại.')
    },
  })
}
