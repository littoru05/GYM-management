import axiosClient from '../api/axiosClient.js'
import { getMemberStatus } from '../utils/formatters.js'
import { MEMBER_STATUS } from '../utils/constants.js'

function addDays(dateStr, days) {
  const d = new Date(dateStr)
  d.setDate(d.getDate() + days)
  return d.toISOString().slice(0, 10)
}

function getTodayStr() {
  return new Date().toISOString().slice(0, 10)
}

/**
 * Tính khoảng ngày YYYY-MM-DD theo range bộ lọc (7d, 30d, 12m, custom)
 */
export function resolveDateRange(range, startDate, endDate) {
  const today = getTodayStr()
  let start = startDate
  let end = endDate || today

  if (range === '7d') {
    start = addDays(today, -6)
    end = today
  } else if (range === '30d') {
    start = addDays(today, -29)
    end = today
  } else if (range === '12m') {
    const d = new Date()
    d.setFullYear(d.getFullYear() - 1)
    d.setDate(1)
    start = d.toISOString().slice(0, 10)
    end = today
  } else {
    if (!start) start = addDays(today, -29)
    if (!end) end = today
  }

  return { start, end }
}

/**
 * Lấy dữ liệu báo cáo doanh thu tổng hợp từ Backend API (UC14)
 * Kết nối REST API: GET /api/admin/reports/revenue?startDate=...&endDate=...&type=...
 */
export async function getRevenueReport({
  range = '30d',
  startDate,
  endDate,
  type = 'ALL',
  simulateEmpty = false,
} = {}) {
  // Test helper: Mô phỏng dữ liệu rỗng theo DoD
  if (simulateEmpty) {
    return {
      summary: {
        total: 0,
        membership: 0,
        retail: 0,
        totalRevenue: 0,
        membershipRevenue: 0,
        retailRevenue: 0,
        totalCheckIns: 0,
        todayRevenue: 0,
        monthRevenue: 0,
        todayTransactions: 0,
        transactionCount: 0,
        totalTrend: 0,
        membershipTrend: 0,
        retailTrend: 0,
      },
      chartData: [],
    }
  }

  const { start, end } = resolveDateRange(range, startDate, endDate)

  try {
    const response = await axiosClient.get('/admin/reports/revenue', {
      params: {
        startDate: start,
        endDate: end,
        type: type || 'ALL',
      },
    })

    const payload = response.data?.data || response.data
    const summaryDto = payload?.summary || {}
    const rawChartList = payload?.chartData || []

    const total = summaryDto.totalRevenue ?? 0
    const membership = summaryDto.membershipRevenue ?? 0
    const retail = summaryDto.retailRevenue ?? 0

    const summary = {
      total,
      membership,
      retail,
      totalRevenue: total,
      membershipRevenue: membership,
      retailRevenue: retail,
      totalCheckIns: summaryDto.totalCheckIns ?? 0,
      todayRevenue: summaryDto.todayRevenue ?? 0,
      monthRevenue: summaryDto.monthRevenue ?? 0,
      todayTransactions: summaryDto.todayTransactions ?? 0,
      transactionCount: summaryDto.todayTransactions ?? 0,
      totalTrend: 0,
      membershipTrend: 0,
      retailTrend: 0,
    }

    const chartData = rawChartList.map((item) => {
      const rawDate = item.date || ''
      const displayDate = rawDate.length >= 10 ? rawDate.slice(5).replace('-', '/') : rawDate

      return {
        date: displayDate, // 'MM/DD' cho trục X của Recharts
        rawDate,
        membershipRevenue: item.membershipRevenue ?? 0,
        retailRevenue: item.retailRevenue ?? 0,
        total: item.total ?? item.totalRevenue ?? 0,
        totalRevenue: item.total ?? item.totalRevenue ?? 0,
        checkInCount: item.checkInCount ?? 0,
      }
    })

    return { summary, chartData }
  } catch (err) {
    if (err.response) {
      throw new Error(err.response.data?.message || 'Lỗi khi tải dữ liệu báo cáo')
    }
    // Network fallback khi Backend chưa khởi động
    return {
      summary: {
        total: 0,
        membership: 0,
        retail: 0,
        totalRevenue: 0,
        membershipRevenue: 0,
        retailRevenue: 0,
        totalCheckIns: 0,
        todayRevenue: 0,
        monthRevenue: 0,
        todayTransactions: 0,
        transactionCount: 0,
        totalTrend: 0,
        membershipTrend: 0,
        retailTrend: 0,
      },
      chartData: [],
    }
  }
}

/**
 * Lấy danh sách điểm dữ liệu chuỗi thời gian cho Biểu đồ xu hướng (Recharts) (UC14)
 */
export async function getRevenueSeries(params = {}) {
  const report = await getRevenueReport(params)
  return report.chartData
}

/**
 * Lấy 3 thẻ thống kê tóm tắt doanh thu (Tổng doanh thu, Gói tập, Bán lẻ) (UC14)
 */
export async function getRevenueSummary(params = {}) {
  const report = await getRevenueReport(params)
  return report.summary
}

/**
 * Lấy số liệu vận hành và thống kê hội viên (UC14)
 * Kết nối REST API: GET /api/members
 */
export async function getMembershipStatistics() {
  try {
    const res = await axiosClient.get('/members', {
      params: { page: 0, size: 500 },
    })

    const pageData = res.data?.data || res.data
    const memberList = pageData?.content || (Array.isArray(pageData) ? pageData : [])

    let active = 0
    let expiringSoon = 0
    let expired = 0
    let newMembers = 0

    const today = getTodayStr()
    const thirtyDaysAgo = addDays(today, -30)

    memberList.forEach((m) => {
      const status = m.status || (m.expiryDate ? getMemberStatus(m.expiryDate) : MEMBER_STATUS.ACTIVE)
      if (status === MEMBER_STATUS.ACTIVE) active += 1
      else if (status === MEMBER_STATUS.EXPIRING_SOON) expiringSoon += 1
      else expired += 1

      if (m.joinDate && m.joinDate >= thirtyDaysAgo) newMembers += 1
    })

    return {
      active,
      expiringSoon,
      expired,
      newMembers,
      total: pageData?.totalElements ?? memberList.length,
      todayCheckIns: 0,
    }
  } catch {
    return {
      active: 0,
      expiringSoon: 0,
      expired: 0,
      newMembers: 0,
      total: 0,
      todayCheckIns: 0,
    }
  }
}
