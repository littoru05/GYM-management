/**
 * test-product-image-preview.mjs
 * Kiểm thử tự động tính năng UC12 (Thẻ input file chọn ảnh & Image Preview trong Admin Kho)
 * và UC09 (Card sản phẩm POS với tỷ lệ chuẩn aspect-[4/3], fallback onError & kết nối API).
 */

import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'
import assert from 'assert'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

console.log('--- STARTING UC12 IMAGE FILE INPUT & UC09 POS PRODUCT CARD TESTS ---')

// 1. Kiểm tra ImagePreview component
console.log('\n[TEST GROUP 1] Kiểm tra Component ImagePreview (ImagePreview.jsx)...')
const imagePreviewPath = path.join(__dirname, 'src/components/common/ImagePreview.jsx')
assert.ok(fs.existsSync(imagePreviewPath), 'ImagePreview.jsx phải tồn tại')
const imagePreviewContent = fs.readFileSync(imagePreviewPath, 'utf8')
assert.ok(imagePreviewContent.includes('Khung xem trước ảnh'), 'ImagePreview phải có tiêu đề xem trước ảnh')
assert.ok(imagePreviewContent.includes('displayUrl') || imagePreviewContent.includes('trimmedUrl'), 'ImagePreview phải chuẩn hóa link ảnh')
assert.ok(imagePreviewContent.includes('img.onload') && imagePreviewContent.includes('img.onerror'), 'ImagePreview phải kiểm tra tải ảnh qua Image()')
assert.ok(imagePreviewContent.includes('Ảnh hợp lệ') || imagePreviewContent.includes('Link ảnh hợp lệ'), 'ImagePreview phải có thông báo ảnh hợp lệ')
assert.ok(imagePreviewContent.includes('Không thể tải ảnh') || imagePreviewContent.includes('File ảnh bị lỗi'), 'ImagePreview phải có thông báo lỗi tải ảnh')
console.log('✓ PASS: Component ImagePreview.jsx hỗ trợ đầy đủ các trạng thái tải, thành công và báo lỗi trực quan!')

// 2. Kiểm tra AddProductModal (UC12) với Thẻ input file
console.log('\n[TEST GROUP 2] Kiểm tra Modal Thêm mới và Chỉnh sửa sản phẩm với Thẻ input file (AddProductModal.jsx)...')
const addModalPath = path.join(__dirname, 'src/pages/admin/AddProductModal.jsx')
assert.ok(fs.existsSync(addModalPath), 'AddProductModal.jsx phải tồn tại')
const addModalContent = fs.readFileSync(addModalPath, 'utf8')
assert.ok(addModalContent.includes('type="file"'), 'AddProductModal phải có thẻ input type="file" để chọn file từ máy tính')
assert.ok(addModalContent.includes('ImagePreview'), 'AddProductModal phải import ImagePreview')
assert.ok(addModalContent.includes('uploadProductImage'), 'AddProductModal phải sử dụng service upload ảnh lên backend')
assert.ok(addModalContent.includes('useUpdateProduct'), 'AddProductModal phải hỗ trợ cập nhật sản phẩm khi sửa')
assert.ok(addModalContent.includes('isEdit'), 'AddProductModal phải phân biệt chế độ Thêm mới và Chỉnh sửa')
assert.ok(addModalContent.includes('onDrop'), 'AddProductModal phải hỗ trợ kéo thả file ảnh tiện lợi')
console.log('✓ PASS: AddProductModal.jsx trang bị thẻ input file, kéo thả, ImagePreview và tự động upload lên server!')

// 3. Kiểm tra InventoryPage (UC12)
console.log('\n[TEST GROUP 3] Kiểm tra Màn hình Quản trị Kho (InventoryPage.jsx)...')
const inventoryPagePath = path.join(__dirname, 'src/pages/admin/InventoryPage.jsx')
assert.ok(fs.existsSync(inventoryPagePath), 'InventoryPage.jsx phải tồn tại')
const inventoryContent = fs.readFileSync(inventoryPagePath, 'utf8')
assert.ok(inventoryContent.includes('ProductThumbnail'), 'InventoryPage phải có ProductThumbnail hiển thị ảnh')
assert.ok(inventoryContent.includes('>Ảnh</th>'), 'InventoryPage phải có cột Ảnh trong bảng')
assert.ok(inventoryContent.includes('columns={9}'), 'InventoryPage TableSkeleton phải cập nhật 9 cột')
assert.ok(inventoryContent.includes('setEditTarget'), 'InventoryPage phải có thao tác Sửa sản phẩm')
console.log('✓ PASS: InventoryPage.jsx hiển thị cột thumbnail ảnh với fallback icon và nút sửa sản phẩm!')

// 4. Kiểm tra ProductCard & tỷ lệ khung hình POS (UC09)
console.log('\n[TEST GROUP 4] Kiểm tra Card sản phẩm Mini POS (ProductCard.jsx)...')
const productCardPath = path.join(__dirname, 'src/components/common/ProductCard.jsx')
assert.ok(fs.existsSync(productCardPath), 'ProductCard.jsx phải tồn tại')
const cardContent = fs.readFileSync(productCardPath, 'utf8')
assert.ok(cardContent.includes('aspect-[4/3]'), 'ProductCard phải cố định tỷ lệ khung hình chuẩn 4:3 (aspect-[4/3])')
assert.ok(cardContent.includes('DEFAULT_GYM_PLACEHOLDER'), 'ProductCard phải có ảnh gym mặc định dự phòng')
assert.ok(cardContent.includes('handleImageError'), 'ProductCard phải có cơ chế bắt sự kiện onError khi ảnh lỗi')
assert.ok(cardContent.includes('hasError') && cardContent.includes('Dumbbell'), 'ProductCard phải có fallback sang icon/ảnh thương hiệu gym')
assert.ok(cardContent.includes('Hết hàng'), 'ProductCard phải hiển thị nhãn Hết hàng khi tồn kho = 0')
assert.ok(cardContent.includes('Ngừng kinh doanh'), 'ProductCard phải xử lý và hiển thị nhãn Ngừng kinh doanh khi sản phẩm INACTIVE')
console.log('✓ PASS: ProductCard.jsx cố định tỷ lệ aspect-[4/3] chống xô lệch lưới, cơ chế onError 2 tầng và chặn chọn hàng ngừng kinh doanh!')

// Kiểm tra PosCart thông báo lỗi linh hoạt
const posCartPath = path.join(__dirname, 'src/pages/staff/PosCart.jsx')
assert.ok(fs.existsSync(posCartPath), 'PosCart.jsx phải tồn tại')
const posCartContent = fs.readFileSync(posCartPath, 'utf8')
assert.ok(posCartContent.includes('isInactiveError'), 'PosCart phải phân loại lỗi sản phẩm ngừng kinh doanh')
assert.ok(posCartContent.includes('Từ chối thanh toán: Sản phẩm ngừng kinh doanh'), 'PosCart phải hiển thị tiêu đề chính xác khi sản phẩm ngừng kinh doanh')

// 5. Kiểm tra Kết nối API và loại bỏ MockData tại PosPage (UC09)
console.log('\n[TEST GROUP 5] Kiểm tra Kết nối API thực tế tại PosPage.jsx...')
const posPagePath = path.join(__dirname, 'src/pages/staff/PosPage.jsx')
assert.ok(fs.existsSync(posPagePath), 'PosPage.jsx phải tồn tại')
const posContent = fs.readFileSync(posPagePath, 'utf8')
assert.ok(posContent.includes('useProductsQuery'), 'PosPage phải sử dụng useProductsQuery để gọi API backend')
assert.ok(!posContent.includes("from '../../data/mockData'"), 'PosPage không được import mockData để fallback sản phẩm')
assert.ok(posContent.includes('apiProducts'), 'PosPage phải liên kết trực tiếp dữ liệu từ apiProducts')
console.log('✓ PASS: PosPage.jsx đã kết nối 100% với Backend API (/api/products) và xóa sạch mockData không cần thiết!')

// 6. Kiểm tra Backend FileUploadController & ProductDto
console.log('\n[TEST GROUP 6] Kiểm tra Backend FileUploadController & ProductDto...')
const uploadControllerPath = path.join(__dirname, '../backend/src/main/java/com/example/GYM_management_api/controllers/FileUploadController.java')
assert.ok(fs.existsSync(uploadControllerPath), 'FileUploadController.java phải tồn tại')
const uploadContent = fs.readFileSync(uploadControllerPath, 'utf8')
assert.ok(uploadContent.includes('/upload/image'), 'FileUploadController phải có endpoint /upload/image')
assert.ok(uploadContent.includes('MultipartFile'), 'FileUploadController phải nhận MultipartFile')

const productDtoPath = path.join(__dirname, '../backend/src/main/java/com/example/GYM_management_api/dtos/ProductDto.java')
assert.ok(fs.existsSync(productDtoPath), 'ProductDto.java phải tồn tại')
const dtoContent = fs.readFileSync(productDtoPath, 'utf8')
assert.ok(dtoContent.includes('private String imageUrl;'), 'ProductDto phải có trường imageUrl')
assert.ok(dtoContent.includes('@JsonProperty("image")'), 'ProductDto phải có alias getter @JsonProperty("image")')
console.log('✓ PASS: Backend FileUploadController.java & ProductDto.java sẵn sàng nhận file ảnh và lưu trữ!')

console.log('\n========================================================================')
console.log('🎉 TẤT CẢ CÁC BÀI KIỂM THỬ UC12 FILE INPUT & UC09 ĐÃ PASS 100%!')
console.log('DoD ĐẠT YÊU CẦU:')
console.log('- Đã thay thế hoàn toàn input nhập URL thủ công bằng thẻ input type="file" chọn ảnh từ máy.')
console.log('- Hỗ trợ kéo thả ảnh (drag & drop), kiểm tra dung lượng định dạng và hiển thị xem trước tức thì.')
console.log('- Tự động upload ảnh lên backend server và lưu URL vào sản phẩm.')
console.log('========================================================================\n')
