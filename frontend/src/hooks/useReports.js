import { useQuery } from '@tanstack/react-query'
import {
  getMembershipStatistics,
  getRevenueSummary,
  getRevenueSeries,
} from '../services/reportService.js'

export function useMembershipStatistics() {
  return useQuery({
    queryKey: ['membership-statistics'],
    queryFn: getMembershipStatistics,
  })
}

export function useRevenueSummary(params) {
  return useQuery({
    queryKey: ['revenue-summary', params],
    queryFn: () => getRevenueSummary(params),
  })
}

export function useRevenueSeries(params) {
  return useQuery({
    queryKey: ['revenue-series', params],
    queryFn: () => getRevenueSeries(params),
  })
}
