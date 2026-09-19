import { Phone } from 'lucide-react'
import SectionHeading from '../../components/public/SectionHeading'
import MembershipCard from '../../components/common/MembershipCard'
import Loading from '../../components/common/Loading'
import EmptyState from '../../components/common/EmptyState'
import { useActiveMemberships } from '../../hooks/useMemberships'

function PricingPage() {
  const { data: memberships, isLoading } = useActiveMemberships()

  return (
    <div>
      <section className="bg-gray-900 py-16 text-center">
        <h1 className="text-3xl font-extrabold text-white sm:text-4xl">Bảng giá Membership</h1>
        <p className="mx-auto mt-3 max-w-2xl text-gray-300">Chọn gói tập phù hợp với mục tiêu và ngân sách của bạn.</p>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <SectionHeading eyebrow="Membership" title="Các gói tập hiện có" description="Không phát sinh chi phí ẩn. Liên hệ để được tư vấn chi tiết." />

        {isLoading && <Loading />}
        {!isLoading && (!memberships || memberships.length === 0) && <EmptyState title="Chưa có gói tập nào" />}

        {!isLoading && memberships && memberships.length > 0 && (
          <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {memberships.map((m) => (
              <MembershipCard key={m.id} membership={m} />
            ))}
          </div>
        )}

        <div className="mt-12 flex items-center justify-center gap-2 text-sm text-gray-500">
          <Phone size={16} className="text-primary-500" />
          Cần tư vấn thêm? Gọi ngay <a href="tel:19001234" className="font-semibold text-primary-600">1900 1234</a>
        </div>
      </section>
    </div>
  )
}

export default PricingPage
