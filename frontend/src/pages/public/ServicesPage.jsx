import SectionHeading from '../../components/public/SectionHeading'
import ServiceCard from '../../components/public/ServiceCard'
import { services } from '../../data/publicContent'

function ServicesPage() {
  return (
    <div>
      <section className="bg-gray-900 py-16 text-center">
        <h1 className="text-3xl font-extrabold text-white sm:text-4xl">Dịch vụ của chúng tôi</h1>
        <p className="mx-auto mt-3 max-w-2xl text-gray-300">Đa dạng loại hình tập luyện, phù hợp với mọi mục tiêu và trình độ.</p>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <SectionHeading eyebrow="Khám phá" title="Tất cả dịch vụ" />
        <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {services.map((s) => (
            <ServiceCard key={s.id} {...s} />
          ))}
        </div>
      </section>
    </div>
  )
}

export default ServicesPage
