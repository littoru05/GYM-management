import {
  transactions as mockTransactions,
  members as mockMembers,
  checkIns as mockCheckIns,
} from '../data/mockData.js'
import { mockRequest, USE_MOCK, axiosClient } from './api.js'
import { getMemberStatus } from '../utils/formatters.js'
import { MEMBER_STATUS, TRANSACTION_TYPE } from '../utils/constants.js'

function addDays(dateStr, days) {
  const d = new Date(dateStr)
  d.setDate(d.getDate() + days)
  return d.toISOString().slice(0, 10)
}

function getTodayStr() {
  return new Date().toISOString().slice(0, 10)
}

function calculateDateRange(range = '30d', startDate, endDate) {
  const today = getTodayStr()
  if (startDate && endDate) {
    return { start: startDate, end: endDate }
  }
  if (range === '7d') {
    return { start: addDays(today, -6), end: today }
  }
  if (range === '30d') {
    return { start: addDays(today, -29), end: today }
  }
  if (range === '12m') {
    const d = new Date()
    d.setMonth(d.getMonth() - 11)
    d.setDate(1)
    return { start: d.toISOString().slice(0, 10), end: today }
  }
  return { start: addDays(today, -29), end: today }
}

/**
 * Filter transactions falling between startDate and endDate (inclusive: 'YYYY-MM-DD').
 */
function filterTransactions(startDate, endDate) {
  return mockTransactions.filter((t) => t.date >= startDate && t.date <= endDate)
}

/**
 * Build daily series for a given date range.
 */
function buildDailyPoints(startDate, endDate, txList) {
  const points = []
  let curr = new Date(startDate)
  const end = new Date(endDate)

  // Safety cap to prevent infinite loop
  let count = 0
  while (curr <= end && count < 120) {
    count++
    const dayIso = curr.toISOString().slice(0, 10)
    const dayTx = txList.filter((t) => t.date === dayIso)
    const membershipRevenue = dayTx
      .filter((t) => t.type === TRANSACTION_TYPE.MEMBERSHIP)
      .reduce((s, t) => s + t.amount, 0)
    const retailRevenue = dayTx
      .filter((t) => t.type === TRANSACTION_TYPE.RETAIL)
      .reduce((s, t) => s + t.amount, 0)

    points.push({
      date: dayIso.slice(5).replace('-', '/'), // MM/DD
      rawDate: dayIso,
      membershipRevenue,
      retailRevenue,
      total: membershipRevenue + retailRevenue,
    })

    curr.setDate(curr.getDate() + 1)
  }

  return points
}

/**
 * Build monthly aggregated points for 12m view.
 */
function buildMonthlyPoints(startDate, endDate, txList) {
  const points = []
  const start = new Date(startDate)
  const end = new Date(endDate)

  let y = start.getFullYear()
  let m = start.getMonth()

  let count = 0
  while (count < 24) {
    count++
    const monthKey = `${y}-${String(m + 1).padStart(2, '0')}`
    const monthTx = txList.filter((t) => t.date.startsWith(monthKey))

    const membershipRevenue = monthTx
      .filter((t) => t.type === TRANSACTION_TYPE.MEMBERSHIP)
      .reduce((s, t) => s + t.amount, 0)
    const retailRevenue = monthTx
      .filter((t) => t.type === TRANSACTION_TYPE.RETAIL)
      .reduce((s, t) => s + t.amount, 0)

    points.push({
      date: `T${m + 1}`,
      rawDate: monthKey,
      membershipRevenue,
      retailRevenue,
      total: membershipRevenue + retailRevenue,
    })

    if (y === end.getFullYear() && m === end.getMonth()) break

    m++
    if (m > 11) {
      m = 0
      y++
    }
  }

  return points
}

/**
 * Get revenue series for timeline charts.
 */
export async function getRevenueSeries({
  range = '30d',
  startDate,
  endDate,
  simulateEmpty = false,
} = {}) {
  if (simulateEmpty) return []

  if (!USE_MOCK) {
    try {
      const dates = calculateDateRange(range, startDate, endDate)
      const res = await axiosClient.get('/admin/reports/revenue', {
        params: {
          startDate: dates.start,
          endDate: dates.end,
          type: 'ALL',
        },
      })
      const report = res.data?.data || res.data
      const chartData = report?.chartData || []

      if (range === '12m') {
        const monthlyMap = {}
        chartData.forEach((item) => {
          const mKey = item.date ? item.date.slice(0, 7) : ''
          if (mKey) {
            if (!monthlyMap[mKey]) {
              const mNum = parseInt(mKey.slice(5, 7), 10)
              monthlyMap[mKey] = {
                date: `T${mNum}`,
                rawDate: mKey,
                membershipRevenue: 0,
                retailRevenue: 0,
                total: 0,
                checkInCount: 0,
              }
            }
            monthlyMap[mKey].membershipRevenue += item.membershipRevenue || 0
            monthlyMap[mKey].retailRevenue += item.retailRevenue || 0
            monthlyMap[mKey].total += item.total || item.totalRevenue || 0
            monthlyMap[mKey].checkInCount += item.checkInCount || 0
          }
        })
        return Object.values(monthlyMap)
      }

      return chartData.map((item) => ({
        date: item.date ? item.date.slice(5).replace('-', '/') : '',
        rawDate: item.date,
        membershipRevenue: item.membershipRevenue || 0,
        retailRevenue: item.retailRevenue || 0,
        total: item.total || item.totalRevenue || 0,
        checkInCount: item.checkInCount || 0,
      }))
    } catch {
      // Fallback to mock calculation
    }
  }

  return mockRequest(() => {
    const today = getTodayStr()
    let start = startDate
    let end = endDate || today

    if (range === '7d') {
      start = addDays(today, -6)
      end = today
      const tx = filterTransactions(start, end)
      return buildDailyPoints(start, end, tx)
    }

    if (range === '30d') {
      start = addDays(today, -29)
      end = today
      const tx = filterTransactions(start, end)
      return buildDailyPoints(start, end, tx)
    }

    if (range === '12m') {
      const d = new Date()
      d.setMonth(d.getMonth() - 11)
      d.setDate(1)
      start = d.toISOString().slice(0, 10)
      end = today
      const tx = filterTransactions(start, end)
      return buildMonthlyPoints(start, end, tx)
    }

    // Custom range
    if (!start) start = addDays(today, -30)
    if (!end) end = today

    const tx = filterTransactions(start, end)
    if (tx.length === 0) return []

    const diffDays = Math.round((new Date(end) - new Date(start)) / (1000 * 60 * 60 * 24))
    if (diffDays <= 45) {
      return buildDailyPoints(start, end, tx)
    }
    return buildMonthlyPoints(start, end, tx)
  }, 250)
}

/**
 * Compute revenue KPI summary cards (Total, Membership, Retail) with trend %
 */
export async function getRevenueSummary({
  range = '30d',
  startDate,
  endDate,
  simulateEmpty = false,
} = {}) {
  if (simulateEmpty) {
    return {
      total: 0,
      membership: 0,
      retail: 0,
      transactionCount: 0,
      totalTrend: 0,
      membershipTrend: 0,
      retailTrend: 0,
    }
  }

  if (!USE_MOCK) {
    try {
      const dates = calculateDateRange(range, startDate, endDate)
      const res = await axiosClient.get('/admin/reports/revenue', {
        params: {
          startDate: dates.start,
          endDate: dates.end,
          type: 'ALL',
        },
      })
      const report = res.data?.data || res.data
      const s = report?.summary || {}
      return {
        total: s.totalRevenue || 0,
        membership: s.membershipRevenue || 0,
        retail: s.retailRevenue || 0,
        transactionCount: s.todayTransactions || 0,
        totalTrend: 0,
        membershipTrend: 0,
        retailTrend: 0,
        totalCheckIns: s.totalCheckIns || 0,
        todayRevenue: s.todayRevenue || 0,
        monthRevenue: s.monthRevenue || 0,
      }
    } catch {
      // Fallback to mock calculation
    }
  }

  return mockRequest(() => {
    const today = getTodayStr()
    let start = startDate
    let end = endDate || today
    let prevStart
    let prevEnd

    if (range === '7d') {
      start = addDays(today, -6)
      end = today
      prevStart = addDays(start, -7)
      prevEnd = addDays(start, -1)
    } else if (range === '30d') {
      start = addDays(today, -29)
      end = today
      prevStart = addDays(start, -30)
      prevEnd = addDays(start, -1)
    } else if (range === '12m') {
      const d = new Date()
      d.setMonth(d.getMonth() - 11)
      d.setDate(1)
      start = d.toISOString().slice(0, 10)
      end = today

      const pd = new Date(start)
      pd.setFullYear(pd.getFullYear() - 1)
      prevStart = pd.toISOString().slice(0, 10)
      const pe = new Date(end)
      pe.setFullYear(pe.getFullYear() - 1)
      prevEnd = pe.toISOString().slice(0, 10)
    } else {
      if (!start) start = addDays(today, -30)
      if (!end) end = today
      const diffMs = new Date(end).getTime() - new Date(start).getTime()
      prevEnd = addDays(start, -1)
      prevStart = new Date(new Date(prevEnd).getTime() - diffMs).toISOString().slice(0, 10)
    }

    const currentTx = filterTransactions(start, end)
    const prevTx = filterTransactions(prevStart, prevEnd)

    const sum = (list) => list.reduce((s, t) => s + t.amount, 0)
    const sumType = (list, type) =>
      list.filter((t) => t.type === type).reduce((s, t) => s + t.amount, 0)

    const total = sum(currentTx)
    const membership = sumType(currentTx, TRANSACTION_TYPE.MEMBERSHIP)
    const retail = sumType(currentTx, TRANSACTION_TYPE.RETAIL)

    const prevTotal = sum(prevTx)
    const prevMembership = sumType(prevTx, TRANSACTION_TYPE.MEMBERSHIP)
    const prevRetail = sumType(prevTx, TRANSACTION_TYPE.RETAIL)

    const calcTrend = (curr, prev) => {
      if (!prev || prev === 0) return curr > 0 ? 100 : 0
      return ((curr - prev) / prev) * 100
    }

    return {
      total,
      membership,
      retail,
      transactionCount: currentTx.length,
      previousTotal: prevTotal,
      previousMembership: prevMembership,
      previousRetail: prevRetail,
      totalTrend: calcTrend(total, prevTotal),
      membershipTrend: calcTrend(membership, prevMembership),
      retailTrend: calcTrend(retail, prevRetail),
    }
  }, 250)
}

/**
 * Statistics on members and gym operations.
 */
export function getMembershipStatistics() {
  return mockRequest(() => {
    const today = getTodayStr()
    const thirtyDaysAgo = addDays(today, -30)

    let active = 0
    let expiringSoon = 0
    let expired = 0
    let newMembers = 0

    mockMembers.forEach((m) => {
      const status = getMemberStatus(m.expiryDate)
      if (status === MEMBER_STATUS.ACTIVE) active += 1
      else if (status === MEMBER_STATUS.EXPIRING_SOON) expiringSoon += 1
      else expired += 1

      if (m.joinDate >= thirtyDaysAgo) newMembers += 1
    })

    const todayCheckIns = mockCheckIns.filter((ci) => ci.time.startsWith(today)).length

    return {
      active,
      expiringSoon,
      expired,
      newMembers,
      total: mockMembers.length,
      todayCheckIns: todayCheckIns || 18,
    }
  }, 200)
}
