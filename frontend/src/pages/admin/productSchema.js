import * as yup from 'yup'

// Ô number để trống sẽ ra NaN -> typeError hiển thị thông báo "bắt buộc" thay vì lỗi kiểu dữ liệu khó hiểu
const positiveInteger = (label) =>
  yup
    .number()
    .typeError(`${label} không được để trống`)
    .required(`${label} không được để trống`)
    .integer(`${label} phải là số nguyên`)
    .positive(`${label} phải lớn hơn 0`)

export const productSchema = yup.object({
  sku: yup
    .string()
    .trim()
    .required('Mã SKU không được để trống')
    .max(50, 'Mã SKU tối đa 50 ký tự')
    .matches(/^[A-Za-z0-9-_]+$/, 'Mã SKU chỉ gồm chữ, số, dấu "-" hoặc "_"'),
  name: yup.string().trim().required('Tên sản phẩm không được để trống').max(150, 'Tên sản phẩm tối đa 150 ký tự'),
  category: yup.string().required('Vui lòng chọn danh mục'),
  unit: yup.string().trim().required('Đơn vị tính không được để trống').max(20, 'Đơn vị tính tối đa 20 ký tự'),
  price: positiveInteger('Giá bán'),
  cost: positiveInteger('Giá vốn'),
  stock: positiveInteger('Số lượng nhập'),
  minStock: positiveInteger('Ngưỡng cảnh báo'),
})

export const EMPTY_PRODUCT = {
  sku: '',
  name: '',
  category: '',
  unit: '',
  price: '',
  cost: '',
  stock: '',
  minStock: '',
}
