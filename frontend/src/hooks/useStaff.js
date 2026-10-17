import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import * as staffService from '../services/staffService'

export function useStaffList(params) {
  return useQuery({
    queryKey: ['staff', params],
    queryFn: () => staffService.getStaffList(params),
  })
}

export function useStaffMutations() {
  const queryClient = useQueryClient()
  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['staff'] })

  const createStaff = useMutation({
    mutationFn: staffService.createStaff,
    onSuccess: invalidate,
  })

  const updateStaff = useMutation({
    mutationFn: ({ id, payload }) => staffService.updateStaff(id, payload),
    onSuccess: invalidate,
  })

  const toggleStaffLock = useMutation({
    mutationFn: staffService.toggleStaffLock,
    onSuccess: invalidate,
  })

  const updateStaffStatus = useMutation({
    mutationFn: ({ id, status }) => staffService.updateStaffStatus(id, status),
    onSuccess: invalidate,
  })

  const changeStaffPassword = useMutation({
    mutationFn: ({ id, password }) => staffService.changeStaffPassword(id, password),
  })

  return {
    createStaff,
    updateStaff,
    toggleStaffLock,
    updateStaffStatus,
    changeStaffPassword,
  }
}
