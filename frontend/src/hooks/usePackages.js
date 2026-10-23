import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import * as packageService from '../services/packageService'
import { showErrorToast, showSuccessToast } from '../utils/toastService'

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
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['packages'] })
      showSuccessToast(`Thêm gói tập "${data?.name || ''}" thành công!`)
    },
    onError: (error) => {
      showErrorToast(error, 'Thêm gói tập thất bại. Vui lòng thử lại.')
    },
  })
}

export function useUpdatePackage() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => packageService.updatePackage(id, payload),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['packages'] })
      showSuccessToast(`Cập nhật gói tập "${data?.name || ''}" thành công!`)
    },
    onError: (error) => {
      showErrorToast(error, 'Cập nhật gói tập thất bại. Vui lòng thử lại.')
    },
  })
}

export function useDeletePackage() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (id) => packageService.deletePackage(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['packages'] })
      showSuccessToast('Đã xóa hoặc cập nhật trạng thái gói tập thành công!')
    },
    onError: (error) => {
      showErrorToast(error, 'Thao tác với gói tập thất bại. Vui lòng thử lại.')
    },
  })
}
