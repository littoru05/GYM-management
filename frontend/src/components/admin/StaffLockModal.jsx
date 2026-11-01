import { Lock, Unlock, AlertTriangle, CheckCircle2 } from 'lucide-react'
import Modal from '../common/Modal'
import Button from '../common/Button'
import { STAFF_STATUS } from '../../utils/constants'

function StaffLockModal({ open, staff, loading, onClose, onConfirm }) {
  if (!staff) return null

  const isLocking = staff.status === STAFF_STATUS.ACTIVE

  return (
    <Modal
      open={open}
      onClose={onClose}
      title={isLocking ? 'Khóa tài khoản nhân viên' : 'Mở khóa tài khoản nhân viên'}
      size="sm"
    >
      <div className="space-y-4">
        <div className="flex items-start gap-3">
          <div
            className={`flex h-11 w-11 shrink-0 items-center justify-center rounded-full ring-8 ${
              isLocking
                ? 'bg-red-100 text-red-600 ring-red-50'
                : 'bg-emerald-100 text-emerald-600 ring-emerald-50'
            }`}
          >
            {isLocking ? <Lock size={20} /> : <Unlock size={20} />}
          </div>

          <div className="flex-1 pt-0.5">
            <p className="text-sm text-gray-700">
              Bạn có chắc chắn muốn {isLocking ? 'khóa' : 'mở khóa'} tài khoản của nhân viên{' '}
              <span className="font-semibold text-gray-900">{staff.name}</span> (
              <span className="font-mono text-xs text-gray-500">{staff.email}</span>)?
            </p>

            {isLocking ? (
              <div className="mt-3 flex items-start gap-2 rounded-lg border border-red-200 bg-red-50 p-2.5 text-xs text-red-700">
                <AlertTriangle size={15} className="shrink-0 text-red-600" />
                <span>
                  Sau khi khóa, nhân viên này sẽ <strong>không thể đăng nhập</strong> hoặc thực hiện
                  bất kỳ thao tác nào trên hệ thống.
                </span>
              </div>
            ) : (
              <div className="mt-3 flex items-start gap-2 rounded-lg border border-emerald-200 bg-emerald-50 p-2.5 text-xs text-emerald-700">
                <CheckCircle2 size={15} className="shrink-0 text-emerald-600" />
                <span>
                  Tài khoản sẽ được kích hoạt lại ngay lập tức. Nhân viên có thể đăng nhập vào hệ
                  thống bình thường.
                </span>
              </div>
            )}
          </div>
        </div>

        <div className="flex items-center justify-end gap-2 border-t border-gray-100 pt-4">
          <Button variant="secondary" size="sm" onClick={onClose} disabled={loading}>
            Hủy bỏ
          </Button>
          <Button
            variant={isLocking ? 'danger' : 'primary'}
            size="sm"
            onClick={() => onConfirm(staff)}
            loading={loading}
          >
            {isLocking ? 'Khóa tài khoản' : 'Mở khóa tài khoản'}
          </Button>
        </div>
      </div>
    </Modal>
  )
}

export default StaffLockModal
