import { ArrowRight } from 'lucide-react'
import { Link } from 'react-router-dom'

function ServiceCard({ icon: Icon, title, description, image, link = '/contact' }) {
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
      <div className="flex flex-col p-6">
        <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
          <Icon size={20} />
        </div>
        <h3 className="mt-4 text-base font-bold text-gray-900">{title}</h3>
        <p className="mt-2 text-sm text-gray-500">{description}</p>
        <Link
          to={link}
          className="mt-4 inline-flex items-center gap-1.5 text-sm font-semibold text-primary-600 transition-all hover:gap-2 hover:text-primary-700"
        >
          Tìm hiểu thêm <ArrowRight size={14} className="transition-transform group-hover:translate-x-0.5" />
        </Link>
      </div>
    </div>
  )
}

export default ServiceCard
