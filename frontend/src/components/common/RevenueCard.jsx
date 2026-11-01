import StatCard from './StatCard'
import { formatCurrency } from '../../utils/formatters'

/**
 * Specialized StatCard that formats numeric value as VND currency.
 */
function RevenueCard({
  icon,
  label,
  value,
  trend,
  trendLabel,
  accent = 'primary',
  subtext,
  className = '',
}) {
  const displayValue = typeof value === 'number' ? formatCurrency(value) : (value ?? '—')

  return (
    <StatCard
      icon={icon}
      label={label}
      value={displayValue}
      trend={trend}
      trendLabel={trendLabel}
      accent={accent}
      subtext={subtext}
      className={className}
    />
  )
}

export default RevenueCard
