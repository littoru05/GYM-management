import { Inbox } from 'lucide-react'

function EmptyState({ icon: Icon = Inbox, title = 'Không có dữ liệu', description, action, className = '' }) {
  return (
    <div className={`flex flex-col items-center justify-center gap-3 rounded-xl border border-dashed border-gray-200 py-14 text-center ${className}`}>
      <div className="flex h-12 w-12 items-center justify-center rounded-full bg-gray-100 text-gray-400">
        <Icon size={22} />
      </div>
      <div>
        <p className="font-medium text-gray-700">{title}</p>
        {description && <p className="mt-1 text-sm text-gray-400">{description}</p>}
      </div>
      {action}
    </div>
  )
}

export default EmptyState
