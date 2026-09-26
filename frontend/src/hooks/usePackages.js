import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import * as packageService from '../services/packageService'

export function usePackagesQuery({ status } = {}) {
  return useQuery({
    queryKey: ['packages', { status }],
    queryFn: () => packageService.getPackages({ status }),
  })
}

export function useCreatePackage() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload) => packageService.createPackage(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['packages'] })
      toast.success('Thêm gói tập thành công')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Thêm gói tập thất bại. Vui lòng thử lại.')
    },
  })
}

export function useUpdatePackage() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => packageService.updatePackage(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['packages'] })
      toast.success('Cập nhật gói tập thành công')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Cập nhật gói tập thất bại. Vui lòng thử lại.')
    },
  })
}

export function useDeletePackage() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id) => packageService.deletePackage(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['packages'] })
      toast.success('Đã cập nhật trạng thái gói tập')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Thao tác thất bại. Vui lòng thử lại.')
    },
  })
}
