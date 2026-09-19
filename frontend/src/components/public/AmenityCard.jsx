function AmenityCard({ icon: Icon, title }) {
  return (
    <div className="flex flex-col items-center gap-3 rounded-2xl border border-gray-100 bg-white p-6 text-center shadow-card transition-transform hover:-translate-y-1">
      <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
        <Icon size={22} />
      </div>
      <p className="text-sm font-semibold text-gray-800">{title}</p>
    </div>
  )
}

export default AmenityCard
