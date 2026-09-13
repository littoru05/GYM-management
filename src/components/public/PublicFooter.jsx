import { Link } from 'react-router-dom'
import { Dumbbell, Phone, Mail, MapPin, MessageCircle, Camera, PlayCircle } from 'lucide-react'

function PublicFooter() {
  return (
    <footer className="bg-gray-900 text-gray-300">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-8 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <div className="flex items-center gap-2">
              <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-500 text-white">
                <Dumbbell size={18} />
              </div>
              <span className="text-lg font-bold text-white">PowerFit Gym</span>
            </div>
            <p className="mt-3 text-sm text-gray-400">
              Không gian tập luyện hiện đại, đội ngũ huấn luyện viên chuyên nghiệp, đồng hành cùng bạn trên hành trình rèn luyện sức khỏe.
            </p>
          </div>

          <div>
            <p className="text-sm font-semibold text-white">Liên kết</p>
            <ul className="mt-3 space-y-2 text-sm">
              <li><Link to="/about" className="hover:text-white">Giới thiệu</Link></li>
              <li><Link to="/services" className="hover:text-white">Dịch vụ</Link></li>
              <li><Link to="/pricing" className="hover:text-white">Bảng giá</Link></li>
              <li><Link to="/contact" className="hover:text-white">Liên hệ</Link></li>
            </ul>
          </div>

          <div>
            <p className="text-sm font-semibold text-white">Liên hệ</p>
            <ul className="mt-3 space-y-2 text-sm">
              <li className="flex items-center gap-2"><Phone size={15} /> 1900 1234</li>
              <li className="flex items-center gap-2"><Mail size={15} /> contact@powerfitgym.vn</li>
              <li className="flex items-center gap-2"><MapPin size={15} /> 123 Đường Thể Thao, Q.1, TP.HCM</li>
            </ul>
          </div>

          <div>
            <p className="text-sm font-semibold text-white">Theo dõi chúng tôi</p>
            <div className="mt-3 flex gap-3">
              <a href="#" aria-label="Facebook" className="flex h-9 w-9 items-center justify-center rounded-lg bg-gray-800 hover:bg-primary-500">
                <MessageCircle size={16} />
              </a>
              <a href="#" aria-label="Instagram" className="flex h-9 w-9 items-center justify-center rounded-lg bg-gray-800 hover:bg-primary-500">
                <Camera size={16} />
              </a>
              <a href="#" aria-label="Youtube" className="flex h-9 w-9 items-center justify-center rounded-lg bg-gray-800 hover:bg-primary-500">
                <PlayCircle size={16} />
              </a>
            </div>
          </div>
        </div>

        <div className="mt-10 border-t border-gray-800 pt-6 text-center text-sm text-gray-500">
          © {new Date().getFullYear()} PowerFit Gym. All rights reserved.
        </div>
      </div>
    </footer>
  )
}

export default PublicFooter
