import { Link } from 'react-router-dom'
import { ArrowRight, Phone, PlayCircle } from 'lucide-react'
import SectionHeading from '../../components/public/SectionHeading'
import AmenityCard from '../../components/public/AmenityCard'
import ServiceCard from '../../components/public/ServiceCard'
import MembershipCard from '../../components/common/MembershipCard'
import Loading from '../../components/common/Loading'
import { heroImage, aboutImage, amenities, services, highlights } from '../../data/publicContent'
import { useActiveMemberships } from '../../hooks/useMemberships'

function HomePage() {
  const { data: memberships, isLoading } = useActiveMemberships()

  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden bg-gray-900">
        <img src={heroImage} alt="Phòng tập PowerFit Gym" className="absolute inset-0 h-full w-full object-cover opacity-40" />
        <div className="absolute inset-0 bg-gradient-to-r from-gray-900 via-gray-900/80 to-transparent" />
        <div className="relative mx-auto max-w-7xl px-4 py-24 sm:px-6 sm:py-32 lg:px-8">
          <p className="text-sm font-semibold uppercase tracking-widest text-primary-400">Modern Gym & Fitness</p>
          <h1 className="mt-4 max-w-2xl text-4xl font-extrabold leading-tight text-white sm:text-5xl lg:text-6xl">
            Kiến tạo phiên bản khỏe mạnh nhất của bạn
          </h1>
          <p className="mt-6 max-w-xl text-lg text-gray-300">
            Không gian tập luyện hiện đại, trang thiết bị đẳng cấp và đội ngũ huấn luyện viên tận tâm — sẵn sàng đồng hành cùng mục tiêu của bạn.
          </p>
          <div className="mt-8 flex flex-wrap gap-4">
            <Link
              to="/pricing"
              className="inline-flex items-center gap-2 rounded-lg bg-primary-500 px-6 py-3 text-sm font-semibold text-white shadow-lg transition-colors hover:bg-primary-600"
            >
              Xem bảng giá <ArrowRight size={16} />
            </Link>
            <Link
              to="/contact"
              className="inline-flex items-center gap-2 rounded-lg border border-white/30 bg-white/10 px-6 py-3 text-sm font-semibold text-white backdrop-blur transition-colors hover:bg-white/20"
            >
              <Phone size={16} /> Liên hệ ngay
            </Link>
          </div>
        </div>
      </section>

      {/* Intro */}
      <section className="mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-2">
          <div className="overflow-hidden rounded-3xl">
            <img src={aboutImage} alt="Không gian tập luyện" className="h-full w-full object-cover" />
          </div>
          <div>
            <SectionHeading eyebrow="Về chúng tôi" title="Không gian, đội ngũ và trang thiết bị đẳng cấp" center={false} />
            <p className="mt-4 text-gray-500">
              PowerFit Gym mang đến không gian tập luyện rộng rãi, thoáng mát cùng hệ thống máy móc nhập khẩu hiện đại. Đội ngũ huấn luyện
              viên giàu kinh nghiệm sẵn sàng tư vấn và đồng hành cùng bạn trong mọi chặng đường rèn luyện.
            </p>
            <div className="mt-6 grid grid-cols-2 gap-4">
              {highlights.map((h) => (
                <div key={h.title} className="rounded-xl border border-gray-100 p-4 shadow-card">
                  <p className="text-sm font-bold text-gray-900">{h.title}</p>
                  <p className="mt-1 text-xs text-gray-500">{h.description}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Amenities */}
      <section className="bg-gray-50 py-20">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeading eyebrow="Tiện ích" title="Tiện ích nổi bật" description="Đầy đủ tiện nghi cho một buổi tập trọn vẹn." />
          <div className="mt-12 grid grid-cols-2 gap-4 sm:grid-cols-4">
            {amenities.map((a) => (
              <AmenityCard key={a.title} {...a} />
            ))}
          </div>
        </div>
      </section>

      {/* Services */}
      <section className="mx-auto max-w-7xl px-4 py-20 sm:px-6 lg:px-8">
        <SectionHeading eyebrow="Dịch vụ" title="Dịch vụ của chúng tôi" description="Đa dạng loại hình tập luyện phù hợp với mọi mục tiêu." />
        <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {services.map((s) => (
            <ServiceCard key={s.id} {...s} />
          ))}
        </div>
      </section>

      {/* Pricing preview */}
      <section className="bg-gray-50 py-20">
        <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
          <SectionHeading eyebrow="Bảng giá" title="Gói tập phù hợp với bạn" description="Chọn gói tập linh hoạt theo nhu cầu tập luyện." />
          {isLoading ? (
            <Loading />
          ) : (
            <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {memberships?.map((m) => (
                <MembershipCard key={m.id} membership={m} compact />
              ))}
            </div>
          )}
          <div className="mt-10 text-center">
            <Link
              to="/pricing"
              className="inline-flex items-center gap-2 rounded-lg border border-gray-300 px-6 py-3 text-sm font-semibold text-gray-700 hover:bg-gray-100"
            >
              Xem tất cả gói tập <ArrowRight size={16} />
            </Link>
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="relative overflow-hidden bg-primary-500 py-20">
        <div className="mx-auto max-w-4xl px-4 text-center sm:px-6 lg:px-8">
          <PlayCircle size={40} className="mx-auto text-white/80" />
          <h2 className="mt-4 text-3xl font-extrabold text-white sm:text-4xl">Đăng ký tập luyện ngay hôm nay</h2>
          <p className="mt-4 text-primary-50">Chỉ mất 2 phút để bắt đầu hành trình rèn luyện sức khỏe cùng PowerFit Gym.</p>
          <div className="mt-8 flex flex-wrap justify-center gap-4">
            <Link to="/contact" className="rounded-lg bg-white px-6 py-3 text-sm font-semibold text-primary-600 hover:bg-gray-100">
              Đăng ký ngay
            </Link>
            <a href="tel:19001234" className="rounded-lg border border-white/40 px-6 py-3 text-sm font-semibold text-white hover:bg-white/10">
              Hotline: 1900 1234
            </a>
          </div>
        </div>
      </section>
    </div>
  )
}

export default HomePage
