import { useQuery } from '@tanstack/react-query'
import { getTransactions } from '../services/transactionService.js'

export function useTransactions({
  limit,
  range,
  startDate,
  endDate,
  type,
  simulateEmpty = false,
} = {}) {
  return useQuery({
    queryKey: ['transactions', { limit, range, startDate, endDate, type, simulateEmpty }],
    queryFn: () => getTransactions({ limit, range, startDate, endDate, type, simulateEmpty }),
  })
}
