import { useState } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowRight,
  CheckCircle2,
  Droplets,
  Lock,
  Phone,
  ShieldCheck,
  ShowerHead,
  Sparkles,
  Wifi,
  X,
  Activity,
  Target,
  Dumbbell,
} from 'lucide-react'
import SectionHeading from './SectionHeading'
import ServiceCard from './ServiceCard'
import { services as defaultServices } from '../../data/publicContent'

const CATEGORY_TABS = [
  { id: 'all', label: 'Tất cả dịch vụ' },
  { id: 'gym', label: 'Thể hình Gym' },
  { id: 'pt', label: 'HLV Cá nhân 1-1' },
  { id: 'yoga', label: 'Yoga & Giãn cơ' },
  { id: 'cardio', label: 'Kickboxing & Cardio' },
]

function ServicesSection({
  services = defaultServices,
  title = 'Hệ Thống Dịch Vụ Tập Luyện Chuyên Nghiệp',
  eyebrow = 'Dịch vụ chủ đạo',
  description = 'PowerFit Gym mang đến 4 nhóm dịch vụ cốt lõi chuẩn khoa học, máy móc nhập khẩu hiện đại cùng đầy đủ tiện ích 5 sao đi kèm.',
  showHeading = true,
  showCategoryTabs = true,
  showAmenitiesBanner = true,
  showCtaBanner = true,
}) {
  const [activeCategory, setActiveCategory] = useState('all')
  const [selectedService, setSelectedService] = useState(null)

  const filteredServices =
    activeCategory === 'all'
      ? services
      : services.filter((s) => s.category === activeCategory)

  return (
    <section className="relative overflow-hidden py-16 sm:py-24">
      {/* Background decoration */}
      <div className="pointer-events-none absolute inset-0 -z-10 flex justify-center">
        <div className="h-96 w-[60rem] rounded-full bg-gradient-to-b from-primary-50/70 to-transparent blur-3xl" />
      </div>

      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        {/* Section Heading */}
        {showHeading && (
          <SectionHeading
            eyebrow={eyebrow}
            title={title}
            description={description}
          />
        )}

        {/* Value Proposition Pills */}
        <div className="mt-8 flex flex-wrap items-center justify-center gap-3 text-xs sm:gap-4 sm:text-sm">
          <div className="inline-flex items-center gap-1.5 rounded-full border border-gray-200 bg-white px-3.5 py-1.5 font-medium text-gray-700 shadow-xs">
            <Dumbbell size={15} className="text-primary-500" />
            <span>100% Thiết bị nhập khẩu Olympic</span>
          </div>
          <div className="inline-flex items-center gap-1.5 rounded-full border border-gray-200 bg-white px-3.5 py-1.5 font-medium text-gray-700 shadow-xs">
            <ShieldCheck size={15} className="text-emerald-500" />
            <span>HLV chứng nhận quốc tế NASM/ACE</span>
          </div>
          <div className="inline-flex items-center gap-1.5 rounded-full border border-gray-200 bg-white px-3.5 py-1.5 font-medium text-gray-700 shadow-xs">
            <Sparkles size={15} className="text-amber-500" />
            <span>Đầy đủ tiện ích 5 sao miễn phí</span>
          </div>
        </div>

        {/* Category Tabs */}
        {showCategoryTabs && (
          <div className="mt-8 flex justify-center">
            <div className="inline-flex flex-wrap items-center justify-center gap-1.5 rounded-2xl border border-gray-200/80 bg-gray-50/80 p-1.5">
              {CATEGORY_TABS.map((tab) => {
                const isActive = activeCategory === tab.id
                return (
                  <button
                    key={tab.id}
                    type="button"
                    onClick={() => setActiveCategory(tab.id)}
                    className={`rounded-xl px-4 py-2 text-xs font-semibold transition-all sm:text-sm ${
                      isActive
                        ? 'bg-white text-primary-600 shadow-sm'
                        : 'text-gray-600 hover:text-gray-900'
                    }`}
                  >
                    {tab.label}
                  </button>
                )
              })}
            </div>
          </div>
        )}

        {/* Service Cards Grid */}
        <div className="mt-10 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-2 xl:grid-cols-4">
          {filteredServices.map((service) => (
            <ServiceCard
              key={service.id}
              {...service}
              onOpenDetails={() => setSelectedService(service)}
            />
          ))}
        </div>

        {/* Included Amenities Highlights Banner */}
        {showAmenitiesBanner && (
          <div className="mt-16 rounded-3xl border border-primary-100 bg-gradient-to-r from-primary-50/60 via-white to-primary-50/40 p-6 shadow-sm sm:p-8">
            <div className="flex flex-col items-start justify-between gap-4 lg:flex-row lg:items-center">
              <div>
                <div className="inline-flex items-center gap-2 rounded-lg bg-primary-100 px-3 py-1 text-xs font-bold uppercase tracking-wider text-primary-700">
                  <Sparkles size={14} />
                  <span>Tiện ích đi kèm trọn gói</span>
                </div>
                <h3 className="mt-2 text-xl font-bold text-gray-900 sm:text-2xl">
                  Trải nghiệm 5 sao chuẩn mực tại mọi buổi tập
                </h3>
                <p className="mt-1 text-sm text-gray-600">
                  Tất cả các dịch vụ tập luyện tại PowerFit Gym đều đã bao gồm trọn gói các tiện ích cao cấp:
                </p>
              </div>
              <Link
                to="/pricing"
                className="inline-flex shrink-0 items-center gap-2 rounded-xl bg-gray-900 px-5 py-2.5 text-xs font-semibold text-white transition-colors hover:bg-gray-800 sm:text-sm"
              >
                Xem bảng giá hội viên <ArrowRight size={15} />
              </Link>
            </div>

            <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
              <div className="flex items-start gap-3.5 rounded-2xl border border-gray-100 bg-white p-4 shadow-xs">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
                  <ShowerHead size={20} />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-gray-900">Phòng tắm nóng lạnh</h4>
                  <p className="mt-1 text-xs text-gray-500">
                    Khu vòi sen riêng tư, hệ thống nóng lạnh ổn định, trang bị máy sấy tóc & sữa tắm cao cấp.
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3.5 rounded-2xl border border-gray-100 bg-white p-4 shadow-xs">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
                  <Lock size={20} />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-gray-900">Tủ locker thông minh</h4>
                  <p className="mt-1 text-xs text-gray-500">
                    Khóa từ cảm ứng an toàn, kích thước rộng rãi, bảo vệ tối đa tư trang cá nhân suốt buổi tập.
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3.5 rounded-2xl border border-gray-100 bg-white p-4 shadow-xs">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
                  <Droplets size={20} />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-gray-900">Nước uống miễn phí</h4>
                  <p className="mt-1 text-xs text-gray-500">
                    Cây lọc nước ion kiềm & khoáng lạnh liên tục, đảm bảo bù nước tức thì trong từng hiệp tập.
                  </p>
                </div>
              </div>

              <div className="flex items-start gap-3.5 rounded-2xl border border-gray-100 bg-white p-4 shadow-xs">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
                  <Wifi size={20} />
                </div>
                <div>
                  <h4 className="text-sm font-bold text-gray-900">Khăn tập & Wifi siêu tốc</h4>
                  <p className="mt-1 text-xs text-gray-500">
                    Khăn bông sạch tiệt trùng mỗi buổi tập, wifi cáp quang tốc độ cao phủ sóng toàn bộ khuôn viên.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Quick Consultation Banner */}
        {showCtaBanner && (
          <div className="mt-12 rounded-3xl bg-gray-900 p-8 text-center text-white sm:p-10">
            <h3 className="text-2xl font-bold sm:text-3xl">
              Chưa chắc chắn bộ môn nào phù hợp với thể trạng của bạn?
            </h3>
            <p className="mx-auto mt-3 max-w-2xl text-sm text-gray-300 sm:text-base">
              Đặt lịch trải nghiệm ngay hôm nay để được đo chỉ số cơ mỡ InBody 770 chuyên sâu và nhận phác thảo giáo án dinh dưỡng hoàn toàn miễn phí!
            </p>
            <div className="mt-6 flex flex-wrap items-center justify-center gap-4">
              <Link
                to="/contact"
                className="inline-flex items-center gap-2 rounded-xl bg-primary-500 px-6 py-3 text-sm font-semibold text-white shadow-lg transition-colors hover:bg-primary-600"
              >
                Nhận tư vấn & Đo InBody miễn phí <ArrowRight size={16} />
              </Link>
              <a
                href="tel:19001234"
                className="inline-flex items-center gap-2 rounded-xl border border-gray-700 bg-gray-800/80 px-6 py-3 text-sm font-semibold text-gray-200 transition-colors hover:bg-gray-700"
              >
                <Phone size={16} /> Hotline: 1900 1234
              </a>
            </div>
          </div>
        )}
      </div>

      {/* Service Detail Modal */}
      {selectedService && (
        <div
          role="dialog"
          aria-modal="true"
          className="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6"
        >
          {/* Backdrop */}
          <div
            className="fixed inset-0 bg-gray-900/60 backdrop-blur-sm transition-opacity"
            onClick={() => setSelectedService(null)}
          />

          {/* Modal Container */}
          <div className="relative max-h-[90vh] w-full max-w-2xl overflow-y-auto rounded-3xl border border-gray-100 bg-white p-6 shadow-2xl sm:p-8">
            {/* Close Button */}
            <button
              type="button"
              onClick={() => setSelectedService(null)}
              className="absolute right-4 top-4 z-10 flex h-9 w-9 items-center justify-center rounded-full bg-gray-100 text-gray-500 transition-colors hover:bg-gray-200 hover:text-gray-900"
            >
              <X size={18} />
            </button>

            {/* Modal Image Header */}
            <div className="relative -mx-6 -mt-6 mb-6 h-56 overflow-hidden rounded-t-3xl bg-gray-900 sm:-mx-8 sm:-mt-8 sm:h-64">
              <img
                src={selectedService.image}
                alt={selectedService.imageAlt || selectedService.title}
                className="h-full w-full object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-gray-950 via-gray-950/40 to-transparent" />
              <div className="absolute bottom-4 left-6 right-6 text-white sm:bottom-6 sm:left-8 sm:right-8">
                {selectedService.badge && (
                  <span className="inline-block rounded-full bg-primary-500 px-3 py-1 text-xs font-bold text-white shadow-sm">
                    {selectedService.badge}
                  </span>
                )}
                <h3 className="mt-2 text-2xl font-bold sm:text-3xl">{selectedService.title}</h3>
                {selectedService.tagline && (
                  <p className="mt-1 text-xs text-gray-200 sm:text-sm">{selectedService.tagline}</p>
                )}
              </div>
            </div>

            {/* Modal Content */}
            <div className="space-y-6">
              {/* Quick Stats Highlights */}
              {selectedService.stats && selectedService.stats.length > 0 && (
                <div className="grid grid-cols-3 gap-2 rounded-2xl border border-gray-100 bg-gray-50 p-3 text-center">
                  {selectedService.stats.map((st, i) => (
                    <div key={i} className="px-1">
                      <p className="text-[11px] text-gray-500">{st.label}</p>
                      <p className="mt-0.5 text-xs font-bold text-gray-900 sm:text-sm">{st.value}</p>
                    </div>
                  ))}
                </div>
              )}

              {/* General Description */}
              <div>
                <h4 className="text-xs font-bold uppercase tracking-wider text-gray-400">
                  Tổng quan không gian & dịch vụ
                </h4>
                <p className="mt-2 text-sm leading-relaxed text-gray-700">
                  {selectedService.description}
                </p>
              </div>

              {/* Training Method */}
              {selectedService.method && (
                <div className="rounded-2xl border border-primary-100 bg-primary-50/50 p-4">
                  <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wide text-primary-700">
                    <Activity size={16} className="text-primary-600" />
                    <span>Phương pháp tập luyện chi tiết</span>
                  </div>
                  {selectedService.method.summary && (
                    <p className="mt-2 text-sm font-semibold text-gray-900">
                      {selectedService.method.summary}
                    </p>
                  )}
                  {selectedService.method.details && (
                    <p className="mt-1.5 text-xs leading-relaxed text-gray-600 sm:text-sm">
                      {selectedService.method.details}
                    </p>
                  )}
                </div>
              )}

              {/* Target Audience */}
              {selectedService.suitableFor && selectedService.suitableFor.length > 0 && (
                <div>
                  <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-gray-700">
                    <Target size={16} className="text-primary-600" />
                    <span>Đối tượng phù hợp nhất</span>
                  </div>
                  <ul className="mt-3 space-y-2">
                    {selectedService.suitableFor.map((item, idx) => (
                      <li key={idx} className="flex items-start gap-2.5 text-sm text-gray-700">
                        <CheckCircle2 size={16} className="mt-0.5 shrink-0 text-emerald-500" />
                        <span>{item}</span>
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Equipment Highlights */}
              {selectedService.equipment && selectedService.equipment.length > 0 && (
                <div>
                  <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-gray-700">
                    <Dumbbell size={16} className="text-primary-600" />
                    <span>Hệ thống máy móc & trang thiết bị</span>
                  </div>
                  <div className="mt-3 grid grid-cols-1 gap-2 sm:grid-cols-2">
                    {selectedService.equipment.map((eq, idx) => (
                      <div
                        key={idx}
                        className="flex items-center gap-2 rounded-xl border border-gray-100 bg-gray-50/80 px-3 py-2 text-xs text-gray-700"
                      >
                        <span className="h-1.5 w-1.5 rounded-full bg-primary-500" />
                        <span className="font-medium">{eq}</span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Included Amenities in Detail */}
              {selectedService.amenities && selectedService.amenities.length > 0 && (
                <div className="rounded-2xl border border-gray-100 bg-gray-50 p-4">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wide text-gray-700">
                      <Sparkles size={16} className="text-amber-500" />
                      <span>Tiện ích đi kèm miễn phí</span>
                    </div>
                    <span className="rounded-full bg-emerald-100 px-2.5 py-0.5 text-[11px] font-bold text-emerald-700">
                      Bao gồm trọn gói
                    </span>
                  </div>
                  <div className="mt-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
                    {selectedService.amenities.map((a) => {
                      const AmenityIcon = a.icon || Sparkles
                      return (
                        <div
                          key={a.id || a.name}
                          className="flex items-start gap-3 rounded-xl border border-gray-100 bg-white p-3 shadow-xs"
                        >
                          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
                            <AmenityIcon size={16} />
                          </div>
                          <div>
                            <p className="text-xs font-bold text-gray-900">{a.name}</p>
                            <p className="mt-0.5 text-[11px] text-gray-500">{a.desc}</p>
                          </div>
                        </div>
                      )
                    })}
                  </div>
                </div>
              )}

              {/* Action Buttons */}
              <div className="flex flex-col gap-3 pt-2 sm:flex-row sm:items-center">
                <Link
                  to={`/contact?service=${selectedService.id}`}
                  onClick={() => setSelectedService(null)}
                  className="inline-flex flex-1 items-center justify-center gap-2 rounded-xl bg-primary-500 px-6 py-3 text-sm font-semibold text-white shadow-md transition-colors hover:bg-primary-600"
                >
                  <span>Đăng ký tư vấn dịch vụ này</span>
                  <ArrowRight size={16} />
                </Link>
                <Link
                  to="/pricing"
                  onClick={() => setSelectedService(null)}
                  className="inline-flex items-center justify-center rounded-xl border border-gray-200 bg-white px-5 py-3 text-sm font-semibold text-gray-700 transition-colors hover:bg-gray-50"
                >
                  Xem bảng giá
                </Link>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}

export default ServicesSection
