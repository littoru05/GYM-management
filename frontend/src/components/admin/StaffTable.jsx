import Table from '../common/Table'
import Badge from '../common/Badge'
import { Pencil, Lock, Unlock, KeyRound, Shield, User } from 'lucide-react'
import { STAFF_ROLE, STAFF_ROLE_LABEL, STAFF_STATUS } from '../../utils/constants'
import { formatDateTime } from '../../utils/formatters'

function getInitials(name = '') {
  const parts = name.trim().split(/\s+/)
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase()
  }
  return (name[0] || 'U').toUpperCase()
}

const AVATAR_BG_COLORS = [
  'bg-blue-100 text-blue-700',
  'bg-emerald-100 text-emerald-700',
  'bg-purple-100 text-purple-700',
  'bg-amber-100 text-amber-700',
  'bg-rose-100 text-rose-700',
  'bg-indigo-100 text-indigo-700',
]

function getAvatarColor(name = '') {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return AVATAR_BG_COLORS[Math.abs(hash) % AVATAR_BG_COLORS.length]
}

function StaffTable({ staffList, loading, onEdit, onToggleLock, onChangePassword }) {
  const columns = [
    {
      key: 'id',
      header: 'Mã NV',
      className: 'w-24 font-medium text-gray-800',
    },
    {
      key: 'name',
      header: 'Họ tên',
      render: (row) => (
        <div className="flex items-center gap-3">
          <div
            className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-semibold ${getAvatarColor(
              row.name
            )}`}
          >
            {getInitials(row.name)}
          </div>
          <span className="font-medium text-gray-800">{row.name}</span>
        </div>
      ),
    },
    {
      key: 'email',
      header: 'Email',
      className: 'text-gray-600',
    },
    {
      key: 'phone',
      header: 'Số điện thoại',
      className: 'text-gray-600',
    },
    {
      key: 'role',
      header: 'Vai trò',
      render: (row) => {
        const isAdmin = row.role === STAFF_ROLE.ADMIN
        return (
          <span
            className={`inline-flex items-center gap-1.5 rounded-md px-2.5 py-1 text-xs font-medium ${
              isAdmin
                ? 'bg-purple-50 text-purple-700 ring-1 ring-inset ring-purple-600/20'
                : 'bg-blue-50 text-blue-700 ring-1 ring-inset ring-blue-600/20'
            }`}
          >
            {isAdmin ? <Shield size={12} /> : <User size={12} />}
            {STAFF_ROLE_LABEL[row.role] || row.role}
          </span>
        )
      },
    },
    {
      key: 'lastLogin',
      header: 'Đăng nhập gần nhất',
      className: 'text-gray-600',
      render: (row) => (row.lastLogin ? formatDateTime(row.lastLogin) : '—'),
    },
    {
      key: 'status',
      header: 'Trạng thái',
      render: (row) => {
        const isActive = row.status === STAFF_STATUS.ACTIVE
        return (
          <Badge
            color={isActive ? 'green' : 'red'}
            dot={true}
            className="font-medium shadow-2xs"
          >
            {isActive ? 'Hoạt động' : 'Đã khóa'}
          </Badge>
        )
      },
    },
    {
      key: 'actions',
      header: 'Thao tác',
      className: 'text-right',
      render: (row) => {
        const isActive = row.status === STAFF_STATUS.ACTIVE
        return (
          <div className="flex items-center justify-end gap-1.5">
            <button
              type="button"
              onClick={() => onEdit(row)}
              className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-primary-600 focus:outline-none focus:ring-2 focus:ring-primary-200"
              title="Chỉnh sửa thông tin nhân viên"
              aria-label="Chỉnh sửa"
            >
              <Pencil size={16} />
            </button>
            <button
              type="button"
              onClick={() => onChangePassword(row)}
              className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-amber-600 focus:outline-none focus:ring-2 focus:ring-amber-200"
              title="Đặt lại mật khẩu"
              aria-label="Đặt lại mật khẩu"
            >
              <KeyRound size={16} />
            </button>
            <button
              type="button"
              onClick={() => onToggleLock(row)}
              className={`rounded-lg p-1.5 transition-colors focus:outline-none focus:ring-2 ${
                isActive
                  ? 'text-gray-400 hover:bg-red-50 hover:text-red-600 focus:ring-red-200'
                  : 'text-gray-400 hover:bg-emerald-50 hover:text-emerald-600 focus:ring-emerald-200'
              }`}
              title={isActive ? 'Khóa tài khoản nhân viên' : 'Mở khóa tài khoản nhân viên'}
              aria-label={isActive ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
            >
              {isActive ? <Lock size={16} /> : <Unlock size={16} />}
            </button>
          </div>
        )
      },
    },
  ]

  return (
    <Table
      columns={columns}
      data={staffList}
      loading={loading}
      rowKey="id"
      emptyMessage="Không tìm thấy nhân viên nào phù hợp"
    />
  )
}

export default StaffTable
