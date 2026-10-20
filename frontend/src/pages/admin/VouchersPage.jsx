import { useState, useMemo } from 'react'
import {
  Plus,
  Pencil,
  Trash2,
  Lock,
  LockOpen,
  Copy,
  Check,
  TicketPercent,
  TrendingUp,
  Clock,
  AlertTriangle,
  Search,
  Filter,
  Percent,
  Banknote,
  RotateCcw,
} from 'lucide-react'
import Card from '../../components/common/Card'
import Button from '../../components/common/Button'
import Badge from '../../components/common/Badge'
import TableSkeleton from '../../components/common/TableSkeleton'
import EmptyState from '../../components/common/EmptyState'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import Pagination from '../../components/common/Pagination'
import VoucherDrawer from '../../components/admin/VoucherDrawer'
import {
  usePromotionsQuery,
  useCreatePromotion,
  useUpdatePromotion,
  useTogglePromotionStatus,
  useDeletePromotion,
} from '../../hooks/usePromotions'
import {
  DISCOUNT_TYPE,
  PROMOTION_STATUS,
  PROMOTION_STATUS_LABEL,
} from '../../utils/constants'
import { formatCurrency, formatDate } from '../../utils/formatters'
import toast from 'react-hot-toast'

const STATUS_VARIANT = {
  [PROMOTION_STATUS.ACTIVE]: 'green',
  [PROMOTION_STATUS.INACTIVE]: 'gray',
  [PROMOTION_STATUS.EXPIRED]: 'red',
  [PROMOTION_STATUS.UPCOMING]: 'blue',
  [PROMOTION_STATUS.USED_UP]: 'amber',
}

function getRelativeDateLabel(startDate, endDate) {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const start = new Date(startDate)
  start.setHours(0, 0, 0, 0)
  const end = new Date(endDate)
  end.setHours(0, 0, 0, 0)

  if (today < start) {
    const diffDays = Math.ceil((start - today) / (1000 * 60 * 60 * 24))
    return { label: `Bắt đầu sau ${diffDays} ngày`, color: 'text-blue-600' }
  }
  if (today > end) {
    return { label: 'Đã hết hạn', color: 'text-red-500' }
  }
  const diffDays = Math.ceil((end - today) / (1000 * 60 * 60 * 24))
  if (diffDays === 0) {
    return { label: 'Hết hạn hôm nay', color: 'text-amber-600 font-semibold' }
  }
  return { label: `Còn ${diffDays} ngày`, color: 'text-gray-500' }
}

function VouchersPage() {
  const [search, setSearch] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [typeFilter, setTypeFilter] = useState('ALL')
  const [drawerState, setDrawerState] = useState({ open: false, voucher: null })
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [copiedCode, setCopiedCode] = useState(null)
  const [currentPage, setCurrentPage] = useState(0)
  const pageSize = 10

  // Queries & Mutations
  const { data: vouchers = [], isLoading } = usePromotionsQuery({
    search,
    status: statusFilter,
    discountType: typeFilter,
  })

  const totalPages = Math.ceil(vouchers.length / pageSize) || 1
  const paginatedVouchers = useMemo(() => {
    const start = currentPage * pageSize
    return vouchers.slice(start, start + pageSize)
  }, [vouchers, currentPage, pageSize])

  // Full vouchers query for stats
  const { data: allVouchers = [] } = usePromotionsQuery()

  const createMutation = useCreatePromotion()
  const updateMutation = useUpdatePromotion()
  const toggleMutation = useTogglePromotionStatus()
  const deleteMutation = useDeletePromotion()

  // Copy code to clipboard
  const handleCopyCode = (code) => {
    navigator.clipboard.writeText(code)
    setCopiedCode(code)
    toast.success(`Đã sao chép mã "${code}"`)
    setTimeout(() => {
      setCopiedCode((prev) => (prev === code ? null : prev))
    }, 2000)
  }

  // Calculate statistics
  const stats = useMemo(() => {
    const total = allVouchers.length
    const active = allVouchers.filter((v) => v.effectiveStatus === PROMOTION_STATUS.ACTIVE).length
    const totalUsed = allVouchers.reduce((acc, v) => acc + (Number(v.usedCount) || 0), 0)
    const inactiveOrExpired = allVouchers.filter(
      (v) =>
        v.effectiveStatus === PROMOTION_STATUS.EXPIRED ||
        v.effectiveStatus === PROMOTION_STATUS.INACTIVE ||
        v.effectiveStatus === PROMOTION_STATUS.USED_UP
    ).length

    return { total, active, totalUsed, inactiveOrExpired }
  }, [allVouchers])

  // Open drawer for create
  const handleOpenCreate = () => {
    setDrawerState({ open: true, voucher: null })
  }

  // Open drawer for edit
  const handleOpenEdit = (voucher) => {
    setDrawerState({ open: true, voucher })
  }

  // Handle drawer submit
  const handleDrawerSubmit = async (payload) => {
    try {
      if (drawerState.voucher?.id) {
        await updateMutation.mutateAsync({
          id: drawerState.voucher.id,
          payload,
        })
      } else {
        await createMutation.mutateAsync(payload)
      }
      setDrawerState({ open: false, voucher: null })
    } catch {
      // Error handled by mutation toast
    }
  }

  // Handle toggle status
  const handleToggleStatus = (voucher) => {
    toggleMutation.mutate(voucher.id)
  }

  // Handle confirm delete
  const handleConfirmDelete = async () => {
    if (!deleteTarget) return
    try {
      await deleteMutation.mutateAsync(deleteTarget.id)
      setDeleteTarget(null)
    } catch {
      // Error handled by mutation toast
    }
  }

  // Reset filters
  const hasActiveFilters = search || statusFilter !== 'ALL' || typeFilter !== 'ALL'
  const handleResetFilters = () => {
    setSearch('')
    setStatusFilter('ALL')
    setTypeFilter('ALL')
    setCurrentPage(0)
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-2xl font-bold text-gray-900">Quản lý Voucher & Khuyến mãi</h1>
            <span className="rounded-full bg-primary-50 px-2.5 py-0.5 text-xs font-semibold text-primary-700">
              {allVouchers.length} mã
            </span>
          </div>
          <p className="mt-1 text-sm text-gray-500">
            Dựng danh sách voucher, thiết lập mức giảm giá (% hoặc VNĐ), thời hạn và số lượt dùng tối đa.
          </p>
        </div>

        <Button icon={Plus} onClick={handleOpenCreate}>
          Thêm voucher mới
        </Button>
      </div>

      {/* Quick Statistics Cards */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        {/* Total Vouchers */}
        <div className="rounded-xl border border-gray-100 bg-white p-4 shadow-xs transition-shadow hover:shadow-md">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-wider text-gray-500">
              Tổng số voucher
            </span>
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
              <TicketPercent size={18} />
            </div>
          </div>
          <p className="mt-2 text-2xl font-bold text-gray-900">{stats.total}</p>
          <p className="mt-1 text-xs text-gray-400">Đã khởi tạo trong hệ thống</p>
        </div>

        {/* Active Vouchers */}
        <div className="rounded-xl border border-gray-100 bg-white p-4 shadow-xs transition-shadow hover:shadow-md">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-wider text-gray-500">
              Đang hoạt động
            </span>
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-emerald-50 text-emerald-600">
              <TrendingUp size={18} />
            </div>
          </div>
          <p className="mt-2 text-2xl font-bold text-emerald-600">{stats.active}</p>
          <p className="mt-1 text-xs text-emerald-600 font-medium">Khách hàng có thể sử dụng</p>
        </div>

        {/* Total Used */}
        <div className="rounded-xl border border-gray-100 bg-white p-4 shadow-xs transition-shadow hover:shadow-md">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-wider text-gray-500">
              Lượt đã sử dụng
            </span>
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-50 text-blue-600">
              <Clock size={18} />
            </div>
          </div>
          <p className="mt-2 text-2xl font-bold text-blue-600">{stats.totalUsed}</p>
          <p className="mt-1 text-xs text-gray-400">Tổng lượt quy đổi giảm giá</p>
        </div>

        {/* Inactive or Expired */}
        <div className="rounded-xl border border-gray-100 bg-white p-4 shadow-xs transition-shadow hover:shadow-md">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium uppercase tracking-wider text-gray-500">
              Hết hạn / Tạm ngưng
            </span>
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-amber-50 text-amber-600">
              <AlertTriangle size={18} />
            </div>
          </div>
          <p className="mt-2 text-2xl font-bold text-amber-600">{stats.inactiveOrExpired}</p>
          <p className="mt-1 text-xs text-gray-400">Không khả dụng lúc này</p>
        </div>
      </div>

      {/* Search & Filter Toolbar */}
      <Card padding="p-4" className="shadow-xs">
        <div className="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
          {/* Search Box */}
          <div className="relative flex-1">
            <Search
              size={16}
              className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400"
            />
            <input
              type="text"
              value={search}
              onChange={(e) => {
                setSearch(e.target.value)
                setCurrentPage(0)
              }}
              placeholder="Tìm kiếm theo mã voucher, tên khuyến mãi..."
              className="w-full rounded-lg border border-gray-200 bg-white py-2 pl-9 pr-3 text-sm text-gray-900 placeholder:text-gray-400 focus:border-primary-500 focus:outline-none focus:ring-2 focus:ring-primary-200"
            />
          </div>

          {/* Filters */}
          <div className="flex flex-wrap items-center gap-2">
            <div className="flex items-center gap-1.5 text-xs font-medium text-gray-500">
              <Filter size={14} /> Lọc:
            </div>

            {/* Status Filter */}
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value)
                setCurrentPage(0)
              }}
              className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-primary-200"
            >
              <option value="ALL">Tất cả trạng thái</option>
              <option value={PROMOTION_STATUS.ACTIVE}>Đang hoạt động</option>
              <option value={PROMOTION_STATUS.INACTIVE}>Tạm ngưng</option>
              <option value={PROMOTION_STATUS.UPCOMING}>Chưa bắt đầu</option>
              <option value={PROMOTION_STATUS.EXPIRED}>Đã hết hạn</option>
              <option value={PROMOTION_STATUS.USED_UP}>Hết lượt dùng</option>
            </select>

            {/* Discount Type Filter */}
            <select
              value={typeFilter}
              onChange={(e) => {
                setTypeFilter(e.target.value)
                setCurrentPage(0)
              }}
              className="rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs font-medium text-gray-700 focus:outline-none focus:ring-2 focus:ring-primary-200"
            >
              <option value="ALL">Tất cả loại giảm</option>
              <option value={DISCOUNT_TYPE.PERCENTAGE}>Theo phần trăm (%)</option>
              <option value={DISCOUNT_TYPE.FIXED}>Số tiền (VNĐ)</option>
            </select>

            {/* Reset Filters */}
            {hasActiveFilters && (
              <button
                type="button"
                onClick={handleResetFilters}
                className="flex items-center gap-1 rounded-lg border border-gray-200 bg-gray-50 px-2.5 py-2 text-xs font-medium text-gray-600 hover:bg-gray-100 transition-colors"
                title="Đặt lại bộ lọc"
              >
                <RotateCcw size={13} />
                Đặt lại
              </button>
            )}
          </div>
        </div>
      </Card>

      {/* Voucher Table View */}
      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-card">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[760px] text-left text-sm">
            <thead className="border-b border-gray-100 bg-gray-50 text-xs font-semibold uppercase tracking-wider text-gray-500">
              <tr>
                <th className="px-4 py-3.5">Mã Voucher</th>
                <th className="px-4 py-3.5">Chương trình khuyến mãi</th>
                <th className="px-4 py-3.5">Mức giảm</th>
                <th className="px-4 py-3.5">Hạn sử dụng</th>
                <th className="px-4 py-3.5">Lượt sử dụng</th>
                <th className="px-4 py-3.5">Trạng thái</th>
                <th className="px-4 py-3.5 text-right">Thao tác</th>
              </tr>
            </thead>
            {isLoading ? (
              <TableSkeleton columns={7} />
            ) : (
              <tbody className="divide-y divide-gray-100 bg-white">
                {paginatedVouchers.map((voucher) => {
                  const isPercentage = voucher.discountType === DISCOUNT_TYPE.PERCENTAGE
                  const relativeDate = getRelativeDateLabel(voucher.startDate, voucher.endDate)
                  const usedCount = Number(voucher.usedCount) || 0
                  const usageLimit = Number(voucher.usageLimit) || 1
                  const usagePercent = Math.min(100, Math.round((usedCount / usageLimit) * 100))
                  const effectiveStatus = voucher.effectiveStatus || PROMOTION_STATUS.ACTIVE
                  const isCopied = copiedCode === voucher.code

                  return (
                    <tr
                      key={voucher.id}
                      className="transition-colors hover:bg-gray-50/80 group"
                    >
                      {/* Code column */}
                      <td className="px-4 py-3.5">
                        <div className="flex items-center gap-1.5">
                          <span className="inline-flex items-center rounded-lg border border-primary-200 bg-primary-50 px-2.5 py-1 font-mono text-xs font-bold tracking-wider text-primary-700">
                            {voucher.code}
                          </span>
                          <button
                            type="button"
                            onClick={() => handleCopyCode(voucher.code)}
                            className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
                            title="Sao chép mã voucher"
                          >
                            {isCopied ? (
                              <Check size={14} className="text-emerald-600" />
                            ) : (
                              <Copy size={14} />
                            )}
                          </button>
                        </div>
                      </td>

                      {/* Name & Description column */}
                      <td className="px-4 py-3.5">
                        <p className="font-semibold text-gray-900 leading-snug">
                          {voucher.name}
                        </p>
                        {voucher.description && (
                          <p className="mt-0.5 text-xs text-gray-500 line-clamp-1">
                            {voucher.description}
                          </p>
                        )}
                      </td>

                      {/* Discount value column */}
                      <td className="px-4 py-3.5 whitespace-nowrap">
                        <div className="flex items-center gap-1.5">
                          <div
                            className={`flex h-6 w-6 items-center justify-center rounded-md ${
                              isPercentage
                                ? 'bg-blue-100 text-blue-700'
                                : 'bg-emerald-100 text-emerald-700'
                            }`}
                          >
                            {isPercentage ? <Percent size={13} /> : <Banknote size={13} />}
                          </div>
                          <div>
                            <span
                              className={`text-sm font-bold ${
                                isPercentage ? 'text-blue-700' : 'text-emerald-700'
                              }`}
                            >
                              {isPercentage
                                ? `Giảm ${voucher.discountValue}%`
                                : `Giảm ${formatCurrency(voucher.discountValue)}`}
                            </span>
                            <p className="text-[10px] text-gray-400 uppercase">
                              {isPercentage ? 'Theo tỷ lệ %' : 'Tiền mặt VNĐ'}
                            </p>
                          </div>
                        </div>
                      </td>

                      {/* Validity Period */}
                      <td className="px-4 py-3.5 whitespace-nowrap">
                        <div className="text-xs text-gray-700">
                          <span className="font-medium">{formatDate(voucher.startDate)}</span>
                          <span className="text-gray-400 mx-1">→</span>
                          <span className="font-medium">{formatDate(voucher.endDate)}</span>
                        </div>
                        <p className={`mt-0.5 text-[11px] ${relativeDate.color}`}>
                          {relativeDate.label}
                        </p>
                      </td>

                      {/* Usage Limit & Progress */}
                      <td className="px-4 py-3.5 whitespace-nowrap">
                        <div className="w-32">
                          <div className="flex items-center justify-between text-xs">
                            <span className="font-semibold text-gray-800">
                              {usedCount}/{usageLimit}
                            </span>
                            <span className="text-[11px] text-gray-500">{usagePercent}%</span>
                          </div>
                          <div className="mt-1 h-1.5 w-full overflow-hidden rounded-full bg-gray-100">
                            <div
                              className={`h-full transition-all duration-300 ${
                                usagePercent >= 100
                                  ? 'bg-amber-500'
                                  : usagePercent >= 75
                                  ? 'bg-primary-500'
                                  : 'bg-emerald-500'
                              }`}
                              style={{ width: `${usagePercent}%` }}
                            />
                          </div>
                        </div>
                      </td>

                      {/* Effective Status */}
                      <td className="px-4 py-3.5 whitespace-nowrap">
                        <Badge
                          variant={STATUS_VARIANT[effectiveStatus] || 'gray'}
                          dot
                        >
                          {PROMOTION_STATUS_LABEL[effectiveStatus] || voucher.status}
                        </Badge>
                      </td>

                      {/* Actions */}
                      <td className="px-4 py-3.5 text-right whitespace-nowrap">
                        <div className="flex items-center justify-end gap-1">
                          <button
                            type="button"
                            onClick={() => handleOpenEdit(voucher)}
                            className="rounded-lg p-1.5 text-gray-500 hover:bg-gray-100 hover:text-primary-600 transition-colors"
                            title="Chỉnh sửa voucher"
                          >
                            <Pencil size={16} />
                          </button>
                          <button
                            type="button"
                            onClick={() => handleToggleStatus(voucher)}
                            className="rounded-lg p-1.5 text-gray-500 hover:bg-gray-100 hover:text-amber-600 transition-colors"
                            title={
                              voucher.status === 'ACTIVE'
                                ? 'Tạm ngưng voucher'
                                : 'Kích hoạt lại voucher'
                            }
                          >
                            {voucher.status === 'ACTIVE' ? (
                              <Lock size={16} />
                            ) : (
                              <LockOpen size={16} />
                            )}
                          </button>
                          <button
                            type="button"
                            onClick={() => setDeleteTarget(voucher)}
                            className="rounded-lg p-1.5 text-gray-500 hover:bg-red-50 hover:text-red-600 transition-colors"
                            title="Xóa voucher"
                          >
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            )}
          </table>
        </div>

        {!isLoading && vouchers.length > 0 && (
          <Pagination
            page={currentPage}
            totalPages={totalPages}
            totalElements={vouchers.length}
            onPageChange={(newPage) => setCurrentPage(newPage)}
          />
        )}

        {/* Empty State */}
        {!isLoading && vouchers.length === 0 && (
          <div className="p-8">
            <EmptyState
              title={
                hasActiveFilters
                  ? 'Không tìm thấy voucher phù hợp'
                  : 'Chưa có voucher nào'
              }
              description={
                hasActiveFilters
                  ? 'Hãy thử thay đổi từ khóa tìm kiếm hoặc bỏ chọn các bộ lọc trạng thái.'
                  : 'Tạo mã voucher đầu tiên để bắt đầu triển khai các chương trình ưu đãi hấp dẫn.'
              }
              actionLabel={hasActiveFilters ? 'Đặt lại bộ lọc' : 'Thêm voucher mới'}
              onAction={hasActiveFilters ? handleResetFilters : handleOpenCreate}
            />
          </div>
        )}
      </div>

      {/* Drawer Create / Edit */}
      <VoucherDrawer
        open={drawerState.open}
        onClose={() => setDrawerState({ open: false, voucher: null })}
        voucher={drawerState.voucher}
        onSubmit={handleDrawerSubmit}
        submitting={createMutation.isPending || updateMutation.isPending}
      />

      {/* Confirm Delete Dialog */}
      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleConfirmDelete}
        loading={deleteMutation.isPending}
        title="Xóa voucher"
        message={`Bạn có chắc chắn muốn xóa voucher "${deleteTarget?.code}" (${deleteTarget?.name})? Hành động này sẽ loại bỏ voucher khỏi hệ thống và không thể hoàn tác.`}
        confirmLabel="Xóa voucher"
      />
    </div>
  )
}

export default VouchersPage
