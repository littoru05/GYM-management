import { useQuery } from '@tanstack/react-query'
import { getTransactions } from '../services/transactionService.js'
import { DASHBOARD_QUERY_CONFIG } from './useRevenue.js'

export function useTransactions({
  limit,
  range,
  startDate,
  endDate,
  type,
  simulateEmpty = false,
} = {}, options = {}) {
  return useQuery({
    queryKey: ['transactions', { limit, range, startDate, endDate, type, simulateEmpty }],
    queryFn: () => getTransactions({ limit, range, startDate, endDate, type, simulateEmpty }),
    ...DASHBOARD_QUERY_CONFIG,
    ...options,
  })
}
