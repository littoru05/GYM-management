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

  // Queries
  const { data: summary, isLoading: summaryLoading } = useRevenueSummary(filterParams)
  const { data: seriesData = [], isLoading: seriesLoading } = useRevenueSeries(filterParams)
  const { data: memberStats, isLoading: statsLoading } = useMembershipStatistics()
  const { data: recentTransactions = [], isLoading: txLoading } = useTransactions({
    limit: 6,
    ...filterParams,
  })

  const handleRangeChange = (newRange) => {
    setRange(newRange)
  }

  const handleCustomDateChange = (start, end) => {
    setCustomStart(start)
    setCustomEnd(end)
  }

  const totalMembers = memberStats
    ? memberStats.active + memberStats.expiringSoon + memberStats.expired
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
          label="Check-in hôm nay"
          value={statsLoading ? '—' : formatNumber(memberStats?.todayCheckIns)}
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
            data={seriesData}
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
          transactions={recentTransactions}
          loading={txLoading}
          emptyMessage="Chưa ghi nhận giao dịch nào trong khoảng thời gian này"
        />
      </div>
    </div>
  )
}

export default DashboardPage
