import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import * as inventoryService from '../services/inventoryService'
import { showErrorToast, showSuccessToast } from '../utils/toastService'

export function useProductsQuery() {
  return useQuery({
    queryKey: ['products'],
    queryFn: () => inventoryService.getProducts(),
  })
}

export function useCategoriesQuery() {
  return useQuery({
    queryKey: ['categories'],
    queryFn: () => inventoryService.getCategories(),
  })
}

export function useCreateProduct() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (payload) => inventoryService.createProduct(payload),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      showSuccessToast('Thêm sản phẩm mới vào kho thành công!')
    },
    onError: (error) => {
      showErrorToast(error, 'Thêm sản phẩm thất bại. Vui lòng kiểm tra lại thông tin.')
    },
  })
}

export function useImportStock() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => inventoryService.importStock(id, payload),
    onSuccess: (product) => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      showSuccessToast(`Nhập kho thành công! Tồn kho mới: ${product?.stock ?? ''}`)
    },
    onError: (error) => {
      showErrorToast(error, 'Nhập kho thất bại. Vui lòng kiểm tra lại số lượng.')
    },
  })
}

export function useAdjustStock() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => inventoryService.adjustStock(id, payload),
    onSuccess: (product) => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      showSuccessToast(`Điều chỉnh tồn kho thành công! Tồn kho mới: ${product?.stock ?? ''}`)
    },
    onError: (error) => {
      showErrorToast(error, 'Điều chỉnh tồn kho thất bại. Vui lòng kiểm tra lại số lượng kiểm kê.')
    },
  })
}
