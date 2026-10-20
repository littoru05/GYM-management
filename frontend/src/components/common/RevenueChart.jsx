import { useState } from 'react'
import {
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from 'recharts'
import { TrendingUp, BarChart2, LineChart, Inbox, Users, DollarSign } from 'lucide-react'
import { formatCurrency, formatNumber } from '../../utils/formatters.js'
import EmptyState from './EmptyState.jsx'
import { ChartSkeleton } from './Skeleton.jsx'

/**
 * Custom formatted tooltip for Recharts.
 */
function CustomChartTooltip({ active, payload, label, isCheckIn = false }) {
  if (!active || !payload || !payload.length) return null

  return (
    <div className="rounded-xl border border-gray-100 bg-white/95 p-3 shadow-lg backdrop-blur-sm">
      <p className="mb-2 text-xs font-semibold text-gray-500">{label}</p>
      <div className="space-y-1.5 text-xs">
        {payload.map((entry) => (
          <div key={entry.name} className="flex items-center justify-between gap-4">
            <span className="flex items-center gap-1.5 font-medium text-gray-600">
              <span
                className="inline-block h-2.5 w-2.5 rounded-full"
                style={{ backgroundColor: entry.color }}
              />
              {entry.name}:
            </span>
            <span className="font-bold text-gray-900">
              {isCheckIn ? `${formatNumber(entry.value)} lượt` : formatCurrency(entry.value)}
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

function RevenueChart({
  data = [],
  loading = false,
  breakdown = true,
  height = 320,
  title = 'Biểu đồ xu hướng',
  subtitle = 'Trực quan hóa biến động doanh thu và lượt tập theo mốc thời gian',
}) {
  const [chartType, setChartType] = useState('area') // 'area' | 'bar'
  const [metric, setMetric] = useState('revenue') // 'revenue' | 'checkin'

  if (loading) {
    return <ChartSkeleton height={height} />
  }

  const isCheckInMetric = metric === 'checkin'
  const hasData =
    Array.isArray(data) &&
    data.length > 0 &&
    (isCheckInMetric
      ? data.some((d) => (d.checkInCount || 0) > 0)
      : data.some((d) => (d.total || 0) > 0 || (d.membershipRevenue || 0) > 0 || (d.retailRevenue || 0) > 0))

  return (
    <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-card">
      <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
        <div>
          <div className="flex items-center gap-2">
            <TrendingUp size={18} className="text-primary-500" />
            <h2 className="font-semibold text-gray-900">
              {isCheckInMetric ? 'Xu hướng lượt tập (Check-in)' : title}
            </h2>
          </div>
          {subtitle && <p className="mt-0.5 text-xs text-gray-500">{subtitle}</p>}
        </div>

        {/* Controls toolbar */}
        <div className="flex flex-wrap items-center gap-2">
          {/* Metric Selector: Doanh thu vs Lượt tập */}
          <div className="flex items-center gap-1 rounded-lg border border-gray-200 bg-gray-50 p-1">
            <button
              type="button"
              onClick={() => setMetric('revenue')}
              className={`flex items-center gap-1 rounded px-2.5 py-1 text-xs font-semibold transition-colors ${
                metric === 'revenue'
                  ? 'bg-white text-primary-600 shadow-sm'
                  : 'text-gray-500 hover:text-gray-800'
              }`}
              title="Xem số liệu doanh thu (VNĐ)"
            >
              <DollarSign size={13} />
              <span>Doanh thu</span>
            </button>
            <button
              type="button"
              onClick={() => setMetric('checkin')}
              className={`flex items-center gap-1 rounded px-2.5 py-1 text-xs font-semibold transition-colors ${
                metric === 'checkin'
                  ? 'bg-white text-emerald-600 shadow-sm'
                  : 'text-gray-500 hover:text-gray-800'
              }`}
              title="Xem số lượt check-in hội viên tập luyện"
            >
              <Users size={13} />
              <span>Lượt tập</span>
            </button>
          </div>

          {/* Chart type toggle */}
          {hasData && (
            <div className="flex items-center gap-1 rounded-lg border border-gray-200 bg-gray-50 p-1">
              <button
                type="button"
                onClick={() => setChartType('area')}
                className={`flex items-center gap-1 rounded px-2.5 py-1 text-xs font-medium transition-colors ${
                  chartType === 'area'
                    ? 'bg-white text-gray-900 shadow-sm'
                    : 'text-gray-500 hover:text-gray-800'
                }`}
                title="Biểu đồ đường / diện tích"
              >
                <LineChart size={13} />
                <span>Đường</span>
              </button>
              <button
                type="button"
                onClick={() => setChartType('bar')}
                className={`flex items-center gap-1 rounded px-2.5 py-1 text-xs font-medium transition-colors ${
                  chartType === 'bar'
                    ? 'bg-white text-gray-900 shadow-sm'
                    : 'text-gray-500 hover:text-gray-800'
                }`}
                title="Biểu đồ cột"
              >
                <BarChart2 size={13} />
                <span>Cột</span>
              </button>
            </div>
          )}
        </div>
      </div>

      {!hasData ? (
        <EmptyState
          icon={Inbox}
          title={isCheckInMetric ? 'Chưa có lượt tập phát sinh' : 'Không có dữ liệu xu hướng'}
          description={
            isCheckInMetric
              ? 'Chưa ghi nhận lượt check-in nào của hội viên trong khoảng thời gian đã chọn.'
              : 'Chưa có giao dịch phát sinh nào trong khoảng thời gian đã chọn.'
          }
          className="my-4 py-12"
        />
      ) : chartType === 'area' ? (
        <ResponsiveContainer width="100%" height={height}>
          <AreaChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
            <defs>
              <linearGradient id="gradientTotal" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#f8471a" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#f8471a" stopOpacity={0.0} />
              </linearGradient>
              <linearGradient id="gradientMembership" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#f97316" stopOpacity={0.3} />
                <stop offset="95%" stopColor="#f97316" stopOpacity={0.0} />
              </linearGradient>
              <linearGradient id="gradientRetail" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.25} />
                <stop offset="95%" stopColor="#3b82f6" stopOpacity={0.0} />
              </linearGradient>
              <linearGradient id="gradientCheckIn" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#10b981" stopOpacity={0.35} />
                <stop offset="95%" stopColor="#10b981" stopOpacity={0.0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
            <XAxis
              dataKey="date"
              tick={{ fontSize: 11, fill: '#64748b' }}
              axisLine={{ stroke: '#e2e8f0' }}
              tickLine={false}
            />
            <YAxis
              tickFormatter={(v) =>
                isCheckInMetric
                  ? `${v}`
                  : `${(v / 1000000).toFixed(v >= 10000000 ? 0 : 1)}tr`
              }
              tick={{ fontSize: 11, fill: '#64748b' }}
              axisLine={false}
              tickLine={false}
              width={48}
            />
            <Tooltip content={<CustomChartTooltip isCheckIn={isCheckInMetric} />} />
            <Legend
              wrapperStyle={{ paddingTop: '14px', fontSize: '12px' }}
              iconType="circle"
              iconSize={8}
            />
            {isCheckInMetric ? (
              <Area
                type="monotone"
                dataKey="checkInCount"
                name="Lượt tập (Check-in)"
                stroke="#10b981"
                fill="url(#gradientCheckIn)"
                strokeWidth={2.5}
                activeDot={{ r: 6 }}
              />
            ) : breakdown ? (
              <>
                <Area
                  type="monotone"
                  dataKey="membershipRevenue"
                  name="Gói tập"
                  stroke="#f8471a"
                  fill="url(#gradientMembership)"
                  strokeWidth={2}
                  activeDot={{ r: 5 }}
                />
                <Area
                  type="monotone"
                  dataKey="retailRevenue"
                  name="Bán lẻ"
                  stroke="#3b82f6"
                  fill="url(#gradientRetail)"
                  strokeWidth={2}
                  activeDot={{ r: 5 }}
                />
              </>
            ) : (
              <Area
                type="monotone"
                dataKey="total"
                name="Tổng doanh thu"
                stroke="#f8471a"
                fill="url(#gradientTotal)"
                strokeWidth={2.5}
                activeDot={{ r: 6 }}
              />
            )}
          </AreaChart>
        </ResponsiveContainer>
      ) : (
        <ResponsiveContainer width="100%" height={height}>
          <BarChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" vertical={false} />
            <XAxis
              dataKey="date"
              tick={{ fontSize: 11, fill: '#64748b' }}
              axisLine={{ stroke: '#e2e8f0' }}
              tickLine={false}
            />
            <YAxis
              tickFormatter={(v) =>
                isCheckInMetric
                  ? `${v}`
                  : `${(v / 1000000).toFixed(v >= 10000000 ? 0 : 1)}tr`
              }
              tick={{ fontSize: 11, fill: '#64748b' }}
              axisLine={false}
              tickLine={false}
              width={48}
            />
            <Tooltip content={<CustomChartTooltip isCheckIn={isCheckInMetric} />} />
            <Legend
              wrapperStyle={{ paddingTop: '14px', fontSize: '12px' }}
              iconType="circle"
              iconSize={8}
            />
            {isCheckInMetric ? (
              <Bar
                dataKey="checkInCount"
                name="Lượt tập (Check-in)"
                fill="#10b981"
                radius={[4, 4, 0, 0]}
                maxBarSize={32}
              />
            ) : (
              <>
                <Bar
                  dataKey="membershipRevenue"
                  name="Gói tập"
                  fill="#f8471a"
                  radius={[4, 4, 0, 0]}
                  maxBarSize={32}
                />
                <Bar
                  dataKey="retailRevenue"
                  name="Bán lẻ"
                  fill="#3b82f6"
                  radius={[4, 4, 0, 0]}
                  maxBarSize={32}
                />
              </>
            )}
          </BarChart>
        </ResponsiveContainer>
      )}
    </div>
  )
}

export default RevenueChart
