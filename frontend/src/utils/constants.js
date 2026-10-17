export const MEMBER_STATUS = {
  ACTIVE: 'ACTIVE',
  EXPIRING_SOON: 'EXPIRING_SOON',
  EXPIRED: 'EXPIRED',
}

export const MEMBER_STATUS_LABEL = {
  [MEMBER_STATUS.ACTIVE]: 'Đang hoạt động',
  [MEMBER_STATUS.EXPIRING_SOON]: 'Sắp hết hạn',
  [MEMBER_STATUS.EXPIRED]: 'Đã hết hạn',
}

export const STAFF_ROLE = {
  STAFF: 'STAFF',
  ADMIN: 'ADMIN',
}

export const STAFF_ROLE_LABEL = {
  [STAFF_ROLE.STAFF]: 'Nhân viên',
  [STAFF_ROLE.ADMIN]: 'Quản trị viên',
}

export const STAFF_STATUS = {
  ACTIVE: 'ACTIVE',
  LOCKED: 'LOCKED',
}

export const STAFF_STATUS_LABEL = {
  [STAFF_STATUS.ACTIVE]: 'Hoạt động',
  [STAFF_STATUS.LOCKED]: 'Đã khóa',
}

export const PRODUCT_CATEGORY = {
  DRINK: 'DRINK',
  SUPPLEMENT: 'SUPPLEMENT',
  CLOTHING: 'CLOTHING',
  ACCESSORY: 'ACCESSORY',
}

export const PRODUCT_CATEGORY_LABEL = {
  [PRODUCT_CATEGORY.DRINK]: 'Nước giải khát',
  [PRODUCT_CATEGORY.SUPPLEMENT]: 'Thực phẩm bổ sung',
  [PRODUCT_CATEGORY.CLOTHING]: 'Quần áo',
  [PRODUCT_CATEGORY.ACCESSORY]: 'Phụ kiện',
}

export const PRODUCT_STATUS = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
}

export const DISCOUNT_TYPE = {
  PERCENTAGE: 'PERCENTAGE',
  FIXED: 'FIXED',
}

export const DISCOUNT_TYPE_LABEL = {
  [DISCOUNT_TYPE.PERCENTAGE]: 'Giảm theo phần trăm (%)',
  [DISCOUNT_TYPE.FIXED]: 'Giảm số tiền (VNĐ)',
}

export const PROMOTION_STATUS = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  EXPIRED: 'EXPIRED',
  UPCOMING: 'UPCOMING',
  USED_UP: 'USED_UP',
}

export const PROMOTION_STATUS_LABEL = {
  [PROMOTION_STATUS.ACTIVE]: 'Đang hoạt động',
  [PROMOTION_STATUS.INACTIVE]: 'Tạm ngưng',
  [PROMOTION_STATUS.EXPIRED]: 'Đã hết hạn',
  [PROMOTION_STATUS.UPCOMING]: 'Chưa bắt đầu',
  [PROMOTION_STATUS.USED_UP]: 'Hết lượt dùng',
}

export const PAYMENT_METHOD = {
  CASH: 'CASH',
  BANK_TRANSFER: 'BANK_TRANSFER',
  CARD: 'CARD',
}

export const PAYMENT_METHOD_LABEL = {
  [PAYMENT_METHOD.CASH]: 'Tiền mặt',
  [PAYMENT_METHOD.BANK_TRANSFER]: 'Chuyển khoản QR',
  [PAYMENT_METHOD.CARD]: 'Thẻ ngân hàng',
}

export const TRANSACTION_TYPE = {
  MEMBERSHIP: 'MEMBERSHIP',
  RETAIL: 'RETAIL',
}

export const TRANSACTION_TYPE_LABEL = {
  [TRANSACTION_TYPE.MEMBERSHIP]: 'Gói tập',
  [TRANSACTION_TYPE.RETAIL]: 'Bán hàng',
}

export const USER_ROLE = {
  ADMIN: 'ADMIN',
  STAFF: 'STAFF',
}
