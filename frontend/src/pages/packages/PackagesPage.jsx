import { useState } from 'react'
import { Plus, Pencil, Lock, LockOpen } from 'lucide-react'
import { useAuthStore } from '../../store/useAuthStore'
import { usePackagesQuery, useDeletePackage, useUpdatePackage } from '../../hooks/usePackages'
import Button from '../../components/common/Button'
import Badge from '../../components/common/Badge'
import TableSkeleton from '../../components/common/TableSkeleton'
import EmptyState from '../../components/common/EmptyState'
import PackageFormModal from './PackageFormModal'
import { formatCurrency } from '../../utils/formatters'

const STATUS_BADGE = {
  ACTIVE: { color: 'green', label: 'Đang bán' },
  INACTIVE: { color: 'gray', label: 'Tạm ngưng' },
}

function PackagesPage() {
  const user = useAuthStore((state) => state.user)
  const isAdmin = user?.role === 'ADMIN'

  const [formModal, setFormModal] = useState({ open: false, pkg: null })
  const { data: packages, isLoading } = usePackagesQuery()
  const deleteMutation = useDeletePackage()
  const updateMutation = useUpdatePackage()

  const handleToggleStatus = (pkg) => {
    if (pkg.status === 'ACTIVE') {
      deleteMutation.mutate(pkg.id)
    } else {
      updateMutation.mutate({ id: pkg.id, payload: { status: 'ACTIVE' } })
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Danh mục gói tập</h1>
          <p className="mt-1 text-sm text-gray-500">
            {isAdmin ? 'Quản lý các gói tập đang kinh doanh.' : 'Danh sách gói tập phục vụ bán hàng.'}
          </p>
        </div>
        {isAdmin && (
          <Button icon={Plus} onClick={() => setFormModal({ open: true, pkg: null })}>
            Thêm gói tập
          </Button>
        )}
      </div>

      <div className="mt-6 overflow-hidden rounded-xl bg-white shadow-card">
        <table className="w-full text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase text-gray-500">
            <tr>
              <th className="px-4 py-3">Tên gói</th>
              <th className="px-4 py-3">Thời hạn</th>
              <th className="px-4 py-3">Đơn giá</th>
              <th className="px-4 py-3">Trạng thái</th>
              {isAdmin && <th className="px-4 py-3 text-right">Thao tác</th>}
            </tr>
          </thead>
          {isLoading ? (
            <TableSkeleton columns={isAdmin ? 5 : 4} />
          ) : (
            <tbody>
              {(packages || []).map((pkg) => {
                const badge = STATUS_BADGE[pkg.status] || STATUS_BADGE.ACTIVE
                return (
                  <tr key={pkg.id} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="px-4 py-3 font-medium text-gray-800">{pkg.name}</td>
                    <td className="px-4 py-3 text-gray-600">{pkg.durationMonths} tháng</td>
                    <td className="px-4 py-3 text-gray-600">{formatCurrency(pkg.price)}</td>
                    <td className="px-4 py-3">
                      <Badge color={badge.color}>{badge.label}</Badge>
                    </td>
                    {isAdmin && (
                      <td className="px-4 py-3">
                        <div className="flex justify-end gap-2">
                          <Button
                            size="sm"
                            variant="ghost"
                            icon={Pencil}
                            onClick={() => setFormModal({ open: true, pkg })}
                          >
                            Sửa
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            icon={pkg.status === 'ACTIVE' ? Lock : LockOpen}
                            onClick={() => handleToggleStatus(pkg)}
                          >
                            {pkg.status === 'ACTIVE' ? 'Khóa' : 'Mở bán'}
                          </Button>
                        </div>
                      </td>
                    )}
                  </tr>
                )
              })}
            </tbody>
          )}
        </table>

        {!isLoading && (packages || []).length === 0 && (
          <EmptyState title="Chưa có gói tập nào" />
        )}
      </div>

      {isAdmin && (
        <PackageFormModal
          open={formModal.open}
          pkg={formModal.pkg}
          onClose={() => setFormModal({ open: false, pkg: null })}
        />
      )}
    </div>
  )
}

export default PackagesPage
