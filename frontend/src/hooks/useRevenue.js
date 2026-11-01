import { useQuery } from '@tanstack/react-query'
import { getRevenueSeries, getRevenueSummary } from '../services/reportService.js'

/**
 * Cấu hình tự động thử lại (retry) nhẹ nhàng và an toàn cho các truy vấn dữ liệu Dashboard
 */
export const DASHBOARD_QUERY_CONFIG = {
  retry: (failureCount, error) => {
    const status = error?.response?.status
    // Không retry các lỗi client 4xx (400, 401, 403, 404, 409, 422)
    if (status && status >= 400 && status < 500) {
      return false
    }
    // Tự động thử lại nhẹ nhàng tối đa 2 lần cho lỗi mạng, timeout (ECONNABORTED) hoặc 5xx từ ALB
    return failureCount < 2
  },
  // Giãn cách thử lại nhẹ nhàng (1s -> 1.5s -> tối đa 3s)
  retryDelay: (attemptIndex) => Math.min(1000 * Math.pow(1.5, attemptIndex), 3000),
  staleTime: 30 * 1000,
  refetchOnWindowFocus: false,
}

export function useRevenueSeries({ range = '30d', startDate, endDate, simulateEmpty = false } = {}, options = {}) {
  return useQuery({
    queryKey: ['revenue-series', { range, startDate, endDate, simulateEmpty }],
    queryFn: () => getRevenueSeries({ range, startDate, endDate, simulateEmpty }),
    ...DASHBOARD_QUERY_CONFIG,
    ...options,
  })
}

export function useRevenueSummary({ range = '30d', startDate, endDate, simulateEmpty = false } = {}, options = {}) {
  return useQuery({
    queryKey: ['revenue-summary', { range, startDate, endDate, simulateEmpty }],
    queryFn: () => getRevenueSummary({ range, startDate, endDate, simulateEmpty }),
    ...DASHBOARD_QUERY_CONFIG,
    ...options,
  })
}
