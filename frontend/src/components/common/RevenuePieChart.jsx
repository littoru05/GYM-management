import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from 'recharts'
import { PieChart as PieIcon, Inbox } from 'lucide-react'
import { formatCurrency } from '../../utils/formatters.js'
import EmptyState from './EmptyState.jsx'
import { DonutSkeleton } from './Skeleton.jsx'

const PIE_COLORS = {
  membership: '#f8471a',
  retail: '#3b82f6',
}

function CustomDonutTooltip({ active, payload }) {
  if (!active || !payload || !payload.length) return null
  const entry = payload[0]

  return (
    <div className="rounded-xl border border-gray-100 bg-white/95 p-3 shadow-lg backdrop-blur-sm">
      <div className="flex items-center gap-1.5 text-xs font-semibold text-gray-700">
        <span
          className="inline-block h-2.5 w-2.5 rounded-full"
          style={{ backgroundColor: entry.payload.fill }}
        />
        {entry.name}
      </div>
      <p className="mt-1 text-sm font-bold text-gray-900">{formatCurrency(entry.value)}</p>
      <p className="text-xs text-gray-500">Tỷ trọng: {entry.payload.percent}%</p>
    </div>
  )
}

function RevenuePieChart({
  membershipRevenue = 0,
  retailRevenue = 0,
  loading = false,
  title = 'Tỷ trọng doanh thu',
  subtitle = 'So sánh gói tập vs bán lẻ',
  height = 300,
}) {
  if (loading) {
    return <DonutSkeleton height={height} />
  }

  const total = membershipRevenue + retailRevenue
  const hasData = total > 0

  const membershipPercent = hasData ? Math.round((membershipRevenue / total) * 100) : 0
  const retailPercent = hasData ? 100 - membershipPercent : 0

  const pieData = hasData
    ? [
        {
          name: 'Doanh thu gói tập',
          value: membershipRevenue,
          percent: membershipPercent,
          fill: PIE_COLORS.membership,
        },
        {
          name: 'Doanh thu bán lẻ',
          value: retailRevenue,
          percent: retailPercent,
          fill: PIE_COLORS.retail,
        },
      ]
    : []

  return (
    <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-card">
      <div className="mb-4 flex items-center justify-between">
        <div>
          <div className="flex items-center gap-2">
            <PieIcon size={18} className="text-primary-500" />
            <h2 className="font-semibold text-gray-900">{title}</h2>
          </div>
          {subtitle && <p className="mt-0.5 text-xs text-gray-500">{subtitle}</p>}
        </div>
      </div>

      {!hasData ? (
        <EmptyState
          icon={Inbox}
          title="Chưa có doanh thu"
          description="Không có số liệu tỷ trọng trong khoảng thời gian đã chọn."
          className="my-4 py-12"
        />
      ) : (
        <div>
          <div className="relative" style={{ height: height - 100 }}>
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={pieData}
                  dataKey="value"
                  nameKey="name"
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  paddingAngle={3}
                  isAnimationActive={true}
                >
                  {pieData.map((entry) => (
                    <Cell key={entry.name} fill={entry.fill} strokeWidth={2} stroke="#fff" />
                  ))}
                </Pie>
                <Tooltip content={<CustomDonutTooltip />} />
              </PieChart>
            </ResponsiveContainer>

            {/* Metric in center of Donut */}
            <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center text-center">
              <span className="text-xs font-medium text-gray-400">Tổng</span>
              <span className="text-sm font-bold text-gray-900">
                {(total / 1000000).toFixed(1)} tr
              </span>
            </div>
          </div>

          {/* Breakdown legend cards */}
          <div className="mt-3 grid grid-cols-2 gap-2.5 border-t border-gray-100 pt-3">
            <div className="rounded-xl bg-orange-50/60 p-2.5">
              <div className="flex items-center gap-1.5">
                <span className="h-2.5 w-2.5 rounded-full bg-primary-500" />
                <span className="truncate text-xs font-medium text-gray-600">Gói tập</span>
              </div>
              <p className="mt-1 text-sm font-bold text-gray-900">
                {formatCurrency(membershipRevenue)}
              </p>
              <p className="text-xs font-semibold text-primary-600">{membershipPercent}%</p>
            </div>

            <div className="rounded-xl bg-blue-50/60 p-2.5">
              <div className="flex items-center gap-1.5">
                <span className="h-2.5 w-2.5 rounded-full bg-blue-500" />
                <span className="truncate text-xs font-medium text-gray-600">Bán lẻ</span>
              </div>
              <p className="mt-1 text-sm font-bold text-gray-900">{formatCurrency(retailRevenue)}</p>
              <p className="text-xs font-semibold text-blue-600">{retailPercent}%</p>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default RevenuePieChart
