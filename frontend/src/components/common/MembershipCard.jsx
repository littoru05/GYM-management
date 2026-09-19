import { Check, Star } from 'lucide-react'
import { formatCurrency } from '../../utils/formatters'
import Button from './Button'

/**
 * Pricing card for a membership package. Used on the public /pricing page,
 * the staff sales flow package picker, and the admin memberships page.
 * `showCta` lets the admin page opt out of the sales CTA, and `footer` lets
 * it inject its own admin action row pinned to the bottom of the card.
 */
function MembershipCard({
  membership,
  selected = false,
  onSelect,
  ctaLabel = 'Liên hệ ngay',
  compact = false,
  showCta = true,
  statusBadge,
  footer,
  className = '',
}) {
  const { name, durationMonths, price, description, benefits, popular } = membership

  return (
    <div
      className={`relative flex h-full flex-col rounded-2xl border p-6 shadow-card transition-all ${
        popular ? 'border-primary-500 ring-2 ring-primary-100' : 'border-gray-100'
      } ${selected ? 'ring-2 ring-primary-400' : ''} ${onSelect ? 'cursor-pointer hover:-translate-y-1' : ''} ${className}`}
      onClick={onSelect}
    >
      {popular && (
        <span className="absolute -top-3 left-1/2 flex -translate-x-1/2 items-center gap-1 rounded-full bg-primary-500 px-3 py-1 text-xs font-semibold text-white">
          <Star size={12} /> Phổ biến
        </span>
      )}

      <div className="flex items-start justify-between gap-2">
        <h3 className="text-lg font-bold text-gray-900">{name}</h3>
        {statusBadge}
      </div>
      <p className="text-sm text-gray-500">{durationMonths} tháng</p>

      <div className="mt-4">
        <span className="text-3xl font-extrabold text-gray-900">{formatCurrency(price)}</span>
      </div>

      {description && !compact && <p className="mt-3 text-sm text-gray-500">{description}</p>}

      {!compact && (
        <ul className="mt-5 flex-1 space-y-2.5">
          {benefits.map((benefit) => (
            <li key={benefit} className="flex items-start gap-2 text-sm text-gray-600">
              <Check size={16} className="mt-0.5 shrink-0 text-primary-500" />
              {benefit}
            </li>
          ))}
        </ul>
      )}

      {!onSelect && showCta && (
        <Button variant={popular ? 'primary' : 'secondary'} fullWidth className="mt-6">
          {ctaLabel}
        </Button>
      )}

      {footer && <div className="mt-auto pt-4">{footer}</div>}
    </div>
  )
}

export default MembershipCard
