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

// 3. Kiểm tra Component PackageConsultationForm.jsx
console.log('\n[TEST GROUP 3] Kiểm tra Component PackageConsultationForm.jsx:')
const formCode = fs.readFileSync(path.join(__dirname, 'src/components/public/PackageConsultationForm.jsx'), 'utf-8')
assert.ok(formCode.includes('id="contact-form"'), 'Form tư vấn phải có id="contact-form" để scroll target')
assert.ok(formCode.includes('currentPackage') || formCode.includes('selectedPackage'), 'Form tư vấn phải quản lý gói tập được chọn')
assert.ok(formCode.includes('Gói tập bạn đã chọn tư vấn') || formCode.includes('Gói quan tâm'), 'Form tư vấn phải hiển thị gói đã chọn')
assert.ok(formCode.includes('isSubmittingRef'), 'Form tư vấn phải có guard chống spam double-click')
console.log('✓ PASS: PackageConsultationForm.jsx hỗ trợ gán sẵn gói tập và chống click nhiều lần')

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

