import { useState } from 'react'
import { Phone } from 'lucide-react'
import SectionHeading from '../../components/public/SectionHeading'
import MembershipCard from '../../components/common/MembershipCard'
import PackageConsultationForm from '../../components/public/PackageConsultationForm'
import Loading from '../../components/common/Loading'
import EmptyState from '../../components/common/EmptyState'
import { useActiveMemberships } from '../../hooks/useMemberships'

function PricingPage() {
  const { data: memberships, isLoading } = useActiveMemberships()
  const [selectedPackage, setSelectedPackage] = useState(null)

  const handleConsultPackage = (pkg) => {
    setSelectedPackage(pkg)
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('select-membership-package', { detail: pkg }))
    }
    const el = document.getElementById('contact-form')
    if (el) {
      el.scrollIntoView({ behavior: 'smooth', block: 'start' })
    }
  }

  return (
    <div>
      {/* Page Header */}
      <section className="bg-gray-900 py-16 text-center text-white">
        <div className="mx-auto max-w-3xl px-4 sm:px-6 lg:px-8">
          <p className="text-xs font-bold uppercase tracking-widest text-primary-400">Gói tập & Đặc quyền hội viên</p>
          <h1 className="mt-3 text-3xl font-extrabold sm:text-5xl">Bảng Giá Membership</h1>
          <p className="mx-auto mt-4 max-w-2xl text-base text-gray-300">
            Hệ thống 4 gói tập chuẩn mực từ Basic đến VIP/Platinum. Mọi gói tập đều bao gồm quyền lợi đo InBody, PT định hướng và dịch vụ Sauna cao cấp.
          </p>
        </div>
      </section>

      {/* Main Pricing Cards Grid */}
      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <SectionHeading
          eyebrow="Membership"
          title="Chọn Gói Tập Phù Hợp Với Bạn"
          description="Bảng giá minh bạch, không phát sinh chi phí ẩn. Bấm 'Nhận tư vấn gói này' để nhận thêm ưu đãi đặc biệt."
        />

        {isLoading && <Loading />}
        {!isLoading && (!memberships || memberships.length === 0) && <EmptyState title="Chưa có gói tập nào" />}

        {!isLoading && memberships && memberships.length > 0 && (
          <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {memberships.map((m) => (
              <MembershipCard
                key={m.id}
                membership={m}
                onConsult={handleConsultPackage}
                ctaLabel="Nhận tư vấn gói này"
              />
            ))}
          </div>
        )}

        {/* Embedded Consultation Form */}
        <div className="mt-16">
          <PackageConsultationForm
            selectedPackage={selectedPackage}
            onSelectPackage={setSelectedPackage}
          />
        </div>

        {/* Hotline Support Bar */}
        <div className="mt-8 flex items-center justify-center gap-2 text-sm text-gray-500">
          <Phone size={16} className="text-primary-500" />
          <span>Cần tư vấn trực tiếp ngay? Gọi hotline:</span>
          <a href="tel:19001234" className="font-semibold text-primary-600 hover:underline">
            1900 1234
          </a>
        </div>
      </section>
    </div>
  )
}

export default PricingPage
