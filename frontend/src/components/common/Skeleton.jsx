export function Skeleton({ className = '' }) {
  return <div className={`animate-pulse rounded-lg bg-gray-200/80 ${className}`} />
}

export function CardSkeleton({ className = '' }) {
  return (
    <div className={`rounded-2xl border border-gray-100 bg-white p-5 shadow-card ${className}`}>
      <div className="flex items-start justify-between">
        <div className="flex-1 space-y-2.5">
          <Skeleton className="h-4 w-28" />
          <Skeleton className="h-7 w-40" />
        </div>
        <Skeleton className="h-12 w-12 rounded-xl" />
      </div>
      <div className="mt-3.5 flex items-center gap-2">
        <Skeleton className="h-4 w-16 rounded" />
        <Skeleton className="h-3 w-24 rounded" />
      </div>
    </div>
  )
}

export function StatCardsSkeleton({ count = 3, gridCols = 'grid-cols-1 sm:grid-cols-3' }) {
  return (
    <div className={`grid gap-4 ${gridCols}`}>
      {Array.from({ length: count }).map((_, i) => (
        <CardSkeleton key={i} />
      ))}
    </div>
  )
}

export function ChartSkeleton({ height = 320, title = true }) {
  return (
    <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-card">
      {title && (
        <div className="mb-6 flex items-center justify-between">
          <Skeleton className="h-5 w-44" />
          <Skeleton className="h-8 w-32 rounded-lg" />
        </div>
      )}
      <div className="flex items-end justify-between gap-3 pt-6" style={{ height }}>
        {[40, 65, 30, 85, 55, 70, 90, 45, 60, 75, 50, 80].map((h, i) => (
          <div key={i} className="flex flex-1 flex-col items-center gap-2">
            <Skeleton className="w-full rounded-t" style={{ height: `${h}%` }} />
            <Skeleton className="h-3 w-6" />
          </div>
        ))}
      </div>
    </div>
  )
}

export function DonutSkeleton({ height = 280 }) {
  return (
    <div className="rounded-2xl border border-gray-100 bg-white p-5 shadow-card">
      <Skeleton className="mb-6 h-5 w-36" />
      <div className="flex items-center justify-center" style={{ height: height - 60 }}>
        <div className="relative flex h-44 w-44 items-center justify-center rounded-full border-8 border-gray-200/80 animate-pulse">
          <Skeleton className="h-16 w-16 rounded-full" />
        </div>
      </div>
      <div className="mt-4 flex justify-center gap-4">
        <Skeleton className="h-4 w-28" />
        <Skeleton className="h-4 w-28" />
      </div>
    </div>
  )
}

export default Skeleton
