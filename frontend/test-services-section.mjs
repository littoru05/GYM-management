import assert from 'node:assert'
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

console.log('--- STARTING SERVICES SECTION & MOCKDATA TESTS ---')

// 1. Kiểm tra mockData từ publicContent.js và mockData.js
console.log('\n[TEST GROUP 1] Kiểm tra mockData 4 nhóm dịch vụ chủ đạo:')
const { services: publicServices } = await import('./src/data/publicContent.js')
const { services: mockDataServices } = await import('./src/data/mockData.js')

assert.strictEqual(publicServices.length, 4, 'Phải có chính xác 4 nhóm dịch vụ chủ đạo')
assert.strictEqual(mockDataServices.length, 4, 'mockData.js phải export đầy đủ 4 nhóm dịch vụ')

const expectedTitles = [
  'Thể hình tự do (Gym Fitness)',
  'Huấn luyện cá nhân 1-1 (Personal Trainer)',
  'Lớp Yoga & Giãn cơ',
  'Kickboxing / Cardio đốt mỡ',
]

expectedTitles.forEach((expectedTitle) => {
  const found = publicServices.find((s) => s.title.toLowerCase().includes(expectedTitle.toLowerCase()))
  assert.ok(found, `Phải tìm thấy dịch vụ: "${expectedTitle}"`)
  console.log(`✓ PASS: Tìm thấy dịch vụ "${expectedTitle}" (id: ${found.id})`)
})

// 2. Kiểm tra chi tiết từng thẻ card dịch vụ theo yêu cầu đề bài
console.log('\n[TEST GROUP 2] Kiểm tra thuộc tính bắt buộc của từng thẻ card:')
publicServices.forEach((service) => {
  // Ảnh minh họa không gian/máy móc
  assert.ok(service.image && typeof service.image === 'string' && service.image.startsWith('http'), `[${service.title}] phải có ảnh minh họa URL hợp lệ`)
  assert.ok(service.imageAlt && service.imageAlt.length > 5, `[${service.title}] phải có mô tả alt ảnh rõ ràng`)

  // Mô tả chi tiết phương pháp
  assert.ok(service.method, `[${service.title}] phải có đối tượng method chứa phương pháp tập luyện`)
  const methodSummary = typeof service.method === 'object' ? service.method.summary : service.method
  const methodDetails = typeof service.method === 'object' ? service.method.details : ''
  assert.ok(methodSummary && methodSummary.length > 10, `[${service.title}] phải có tóm tắt phương pháp tập luyện`)
  assert.ok(methodDetails && methodDetails.length > 20, `[${service.title}] phải có mô tả chi tiết phương pháp tập luyện`)

  // Đối tượng phù hợp
  assert.ok(Array.isArray(service.suitableFor) && service.suitableFor.length >= 2, `[${service.title}] phải có danh sách đối tượng phù hợp`)

  // Tiện ích đi kèm (phòng tắm, tủ locker, nước uống)
  assert.ok(Array.isArray(service.amenities) && service.amenities.length >= 3, `[${service.title}] phải có ít nhất 3 tiện ích đi kèm`)

  const amenityNames = service.amenities.map((a) => a.name.toLowerCase()).join(' ')
  assert.ok(amenityNames.includes('phòng tắm') || amenityNames.includes('tắm'), `[${service.title}] tiện ích phải có phòng tắm`)
  assert.ok(amenityNames.includes('locker') || amenityNames.includes('tủ'), `[${service.title}] tiện ích phải có tủ locker`)
  assert.ok(amenityNames.includes('nước') || amenityNames.includes('trà'), `[${service.title}] tiện ích phải có nước uống`)

  console.log(`✓ PASS: Dịch vụ "${service.title}" đạt chuẩn đầy đủ: Ảnh, Phương pháp, Đối tượng & Tiện ích (phòng tắm, locker, nước uống)`)
})

// 3. Kiểm tra code tích hợp ServicesSection & ServiceCard
console.log('\n[TEST GROUP 3] Kiểm tra tích hợp Component ServicesSection & ServiceCard:')
const serviceCardCode = fs.readFileSync(path.join(__dirname, 'src/components/public/ServiceCard.jsx'), 'utf-8')
assert.ok(serviceCardCode.includes('Phương pháp tập luyện'), 'ServiceCard phải hiển thị khối Phương pháp tập luyện')
assert.ok(serviceCardCode.includes('Đối tượng phù hợp'), 'ServiceCard phải hiển thị khối Đối tượng phù hợp')
assert.ok(serviceCardCode.includes('Tiện ích đi kèm'), 'ServiceCard phải hiển thị khối Tiện ích đi kèm')
console.log('✓ PASS: ServiceCard.jsx hiển thị đầy đủ trực quan các trường thông tin')

const servicesSectionCode = fs.readFileSync(path.join(__dirname, 'src/components/public/ServicesSection.jsx'), 'utf-8')
assert.ok(servicesSectionCode.includes('ServiceCard'), 'ServicesSection phải import và render ServiceCard')
assert.ok(servicesSectionCode.includes('Phòng tắm nóng lạnh') && servicesSectionCode.includes('Tủ locker'), 'ServicesSection có banner cam kết tiện ích 5 sao')
assert.ok(servicesSectionCode.includes('selectedService'), 'ServicesSection hỗ trợ xem modal chi tiết dịch vụ trực quan')
console.log('✓ PASS: ServicesSection.jsx trang bị tab bộ lọc, banner tiện ích và modal chi tiết')

const homePageCode = fs.readFileSync(path.join(__dirname, 'src/pages/public/HomePage.jsx'), 'utf-8')
assert.ok(homePageCode.includes('ServicesSection'), 'HomePage.jsx phải tích hợp ServicesSection')
console.log('✓ PASS: HomePage.jsx đã tích hợp ServicesSection trực quan')

const servicesPageCode = fs.readFileSync(path.join(__dirname, 'src/pages/public/ServicesPage.jsx'), 'utf-8')
assert.ok(servicesPageCode.includes('ServicesSection'), 'ServicesPage.jsx phải tích hợp ServicesSection')
console.log('✓ PASS: ServicesPage.jsx đã tích hợp ServicesSection trực quan')

console.log('\n========================================================================')
console.log('🎉 TẤT CẢ TEST SERVICES SECTION & MOCKDATA ĐÃ PASS 100%!')
console.log('========================================================================\n')

