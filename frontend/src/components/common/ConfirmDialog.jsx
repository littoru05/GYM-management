import { AlertTriangle } from 'lucide-react'
import Modal from './Modal'
import Button from './Button'

function ConfirmDialog({
  open,
  onClose,
  onConfirm,
  title = 'Xác nhận xóa',
  message = 'Bạn có chắc chắn muốn thực hiện thao tác này không?',
  confirmLabel = 'Xác nhận xóa',
  cancelLabel = 'Hủy',
  variant = 'danger',
  loading = false,
}) {
  return (
    <Modal open={open} onClose={onClose} title={title} size="sm">
      <div className="space-y-4">
        <div className="flex items-start gap-3">
          <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-red-100 text-red-600">
            <AlertTriangle size={20} />
          </div>
          <div className="flex-1 pt-1 text-sm text-gray-600">
            <p>{message}</p>
          </div>
        </div>

        <div className="flex justify-end gap-2 border-t border-gray-100 pt-4">
          <Button variant="secondary" size="sm" onClick={onClose} disabled={loading}>
            {cancelLabel}
          </Button>
          <Button variant={variant} size="sm" onClick={onConfirm} loading={loading}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </Modal>
  )
}

export default ConfirmDialog
