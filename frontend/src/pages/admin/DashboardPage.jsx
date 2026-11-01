import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Wallet,
  Tags,
  ShoppingBag,
  Users,
  UserCheck,
  ScanLine,
  UserPlus,
  ArrowRight,
  WifiOff,
  RefreshCw,
} from 'lucide-react'

import RevenueCard from '../../components/common/RevenueCard.jsx'
import StatCard from '../../components/common/StatCard.jsx'
import RevenueChart from '../../components/common/RevenueChart.jsx'
import RevenuePieChart from '../../components/common/RevenuePieChart.jsx'
import TransactionTable from '../../components/common/TransactionTable.jsx'
import DateRangeFilter from '../../components/common/DateRangeFilter.jsx'
import { StatCardsSkeleton } from '../../components/common/Skeleton.jsx'
import { useRevenueSummary, useRevenueSeries } from '../../hooks/useRevenue.js'
import { useMembershipStatistics } from '../../hooks/useReports.js'
import { useTransactions } from '../../hooks/useTransactions.js'
import { formatNumber } from '../../utils/formatters.js'

function DashboardPage() {
  const [range, setRange] = useState('30d')
  const [customStart, setCustomStart] = useState('')
  const [customEnd, setCustomEnd] = useState('')
  const [simulateEmpty, setSimulateEmpty] = useState(false)

  const filterParams = {
    range,
    startDate: range === 'custom' ? customStart : undefined,
    endDate: range === 'custom' ? customEnd : undefined,
    simulateEmpty,
  }

  // Queries với cơ chế tự động thử lại nhẹ nhàng (gentle retry)
  const {
    data: summary,
    isLoading: summaryLoading,
    isError: isSummaryError,
    refetch: refetchSummary,
    isFetching: isSummaryFetching,
  } = useRevenueSummary(filterParams)

  const {
    data: seriesData = [],
    isLoading: seriesLoading,
    isError: isSeriesError,
    refetch: refetchSeries,
    isFetching: isSeriesFetching,
  } = useRevenueSeries(filterParams)

  const {
    data: memberStats,
    isLoading: statsLoading,
    isError: isStatsError,
    refetch: refetchStats,
    isFetching: isStatsFetching,
  } = useMembershipStatistics()

  const {
    data: recentTransactions = [],
    isLoading: txLoading,
    isError: isTxError,
    refetch: refetchTx,
    isFetching: isTxFetching,
  } = useTransactions({
    limit: 6,
    ...filterParams,
  })

  const hasNetworkError = isSummaryError || isSeriesError || isStatsError || isTxError
  const isRefetching = isSummaryFetching || isSeriesFetching || isStatsFetching || isTxFetching

  const handleRetryAll = () => {
    refetchSummary()
    refetchSeries()
    refetchStats()
    refetchTx()
  }

  const safeSeriesData = Array.isArray(seriesData) ? seriesData : []
  const safeTransactions = Array.isArray(recentTransactions) ? recentTransactions : []

  const handleRangeChange = (newRange) => {
    setRange(newRange)
  }

  const handleCustomDateChange = (start, end) => {
    setCustomStart(start)
    setCustomEnd(end)
  }

  const totalMembers = memberStats
    ? (memberStats.active || 0) + (memberStats.expiringSoon || 0) + (memberStats.expired || 0)
    : 0

  return (
    <div className="space-y-6">
      {/* Header & Quick Filter Bar */}
      <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-gray-900">
            Bảng điều khiển Admin
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            Tổng quan tình hình doanh thu và vận hành phòng tập PowerFit
          </p>
        </div>

        {/* Quick Filter: 7 ngày, 30 ngày, 12 tháng, Tùy chọn ngày */}
        <DateRangeFilter
          range={range}
          onRangeChange={handleRangeChange}
          customStartDate={customStart}
          customEndDate={customEnd}
          onCustomDateChange={handleCustomDateChange}
          simulateEmpty={simulateEmpty}
          onToggleSimulateEmpty={() => setSimulateEmpty(!simulateEmpty)}
        />
      </div>

      {/* Network / ALB Interruption Warning Banner */}
      {hasNetworkError && (
        <div
          role="alert"
          className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 rounded-2xl border border-amber-200 bg-amber-50/95 p-4 shadow-sm backdrop-blur-sm"
        >
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-amber-100 text-amber-600">
              <WifiOff size={20} />
            </div>
            <div>
              <h3 className="text-sm font-semibold text-amber-900">
                Đường truyền mạng tới máy chủ (ALB) bị gián đoạn hoặc quá thời gian chờ (Timeout)
              </h3>
              <p className="text-xs text-amber-700">
                Hệ thống đã tự động kích hoạt cơ chế thử lại nhẹ nhàng (retry). Nhấn nút bên cạnh để tải lại dữ liệu khi mạng ổn định.
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={handleRetryAll}
            disabled={isRefetching}
            className="inline-flex items-center gap-2 rounded-xl bg-amber-600 px-4 py-2 text-xs font-semibold text-white shadow-sm transition hover:bg-amber-700 disabled:opacity-50 shrink-0 active:scale-95"
          >
            <RefreshCw size={14} className={isRefetching ? 'animate-spin' : ''} />
            {isRefetching ? 'Đang tải lại...' : 'Thử lại ngay'}
          </button>
        </div>
      )}

      {/* 3 Summary Cards (Theo SRS Wireframe: Tổng DT, DT Gói tập, DT Bán lẻ) */}
      <div>
        <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-gray-400">
          Chỉ số doanh thu chính
        </h2>
        {summaryLoading ? (
          <StatCardsSkeleton count={3} />
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
            <RevenueCard
              icon={Wallet}
              label="Tổng doanh thu"
              value={summary?.total}
              trend={summary?.totalTrend}
              accent="primary"
              subtext={`${summary?.transactionCount || 0} giao dịch trong kỳ`}
            />
            <RevenueCard
              icon={Tags}
              label="Doanh thu gói tập"
              value={summary?.membership}
              trend={summary?.membershipTrend}
              accent="blue"
              subtext="Đăng ký & gia hạn thẻ tập"
            />
            <RevenueCard
              icon={ShoppingBag}
              label="Doanh thu bán lẻ"
              value={summary?.retail}
              trend={summary?.retailTrend}
              accent="green"
              subtext="Đồ uống, phụ kiện, thực phẩm"
            />
          </div>
        )}
      </div>

      {/* Secondary Operational Stats */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <StatCard
          icon={Users}
          label="Tổng hội viên"
          value={statsLoading ? '—' : formatNumber(totalMembers)}
          accent="purple"
        />
        <StatCard
          icon={UserCheck}
          label="Đang hoạt động"
          value={statsLoading ? '—' : formatNumber(memberStats?.active)}
          accent="green"
        />
        <StatCard
          icon={ScanLine}
          label="Lượt check-in trong kỳ"
          value={summaryLoading ? '—' : formatNumber(summary?.totalCheckIns ?? memberStats?.todayCheckIns ?? 0)}
          accent="yellow"
        />
        <StatCard
          icon={UserPlus}
          label="Hội viên mới (30 ngày)"
          value={statsLoading ? '—' : formatNumber(memberStats?.newMembers)}
          accent="blue"
        />
      </div>

      {/* Visual Charts: Biểu đồ xu hướng (cột/đường) + Biểu đồ tỷ trọng (Donut/Pie) */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <RevenueChart
            data={safeSeriesData}
            loading={seriesLoading}
            title="Biểu đồ xu hướng doanh thu"
            subtitle="Phản hồi theo mốc thời gian lọc (chuyển đổi giữa Đường & Cột)"
            height={320}
          />
        </div>

        <div className="lg:col-span-1">
          <RevenuePieChart
            membershipRevenue={summary?.membership || 0}
            retailRevenue={summary?.retail || 0}
            loading={summaryLoading}
            title="Biểu đồ tỷ trọng"
            subtitle="Tỷ lệ cơ cấu doanh thu"
            height={320}
          />
        </div>
      </div>

      {/* Recent Transactions Table */}
      <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-card">
        <div className="mb-4 flex items-center justify-between">
          <div>
            <h2 className="font-semibold text-gray-900">Giao dịch gần đây</h2>
            <p className="text-xs text-gray-500">Các giao dịch phát sinh mới nhất trong hệ thống</p>
          </div>
          <Link
            to="/admin/reports"
            className="inline-flex items-center gap-1 text-xs font-semibold text-primary-600 hover:text-primary-700 hover:underline"
          >
            <span>Xem toàn bộ báo cáo</span>
            <ArrowRight size={14} />
          </Link>
        </div>

        <TransactionTable
          transactions={safeTransactions}
          loading={txLoading}
          emptyMessage="Chưa ghi nhận giao dịch nào trong khoảng thời gian này"
        />
      </div>
    </div>
  )
}

export default DashboardPage
