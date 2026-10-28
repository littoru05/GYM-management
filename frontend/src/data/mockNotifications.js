/**
 * Mock data danh sách liên hệ / thông báo mới nhất cho hệ thống gym
 * Bao gồm tên khách hàng, số điện thoại, email, nội dung lời nhắn, thời gian và trạng thái đọc
 */
export const initialMockNotifications = [
  {
    id: 1,
    name: 'Nguyễn Văn Minh',
    phone: '0987654321',
    email: 'minh.nguyen@gmail.com',
    message: 'Chào PowerFit Gym, mình muốn tìm hiểu về gói tập VIP kèm PT cá nhân 1-1 và lịch tập các buổi tối sau 18h. Mong trung tâm tư vấn sớm giúp mình qua Zalo hoặc gọi trực tiếp nhé!',
    createdAt: new Date(Date.now() - 5 * 60 * 1000).toISOString(), // 5 phút trước
    isRead: false,
    status: 'NEW',
  },
  {
    id: 2,
    name: 'Trần Thị Thu Hà',
    phone: '0912345678',
    email: 'thuha.tran@outlook.com',
    message: 'Em muốn đăng ký tập thử 3 ngày trải nghiệm phòng gym và lớp Yoga bên mình. Cho em hỏi các cơ sở có mở cửa vào thứ 7 và Chủ Nhật không ạ?',
    createdAt: new Date(Date.now() - 25 * 60 * 1000).toISOString(), // 25 phút trước
    isRead: false,
    status: 'NEW',
  },
  {
    id: 3,
    name: 'Lê Hoàng Nam',
    phone: '0903123456',
    email: 'nam.le@company.vn',
    message: 'Công ty chúng tôi có khoảng 25 nhân sự muốn đăng ký gói tập doanh nghiệp hàng tháng. Xin vui lòng gửi báo giá và chính sách ưu đãi chiết khấu qua email.',
    createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(), // 2 giờ trước
    isRead: false,
    status: 'NEW',
  },
  {
    id: 4,
    name: 'Phạm Quỳnh Anh',
    phone: '0978901234',
    email: 'quynhanh.pham@yahoo.com',
    message: 'Gói Standard 3 tháng hiện tại có được tặng bình nước hoặc áp dụng voucher giảm giá không ạ? Mình đã tập ở cơ sở khác trước đây.',
    createdAt: new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString(), // 1 ngày trước
    isRead: true,
    status: 'CONTACTED',
  },
  {
    id: 5,
    name: 'Đặng Tuấn Kiệt',
    phone: '0934567890',
    email: 'kiet.dang@gmail.com',
    message: 'Tư vấn giúp em lịch đo InBody miễn phí và tư vấn dinh dưỡng cho người muốn tăng cân, tăng cơ trong vòng 6 tháng tới.',
    createdAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000).toISOString(), // 3 ngày trước
    isRead: true,
    status: 'RESOLVED',
  },
]
