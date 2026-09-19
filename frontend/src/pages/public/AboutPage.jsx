import { Target, Eye, Award } from 'lucide-react'
import SectionHeading from '../../components/public/SectionHeading'
import { aboutImage, highlights } from '../../data/publicContent'

function AboutPage() {
  return (
    <div>
      <section className="bg-gray-900 py-16 text-center">
        <h1 className="text-3xl font-extrabold text-white sm:text-4xl">Giới thiệu về PowerFit Gym</h1>
        <p className="mx-auto mt-3 max-w-2xl text-gray-300">Hành trình xây dựng một không gian tập luyện đẳng cấp cho cộng đồng.</p>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 items-center gap-12 lg:grid-cols-2">
          <div className="overflow-hidden rounded-3xl">
            <img src={aboutImage} alt="Không gian PowerFit Gym" className="h-full w-full object-cover" />
          </div>
          <div>
            <SectionHeading
              eyebrow="Câu chuyện"
              title="Hơn một phòng gym — một cộng đồng rèn luyện"
              description="PowerFit Gym được thành lập với mong muốn mang đến một môi trường tập luyện chuyên nghiệp, thân thiện và truyền cảm hứng cho mọi người ở mọi trình độ."
              center={false}
            />
          </div>
        </div>
      </section>

      <section className="bg-gray-50 py-16">
        <div className="mx-auto grid max-w-7xl grid-cols-1 gap-6 px-4 sm:grid-cols-3 sm:px-6 lg:px-8">
          <div className="rounded-2xl border border-gray-100 bg-white p-6 text-center shadow-card">
            <Target size={28} className="mx-auto text-primary-500" />
            <p className="mt-3 font-bold text-gray-900">Sứ mệnh</p>
            <p className="mt-2 text-sm text-gray-500">Giúp mọi người xây dựng lối sống khỏe mạnh, bền vững thông qua tập luyện đúng cách.</p>
          </div>
          <div className="rounded-2xl border border-gray-100 bg-white p-6 text-center shadow-card">
            <Eye size={28} className="mx-auto text-primary-500" />
            <p className="mt-3 font-bold text-gray-900">Tầm nhìn</p>
            <p className="mt-2 text-sm text-gray-500">Trở thành hệ thống phòng tập được yêu thích nhất, tiên phong về chất lượng dịch vụ.</p>
          </div>
          <div className="rounded-2xl border border-gray-100 bg-white p-6 text-center shadow-card">
            <Award size={28} className="mx-auto text-primary-500" />
            <p className="mt-3 font-bold text-gray-900">Giá trị</p>
            <p className="mt-2 text-sm text-gray-500">Chuyên nghiệp — Tận tâm — Kỷ luật — Đồng hành cùng hội viên trên mọi hành trình.</p>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <SectionHeading eyebrow="Điểm nổi bật" title="Vì sao chọn PowerFit Gym" />
        <div className="mt-12 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {highlights.map((h) => (
            <div key={h.title} className="rounded-2xl border border-gray-100 p-6 shadow-card">
              <p className="font-bold text-gray-900">{h.title}</p>
              <p className="mt-2 text-sm text-gray-500">{h.description}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

export default AboutPage
