import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import toast from 'react-hot-toast'
import * as inventoryService from '../services/inventoryService'

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
      toast.success('Thêm sản phẩm thành công')
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Thêm sản phẩm thất bại. Vui lòng thử lại.')
    },
  })
}

export function useImportStock() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => inventoryService.importStock(id, payload),
    onSuccess: (product) => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      toast.success(`Nhập kho thành công. Tồn kho mới: ${product.stock}`)
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Nhập kho thất bại. Vui lòng thử lại.')
    },
  })
}

export function useAdjustStock() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ id, payload }) => inventoryService.adjustStock(id, payload),
    onSuccess: (product) => {
      queryClient.invalidateQueries({ queryKey: ['products'] })
      toast.success(`Điều chỉnh tồn kho thành công. Tồn kho mới: ${product.stock}`)
    },
    onError: (error) => {
      toast.error(error.response?.data?.message || 'Điều chỉnh tồn kho thất bại. Vui lòng thử lại.')
    },
  })
}
