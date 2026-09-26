import { useState } from 'react'
import { Search, Plus, Pencil, CreditCard } from 'lucide-react'
import { useDebounce } from '../../hooks/useDebounce'
import { useMembersQuery } from '../../hooks/useMembers'
import Input from '../../components/common/Input'
import Select from '../../components/common/Select'
import Button from '../../components/common/Button'
import Badge from '../../components/common/Badge'
import Pagination from '../../components/common/Pagination'
import TableSkeleton from '../../components/common/TableSkeleton'
import EmptyState from '../../components/common/EmptyState'
import MemberFormModal from './MemberFormModal'
import SubscriptionModal from './SubscriptionModal'
import { formatDate } from '../../utils/formatters'

const STATUS_OPTIONS = [
  { value: '', label: 'Tất cả trạng thái' },
  { value: 'ACTIVE', label: 'Đang tập' },
  { value: 'EXPIRING_SOON', label: 'Sắp hết hạn' },
  { value: 'EXPIRED', label: 'Hết hạn' },
  { value: 'LOCKED', label: 'Tạm khóa' },
]

const STATUS_BADGE = {
  ACTIVE: { color: 'green', label: 'Đang tập' },
  EXPIRING_SOON: { color: 'yellow', label: 'Sắp hết hạn' },
  EXPIRED: { color: 'red', label: 'Hết hạn' },
  LOCKED: { color: 'gray', label: 'Tạm khóa' },
}

const PAGE_SIZE = 10

function MembersPage() {
  const [page, setPage] = useState(0)
  const [keyword, setKeyword] = useState('')
  const [status, setStatus] = useState('')
  const debouncedKeyword = useDebounce(keyword, 400)

  const [formModal, setFormModal] = useState({ open: false, member: null })
  const [subscriptionModal, setSubscriptionModal] = useState({ open: false, member: null })

  const { data, isLoading, isFetching } = useMembersQuery({
    page,
    size: PAGE_SIZE,
    keyword: debouncedKeyword,
    status,
  })

  const members = data?.content || []
  const totalPages = data?.totalPages || 0
  const totalElements = data?.totalElements || 0

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý hội viên</h1>
          <p className="mt-1 text-sm text-gray-500">Danh sách hội viên, tìm kiếm và đăng ký gói tập.</p>
        </div>
        <Button icon={Plus} onClick={() => setFormModal({ open: true, member: null })}>
          Thêm hội viên
        </Button>
      </div>

      <div className="mt-6 flex flex-wrap items-center gap-3 rounded-xl bg-white p-4 shadow-card">
        <Input
          icon={Search}
          placeholder="Tìm theo tên, số điện thoại hoặc mã hội viên..."
          value={keyword}
          onChange={(e) => {
            setKeyword(e.target.value)
            setPage(0)
          }}
          containerClassName="min-w-[280px] flex-1"
        />
        <Select
          options={STATUS_OPTIONS}
          value={status}
          onChange={(e) => {
            setStatus(e.target.value)
            setPage(0)
          }}
          containerClassName="w-52"
        />
      </div>

      <div className="mt-4 overflow-hidden rounded-xl bg-white shadow-card">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase text-gray-500">
            <tr>
              <th className="px-4 py-3">Mã HV</th>
              <th className="px-4 py-3">Họ tên</th>
              <th className="px-4 py-3">Số điện thoại</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Gói tập</th>
              <th className="px-4 py-3">Hạn sử dụng</th>
              <th className="px-4 py-3">Trạng thái</th>
              <th className="px-4 py-3 text-right">Thao tác</th>
            </tr>
          </thead>
          {isLoading ? (
            <TableSkeleton columns={8} />
          ) : (
            <tbody>
              {members.map((member) => {
                const badge = STATUS_BADGE[member.status] || STATUS_BADGE.ACTIVE
                return (
                  <tr key={member.id} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium text-gray-800">{member.code}</td>
                    <td className="px-4 py-3 text-gray-800">{member.name}</td>
                    <td className="px-4 py-3 text-gray-600">{member.phone}</td>
                    <td className="px-4 py-3 text-gray-600">{member.email || '—'}</td>
                    <td className="px-4 py-3 text-gray-600">{member.membershipName || '—'}</td>
                    <td className="px-4 py-3 text-gray-600">{formatDate(member.expiryDate)}</td>
                    <td className="px-4 py-3">
                      <Badge color={badge.color}>{badge.label}</Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <Button
                          size="sm"
                          variant="ghost"
                          icon={CreditCard}
                          onClick={() => setSubscriptionModal({ open: true, member })}
                        >
                          Đăng ký gói
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          icon={Pencil}
                          onClick={() => setFormModal({ open: true, member })}
                        >
                          Sửa
                        </Button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          )}
        </table>

        {!isLoading && members.length === 0 && (
          <EmptyState title="Không tìm thấy hội viên phù hợp" />
        )}

        {!isLoading && totalPages > 0 && (
          <Pagination page={page} totalPages={totalPages} totalElements={totalElements} onPageChange={setPage} />
        )}
      </div>

      {isFetching && !isLoading && (
        <p className="mt-2 text-xs text-gray-400">Đang cập nhật dữ liệu...</p>
      )}

      <MemberFormModal
        open={formModal.open}
        member={formModal.member}
        onClose={() => setFormModal({ open: false, member: null })}
      />

      <SubscriptionModal
        open={subscriptionModal.open}
        member={subscriptionModal.member}
        onClose={() => setSubscriptionModal({ open: false, member: null })}
      />
    </div>
  )
}

export default MembersPage
