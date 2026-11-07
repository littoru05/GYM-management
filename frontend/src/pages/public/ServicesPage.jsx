import ServicesSection from '../../components/public/ServicesSection'

function ServicesPage() {
  return (
    <div>
      {/* Header Banner */}
      <section className="relative overflow-hidden bg-gray-900 py-20 text-center text-white">
        <div className="absolute inset-0 bg-gradient-to-r from-gray-900 via-gray-800 to-gray-900 opacity-90" />
        <div className="relative mx-auto max-w-3xl px-4 sm:px-6 lg:px-8">
          <p className="text-xs font-bold uppercase tracking-widest text-primary-400">Dịch vụ & Tiện ích tiêu chuẩn 5 sao</p>
          <h1 className="mt-3 text-3xl font-extrabold tracking-tight sm:text-5xl">
            Các Nhóm Dịch Vụ Chủ Đạo
          </h1>
          <p className="mx-auto mt-4 max-w-2xl text-base text-gray-300 sm:text-lg">
            Hệ thống bộ môn tập luyện toàn diện: Thể hình tự do, Huấn luyện cá nhân 1-1, Yoga & Giãn cơ, Kickboxing Cardio.
            Được trang bị máy móc tối tân và miễn phí 100% tiện ích cao cấp.
          </p>
        </div>
      </section>

      {/* Main Services Section */}
      <ServicesSection
        showHeading={false}
        showCategoryTabs={true}
        showAmenitiesBanner={true}
        showCtaBanner={true}
      />
    </div>
  )
}

export default ServicesPage
