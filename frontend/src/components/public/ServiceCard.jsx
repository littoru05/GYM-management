import { Activity, ArrowRight, Sparkles, Target, Eye } from 'lucide-react'
import { Link } from 'react-router-dom'

function ServiceCard({
  id,
  icon: Icon,
  title,
  tagline,
  badge,
  badgeColor = 'primary',
  description,
  image,
  imageAlt,
  method,
  suitableFor,
  amenities,
  stats,
  equipment,
  link = '/contact',
  onOpenDetails,
}) {
  const serviceObj = {
    id,
    icon: Icon,
    title,
    tagline,
    badge,
    badgeColor,
    description,
    image,
    imageAlt,
    method,
    suitableFor,
    amenities,
    stats,
    equipment,
    link,
  }

  const methodSummary = typeof method === 'object' ? method?.summary : method
  const suitableItems = Array.isArray(suitableFor) ? suitableFor : []
  const amenityItems = Array.isArray(amenities) ? amenities : []

  return (
    <article className="group relative flex flex-col overflow-hidden rounded-2xl border border-gray-200/80 bg-white shadow-card transition-all duration-300 hover:-translate-y-1 hover:border-primary-300 hover:shadow-xl">
      {/* Service Cover Image */}
      {image && (
        <div className="relative h-48 w-full overflow-hidden bg-gray-900 sm:h-52">
          <img
            src={image}
            alt={imageAlt || title}
            className="h-full w-full object-cover transition-transform duration-500 ease-out group-hover:scale-105"
            loading="lazy"
          />
          {/* Subtle gradient overlay */}
          <div className="absolute inset-0 bg-gradient-to-t from-gray-950/85 via-gray-950/25 to-transparent" />

          {/* Badge */}
          {badge && (
            <div className="absolute right-3 top-3">
              <span className="inline-flex items-center gap-1 rounded-full bg-white/95 px-2.5 py-0.5 text-[11px] font-bold text-gray-900 shadow-md backdrop-blur">
                <span className="h-1.5 w-1.5 rounded-full bg-primary-500" />
                {badge}
              </span>
            </div>
          )}

          {/* Top-left Icon badge */}
          {Icon && (
            <div className="absolute left-3 top-3 flex h-9 w-9 items-center justify-center rounded-xl bg-primary-500/95 text-white shadow-md backdrop-blur">
              <Icon size={18} />
            </div>
          )}

          {/* Title on Image */}
          <div className="absolute bottom-3 left-3 right-3 text-white">
            <h3 className="text-base font-bold leading-tight drop-shadow-sm sm:text-lg">{title}</h3>
            {tagline && <p className="mt-0.5 line-clamp-1 text-xs text-gray-200">{tagline}</p>}
          </div>
        </div>
      )}

      {/* Card Body - Clean & Compact */}
      <div className="flex flex-1 flex-col p-4 sm:p-5">
        {/* Fallback Header if no image */}
        {!image && (
          <div className="mb-2">
            <h3 className="text-base font-bold text-gray-900">{title}</h3>
            {tagline && <p className="text-xs text-primary-600">{tagline}</p>}
          </div>
        )}

        {/* Short description */}
        {description && (
          <p className="line-clamp-2 text-xs leading-relaxed text-gray-600">{description}</p>
        )}

        {/* Phương pháp tập luyện (Gọn gàng) */}
        {methodSummary && (
          <div className="mt-3 rounded-xl border border-primary-100 bg-primary-50/50 p-2.5">
            <div className="flex items-center gap-1.5 text-[11px] font-bold uppercase tracking-wide text-primary-700">
              <Activity size={13} className="text-primary-600" />
              <span>Phương pháp tập luyện</span>
            </div>
            <p className="mt-1 line-clamp-2 text-xs text-gray-700">{methodSummary}</p>
          </div>
        )}

        {/* Đối tượng phù hợp (Ngắn gọn 1-2 tiêu biểu) */}
        {suitableItems.length > 0 && (
          <div className="mt-2.5">
            <div className="flex items-center gap-1.5 text-[11px] font-bold uppercase tracking-wide text-gray-700">
              <Target size={13} className="text-primary-600" />
              <span>Đối tượng phù hợp</span>
            </div>
            <p className="mt-1 line-clamp-1 text-xs text-gray-600">
              • {suitableItems[0]}
            </p>
          </div>
        )}

        {/* Tiện ích đi kèm (Pills gọn gàng) */}
        {amenityItems.length > 0 && (
          <div className="mt-3 border-t border-gray-100 pt-2.5">
            <div className="flex items-center justify-between text-[11px] font-semibold text-gray-700">
              <span className="flex items-center gap-1">
                <Sparkles size={12} className="text-amber-500" />
                <span>Tiện ích đi kèm</span>
              </span>
              <span className="text-[10px] text-gray-600">Miễn phí</span>
            </div>
            <div className="mt-1.5 flex flex-wrap gap-1.5">
              {amenityItems.slice(0, 3).map((a) => {
                const AmenityIcon = a.icon || Sparkles
                return (
                  <span
                    key={a.id || a.name}
                    className="inline-flex items-center gap-1 rounded-md border border-gray-100 bg-gray-50 px-2 py-0.5 text-[11px] font-medium text-gray-600"
                    title={a.desc || a.name}
                  >
                    <AmenityIcon size={11} className="text-primary-500" />
                    <span>{a.name}</span>
                  </span>
                )
              })}
            </div>
          </div>
        )}

        {/* Card Footer Actions */}
        <div className="mt-auto pt-4">
          <div className="flex items-center gap-2">
            {onOpenDetails && (
              <button
                type="button"
                onClick={() => onOpenDetails(serviceObj)}
                className="inline-flex flex-1 items-center justify-center gap-1.5 rounded-xl border border-gray-200 bg-white px-3 py-2 text-xs font-semibold text-gray-700 shadow-xs transition-colors hover:border-primary-300 hover:bg-primary-50/50 hover:text-primary-700"
              >
                <Eye size={13} />
                <span>Xem chi tiết</span>
              </button>
            )}
            <Link
              to={link || `/contact?service=${id}`}
              className="inline-flex items-center justify-center gap-1 rounded-xl bg-primary-500 px-3.5 py-2 text-xs font-semibold text-white shadow-xs transition-colors hover:bg-primary-600"
            >
              <span>Đăng ký</span>
              <ArrowRight size={13} />
            </Link>
          </div>
        </div>
      </div>
    </article>
  )
}

export default ServiceCard
