/**
 * 4 lựa chọn dịch vụ tư vấn chủ đạo của PowerFit Gym
 */
export const SERVICE_OPTIONS = [
  'Gói tập dài hạn (6-12 tháng)',
  'Thuê HLV cá nhân 1-1',
  'Trải nghiệm tập thử',
  'Đăng ký lớp Yoga/GroupX',
]

/**
 * Mapping thông minh từ thẻ gói tập sang dịch vụ quan tâm tương ứng:
 * - Gói dài hạn (>= 6 tháng như Premium, VIP/Platinum): "Gói tập dài hạn (6-12 tháng)"
 * - Gói có PT 1-1 chuyên sâu: "Thuê HLV cá nhân 1-1"
 * - Gói Yoga/GroupX: "Đăng ký lớp Yoga/GroupX"
 * - Gói ngắn hạn (<= 3 tháng như Basic, Standard) hoặc trải nghiệm: "Trải nghiệm tập thử"
 */
export function mapPackageToService(pkg) {
  if (!pkg) return ''
  const name = (pkg.name || '').toLowerCase()
  const durationMonths = Number(pkg.durationMonths) || 0

  // 1. Gói thời hạn từ 6 tháng trở lên (Premium 6 tháng, VIP / Platinum 12 tháng)
  if (durationMonths >= 6 || name.includes('premium') || name.includes('vip') || name.includes('platinum')) {
    return 'Gói tập dài hạn (6-12 tháng)'
  }

  // 2. Tên gói nhấn mạnh PT / HLV cá nhân
  if (name.includes('pt') || name.includes('personal trainer') || name.includes('huấn luyện')) {
    return 'Thuê HLV cá nhân 1-1'
  }

  // 3. Tên gói nhấn mạnh Yoga hoặc GroupX
  if (name.includes('yoga') || name.includes('groupx') || name.includes('group x') || name.includes('lớp')) {
    return 'Đăng ký lớp Yoga/GroupX'
  }

  // 4. Các gói ngắn hạn (1 - 3 tháng) hoặc tập thử
  if (durationMonths <= 1 || name.includes('basic') || name.includes('thử') || name.includes('trải nghiệm')) {
    return 'Trải nghiệm tập thử'
  }

  // Mặc định
  return 'Trải nghiệm tập thử'
}

/**
 * Mapping từ URL params (service/package) sang dịch vụ quan tâm
 */
export function mapParamsToService(serviceParam, packageParam) {
  if (packageParam) {
    return mapPackageToService({ name: packageParam })
  }

  switch (serviceParam) {
    case 'personal-trainer':
      return 'Thuê HLV cá nhân 1-1'
    case 'yoga-stretching':
    case 'kickboxing-cardio':
      return 'Đăng ký lớp Yoga/GroupX'
    case 'gym-fitness':
      return 'Trải nghiệm tập thử'
    default:
      return ''
  }
}

