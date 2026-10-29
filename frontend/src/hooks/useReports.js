import { useQuery } from '@tanstack/react-query'
import {
  getMembershipStatistics,
  getRevenueSummary,
  getRevenueSeries,
} from '../services/reportService.js'
import { DASHBOARD_QUERY_CONFIG } from './useRevenue.js'

export function useMembershipStatistics(options = {}) {
  return useQuery({
    queryKey: ['membership-statistics'],
    queryFn: getMembershipStatistics,
    ...DASHBOARD_QUERY_CONFIG,
    ...options,
  })
}

export function useRevenueSummary(params, options = {}) {
  return useQuery({
    queryKey: ['revenue-summary', params],
    queryFn: () => getRevenueSummary(params),
    ...DASHBOARD_QUERY_CONFIG,
    ...options,
  })
}

export function useRevenueSeries(params, options = {}) {
  return useQuery({
    queryKey: ['revenue-series', params],
    queryFn: () => getRevenueSeries(params),
    ...DASHBOARD_QUERY_CONFIG,
    ...options,
  })
}
