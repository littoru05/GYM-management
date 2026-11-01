import { TrendingUp, TrendingDown } from 'lucide-react'

const ACCENT_STYLES = {
  primary: 'bg-primary-50 text-primary-600 border-primary-100',
  blue: 'bg-blue-50 text-blue-600 border-blue-100',
  green: 'bg-emerald-50 text-emerald-600 border-emerald-100',
  emerald: 'bg-emerald-50 text-emerald-600 border-emerald-100',
  yellow: 'bg-amber-50 text-amber-600 border-amber-100',
  amber: 'bg-amber-50 text-amber-600 border-amber-100',
  purple: 'bg-purple-50 text-purple-600 border-purple-100',
}

/**
 * KPI card component used across admin and staff dashboards.
 * @param {object} props
 * @param {React.ComponentType} [props.icon] Lucide icon component
 * @param {string} props.label Card title/description
 * @param {string|number} props.value Main KPI value to display
 * @param {number} [props.trend] Signed % change vs previous period (e.g. 12.5 or -4.2)
 * @param {string} [props.trendLabel] Text following percentage (default: "so với kỳ trước")
 * @param {string} [props.accent] Color accent: 'primary' | 'blue' | 'green' | 'yellow' | 'purple'
 * @param {string} [props.subtext] Optional helper note below value
 * @param {string} [props.className] Additional CSS classes
 */
function StatCard({
  icon: Icon,
  label,
  value,
  trend,
  trendLabel = 'so với kỳ trước',
  accent = 'primary',
  subtext,
  className = '',
}) {
  const hasTrend = typeof trend === 'number' && !Number.isNaN(trend)
  const isPositive = hasTrend && trend >= 0
  const accentClasses = ACCENT_STYLES[accent] || ACCENT_STYLES.primary

  return (
    <div
      className={`relative overflow-hidden rounded-2xl border border-gray-100 bg-white p-5 shadow-card transition-all duration-200 hover:-translate-y-0.5 hover:shadow-md ${className}`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-medium text-gray-500">{label}</p>
          <p className="mt-1.5 text-2xl font-bold tracking-tight text-gray-900">{value}</p>
          {subtext && <p className="mt-1 text-xs text-gray-400">{subtext}</p>}
        </div>
        {Icon && (
          <div
            className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-xl border ${accentClasses}`}
          >
            <Icon size={22} />
          </div>
        )}
      </div>

      {hasTrend && (
        <div
          className={`mt-3 flex items-center gap-1.5 text-xs font-semibold ${
            isPositive ? 'text-emerald-600' : 'text-red-500'
          }`}
        >
          <span
            className={`inline-flex items-center gap-0.5 rounded-md px-1.5 py-0.5 ${
              isPositive ? 'bg-emerald-50' : 'bg-red-50'
            }`}
          >
            {isPositive ? <TrendingUp size={13} /> : <TrendingDown size={13} />}
            <span>
              {isPositive ? '+' : ''}
              {trend.toFixed(1)}%
            </span>
          </span>
          <span className="font-normal text-gray-400">{trendLabel}</span>
        </div>
      )}
    </div>
  )
}

export default StatCard
