import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import * as checkInService from '../services/checkInService'

export function useTodayCheckInsQuery() {
  return useQuery({
    queryKey: ['check-ins', 'today'],
    queryFn: checkInService.getTodayCheckIns,
  })
}

export function useCheckInMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (phone) => checkInService.checkIn(phone),
    onSuccess: (data) => {
      queryClient.setQueryData(['check-ins', 'today'], (prev) => [data, ...(prev || [])])
    },
  })
}
