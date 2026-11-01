import { useState, useMemo } from 'react'
import {
  Wallet,
  Tags,
  ShoppingBag,
  FileSpreadsheet,
  Search,
  WifiOff,
  RefreshCw,
} from 'lucide-react'
import { showErrorToast, showSuccessToast } from '../../utils/toastService'
import Button from '../../components/common/Button.jsx'
import RevenueCard from '../../components/common/RevenueCard.jsx'
import RevenueChart from '../../components/common/RevenueChart.jsx'
import RevenuePieChart from '../../components/common/RevenuePieChart.jsx'
import TransactionTable from '../../components/common/TransactionTable.jsx'
import DateRangeFilter from '../../components/common/DateRangeFilter.jsx'
import Pagination from '../../components/common/Pagination.jsx'
import { StatCardsSkeleton } from '../../components/common/Skeleton.jsx'
import { useRevenueSummary, useRevenueSeries } from '../../hooks/useRevenue.js'
import { useTransactions } from '../../hooks/useTransactions.js'
import { TRANSACTION_TYPE } from '../../utils/constants.js'


import { exportRevenueReportExcel, resolveDateRange } from '../../services/reportService.js'

const TYPE_FILTER_OPTIONS = [
  { value: '', label: 'Tất cả loại giao dịch' },
  { value: TRANSACTION_TYPE.MEMBERSHIP, label: 'Doanh thu gói tập' },
  { value: TRANSACTION_TYPE.RETAIL, label: 'Doanh thu bán lẻ' },
]

function ReportsPage() {
  const [range, setRange] = useState('30d')
  const [customStart, setCustomStart] = useState('')
  const [customEnd, setCustomEnd] = useState('')
  const [simulateEmpty, setSimulateEmpty] = useState(false)
  const [isExportingExcel, setIsExportingExcel] = useState(false)

  // Table filters & pagination
  const [typeFilter, setTypeFilter] = useState('')
  const [searchKeyword, setSearchKeyword] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const pageSize = 10

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
    data: allTransactions = [],
    isLoading: txLoading,
    isError: isTxError,
    refetch: refetchTx,
    isFetching: isTxFetching,
  } = useTransactions(filterParams)

  const hasNetworkError = isSummaryError || isSeriesError || isTxError
  const isRefetching = isSummaryFetching || isSeriesFetching || isTxFetching

  const handleRetryAll = () => {
    refetchSummary()
    refetchSeries()
    refetchTx()
  }

  const safeSeriesData = Array.isArray(seriesData) ? seriesData : []
  const safeTransactions = useMemo(
    () => (Array.isArray(allTransactions) ? allTransactions : []),
    [allTransactions]
  )

  // Filter transactions in table based on search & type (an toàn tuyệt đối chống crash)
  const filteredTransactions = useMemo(() => {
    return safeTransactions.filter((tx) => {
      if (!tx) return false
      const matchType = !typeFilter || tx.type === typeFilter
      const q = (searchKeyword || '').toLowerCase().trim()
      const matchSearch =
        !q ||
        tx.id?.toLowerCase().includes(q) ||
        tx.memberName?.toLowerCase().includes(q) ||
        tx.description?.toLowerCase().includes(q)
      return matchType && matchSearch
    })
  }, [safeTransactions, typeFilter, searchKeyword])

  const totalPages = Math.ceil(filteredTransactions.length / pageSize) || 1
  const paginatedTransactions = useMemo(() => {
    const start = (currentPage - 1) * pageSize
    return filteredTransactions.slice(start, start + pageSize)
  }, [filteredTransactions, currentPage, pageSize])

  const handleExportExcel = async () => {
    try {
      setIsExportingExcel(true)
      const { start, end } = resolveDateRange(range, customStart, customEnd)
      const blobData = await exportRevenueReportExcel({
        startDate: start,
        endDate: end,
        type: typeFilter || 'ALL',
      })

      const blob = new Blob([blobData], {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })
      const downloadUrl = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = downloadUrl
      link.setAttribute('download', `revenue-report_${start}_${end}.xlsx`)
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)

      showSuccessToast(`Đã xuất báo cáo Excel thành công: revenue-report_${start}_${end}.xlsx`)
    } catch (err) {
      showErrorToast(err, 'Lỗi khi xuất file Excel báo cáo doanh thu.')
    } finally {
      setIsExportingExcel(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-gray-900">
            Báo cáo doanh thu & Thống kê
          </h1>
          <p className="mt-1 text-sm text-gray-500">
            Phân tích chuyên sâu doanh thu gói tập, bán lẻ và dữ liệu tài chính
          </p>
        </div>

        <Button
          variant="secondary"
          icon={FileSpreadsheet}
          onClick={handleExportExcel}
          loading={isExportingExcel}
          disabled={isExportingExcel}
          className="shadow-sm"
        >
          Xuất Excel
        </Button>
      </div>

      {/* Quick Filter: 7 ngày, 30 ngày, 12 tháng, Tùy chọn ngày */}
      <DateRangeFilter
        range={range}
        onRangeChange={(r) => {
          setRange(r)
          setCurrentPage(1)
        }}
        customStartDate={customStart}
        customEndDate={customEnd}
        onCustomDateChange={(start, end) => {
          setCustomStart(start)
          setCustomEnd(end)
          setCurrentPage(1)
        }}
        simulateEmpty={simulateEmpty}
        onToggleSimulateEmpty={() => setSimulateEmpty(!simulateEmpty)}
      />

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
                Hệ thống đã tự động thử lại nhẹ nhàng (retry). Dữ liệu sẽ tự động tải lại khi mạng ổn định hoặc nhấn nút bên cạnh.
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
          Tổng kết kỳ báo cáo
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
              subtext={`${summary?.transactionCount || 0} lượt giao dịch hoàn tất`}
            />
            <RevenueCard
              icon={Tags}
              label="Doanh thu gói tập"
              value={summary?.membership}
              trend={summary?.membershipTrend}
              accent="blue"
              subtext="Đăng ký mới & gia hạn gói hội viên"
            />
            <RevenueCard
              icon={ShoppingBag}
              label="Doanh thu bán lẻ"
              value={summary?.retail}
              trend={summary?.retailTrend}
              accent="green"
              subtext="Nước uống, thực phẩm bổ sung, phụ kiện"
            />
          </div>
        )}
      </div>

      {/* Charts Grid: Biểu đồ xu hướng (cột/đường) + Biểu đồ tỷ trọng (Donut/Pie) */}
      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="lg:col-span-2">
          <RevenueChart
            data={safeSeriesData}
            loading={seriesLoading}
            title="Biểu đồ xu hướng doanh thu"
            subtitle="Phân bổ dòng tiền theo mốc thời gian"
            height={340}
          />
        </div>

        <div className="lg:col-span-1">
          <RevenuePieChart
            membershipRevenue={summary?.membership || 0}
            retailRevenue={summary?.retail || 0}
            loading={summaryLoading}
            title="Biểu đồ tỷ trọng cơ cấu"
            subtitle="Tỷ lệ đóng góp doanh thu theo ngành hàng"
            height={340}
          />
        </div>
      </div>

      {/* Detailed Transactions List */}
      <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-card">
        <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="font-semibold text-gray-900">Chi tiết giao dịch trong kỳ</h2>
            <p className="text-xs text-gray-500">
              Hiển thị {filteredTransactions.length} giao dịch phù hợp
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-2.5">
            {/* Search */}
            <div className="relative">
              <Search
                size={15}
                className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
              />
              <input
                type="text"
                placeholder="Tìm mã GD, hội viên..."
                value={searchKeyword}
                onChange={(e) => {
                  setSearchKeyword(e.target.value)
                  setCurrentPage(1)
                }}
                className="w-48 rounded-lg border border-gray-200 bg-gray-50 py-1.5 pl-8 pr-3 text-xs text-gray-900 placeholder:text-gray-400 focus:border-primary-500 focus:bg-white focus:outline-none sm:w-56"
              />
            </div>

            {/* Type Filter */}
            <select
              value={typeFilter}
              onChange={(e) => {
                setTypeFilter(e.target.value)
                setCurrentPage(1)
              }}
              className="rounded-lg border border-gray-200 bg-gray-50 px-3 py-1.5 text-xs text-gray-700 focus:border-primary-500 focus:bg-white focus:outline-none"
            >
              {TYPE_FILTER_OPTIONS.map((opt) => (
                <option key={opt.value} value={opt.value}>
                  {opt.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <TransactionTable
          transactions={paginatedTransactions}
          loading={txLoading}
          emptyMessage="Không tìm thấy giao dịch nào thỏa mãn tiêu chí lọc"
        />

        {filteredTransactions.length > pageSize && (
          <div className="mt-4 border-t border-gray-100 pt-3">
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={setCurrentPage}
            />
          </div>
        )}
      </div>
    </div>
  )
}

export default ReportsPage
