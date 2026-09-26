import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import * as memberService from '../services/memberService'

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
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members'] })
      toast.success('Thêm hội viên thành công')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Thêm hội viên thất bại. Vui lòng thử lại.')
    },
  })
}

export function useUpdateMember() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => memberService.updateMember(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members'] })
      toast.success('Cập nhật hội viên thành công')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Cập nhật hội viên thất bại. Vui lòng thử lại.')
    },
  })
}

export function useRegisterSubscription() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ memberId, payload }) => memberService.registerSubscription(memberId, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['members'] })
      toast.success('Đăng ký gói tập thành công')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Đăng ký gói tập thất bại. Vui lòng thử lại.')
    },
  })
}
