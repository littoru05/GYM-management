import { Phone, Mail, MapPin, Clock } from 'lucide-react'
import SectionHeading from '../../components/public/SectionHeading'
import Input from '../../components/common/Input'
import Button from '../../components/common/Button'

function ContactPage() {
  const handleSubmit = (e) => {
    e.preventDefault()
  }

  return (
    <div>
      <section className="bg-gray-900 py-16 text-center">
        <h1 className="text-3xl font-extrabold text-white sm:text-4xl">Liên hệ với chúng tôi</h1>
        <p className="mx-auto mt-3 max-w-2xl text-gray-300">Đội ngũ PowerFit Gym luôn sẵn sàng hỗ trợ bạn.</p>
      </section>

      <section className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-12 lg:grid-cols-2">
          <div>
            <SectionHeading eyebrow="Thông tin" title="Thông tin liên hệ" center={false} />
            <div className="mt-8 space-y-5">
              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
                  <Phone size={18} />
                </div>
                <div>
                  <p className="font-semibold text-gray-900">Hotline</p>
                  <p className="text-sm text-gray-500">1900 1234</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
                  <Mail size={18} />
                </div>
                <div>
                  <p className="font-semibold text-gray-900">Email</p>
                  <p className="text-sm text-gray-500">contact@powerfitgym.vn</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
                  <MapPin size={18} />
                </div>
                <div>
                  <p className="font-semibold text-gray-900">Địa chỉ</p>
                  <p className="text-sm text-gray-500">123 Đường Thể Thao, Quận 1, TP.HCM</p>
                </div>
              </div>
              <div className="flex items-start gap-3">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-primary-50 text-primary-600">
                  <Clock size={18} />
                </div>
                <div>
                  <p className="font-semibold text-gray-900">Giờ mở cửa</p>
                  <p className="text-sm text-gray-500">05:00 – 23:00 (Tất cả các ngày trong tuần)</p>
                </div>
              </div>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="rounded-2xl border border-gray-100 bg-white p-6 shadow-card sm:p-8">
            <h3 className="text-lg font-bold text-gray-900">Gửi tin nhắn cho chúng tôi</h3>
            <div className="mt-5 space-y-4">
              <Input label="Họ và tên" placeholder="Nguyễn Văn A" required />
              <Input label="Số điện thoại" placeholder="09xxxxxxxx" required />
              <Input label="Email" type="email" placeholder="email@example.com" />
              <div>
                <label className="mb-1.5 block text-sm font-medium text-gray-700">Nội dung</label>
                <textarea
                  rows={4}
                  placeholder="Bạn muốn tìm hiểu về gói tập nào?"
                  className="w-full rounded-lg border border-gray-300 bg-white px-3 py-2 text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-200"
                />
              </div>
              <Button type="submit" fullWidth>
                Gửi liên hệ
              </Button>
            </div>
          </form>
        </div>
      </section>
    </div>
  )
}

export default ContactPage
