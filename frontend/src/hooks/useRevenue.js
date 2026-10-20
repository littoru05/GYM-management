import { useQuery } from '@tanstack/react-query'
import { getRevenueSeries, getRevenueSummary } from '../services/reportService.js'

export function useRevenueSeries({ range = '30d', startDate, endDate, simulateEmpty = false } = {}) {
  return useQuery({
    queryKey: ['revenue-series', { range, startDate, endDate, simulateEmpty }],
    queryFn: () => getRevenueSeries({ range, startDate, endDate, simulateEmpty }),
  })
}

export function useRevenueSummary({ range = '30d', startDate, endDate, simulateEmpty = false } = {}) {
  return useQuery({
    queryKey: ['revenue-summary', { range, startDate, endDate, simulateEmpty }],
    queryFn: () => getRevenueSummary({ range, startDate, endDate, simulateEmpty }),
  })
}
