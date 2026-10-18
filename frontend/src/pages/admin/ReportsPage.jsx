import { useState, useMemo } from 'react'
import {
  Wallet,
  Tags,
  ShoppingBag,
  FileSpreadsheet,
  Search,
} from 'lucide-react'
import toast from 'react-hot-toast'
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
import { formatDate } from '../../utils/formatters.js'
import { TRANSACTION_TYPE } from '../../utils/constants.js'


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

  // Queries
  const { data: summary, isLoading: summaryLoading } = useRevenueSummary(filterParams)
  const { data: seriesData = [], isLoading: seriesLoading } = useRevenueSeries(filterParams)
  const { data: allTransactions = [], isLoading: txLoading } = useTransactions(filterParams)

  // Filter transactions in table based on search & type
  const filteredTransactions = useMemo(() => {
    return allTransactions.filter((tx) => {
      const matchType = !typeFilter || tx.type === typeFilter
      const q = searchKeyword.toLowerCase().trim()
      const matchSearch =
        !q ||
        tx.id?.toLowerCase().includes(q) ||
        tx.memberName?.toLowerCase().includes(q) ||
        tx.description?.toLowerCase().includes(q)
      return matchType && matchSearch
    })
  }, [allTransactions, typeFilter, searchKeyword])

  const totalPages = Math.ceil(filteredTransactions.length / pageSize) || 1
  const paginatedTransactions = useMemo(() => {
    const start = (currentPage - 1) * pageSize
    return filteredTransactions.slice(start, start + pageSize)
  }, [filteredTransactions, currentPage, pageSize])

  const handleExportCSV = () => {
    if (filteredTransactions.length === 0) {
      toast.error('Không có dữ liệu giao dịch để xuất báo cáo')
      return
    }

    const headers = ['Mã GD', 'Ngày', 'Khách hàng', 'Nội dung', 'Phân loại', 'Hình thức', 'Số tiền (VNĐ)']
    const rows = filteredTransactions.map((tx) => [
      tx.id,
      formatDate(tx.date),
      `"${tx.memberName || 'Khách lẻ'}"`,
      `"${tx.description || ''}"`,
      tx.type === 'MEMBERSHIP' ? 'Gói tập' : 'Bán lẻ',
      tx.paymentMethod || 'Tiền mặt',
      tx.amount,
    ])

    const csvContent =
      'data:text/csv;charset=utf-8,\uFEFF' +
      [headers.join(','), ...rows.map((e) => e.join(','))].join('\n')

    const encodedUri = encodeURI(csvContent)
    const link = document.createElement('a')
    link.setAttribute('href', encodedUri)
    link.setAttribute('download', `Bao_cao_doanh_thu_${range}_${new Date().toISOString().slice(0, 10)}.csv`)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

    toast.success('Xuất file báo cáo thành công!')
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
          onClick={handleExportCSV}
          className="shadow-sm"
        >
          Xuất báo cáo Excel / CSV
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
            data={seriesData}
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
