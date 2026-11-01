import { useState, useMemo } from 'react'
import { Plus, Users, UserCheck, UserX, ShieldCheck, Search, FilterX } from 'lucide-react'
import Button from '../../components/common/Button'
import Input from '../../components/common/Input'
import Select from '../../components/common/Select'
import Modal from '../../components/common/Modal'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import Pagination from '../../components/common/Pagination'
import StaffTable from '../../components/admin/StaffTable'
import StaffForm from '../../components/admin/StaffForm'
import StaffLockModal from '../../components/admin/StaffLockModal'
import { useStaffList, useStaffMutations } from '../../hooks/useStaff'
import { useDebounce } from '../../hooks/useDebounce'
import { showErrorToast, showSuccessToast } from '../../utils/toastService'
import { STAFF_ROLE, STAFF_ROLE_LABEL, STAFF_STATUS, STAFF_STATUS_LABEL } from '../../utils/constants'

const ROLE_FILTER_OPTIONS = [
  { value: '', label: 'Tất cả vai trò' },
  { value: STAFF_ROLE.ADMIN, label: STAFF_ROLE_LABEL[STAFF_ROLE.ADMIN] },
  { value: STAFF_ROLE.STAFF, label: STAFF_ROLE_LABEL[STAFF_ROLE.STAFF] },
]

const STATUS_FILTER_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: STAFF_STATUS.ACTIVE, label: STAFF_STATUS_LABEL[STAFF_STATUS.ACTIVE] },
  { value: STAFF_STATUS.LOCKED, label: STAFF_STATUS_LABEL[STAFF_STATUS.LOCKED] },
]

function StaffManagementPage() {
  const [keyword, setKeyword] = useState('')
  const [roleFilter, setRoleFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [currentPage, setCurrentPage] = useState(0)
  const pageSize = 10
  const debouncedKeyword = useDebounce(keyword, 300)

  // Modals state
  const [formOpen, setFormOpen] = useState(false)
  const [editingStaff, setEditingStaff] = useState(null)
  const [lockTarget, setLockTarget] = useState(null)
  const [passwordTarget, setPasswordTarget] = useState(null)

  // API query & mutations
  const { data: staffData, isLoading, isFetching } = useStaffList({
    page: currentPage,
    size: pageSize,
    keyword: debouncedKeyword,
    role: roleFilter,
    status: statusFilter,
  })

  // Full list for KPI calculation
  const { data: allStaffData } = useStaffList({ page: 0, size: 500 })

  const { createStaff, updateStaff, toggleStaffLock, changeStaffPassword } = useStaffMutations()

  const staffList = useMemo(() => {
    if (!staffData) return []
    if (Array.isArray(staffData)) return staffData
    return staffData.content || []
  }, [staffData])

  const totalElements = staffData?.totalElements ?? staffList.length
  const totalPages = staffData?.totalPages ?? (Math.ceil(totalElements / pageSize) || 1)

  const allStaffList = useMemo(() => {
    if (!allStaffData) return staffList
    if (Array.isArray(allStaffData)) return allStaffData
    return allStaffData.content || []
  }, [allStaffData, staffList])

  // KPI stats calculations
  const stats = useMemo(() => {
    const total = allStaffList.length
    const active = allStaffList.filter((s) => s.status === STAFF_STATUS.ACTIVE).length
    const locked = allStaffList.filter((s) => s.status === STAFF_STATUS.LOCKED).length
    const admin = allStaffList.filter((s) => s.role === STAFF_ROLE.ADMIN).length
    return { total, active, locked, admin }
  }, [allStaffList])

  const openCreateForm = () => {
    setEditingStaff(null)
    setFormOpen(true)
  }

  const openEditForm = (staff) => {
    setEditingStaff(staff)
    setFormOpen(true)
  }

  const handleFormSubmit = async (payload) => {
    try {
      if (editingStaff) {
        await updateStaff.mutateAsync({ id: editingStaff.id, payload })
        showSuccessToast(`Cập nhật thông tin nhân viên "${payload.name}" thành công!`)
      } else {
        await createStaff.mutateAsync(payload)
        showSuccessToast(`Thêm nhân viên "${payload.name}" thành công!`)
      }
      setFormOpen(false)
      setEditingStaff(null)
    } catch (err) {
      showErrorToast(err, 'Có lỗi xảy ra, vui lòng kiểm tra lại thông tin nhân viên!')
    }
  }

  const handleConfirmToggleLock = async (target) => {
    if (!target) return
    const isLocking = target.status === STAFF_STATUS.ACTIVE
    try {
      await toggleStaffLock.mutateAsync(target.id)
      if (isLocking) {
        showSuccessToast(`Đã khóa tài khoản nhân viên "${target.name}"!`)
      } else {
        showSuccessToast(`Đã mở khóa tài khoản nhân viên "${target.name}"!`)
      }
      setLockTarget(null)
    } catch (err) {
      showErrorToast(err, 'Không thể cập nhật trạng thái tài khoản!')
    }
  }

  const handleConfirmResetPassword = async () => {
    if (!passwordTarget) return
    try {
      await changeStaffPassword.mutateAsync({ id: passwordTarget.id, password: 'defaultPassword' })
      showSuccessToast(`Đã đặt lại mật khẩu mặc định (123456) cho "${passwordTarget.name}"!`)
      setPasswordTarget(null)
    } catch (err) {
      showErrorToast(err, 'Không thể đặt lại mật khẩu!')
    }
  }

  const handleResetFilters = () => {
    setKeyword('')
    setRoleFilter('')
    setStatusFilter('')
    setCurrentPage(0)
  }

  const hasActiveFilter = Boolean(keyword || roleFilter || statusFilter)

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-gray-900">Quản lý nhân viên</h1>
          <p className="mt-1 text-sm text-gray-500">
            Danh sách nhân viên, thông tin liên hệ và phân quyền truy cập hệ thống.
          </p>
        </div>
        <Button icon={Plus} onClick={openCreateForm} className="shadow-xs">
          Thêm nhân viên
        </Button>
      </div>

      {/* KPI Overview Cards */}
      <div className="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <div className="flex items-center gap-3.5 rounded-xl border border-gray-100 bg-white p-4 shadow-xs">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-blue-50 text-blue-600">
            <Users size={22} />
          </div>
          <div>
            <div className="text-2xl font-bold text-gray-900">{stats.total}</div>
            <div className="text-xs font-medium text-gray-500">Tổng số nhân sự</div>
          </div>
        </div>

        <div className="flex items-center gap-3.5 rounded-xl border border-gray-100 bg-white p-4 shadow-xs">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-emerald-50 text-emerald-600">
            <UserCheck size={22} />
          </div>
          <div>
            <div className="text-2xl font-bold text-emerald-600">{stats.active}</div>
            <div className="text-xs font-medium text-gray-500">Đang hoạt động</div>
          </div>
        </div>

        <div className="flex items-center gap-3.5 rounded-xl border border-gray-100 bg-white p-4 shadow-xs">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-rose-50 text-rose-600">
            <UserX size={22} />
          </div>
          <div>
            <div className="text-2xl font-bold text-rose-600">{stats.locked}</div>
            <div className="text-xs font-medium text-gray-500">Đã khóa tài khoản</div>
          </div>
        </div>

        <div className="flex items-center gap-3.5 rounded-xl border border-gray-100 bg-white p-4 shadow-xs">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-purple-50 text-purple-600">
            <ShieldCheck size={22} />
          </div>
          <div>
            <div className="text-2xl font-bold text-purple-600">{stats.admin}</div>
            <div className="text-xs font-medium text-gray-500">Quản trị viên</div>
          </div>
        </div>
      </div>

      {/* Filter and Search Bar */}
      <div className="flex flex-col gap-3 rounded-xl border border-gray-100 bg-white p-4 shadow-xs md:flex-row md:items-center">
        <div className="flex-1">
          <Input
            icon={Search}
            placeholder="Tìm theo tên, email, số điện thoại hoặc mã NV..."
            value={keyword}
            onChange={(e) => {
              setKeyword(e.target.value)
              setCurrentPage(0)
            }}
            containerClassName="w-full"
          />
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <Select
            options={ROLE_FILTER_OPTIONS}
            value={roleFilter}
            onChange={(e) => {
              setRoleFilter(e.target.value)
              setCurrentPage(0)
            }}
            containerClassName="w-40"
          />

          <Select
            options={STATUS_FILTER_OPTIONS}
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value)
              setCurrentPage(0)
            }}
            containerClassName="w-40"
          />

          {hasActiveFilter && (
            <Button
              variant="ghost"
              size="sm"
              icon={FilterX}
              onClick={handleResetFilters}
              className="text-gray-500 hover:text-gray-800"
              title="Xóa bộ lọc"
            >
              Xóa lọc
            </Button>
          )}
        </div>
      </div>

      {/* Staff Table Card */}
      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white shadow-xs">
        <div className="border-b border-gray-100 px-6 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="text-sm font-semibold text-gray-900">Danh sách nhân viên</span>
              <span className="rounded-full bg-gray-100 px-2.5 py-0.5 text-xs font-medium text-gray-600">
                {totalElements} nhân sự
              </span>
            </div>
            {isFetching && !isLoading && (
              <span className="text-xs text-gray-400 animate-pulse">Đang cập nhật...</span>
            )}
          </div>
        </div>

        <StaffTable
          staffList={staffList}
          loading={isLoading}
          onEdit={openEditForm}
          onToggleLock={setLockTarget}
          onChangePassword={setPasswordTarget}
        />

        <Pagination
          page={currentPage}
          totalPages={totalPages}
          totalElements={totalElements}
          onPageChange={(newPage) => setCurrentPage(newPage)}
        />
      </div>

      {/* Modal: Thêm / Sửa nhân viên */}
      <Modal
        open={formOpen}
        onClose={() => {
          if (!createStaff.isPending && !updateStaff.isPending) {
            setFormOpen(false)
            setEditingStaff(null)
          }
        }}
        title={editingStaff ? 'Chỉnh sửa thông tin nhân viên' : 'Thêm nhân viên mới'}
        size="md"
      >
        <StaffForm
          staff={editingStaff}
          onSubmit={handleFormSubmit}
          onCancel={() => {
            setFormOpen(false)
            setEditingStaff(null)
          }}
          submitting={createStaff.isPending || updateStaff.isPending}
        />
      </Modal>

      {/* Modal Popup: Xác nhận khóa / mở tài khoản theo wireframe */}
      <StaffLockModal
        open={Boolean(lockTarget)}
        staff={lockTarget}
        loading={toggleStaffLock.isPending}
        onClose={() => setLockTarget(null)}
        onConfirm={handleConfirmToggleLock}
      />

      {/* Modal: Đặt lại mật khẩu */}
      <ConfirmDialog
        open={Boolean(passwordTarget)}
        onClose={() => setPasswordTarget(null)}
        onConfirm={handleConfirmResetPassword}
        loading={changeStaffPassword.isPending}
        title="Đặt lại mật khẩu"
        message={`Bạn có chắc muốn đặt lại mật khẩu về mặc định (123456) cho nhân viên "${passwordTarget?.name}"?`}
        confirmLabel="Đặt lại mật khẩu"
        cancelLabel="Hủy bỏ"
        variant="primary"
      />
    </div>
  )
}

export default StaffManagementPage
