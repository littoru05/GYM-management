import { Dumbbell, HeartPulse, UserCheck, Users2, Lock, ShowerHead, ParkingSquare, Wifi } from 'lucide-react'

export const heroImage = 'https://picsum.photos/seed/gym-hero/1600/900'
export const aboutImage = 'https://picsum.photos/seed/gym-about/900/1000'

export const amenities = [
  { icon: Dumbbell, title: 'Phòng tập hiện đại' },
  { icon: HeartPulse, title: 'Lớp Yoga' },
  { icon: UserCheck, title: 'Personal Trainer' },
  { icon: Lock, title: 'Locker cá nhân' },
  { icon: ShowerHead, title: 'Phòng tắm tiện nghi' },
  { icon: ParkingSquare, title: 'Bãi đỗ xe rộng rãi' },
  { icon: Wifi, title: 'Wifi tốc độ cao' },
  { icon: Users2, title: 'Cộng đồng tập luyện' },
]

export const services = [
  {
    id: 'gym',
    icon: Dumbbell,
    title: 'Gym',
    description: 'Hệ thống máy tập nhập khẩu, không gian rộng rãi, thoáng mát cho mọi cấp độ.',
    image: 'https://picsum.photos/seed/gym-service-1/600/400',
  },
  {
    id: 'yoga',
    icon: HeartPulse,
    title: 'Yoga',
    description: 'Các lớp Yoga đa dạng giúp cải thiện sự dẻo dai và cân bằng tinh thần.',
    image: 'https://picsum.photos/seed/gym-service-2/600/400',
  },
  {
    id: 'pt',
    icon: UserCheck,
    title: 'Personal Training',
    description: 'Huấn luyện viên cá nhân xây dựng giáo án riêng theo mục tiêu của bạn.',
    image: 'https://picsum.photos/seed/gym-service-3/600/400',
  },
  {
    id: 'group',
    icon: Users2,
    title: 'Group Classes',
    description: 'Các lớp học nhóm sôi động: HIIT, Zumba, Boxing, Cycling...',
    image: 'https://picsum.photos/seed/gym-service-4/600/400',
  },
]

export const highlights = [
  { title: '10+ năm kinh nghiệm', description: 'Đồng hành cùng hàng ngàn hội viên trên hành trình rèn luyện sức khỏe.' },
  { title: 'Đội ngũ HLV chuyên nghiệp', description: 'Huấn luyện viên được chứng nhận quốc tế, tận tâm với từng học viên.' },
  { title: 'Trang thiết bị hiện đại', description: 'Đầu tư máy móc nhập khẩu, bảo trì định kỳ, an toàn tuyệt đối.' },
  { title: 'Không gian đẳng cấp', description: 'Thiết kế hiện đại, thoáng mát, tạo cảm hứng cho mỗi buổi tập.' },
]
