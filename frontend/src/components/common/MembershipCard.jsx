import { Activity, ArrowRight, CheckCircle2, Flame, Sparkles, Star, UserCheck } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { formatCurrency } from '../../utils/formatters'

/**
 * Pricing card for a membership package (UC01).
 * Displays detailed package privileges (InBody sessions, PT sessions, Sauna access),
 * formatted price, benefit list, and "Nhận tư vấn gói này" consultation CTA button.
 */
function MembershipCard({
  membership,
  selected = false,
  onSelect,
  ctaLabel = 'Nhận tư vấn gói này',
  ctaLink = '/contact',
  compact = false,
  showCta = true,
  statusBadge,
  footer,
  className = '',
  onConsult,
}) {
  const navigate = useNavigate()
  const {
    name,
    durationMonths = 1,
    price = 0,
    description,
    benefits = [],
    popular = false,
    badge,
    inbodySessions,
    ptSessions,
    saunaAccess,
    saunaLabel,
  } = membership || {}

  // Derive quantified privileges if not directly specified
  const inbodyCount =
    inbodySessions ||
    (benefits.find((b) => /inbody/i.test(b))?.match(/(\d+\s*buổi|không giới hạn)/i)?.[0] || '01 buổi')

  const ptCount =
    ptSessions ||
    (benefits.find((b) => /personal trainer|pt|huấn luyện viên/i.test(b))?.match(/(\d+\s*buổi[^,]*|1-1)/i)?.[0] || '01 buổi')

  const hasSauna =
    saunaAccess ??
    benefits.some((b) => /sauna|xông hơi/i.test(b))

  const displaySaunaLabel =
    saunaLabel || (hasSauna ? 'Sauna & Đá muối' : 'Chưa bao gồm')

  const monthlyPrice = durationMonths > 0 ? Math.round(price / durationMonths) : price

  const handleConsultClick = (e) => {
    e.preventDefault()
    e.stopPropagation()

    if (onConsult) {
      onConsult(membership)
      return
    }

    // Try scrolling smoothly to contact-form if present on the page
    const formEl = document.getElementById('contact-form') || document.getElementById('contact')
    if (formEl) {
      window.dispatchEvent(new CustomEvent('select-membership-package', { detail: membership }))
      formEl.scrollIntoView({ behavior: 'smooth', block: 'start' })
    } else {
      navigate(`${ctaLink}?package=${encodeURIComponent(name)}#contact-form`)
    }
  }

  // Display top 3 benefits in compact mode, or all in full mode
  const displayedBenefits = compact ? benefits.slice(0, 3) : benefits

  return (
    <div
      className={`group relative flex h-full flex-col rounded-2xl border bg-white p-5 sm:p-6 shadow-card transition-all duration-300 ${
        popular
          ? 'border-primary-500 shadow-primary-500/10 ring-2 ring-primary-100 hover:shadow-xl'
          : 'border-gray-200/90 hover:border-primary-300 hover:shadow-lg'
      } ${selected ? 'ring-2 ring-primary-400' : ''} ${
        onSelect ? 'cursor-pointer hover:-translate-y-1' : ''
      } ${className}`}
      onClick={onSelect}
    >
      {/* Popular Badge: floating above card with ring and z-index to avoid clipping */}
      {popular && (
        <div className="absolute -top-3.5 left-1/2 -translate-x-1/2 z-10 whitespace-nowrap">
          <span className="inline-flex items-center gap-1.5 rounded-full bg-gradient-to-r from-primary-500 to-primary-600 px-3.5 py-1 text-xs font-bold text-white shadow-md ring-2 ring-white">
            <Star size={12} className="fill-white" />
            <span>{badge || 'Phổ biến nhất'}</span>
          </span>
        </div>
      )}

      {/* Non-popular custom badge */}
      {!popular && badge && (
        <span className="absolute right-4 top-4 rounded-full bg-gray-100 px-2.5 py-0.5 text-[11px] font-semibold text-gray-700">
          {badge}
        </span>
      )}

      {/* Package Name & Duration */}
      <div className="flex items-start justify-between gap-2">
        <div>
          <h3 className="text-xl font-extrabold text-gray-900 group-hover:text-primary-600 transition-colors">
            {name}
          </h3>
          <span className="mt-1 inline-block rounded-md bg-primary-50 px-2 py-0.5 text-xs font-semibold text-primary-700">
            Thời hạn: {durationMonths} tháng
          </span>
        </div>
        {statusBadge}
      </div>

      {/* Pricing Header */}
      <div className="mt-4">
        <div className="flex items-baseline gap-1.5">
          <span className="text-3xl font-extrabold tracking-tight text-gray-900">
            {formatCurrency(price)}
          </span>
        </div>
        {durationMonths > 1 && (
          <p className="mt-0.5 text-xs font-medium text-gray-600">
            ~ {formatCurrency(monthlyPrice)} / tháng
          </p>
        )}
      </div>

      {description && (
        <p className="mt-2.5 text-xs leading-relaxed text-gray-600 line-clamp-2">
          {description}
        </p>
      )}

      {/* Khối quyền lợi cụ thể: InBody, PT, Sauna (UC01) */}
      <div className="mt-3.5 rounded-xl border border-gray-100 bg-gray-50/90 p-2.5 space-y-1.5">
        <div className="flex items-center justify-between text-[11px] font-bold uppercase tracking-wider text-gray-700">
          <span className="flex items-center gap-1">
            <Sparkles size={12} className="text-amber-500" />
            <span>Quyền lợi cụ thể</span>
          </span>
          <span className="text-[10px] font-normal text-gray-500 lowercase">chuẩn gói</span>
        </div>

        {/* InBody Sessions */}
        <div className="flex items-center justify-between text-xs">
          <span className="flex items-center gap-1.5 text-gray-600">
            <Activity size={13} className="text-primary-500" />
            <span>Đo chỉ số InBody:</span>
          </span>
          <span className="font-bold text-gray-900 bg-white border border-gray-200/80 px-2 py-0.5 rounded-md text-[11px]">
            {inbodyCount}
          </span>
        </div>

        {/* PT Sessions */}
        <div className="flex items-center justify-between text-xs">
          <span className="flex items-center gap-1.5 text-gray-600">
            <UserCheck size={13} className="text-primary-500" />
            <span>HLV định hướng (PT):</span>
          </span>
          <span className="font-bold text-gray-900 bg-white border border-gray-200/80 px-2 py-0.5 rounded-md text-[11px]">
            {ptCount}
          </span>
        </div>

        {/* Sauna Access */}
        <div className="flex items-center justify-between text-xs">
          <span className="flex items-center gap-1.5 text-gray-600">
            <Flame size={13} className={hasSauna ? 'text-amber-500' : 'text-gray-400'} />
            <span>Phòng xông hơi Sauna:</span>
          </span>
          <span
            className={`font-bold px-2 py-0.5 rounded-md text-[11px] ${
              hasSauna
                ? 'bg-amber-50 text-amber-700 border border-amber-200/80'
                : 'bg-gray-100 text-gray-400'
            }`}
          >
            {displaySaunaLabel}
          </span>
        </div>
      </div>

      {/* Danh sách quyền lợi chi tiết */}
      <ul className="mt-3.5 flex-1 space-y-1.5 text-xs">
        {displayedBenefits.map((benefit, idx) => (
          <li key={idx} className="flex items-start gap-2 text-gray-600">
            <CheckCircle2 size={14} className="mt-0.5 shrink-0 text-emerald-500" />
            <span className="leading-relaxed">{benefit}</span>
          </li>
        ))}
        {compact && benefits.length > 3 && (
          <li className="text-[11px] font-medium text-primary-600 italic">
            + {benefits.length - 3} quyền lợi đặc quyền khác...
          </li>
        )}
      </ul>

      {/* Nút "Nhận tư vấn gói này" (CTA) */}
      {!onSelect && showCta && (
        <button
          type="button"
          onClick={handleConsultClick}
          className={`mt-4 flex w-full items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-xs sm:text-sm font-semibold transition-all shadow-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:ring-offset-2 ${
            popular
              ? 'bg-primary-500 text-white hover:bg-primary-600 hover:shadow-md'
              : 'border border-gray-200 bg-white text-gray-700 hover:border-primary-300 hover:bg-primary-50/50 hover:text-primary-700'
          }`}
        >
          <Sparkles size={14} className={popular ? 'text-white' : 'text-primary-500'} />
          <span>{ctaLabel}</span>
          <ArrowRight size={14} />
        </button>
      )}

      {footer && <div className="mt-auto pt-3">{footer}</div>}
    </div>
  )
}

export default MembershipCard
