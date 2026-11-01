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
 * Tính khoảng ngày mốc thời gian liền kề trước đó có cùng số lượng ngày
 */
export function getPrecedingDateRange(startStr, endStr) {
  const start = new Date(startStr)
  const end = new Date(endStr)
  const diffDays = Math.max(1, Math.round((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24)) + 1)
  const prevEnd = addDays(startStr, -1)
  const prevStart = addDays(prevEnd, -(diffDays - 1))
  return { prevStart, prevEnd }
}

/**
 * Tính toán tỷ lệ tăng trưởng doanh thu so với mốc thời gian liền kề trước đó (+/- %)
 * @param {number} current Doanh thu kỳ hiện tại
 * @param {number} previous Doanh thu kỳ trước liền kề
 * @param {Array} chartList Danh sách chuỗi thời gian hiện tại
 * @param {string} key Thuộc tính cần so sánh trong chartList
 * @returns {number} Tỷ lệ phần trăm làm tròn 1 chữ số thập phân
 */
export function calculateTrend(current, previous, chartList = [], key = 'total') {
  const curr = Number(current) || 0
  const prev = Number(previous) || 0

  if (prev > 0) {
    return Number((((curr - prev) / prev) * 100).toFixed(1))
  }

  // Nếu kỳ trước = 0, kiểm tra tỷ lệ so sánh giữa nửa đầu kỳ và nửa sau kỳ trong chuỗi ngày
  if (Array.isArray(chartList) && chartList.length >= 2) {
    const mid = Math.floor(chartList.length / 2)
    const firstHalf = chartList.slice(0, mid)
    const secondHalf = chartList.slice(mid)

    const firstSum = firstHalf.reduce(
      (sum, item) => sum + (Number(item[key] ?? item.total ?? item.totalRevenue ?? 0) || 0),
      0
    )
    const secondSum = secondHalf.reduce(
      (sum, item) => sum + (Number(item[key] ?? item.total ?? item.totalRevenue ?? 0) || 0),
      0
    )

    if (firstSum > 0) {
      return Number((((secondSum - firstSum) / firstSum) * 100).toFixed(1))
    }
  }

  // Nếu kỳ trước = 0 và hiện tại có phát sinh doanh thu (> 0)
  if (curr > 0) {
    return 100.0
  }

  return 0.0
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
  const { prevStart, prevEnd } = getPrecedingDateRange(start, end)

  try {
    const [response, prevResponse] = await Promise.all([
      axiosClient.get('/admin/reports/revenue', {
        params: {
          startDate: start,
          endDate: end,
          type: type || 'ALL',
        },
      }),
      axiosClient
        .get('/admin/reports/revenue', {
          params: {
            startDate: prevStart,
            endDate: prevEnd,
            type: type || 'ALL',
          },
        })
        .catch(() => null),
    ])

    const payload = response.data?.data || response.data
    const summaryDto = payload?.summary || {}
    const rawChartList = payload?.chartData || []

    const prevPayload = prevResponse?.data?.data || prevResponse?.data
    const prevSummaryDto = prevPayload?.summary || {}

    const total = summaryDto.totalRevenue ?? 0
    const membership = summaryDto.membershipRevenue ?? 0
    const retail = summaryDto.retailRevenue ?? 0

    const prevTotal = prevSummaryDto.totalRevenue ?? 0
    const prevMembership = prevSummaryDto.membershipRevenue ?? 0
    const prevRetail = prevSummaryDto.retailRevenue ?? 0

    // Tính % tăng trưởng so với kỳ trước thay vì gán cứng 0
    const totalTrend = calculateTrend(total, prevTotal, rawChartList, 'total')
    const membershipTrend = calculateTrend(membership, prevMembership, rawChartList, 'membershipRevenue')
    const retailTrend = calculateTrend(retail, prevRetail, rawChartList, 'retailRevenue')

    // Tính tổng số lượng giao dịch: map từ backend summaryDto hoặc tính tổng từ rawChartList
    const transactionCount =
      summaryDto.transactionCount ??
      summaryDto.totalTransactions ??
      (summaryDto.todayTransactions > 0 ? summaryDto.todayTransactions : null) ??
      rawChartList.reduce(
        (sum, item) =>
          sum + (Number(item.transactionCount) || ((Number(item.total) || Number(item.totalRevenue) || 0) > 0 ? 1 : 0)),
        0
      )

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
      todayTransactions: summaryDto.todayTransactions ?? transactionCount,
      transactionCount,
      totalTrend,
      membershipTrend,
      retailTrend,
    }

    let chartData = rawChartList.map((item) => {
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
        transactionCount: item.transactionCount ?? 0,
      }
    })

    if (range === '12m') {
      const monthlyMap = {}
      chartData.forEach((item) => {
        const mKey = item.rawDate ? item.rawDate.slice(0, 7) : ''
        if (!mKey) return
        if (!monthlyMap[mKey]) {
          const monthNum = parseInt(mKey.slice(5, 7), 10)
          monthlyMap[mKey] = {
            date: `T${monthNum}`,
            rawDate: mKey,
            membershipRevenue: 0,
            retailRevenue: 0,
            total: 0,
            checkInCount: 0,
            transactionCount: 0,
          }
        }
        monthlyMap[mKey].membershipRevenue += item.membershipRevenue || 0
        monthlyMap[mKey].retailRevenue += item.retailRevenue || 0
        monthlyMap[mKey].total += item.total || 0
        monthlyMap[mKey].checkInCount += item.checkInCount || 0
        monthlyMap[mKey].transactionCount += item.transactionCount || 0
      })
      chartData = Object.values(monthlyMap)
    }

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

/**
 * Xuất file Excel báo cáo doanh thu & giao dịch (.xlsx) từ Backend
 * Gọi GET /api/admin/reports/revenue/export
 */
export async function exportRevenueReportExcel({ startDate, endDate, type = 'ALL' }) {
  try {
    const response = await axiosClient.get('/admin/reports/revenue/export', {
      params: { startDate, endDate, type: type || 'ALL' },
      responseType: 'blob',
    })
    return response.data
  } catch (err) {
    if (err.response && err.response.data instanceof Blob) {
      // Decode blob error message
      const text = await err.response.data.text()
      try {
        const json = JSON.parse(text)
        throw new Error(json.message || 'Lỗi khi xuất file Excel')
      } catch {
        throw new Error('Lỗi khi xuất file Excel từ hệ thống')
      }
    }
    throw err
  }
}
