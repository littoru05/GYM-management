import { Phone, Mail, MapPin, Clock } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import SectionHeading from '../../components/public/SectionHeading'
import Input from '../../components/common/Input'
import Textarea from '../../components/common/Textarea'
import Button from '../../components/common/Button'
import { useSubmitContactLead } from '../../hooks/useContactLeads'

const schema = yup.object({
  name: yup
    .string()
    .trim()
    .required('Họ và tên không được để trống')
    .min(2, 'Họ và tên phải từ 2 đến 50 ký tự')
    .max(50, 'Họ và tên phải từ 2 đến 50 ký tự')
    .matches(/^[a-zA-ZÀ-ỹ\s]+$/u, 'Họ và tên chỉ được chứa chữ cái, không được chứa số hoặc ký tự đặc biệt'),
  email: yup
    .string()
    .trim()
    .required('Email không được để trống')
    .matches(/^[a-zA-Z0-9._%+-]+@gmail\.com$/, 'Email phải đúng định dạng và có đuôi @gmail.com (ví dụ: yourname@gmail.com)'),
  phone: yup
    .string()
    .trim()
    .required('Số điện thoại không được để trống')
    .matches(/^(0[3|5|7|8|9])[0-9]{8}$/, 'Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 03, 05, 07, 08 hoặc 09'),
  message: yup.string().trim().max(500, 'Nội dung tư vấn tối đa 500 ký tự'),
})

function ContactPage() {
  const submitMutation = useSubmitContactLead()

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    defaultValues: { name: '', email: '', phone: '', message: '' },
  })

  const onSubmit = (values) => {
    submitMutation.mutate(values, { onSuccess: () => reset() })
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

          <form
            onSubmit={handleSubmit(onSubmit)}
            noValidate
            className="rounded-2xl border border-gray-100 bg-white p-6 shadow-card sm:p-8"
          >
            <h3 className="text-lg font-bold text-gray-900">Gửi tin nhắn cho chúng tôi</h3>
            <div className="mt-5 space-y-4">
              <Input
                label="Họ và tên"
                placeholder="Nguyễn Văn A"
                error={errors.name?.message}
                {...register('name')}
              />
              <Input
                label="Số điện thoại"
                placeholder="09xxxxxxxx"
                error={errors.phone?.message}
                {...register('phone')}
              />
              <Input
                label="Email"
                type="email"
                placeholder="yourname@gmail.com"
                error={errors.email?.message}
                {...register('email')}
              />
              <Textarea
                label="Nội dung"
                placeholder="Bạn muốn tìm hiểu về gói tập nào?"
                rows={4}
                error={errors.message?.message}
                {...register('message')}
              />
              <Button type="submit" fullWidth loading={submitMutation.isPending} disabled={submitMutation.isPending}>
                {submitMutation.isPending ? 'Đang gửi...' : 'Gửi liên hệ'}
              </Button>
            </div>
          </form>
        </div>
      </section>
    </div>
  )
}

export default ContactPage
