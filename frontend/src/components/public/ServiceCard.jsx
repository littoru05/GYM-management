import { ArrowRight } from 'lucide-react'

function ServiceCard({ icon: Icon, title, description, image }) {
  return (
    <div className="group overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-card transition-shadow hover:shadow-lg">
      {image && (
        <div className="h-40 w-full overflow-hidden">
          <img
            src={image}
            alt={title}
            className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        </div>
      )}
      <div className="p-6">
        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
          <Icon size={20} />
        </div>
        <h3 className="mt-4 text-base font-bold text-gray-900">{title}</h3>
        <p className="mt-2 text-sm text-gray-500">{description}</p>
        <div className="mt-4 flex items-center gap-1 text-sm font-medium text-primary-600">
          Tìm hiểu thêm <ArrowRight size={14} />
        </div>
      </div>
    </div>
  )
}

export default ServiceCard
