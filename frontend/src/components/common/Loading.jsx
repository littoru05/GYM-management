import { Loader2 } from 'lucide-react'

function Loading({ label = 'Đang tải...', size = 24, className = '' }) {
  return (
    <div className={`flex flex-col items-center justify-center gap-2 py-10 text-gray-400 ${className}`}>
      <Loader2 size={size} className="animate-spin text-primary-500" />
      {label && <p className="text-sm">{label}</p>}
    </div>
  )
}

export default Loading
