import assert from 'node:assert'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

console.log('--- STARTING UC01 MEMBERSHIP CARD & CONSULTATION TESTS ---')

// 1. Kiểm tra mockData gói tập (UC01)
console.log('\n[TEST GROUP 1] Kiểm tra danh sách quyền lợi cụ thể cho từng gói tập:')
const { memberships } = await import('./src/data/mockData.js')

assert.ok(Array.isArray(memberships) && memberships.length >= 4, 'Phải có ít nhất 4 gói tập')

const vipPkg = memberships.find((m) => m.name.toLowerCase().includes('vip'))
assert.ok(vipPkg, 'Phải có gói VIP / Platinum')
assert.ok(vipPkg.inbodySessions, 'Gói VIP/Platinum phải có thông tin buổi đo InBody')
assert.ok(vipPkg.ptSessions, 'Gói VIP/Platinum phải có số buổi PT định hướng')
assert.strictEqual(vipPkg.saunaAccess, true, 'Gói VIP/Platinum phải có quyền sử dụng phòng xông hơi Sauna')

console.log(`✓ PASS: Gói VIP/Platinum: ${vipPkg.inbodySessions} InBody, ${vipPkg.ptSessions} PT, Sauna: ${vipPkg.saunaLabel}`)

memberships.forEach((pkg) => {
  assert.ok(pkg.name, 'Mỗi gói tập phải có tên')
  assert.ok(pkg.price > 0, `Gói ${pkg.name} phải có giá hợp lệ`)
  assert.ok(pkg.inbodySessions, `Gói ${pkg.name} phải có quyền lợi đo InBody`)
  assert.ok(pkg.ptSessions, `Gói ${pkg.name} phải có quyền lợi HLV/PT định hướng`)
  assert.ok(typeof pkg.saunaAccess === 'boolean', `Gói ${pkg.name} phải có cờ quyền lợi xông hơi Sauna`)
  assert.ok(Array.isArray(pkg.benefits) && pkg.benefits.length >= 3, `Gói ${pkg.name} phải có danh sách quyền lợi chi tiết`)
  console.log(`✓ PASS: Gói "${pkg.name}" (${pkg.durationMonths} tháng) đầy đủ thông số quyền lợi cụ thể`)
})

// 2. Kiểm tra Component MembershipCard.jsx
console.log('\n[TEST GROUP 2] Kiểm tra Component MembershipCard.jsx:')
const membershipCardCode = fs.readFileSync(path.join(__dirname, 'src/components/common/MembershipCard.jsx'), 'utf-8')

assert.ok(membershipCardCode.includes('Đo chỉ số InBody'), 'MembershipCard phải hiển thị khối Đo chỉ số InBody')
assert.ok(membershipCardCode.includes('HLV định hướng (PT)'), 'MembershipCard phải hiển thị khối HLV định hướng')
assert.ok(membershipCardCode.includes('Phòng xông hơi Sauna'), 'MembershipCard phải hiển thị khối Phòng xông hơi Sauna')
assert.ok(membershipCardCode.includes('Nhận tư vấn gói này'), 'MembershipCard phải có nút CTA "Nhận tư vấn gói này"')
assert.ok(membershipCardCode.includes('handleConsultClick'), 'MembershipCard phải xử lý click cuộn tới form liên hệ')
console.log('✓ PASS: MembershipCard.jsx hiển thị đầy đủ quyền lợi InBody, PT, Sauna và nút "Nhận tư vấn gói này"')

// 3. Kiểm tra Component PackageConsultationForm.jsx & Dropdown Dịch vụ quan tâm
console.log('\n[TEST GROUP 3] Kiểm tra Component PackageConsultationForm.jsx & Dropdown Dịch vụ quan tâm:')
const formCode = fs.readFileSync(path.join(__dirname, 'src/components/public/PackageConsultationForm.jsx'), 'utf-8')
assert.ok(formCode.includes('id="contact-form"'), 'Form tư vấn phải có id="contact-form" để scroll target')
assert.ok(formCode.includes('currentPackage') || formCode.includes('selectedPackage'), 'Form tư vấn phải quản lý gói tập được chọn')
assert.ok(formCode.includes('Gói tập bạn đã chọn tư vấn') || formCode.includes('Gói quan tâm'), 'Form tư vấn phải hiển thị gói đã chọn')
assert.ok(formCode.includes('isSubmittingRef'), 'Form tư vấn phải có guard chống spam double-click')

// Kiểm tra 4 dịch vụ trong Dropdown "Dịch vụ bạn quan tâm"
const { SERVICE_OPTIONS, mapPackageToService } = await import('./src/utils/consultationHelpers.js')
assert.ok(Array.isArray(SERVICE_OPTIONS), 'SERVICE_OPTIONS phải là một mảng')
assert.strictEqual(SERVICE_OPTIONS.length, 4, 'Phải có đúng 4 tùy chọn dịch vụ quan tâm')
assert.ok(SERVICE_OPTIONS.includes('Gói tập dài hạn (6-12 tháng)'), 'Phải có option "Gói tập dài hạn (6-12 tháng)"')
assert.ok(SERVICE_OPTIONS.includes('Thuê HLV cá nhân 1-1'), 'Phải có option "Thuê HLV cá nhân 1-1"')
assert.ok(SERVICE_OPTIONS.includes('Trải nghiệm tập thử'), 'Phải có option "Trải nghiệm tập thử"')
assert.ok(SERVICE_OPTIONS.includes('Đăng ký lớp Yoga/GroupX'), 'Phải có option "Đăng ký lớp Yoga/GroupX"')

// Kiểm tra tự động bắt sự kiện và mapping từ bảng gói tập
const premiumService = mapPackageToService({ name: 'Premium', durationMonths: 6 })
assert.strictEqual(premiumService, 'Gói tập dài hạn (6-12 tháng)', 'Gói 6 tháng phải map sang "Gói tập dài hạn (6-12 tháng)"')

const vipService = mapPackageToService({ name: 'VIP / Platinum', durationMonths: 12 })
assert.strictEqual(vipService, 'Gói tập dài hạn (6-12 tháng)', 'Gói VIP 12 tháng phải map sang "Gói tập dài hạn (6-12 tháng)"')

const basicService = mapPackageToService({ name: 'Basic', durationMonths: 1 })
assert.strictEqual(basicService, 'Trải nghiệm tập thử', 'Gói Basic 1 tháng phải map sang "Trải nghiệm tập thử"')

const ptService = mapPackageToService({ name: 'Gói PT 1-1 Chuyên Sâu', durationMonths: 1 })
assert.strictEqual(ptService, 'Thuê HLV cá nhân 1-1', 'Gói PT phải map sang "Thuê HLV cá nhân 1-1"')

const yogaService = mapPackageToService({ name: 'Gói Yoga & GroupX Unlimited', durationMonths: 1 })
assert.strictEqual(yogaService, 'Đăng ký lớp Yoga/GroupX', 'Gói Yoga phải map sang "Đăng ký lớp Yoga/GroupX"')

// Kiểm tra validation bắt buộc chọn dịch vụ và SĐT hợp lệ
assert.ok(formCode.includes('Dịch vụ bạn quan tâm'), 'Form phải có nhãn "Dịch vụ bạn quan tâm"')
assert.ok(formCode.includes('Vui lòng chọn dịch vụ bạn quan tâm'), 'Schema phải validate bắt buộc chọn dịch vụ')
assert.ok(formCode.includes('Số điện thoại phải gồm 10 chữ số'), 'Schema phải validate SĐT Việt Nam 10 chữ số')
assert.ok(formCode.includes('formattedPayload') && formCode.includes('service: values.service'), 'Payload phải đóng gói service để gửi lên Backend')
console.log('✓ PASS: PackageConsultationForm.jsx tích hợp chuẩn dropdown 4 dịch vụ, auto-map từ gói tập và validate SĐT/dịch vụ')

// 4. Kiểm tra tích hợp trên PricingPage.jsx & HomePage.jsx
console.log('\n[TEST GROUP 4] Kiểm tra tích hợp trên PricingPage.jsx & HomePage.jsx:')
const pricingPageCode = fs.readFileSync(path.join(__dirname, 'src/pages/public/PricingPage.jsx'), 'utf-8')
assert.ok(pricingPageCode.includes('PackageConsultationForm'), 'PricingPage phải tích hợp PackageConsultationForm')
assert.ok(pricingPageCode.includes('handleConsultPackage'), 'PricingPage phải có handler tự động cuộn xuống form liên hệ')

const homePageCode = fs.readFileSync(path.join(__dirname, 'src/pages/public/HomePage.jsx'), 'utf-8')
assert.ok(homePageCode.includes('PackageConsultationForm'), 'HomePage phải tích hợp PackageConsultationForm')
assert.ok(homePageCode.includes('handleConsultPackage'), 'HomePage phải có handler tự động cuộn xuống form liên hệ')

console.log('✓ PASS: Cả PricingPage.jsx và HomePage.jsx đã tích hợp form tư vấn và cơ chế tự động cuộn gán gói')

console.log('\n========================================================================')
console.log('🎉 TẤT CẢ TEST UC01 MEMBERSHIP CARD & FORM TƯ VẤN ĐÃ PASS 100%!')
console.log('========================================================================\n')
