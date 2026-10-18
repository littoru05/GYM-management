import {
  DISCOUNT_TYPE,
  MEMBER_STATUS,
  STAFF_ROLE,
  TRANSACTION_TYPE,
  PAYMENT_METHOD,
} from '../utils/constants.js'
import { getMemberStatus } from '../utils/formatters.js'

export const memberships = [
  {
    id: 'MS1',
    name: 'Basic',
    durationMonths: 1,
    price: 500000,
    description: 'Gói tập linh hoạt cho người mới bắt đầu.',
    benefits: ['Sử dụng toàn bộ máy tập', 'Tủ locker miễn phí', 'Tư vấn tập luyện cơ bản'],
    status: 'ACTIVE',
    popular: false,
  },
  {
    id: 'MS2',
    name: 'Standard',
    durationMonths: 3,
    price: 1350000,
    description: 'Lựa chọn tiết kiệm cho hành trình 3 tháng.',
    benefits: ['Toàn bộ quyền lợi gói Basic', '1 buổi Personal Trainer', 'Tham gia lớp Group Class'],
    status: 'ACTIVE',
    popular: false,
  },
  {
    id: 'MS3',
    name: 'Premium',
    durationMonths: 6,
    price: 2400000,
    description: 'Gói phổ biến nhất với nhiều đặc quyền.',
    benefits: ['Toàn bộ quyền lợi gói Standard', '3 buổi Personal Trainer', 'Miễn phí nước uống', 'Ưu tiên đặt lịch lớp học'],
    status: 'ACTIVE',
    popular: true,
  },
  {
    id: 'MS4',
    name: 'VIP',
    durationMonths: 12,
    price: 4200000,
    description: 'Trải nghiệm cao cấp trọn năm, tiết kiệm tối đa.',
    benefits: ['Toàn bộ quyền lợi gói Premium', '10 buổi Personal Trainer', 'Khăn tập & đồ uống miễn phí', 'Locker VIP riêng', 'Ưu đãi sinh nhật'],
    status: 'ACTIVE',
    popular: false,
  },
]

export const categories = [
  { id: '1', code: 'CAT-BEVERAGE', name: 'Nước giải khát', description: 'Nước giải khát và đồ uống thể thao', status: 'ACTIVE' },
  { id: '2', code: 'CAT-SUPPLEMENT', name: 'Thực phẩm bổ sung', description: 'Whey, creatine và các sản phẩm hỗ trợ', status: 'ACTIVE' },
  { id: '3', code: 'CAT-ACCESSORY', name: 'Phụ kiện', description: 'Phụ kiện phục vụ tập luyện tại phòng gym', status: 'ACTIVE' },
]

export const products = [
  {
    id: '1',
    sku: 'SUP-WHEY-VAN-001',
    name: 'Whey Protein Vanilla 1kg',
    category: 'CAT-SUPPLEMENT',
    categoryName: 'Thực phẩm bổ sung',
    price: 1250000,
    cost: 820000,
    stockQuantity: 72,
    stock: 72,
    minStock: 10,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1593095948071-474c5cc2989d?auto=format&fit=crop&w=400&q=80',
  },
  {
    id: '2',
    sku: 'SUP-CREATINE-300-001',
    name: 'Creatine Monohydrate 300g',
    category: 'CAT-SUPPLEMENT',
    categoryName: 'Thực phẩm bổ sung',
    price: 650000,
    cost: 420000,
    stockQuantity: 54,
    stock: 54,
    minStock: 8,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1579722820308-d74e571900a9?auto=format&fit=crop&w=400&q=80',
  },
  {
    id: '3',
    sku: 'DRK-ELECTRO-ORANGE-001',
    name: 'Nước điện giải vị cam 500ml',
    category: 'CAT-BEVERAGE',
    categoryName: 'Nước giải khát',
    price: 22000,
    cost: 11000,
    stockQuantity: 100,
    stock: 100,
    minStock: 20,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1527661591475-527312dd65f5?auto=format&fit=crop&w=400&q=80',
  },
  {
    id: '4',
    sku: 'ACC-GLOVE-GYM-001',
    name: 'Găng tay tập gym chuyên dụng',
    category: 'CAT-ACCESSORY',
    categoryName: 'Phụ kiện',
    price: 350000,
    cost: 200000,
    stockQuantity: 50,
    stock: 50,
    minStock: 10,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?auto=format&fit=crop&w=400&q=80',
  },
  {
    id: '5',
    sku: 'ACC-SHAKER-700-001',
    name: 'Bình lắc protein 700ml',
    category: 'CAT-ACCESSORY',
    categoryName: 'Phụ kiện',
    price: 160000,
    cost: 90000,
    stockQuantity: 12,
    stock: 12,
    minStock: 12,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?auto=format&fit=crop&w=400&q=80',
  },
  {
    id: '6',
    sku: 'SUP-WHEY-CHOCO-001',
    name: 'Whey Protein Chocolate 1kg',
    category: 'CAT-SUPPLEMENT',
    categoryName: 'Thực phẩm bổ sung',
    price: 1240000,
    cost: 810000,
    stockQuantity: 4,
    stock: 4,
    minStock: 10,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1579722820308-d74e571900a9?auto=format&fit=crop&w=400&q=80',
  },
  {
    id: '7',
    sku: 'DRK-ELECTRO-LEMON-001',
    name: 'Nước điện giải vị chanh 500ml',
    category: 'CAT-BEVERAGE',
    categoryName: 'Nước giải khát',
    price: 22000,
    cost: 10000,
    stockQuantity: 15,
    stock: 15,
    minStock: 15,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=400&q=80',
  },
  {
    id: '8',
    sku: 'ACC-RESISTANCE-BAND-001',
    name: 'Dây kháng lực tập mông đùi',
    category: 'CAT-ACCESSORY',
    categoryName: 'Phụ kiện',
    price: 130000,
    cost: 70000,
    stockQuantity: 0,
    stock: 0,
    minStock: 5,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1598289431512-b97b0917affc?auto=format&fit=crop&w=400&q=80',
  },
  {
    id: '9',
    sku: 'DRK-WATER-500-001',
    name: 'Nước khoáng đóng chai 500ml',
    category: 'CAT-BEVERAGE',
    categoryName: 'Nước giải khát',
    price: 10000,
    cost: 5000,
    stockQuantity: 45,
    stock: 45,
    minStock: 10,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1548839140-29a749e1bc4e?auto=format&fit=crop&w=400&q=80',
  },
  {
    id: '10',
    sku: 'SUP-BAR-BANANA-001',
    name: 'Thanh Protein Bar vị chuối 60g',
    category: 'CAT-SUPPLEMENT',
    categoryName: 'Thực phẩm bổ sung',
    price: 45000,
    cost: 25000,
    stockQuantity: 0,
    stock: 0,
    minStock: 10,
    status: 'ACTIVE',
    image: 'https://images.unsplash.com/photo-1622484216864-b0a7dbf5d6f3?auto=format&fit=crop&w=400&q=80',
  },
]

export const staff = [
  {
    id: 'S001',
    name: 'Admin Quản Trị',
    email: 'admin@gym.com',
    phone: '0912345678',
    role: 'ADMIN',
    status: 'ACTIVE',
    createdDate: '2024-01-10',
    lastLogin: '2026-10-16T10:30:00Z',
  },
  {
    id: 'S002',
    name: 'Lê Thị Lễ Tân',
    email: 'staff@gym.com',
    phone: '0987654321',
    role: 'STAFF',
    status: 'ACTIVE',
    createdDate: '2024-02-15',
    lastLogin: '2026-10-16T08:15:00Z',
  },
  {
    id: 'S003',
    name: 'Nguyễn Văn Huấn',
    email: 'huan.nguyen@gym.com',
    phone: '0903123456',
    role: 'STAFF',
    status: 'ACTIVE',
    createdDate: '2024-03-01',
    lastLogin: '2026-10-14T17:45:00Z',
  },
  {
    id: 'S004',
    name: 'Trần Minh Đức',
    email: 'duc.tran@gym.com',
    phone: '0938456789',
    role: 'STAFF',
    status: 'LOCKED',
    createdDate: '2024-03-20',
    lastLogin: '2026-09-30T11:20:00Z',
  },
  {
    id: 'S005',
    name: 'Phạm Thu Hà',
    email: 'ha.pham@gym.com',
    phone: '0978901234',
    role: 'ADMIN',
    status: 'ACTIVE',
    createdDate: '2024-04-05',
    lastLogin: '2026-10-16T14:10:00Z',
  },
  {
    id: 'S006',
    name: 'Hoàng Quốc Việt',
    email: 'viet.hoang@gym.com',
    phone: '0965112233',
    role: 'STAFF',
    status: 'ACTIVE',
    createdDate: '2024-05-12',
    lastLogin: '2026-10-13T10:00:00Z',
  },
  {
    id: 'S007',
    name: 'Đỗ Thảo Vy',
    email: 'vy.do@gym.com',
    phone: '0944556677',
    role: 'STAFF',
    status: 'LOCKED',
    createdDate: '2024-06-18',
    lastLogin: '2026-08-25T16:00:00Z',
  },
]

function addDaysIso(days) {
  const d = new Date()
  d.setDate(d.getDate() + days)
  return d.toISOString().slice(0, 10)
}

export const promotions = [
  {
    id: 'V001',
    code: 'GYM10',
    name: 'Giảm 10% toàn bộ gói tập',
    description: 'Áp dụng cho mọi gói hội viên từ 1 tháng trở lên',
    discountType: DISCOUNT_TYPE.PERCENTAGE,
    discountValue: 10,
    startDate: addDaysIso(-30),
    endDate: addDaysIso(30),
    usageLimit: 100,
    usedCount: 42,
    status: 'ACTIVE',
  },
  {
    id: 'V002',
    code: 'SUMMER20',
    name: 'Khuyến mãi hè giảm 20%',
    description: 'Ưu đãi đăng ký gói tập mùa hè',
    discountType: DISCOUNT_TYPE.PERCENTAGE,
    discountValue: 20,
    startDate: addDaysIso(-10),
    endDate: addDaysIso(20),
    usageLimit: 50,
    usedCount: 50,
    status: 'ACTIVE',
  },
  {
    id: 'V003',
    code: 'NEWBIE50K',
    name: 'Giảm 50.000đ hội viên mới',
    description: 'Dành riêng cho khách hàng đăng ký lần đầu',
    discountType: DISCOUNT_TYPE.FIXED,
    discountValue: 50000,
    startDate: addDaysIso(-60),
    endDate: addDaysIso(-5),
    usageLimit: 200,
    usedCount: 180,
    status: 'ACTIVE',
  },
  {
    id: 'V004',
    code: 'VIP15',
    name: 'Giảm 15% gói VIP trọn năm',
    description: 'Áp dụng khi mua gói VIP 12 tháng',
    discountType: DISCOUNT_TYPE.PERCENTAGE,
    discountValue: 15,
    startDate: addDaysIso(5),
    endDate: addDaysIso(45),
    usageLimit: 30,
    usedCount: 0,
    status: 'ACTIVE',
  },
  {
    id: 'V005',
    code: 'RETAIL5',
    name: 'Giảm 5% hoá đơn phụ kiện',
    description: 'Áp dụng khi mua đồ uống và thực phẩm bổ sung',
    discountType: DISCOUNT_TYPE.PERCENTAGE,
    discountValue: 5,
    startDate: addDaysIso(-90),
    endDate: addDaysIso(90),
    usageLimit: 500,
    usedCount: 210,
    status: 'INACTIVE',
  },
  {
    id: 'V006',
    code: 'FIX100K',
    name: 'Giảm ngay 100.000đ',
    description: 'Ưu đãi tri ân khách hàng thân thiết',
    discountType: DISCOUNT_TYPE.FIXED,
    discountValue: 100000,
    startDate: addDaysIso(-5),
    endDate: addDaysIso(10),
    usageLimit: 20,
    usedCount: 5,
    status: 'ACTIVE',
  },
  {
    id: 'V007',
    code: 'POWER200K',
    name: 'Đại tiệc PowerFit giảm 200.000đ',
    description: 'Mã giảm giá khủng cho hoá đơn từ 1.000.000đ',
    discountType: DISCOUNT_TYPE.FIXED,
    discountValue: 200000,
    startDate: addDaysIso(-1),
    endDate: addDaysIso(60),
    usageLimit: 80,
    usedCount: 18,
    status: 'ACTIVE',
  },
]

// ---------------------------------------------------------------------------
// Deterministic generator helpers
// ---------------------------------------------------------------------------
function mulberry32(seed) {
  return function () {
    seed |= 0
    seed = (seed + 0x6d2b79f5) | 0
    let t = Math.imul(seed ^ (seed >>> 15), 1 | seed)
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296
  }
}
const rand = mulberry32(20261018)
const pick = (arr) => arr[Math.floor(rand() * arr.length)]
const randomInt = (min, max) => Math.floor(rand() * (max - min + 1)) + min

const FIRST_NAMES = ['Nguyễn', 'Trần', 'Lê', 'Phạm', 'Hoàng', 'Huỳnh', 'Phan', 'Vũ', 'Võ', 'Đặng', 'Bùi', 'Đỗ']

const MIDDLE_NAMES = ['Văn', 'Thị', 'Hữu', 'Đức', 'Minh', 'Thành', 'Ngọc', 'Quang', 'Thu', 'Anh']
const LAST_NAMES = ['An', 'Bình', 'Cường', 'Dũng', 'Duy', 'Giang', 'Hà', 'Hải', 'Hằng', 'Hiếu', 'Huy', 'Khang', 'Khánh', 'Linh', 'Long', 'Mai', 'Minh', 'Nam', 'Nga', 'Ngọc', 'Nhi', 'Phong', 'Phúc', 'Quân', 'Quỳnh', 'Sơn', 'Tâm', 'Thảo', 'Thắng', 'Trang', 'Trung', 'Tuấn', 'Tú', 'Uyên', 'Vy', 'Yến']

function randomName() {
  return `${pick(FIRST_NAMES)} ${pick(MIDDLE_NAMES)} ${pick(LAST_NAMES)}`
}

function randomPhone() {
  return `09${randomInt(10000000, 99999999)}`
}

// ---------------------------------------------------------------------------
// Members
// ---------------------------------------------------------------------------
export const members = Array.from({ length: 50 }, (_, i) => {
  const id = `M${String(i + 1).padStart(4, '0')}`
  const membership = pick(memberships)
  const joinOffset = randomInt(5, 360)
  const joinDate = addDaysIso(-joinOffset)
  const expiryOffset = randomInt(-20, membership.durationMonths * 30)
  const expiryDate = addDaysIso(expiryOffset)
  const name = randomName()
  return {
    id,
    name,
    phone: randomPhone(),
    email: `${name.toLowerCase().replace(/\s+/g, '.')}${i + 1}@gmail.com`,
    joinDate,
    membershipId: membership.id,
    membershipName: membership.name,
    expiryDate,
    status: getMemberStatus(expiryDate),
    avatar: `https://i.pravatar.cc/150?u=${id}`,
  }
})

// ---------------------------------------------------------------------------
// Transactions (membership sales + retail) over the past 365 days
// ---------------------------------------------------------------------------
export const transactions = Array.from({ length: 220 }, (_, i) => {
  // Biased distribution: more transactions in recent days/weeks
  const dice = rand()
  let daysAgo
  if (dice < 0.25) {
    daysAgo = randomInt(0, 7) // Last 7 days
  } else if (dice < 0.55) {
    daysAgo = randomInt(8, 30) // Last 30 days
  } else if (dice < 0.85) {
    daysAgo = randomInt(31, 180) // Last 6 months
  } else {
    daysAgo = randomInt(181, 360) // Older up to 12 months
  }

  const date = addDaysIso(-daysAgo)
  const isMembership = rand() < 0.45
  const type = isMembership ? TRANSACTION_TYPE.MEMBERSHIP : TRANSACTION_TYPE.RETAIL
  const member = pick(members)
  const staffMember = pick(staff.filter((s) => s.role === STAFF_ROLE.STAFF) || staff)

  let description
  let amount
  if (isMembership) {
    const pkg = pick(memberships)
    description = `Đăng ký gói ${pkg.name} (${pkg.durationMonths} tháng)`
    amount = pkg.price
  } else {
    const prod = pick(products)
    const qty = randomInt(1, 4)
    description = `Mua lẻ ${qty}x ${prod.name}`
    amount = prod.price * qty
  }

  return {
    id: `TX${String(i + 1).padStart(4, '0')}`,
    date,
    type,
    memberId: member.id,
    memberName: member.name,
    description,
    amount,
    paymentMethod: pick([PAYMENT_METHOD.CASH, PAYMENT_METHOD.BANK_TRANSFER, PAYMENT_METHOD.CARD]),
    staffName: staffMember?.name || 'Lê Thị Lễ Tân',
  }
}).sort((a, b) => new Date(b.date) - new Date(a.date))

// ---------------------------------------------------------------------------
// Invoices
// ---------------------------------------------------------------------------
export const invoices = transactions.slice(0, 80).map((tx, i) => ({
  id: `INV${String(i + 1).padStart(4, '0')}`,
  transactionId: tx.id,
  date: tx.date,
  memberName: tx.memberName,
  items: [{ name: tx.description, quantity: 1, price: tx.amount }],
  total: tx.amount,
  paymentMethod: tx.paymentMethod,
}))

// ---------------------------------------------------------------------------
// Check-ins over the last 14 days
// ---------------------------------------------------------------------------
export const checkIns = Array.from({ length: 120 }, (_, i) => {
  const member = pick(members)
  const daysAgo = randomInt(0, 14)
  const date = addDaysIso(-daysAgo)
  const hour = randomInt(6, 21)
  const minute = randomInt(0, 59)
  return {
    id: `CI${String(i + 1).padStart(4, '0')}`,
    memberId: member.id,
    memberName: member.name,
    avatar: member.avatar,
    membershipName: member.membershipName,
    time: `${date}T${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:00`,
    status: member.status === MEMBER_STATUS.EXPIRED ? 'DENIED' : 'SUCCESS',
  }
}).sort((a, b) => new Date(b.time) - new Date(a.time))

// ---------------------------------------------------------------------------
// Prebuilt Revenue helpers
// ---------------------------------------------------------------------------
export function buildDailyRevenue(days = 30) {
  return Array.from({ length: days }, (_, i) => {
    const date = addDaysIso(-(days - 1 - i))
    const dayTx = transactions.filter((t) => t.date === date)
    const membershipRevenue = dayTx
      .filter((t) => t.type === TRANSACTION_TYPE.MEMBERSHIP)
      .reduce((s, t) => s + t.amount, 0)
    const retailRevenue = dayTx
      .filter((t) => t.type === TRANSACTION_TYPE.RETAIL)
      .reduce((s, t) => s + t.amount, 0)
    return {
      date: date.slice(5).replace('-', '/'), // MM/DD
      rawDate: date,
      membershipRevenue,
      retailRevenue,
      total: membershipRevenue + retailRevenue,
      transactionCount: dayTx.length,
    }
  })
}

export function buildMonthlyRevenue(months = 12) {
  return Array.from({ length: months }, (_, i) => {
    const d = new Date()
    d.setDate(1)
    d.setMonth(d.getMonth() - (months - 1 - i))
    const yearMonth = d.toISOString().slice(0, 7) // YYYY-MM
    const label = `${d.getMonth() + 1}/${d.getFullYear()}`

    const monthTx = transactions.filter((t) => t.date.startsWith(yearMonth))
    const membershipRevenue = monthTx
      .filter((t) => t.type === TRANSACTION_TYPE.MEMBERSHIP)
      .reduce((s, t) => s + t.amount, 0)
    const retailRevenue = monthTx
      .filter((t) => t.type === TRANSACTION_TYPE.RETAIL)
      .reduce((s, t) => s + t.amount, 0)

    return {
      date: label,
      rawDate: yearMonth,
      membershipRevenue,
      retailRevenue,
      total: membershipRevenue + retailRevenue,
      transactionCount: monthTx.length,
    }
  })
}

export const revenue = {
  daily: buildDailyRevenue(30),
  weekly: buildDailyRevenue(7),
  monthly: buildMonthlyRevenue(12),
}

