import {
  Dumbbell,
  HeartPulse,
  UserCheck,
  Flame,
  Users2,
  Lock,
  ShowerHead,
  ParkingSquare,
  Wifi,
  Droplets,
  Sparkles,
} from 'lucide-react'

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
    id: 'gym-fitness',
    title: 'Thể hình tự do (Gym Fitness)',
    shortTitle: 'Gym Fitness',
    category: 'gym',
    tagline: 'Chủ động tập luyện - Tối ưu sức mạnh & Thể hình',
    badge: 'Linh hoạt 24/7',
    badgeColor: 'orange',
    icon: Dumbbell,
    image: 'https://images.unsplash.com/photo-1534438327276-14e5300c3a48?auto=format&fit=crop&w=800&q=80',
    imageAlt: 'Không gian máy tập và khu tạ tự do chuẩn quốc tế tại PowerFit',
    description:
      'Không gian tập luyện rộng 1.000m² với hơn 80+ dàn máy tập nhập khẩu chuẩn Olympic, hỗ trợ đầy đủ từ bài tập tạ tự do đến máy khối kháng lực chuyên sâu.',
    method: {
      summary: 'Ứng dụng nguyên lý Hypertrophy và Progressive Overload (tăng tiến mức tạ có kiểm soát).',
      details:
        'Hội viên tự do ứng dụng các lịch tập khoa học như Push-Pull-Legs hoặc Upper-Lower kết hợp giữa tạ đơn/tạ đòn Olympic và hệ thống máy khối kháng lực chuyên sâu. Giúp kích thích tối đa sợi cơ phát triển, tăng cường mật độ xương và nâng cao chỉ số chuyển hóa năng lượng tự nhiên (BMR).',
    },
    suitableFor: [
      'Người muốn chủ động thời gian luyện tập linh hoạt theo lịch cá nhân',
      'Hội viên đã có kỹ thuật cơ bản hoặc muốn tập trung tăng cơ, giảm mỡ',
      'Học sinh, sinh viên, nhân viên văn phòng yêu thích lối sống thể thao năng động',
    ],
    amenities: [
      { id: 'shower', name: 'Phòng tắm nóng lạnh', icon: ShowerHead, desc: 'Vòi sen riêng tư, máy sấy tóc & sữa tắm cao cấp' },
      { id: 'locker', name: 'Tủ locker thông minh', icon: Lock, desc: 'Khóa từ cảm ứng an toàn bảo mật tuyệt đối' },
      { id: 'water', name: 'Nước uống miễn phí', icon: Droplets, desc: 'Cây lọc nước ion kiềm & nước mát bổ sung liên tục' },
      { id: 'wifi', name: 'Wifi & Khăn tập sạch', icon: Wifi, desc: 'Wifi tốc độ cao và khăn tập kháng khuẩn miễn phí' },
    ],
    equipment: ['Dàn tạ đòn & tạ đơn Olympic (1kg - 50kg)', 'Dàn cáp trợ lực đa năng 8 vị trí', 'Hệ thống máy khối cao cấp nhập khẩu Mỹ', 'Khu Power Rack & Deadlift tiêu chuẩn'],
    stats: [
      { label: 'Diện tích sàn', value: '1.000m²' },
      { label: 'Thiết bị nhập khẩu', value: '80+ Máy' },
      { label: 'Thời gian mở cửa', value: '05:30 - 22:00' },
    ],
    link: '/contact?service=gym-fitness',
  },
  {
    id: 'personal-trainer',
    title: 'Huấn luyện cá nhân 1-1 (Personal Trainer)',
    shortTitle: 'Personal Trainer',
    category: 'pt',
    tagline: 'Lộ trình độc bản - Cam kết đạt mục tiêu gấp 3 lần',
    badge: 'Hiệu quả tối đa',
    badgeColor: 'amber',
    icon: UserCheck,
    image: 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?auto=format&fit=crop&w=800&q=80',
    imageAlt: 'Huấn luyện viên cá nhân 1 kèm 1 đồng hành hướng dẫn kỹ thuật',
    description:
      'Chương trình huấn luyện kèm cặp 1 kèm 1 cùng đội ngũ HLV chứng chỉ quốc tế (NASM/ACE), thiết kế giáo án và chế độ dinh dưỡng độc bản cho từng học viên.',
    method: {
      summary: 'Khởi đầu với phân tích chỉ số InBody 770 chuyên sâu và kiểm tra tầm vận động FMS.',
      details:
        'Huấn luyện viên cá nhân thiết kế giáo án độc bản 1-1, đồng hành sửa từng nhịp thở và góc nâng tạ để loại bỏ 100% rủi ro chấn thương. Cập nhật và đánh giá tiến độ định kỳ mỗi 2 tuần, đồng thời tư vấn thực đơn dinh dưỡng định lượng Macro (Đạm - Bột đường - Chất béo) phù hợp từng giai đoạn.',
    },
    suitableFor: [
      'Người mới bắt đầu chưa tự tin vào kỹ thuật hoặc sợ chấn thương khi tập nặng',
      'Người cần giảm cân, siết eo cấp tốc cho sự kiện hoặc chững cân lâu ngày',
      'Người gặp vấn đề cột sống, gù lưng, võng lưng hoặc cần phục hồi vận động an toàn',
    ],
    amenities: [
      { id: 'shower', name: 'Phòng tắm nóng lạnh', icon: ShowerHead, desc: 'Phòng tắm riêng biệt tiện nghi cao cấp' },
      { id: 'locker', name: 'Tủ locker VIP riêng', icon: Lock, desc: 'Tủ locker VIP cá nhân bảo mật cao cấp' },
      { id: 'water', name: 'Nước uống bù khoáng', icon: Droplets, desc: 'Nước khoáng bù điện giải và năng lượng tức thì' },
      { id: 'inbody', name: 'Đo InBody định kỳ', icon: Sparkles, desc: 'Đo chỉ số cơ mỡ & tư vấn dinh dưỡng 2 tuần/lần miễn phí' },
    ],
    equipment: ['Thiết bị đo chỉ số InBody 770 chuyên sâu', 'Dụng cụ chỉnh sửa tư thế và giải cơ chuyên nghiệp', 'Phụ kiện tập luyện chuyên biệt 1-1', 'Bảng theo dõi nhật ký thể lực cá nhân hóa'],
    stats: [
      { label: 'HLV chứng chỉ', value: '100% NASM/ACE' },
      { label: 'Cá nhân hóa', value: '100% Độc bản' },
      { label: 'Tỷ lệ đạt mục tiêu', value: '98.5%' },
    ],
    link: '/contact?service=personal-trainer',
  },
  {
    id: 'yoga-stretching',
    title: 'Lớp Yoga & Giãn cơ',
    shortTitle: 'Yoga & Giãn cơ',
    category: 'yoga',
    tagline: 'Cân bằng thân tâm - Phục hồi cơ thể & Giảm căng thẳng',
    badge: 'Phục hồi & Thư giãn',
    badgeColor: 'emerald',
    icon: HeartPulse,
    image: 'https://images.unsplash.com/photo-1545205597-3d9d02c29597?auto=format&fit=crop&w=800&q=80',
    imageAlt: 'Studio lớp Yoga và giãn cơ tĩnh tâm tràn ngập ánh sáng tự nhiên',
    description:
      'Không gian Studio yên tĩnh, cách âm tiêu chuẩn cùng các lớp Yoga đa dạng giúp kéo giãn nhóm cơ bị co rút, giảm đau mỏi cột sống và tái tạo năng lượng.',
    method: {
      summary: 'Hòa quyện giữa trường phái Hatha/Vinyasa Yoga và giải phóng cơ mạc Myofascial Release.',
      details:
        'Tập trung kiểm soát hơi thở Pranayama kết hợp các chuỗi Asana và bài tập kéo giãn cơ sâu. Phương pháp giúp giải phóng các điểm căng cứng (trigger points), giảm áp lực chèn ép lên đĩa đệm cột sống, tăng cường độ đàn hồi của dây chằng và kích hoạt hệ thần kinh phó giao cảm giúp tinh thần thư thái.',
    },
    suitableFor: [
      'Dân văn phòng ngồi nhiều bị đau mỏi cổ vai gáy, cứng khớp hoặc thoái hóa nhẹ',
      'Người chịu áp lực công việc lớn, căng thẳng tinh thần, khó ngủ kéo dài',
      'Người mong muốn nâng cao sự dẻo dai cơ thể, cân bằng dáng vóc thon gọn',
    ],
    amenities: [
      { id: 'shower', name: 'Phòng tắm nóng lạnh', icon: ShowerHead, desc: 'Vòi sen thảo dược thư giãn tinh thần sau buổi tập' },
      { id: 'locker', name: 'Tủ locker thông minh', icon: Lock, desc: 'Tủ khóa từ an toàn, bảo vệ tư trang trọn vẹn' },
      { id: 'water', name: 'Trà thảo mộc & Nước ấm', icon: Droplets, desc: 'Cung cấp trà thảo mộc thanh nhiệt và nước ấm miễn phí' },
      { id: 'mat', name: 'Thảm & dụng cụ cao cấp', icon: Sparkles, desc: 'Thảm TPE kháng khuẩn, gạch yoga và dây đai sẵn có' },
    ],
    equipment: ['Studio sàn gỗ tiêu chuẩn chống sốc', 'Thảm tập Manduka/Liforme cao cấp', 'Gạch xốp, dây đai hỗ trợ giãn cơ', 'Hệ thống tinh dầu khuếch tán tự nhiên'],
    stats: [
      { label: 'Số lớp mỗi tuần', value: '25+ Lớp' },
      { label: 'Cấp độ lớp', value: 'Cơ bản - Nâng cao' },
      { label: 'Quy mô học viên', value: '12 - 15 người' },
    ],
    link: '/contact?service=yoga-stretching',
  },
  {
    id: 'kickboxing-cardio',
    title: 'Kickboxing / Cardio đốt mỡ',
    shortTitle: 'Kickboxing & Cardio',
    category: 'cardio',
    tagline: 'Đốt mỡ siêu tốc - Bùng nổ năng lượng & Phản xạ tự vệ',
    badge: 'Đốt 600 - 900 kcal',
    badgeColor: 'rose',
    icon: Flame,
    image: 'https://images.unsplash.com/photo-1549060279-7e168fcee0c2?auto=format&fit=crop&w=800&q=80',
    imageAlt: 'Khu vực sàn đấu kickboxing và bao cát tập luyện cardio đốt mỡ',
    description:
      'Sự kết hợp bùng nổ giữa đòn thế đấm đá võ thuật đối kháng và chuỗi bài tập Cardio ngắt quãng cường độ cao (HIIT/Tabata), đốt mỡ triệt để.',
    method: {
      summary: 'Kỹ thuật võ thuật thực chiến kết hợp Cardio ngắt quãng cường độ cao (HIIT/Tabata).',
      details:
        'Rèn luyện các tổ hợp đòn đấm (Punch), đòn đá (Kick) và đòn gối kết hợp chuỗi bài tập dây thừng lực (Battle Rope), tạ chuông (Kettlebell) và bước nhảy tốc độ. Kích hoạt hiệu ứng tiêu thụ oxy dư thừa sau tập (EPOC), giúp cơ thể tiếp tục đốt mỡ liên tục suốt 12 - 24 giờ sau khi buổi tập kết thúc.',
    },
    suitableFor: [
      'Người muốn giảm mỡ toàn thân cấp tốc, săn chắc cơ bắp vùng bụng, tay và đùi',
      'Người cần xả stress cực đỉnh, giải phóng năng lượng tiêu cực sau giờ làm',
      'Người yêu thích rèn luyện kỹ năng tự vệ thực chiến và nâng cao phản xạ linh hoạt',
    ],
    amenities: [
      { id: 'shower', name: 'Phòng tắm nóng lạnh', icon: ShowerHead, desc: 'Vòi sen áp lực cao xua tan mệt mỏi tức thì' },
      { id: 'locker', name: 'Tủ locker thông minh', icon: Lock, desc: 'Khóa từ cảm ứng an toàn bảo mật tuyệt đối' },
      { id: 'water', name: 'Nước điện giải thể thao', icon: Droplets, desc: 'Trạm nước bổ sung điện giải và muối khoáng lạnh' },
      { id: 'gear', name: 'Găng & băng quấn tay', icon: Sparkles, desc: 'Trang bị găng boxing Fairtex và băng quấn sạch khuẩn' },
    ],
    equipment: ['Hệ thống bao cát Heavy Bag treo chịu lực', 'Võ đài Boxing/Kickboxing mini chuẩn tập luyện', 'Dây thừng thể lực Battle Rope 15m', 'Hệ thống đệm đỡ đòn Focus Mitts & Thai Pads'],
    stats: [
      { label: 'Tiêu hao calo', value: '600 - 900 kcal' },
      { label: 'Hiệu ứng đốt mỡ', value: 'EPOC 24 giờ' },
      { label: 'Thời lượng buổi tập', value: '45 - 60 Phút' },
    ],
    link: '/contact?service=kickboxing-cardio',
  },
]

export const highlights = [
  { title: '10+ năm kinh nghiệm', description: 'Đồng hành cùng hàng ngàn hội viên trên hành trình rèn luyện sức khỏe.' },
  { title: 'Đội ngũ HLV chuyên nghiệp', description: 'Huấn luyện viên được chứng nhận quốc tế, tận tâm với từng học viên.' },
  { title: 'Trang thiết bị hiện đại', description: 'Đầu tư máy móc nhập khẩu, bảo trì định kỳ, an toàn tuyệt đối.' },
  { title: 'Không gian đẳng cấp', description: 'Thiết kế hiện đại, thoáng mát, tạo cảm hứng cho mỗi buổi tập.' },
]
