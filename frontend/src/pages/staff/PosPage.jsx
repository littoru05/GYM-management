import { useState, useMemo } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { ShoppingCart, X, AlertCircle, User } from 'lucide-react'
import Card from '../../components/common/Card'
import SearchInput from '../../components/common/SearchInput'
import Button from '../../components/common/Button'
import ProductCard from '../../components/common/ProductCard'
import PosCart from './PosCart'
import InvoiceReceiptModal from '../../components/common/InvoiceReceiptModal'
import EmptyState from '../../components/common/EmptyState'
import { useProductsQuery, useCategoriesQuery } from '../../hooks/useProducts'
import { useCartStore } from '../../store/cartStore'
import {
  checkoutOrder,
  getCheckoutErrorMessage,
  mapOrderToInvoice,
} from '../../services/posService'
import { useAuthStore } from '../../store/useAuthStore'
import { formatCurrency } from '../../utils/formatters'
import { PAYMENT_METHOD } from '../../utils/constants'
import { products as fallbackProducts, categories as fallbackCategories } from '../../data/mockData'
import toast from 'react-hot-toast'

const CATEGORY_TABS = [
  { id: 'ALL', label: 'Tất cả' },
  { id: 'BEVERAGE', label: 'Nước giải khát' },
  { id: 'SUPPLEMENT', label: 'Thực phẩm bổ sung' },
  { id: 'ACCESSORY', label: 'Phụ kiện' },
]

function matchesCategoryTab(product, tabId, categoryMap = {}) {
  if (tabId === 'ALL') return true

  const catCode = (product.category || '').toString().toLowerCase()
  const catNameFromMap = (categoryMap[product.category] || categoryMap[product.categoryId] || '').toLowerCase()
  const catName = (product.categoryName || '').toLowerCase()
  const combined = `${catCode} ${catNameFromMap} ${catName}`

  if (tabId === 'BEVERAGE') {
    return (
      catCode === 'cat-beverage' ||
      catCode === 'drink' ||
      catCode === 'beverage' ||
      combined.includes('nước') ||
      combined.includes('giải khát') ||
      combined.includes('drink') ||
      combined.includes('beverage')
    )
  }

  if (tabId === 'SUPPLEMENT') {
    return (
      catCode === 'cat-supplement' ||
      catCode === 'supplement' ||
      combined.includes('thực phẩm bổ sung') ||
      combined.includes('bổ sung') ||
      combined.includes('supplement') ||
      combined.includes('whey') ||
      combined.includes('creatine')
    )
  }

  if (tabId === 'ACCESSORY') {
    return (
      catCode === 'cat-accessory' ||
      catCode === 'accessory' ||
      combined.includes('phụ kiện') ||
      combined.includes('accessory')
    )
  }

  return true
}

function ProductSkeleton() {
  return (
    <div className="flex flex-col overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-card animate-pulse">
      <div className="h-40 w-full bg-gray-200" />
      <div className="p-4 space-y-3">
        <div className="h-4 w-3/4 rounded bg-gray-200" />
        <div className="flex justify-between items-center">
          <div className="h-5 w-1/3 rounded bg-gray-200" />
          <div className="h-5 w-1/4 rounded-full bg-gray-200" />
        </div>
        <div className="h-8 w-full rounded-xl bg-gray-200 mt-2" />
      </div>
    </div>
  )
}

function PosPage() {
  const [search, setSearch] = useState('')
  const [activeTab, setActiveTab] = useState('ALL')
  const [mobileCartOpen, setMobileCartOpen] = useState(false)
  const [paymentMethod, setPaymentMethod] = useState(PAYMENT_METHOD.CASH)
  const [invoice, setInvoice] = useState(null)
  const [checkoutError, setCheckoutError] = useState(null)

  const { data: apiProducts, isLoading, isError, refetch } = useProductsQuery()
  const { data: apiCategories } = useCategoriesQuery()
  const user = useAuthStore((state) => state.user)
  const queryClient = useQueryClient()

  // Trích xuất thông tin nhân viên thu ngân từ Auth store
  const cashierName = user?.name || user?.fullName || 'Thu ngân quầy'
  const cashierCode = user?.code || (user?.id ? `NV${String(user.id).padStart(3, '0')}` : '')

  // Use API products, with fallback to demo data if API fails / offline
  const allProducts = useMemo(() => {
    if (apiProducts && Array.isArray(apiProducts) && apiProducts.length > 0) {
      return apiProducts
    }
    if (isError) {
      return fallbackProducts
    }
    return apiProducts || fallbackProducts
  }, [apiProducts, isError])

  const categories = apiCategories || fallbackCategories

  const categoryMap = useMemo(() => {
    const map = {}
    ;(categories || []).forEach((c) => {
      if (c.code) map[c.code] = c.name
      if (c.id) map[c.id] = c.name
    })
    return map
  }, [categories])

  const { items, discount, addItem, clearCart, getTotal } = useCartStore()

  const total = getTotal()

  const saleMutation = useMutation({
    mutationFn: async ({ cartItems, discount: discountAmount, paymentMethod: method }) => {
      // Gửi request POST /api/pos/checkout kèm payload chuẩn cấu trúc Backend yêu cầu
      const orderResponse = await checkoutOrder({
        items: cartItems.map((item) => ({
          productId: Number(item.id ?? item.productId),
          quantity: Number(item.quantity || 1),
        })),
        paymentMethod: method,
        discountAmount,
        notes: `Bán lẻ tại quầy POS - Thu ngân: ${cashierName}`,
      })
      // Trích xuất thông tin nhân viên thu ngân từ Auth store để hiển thị lên đơn bán
      return mapOrderToInvoice(orderResponse, user)
    },
    onSuccess: (formattedInvoice) => {
      setCheckoutError(null)
      setInvoice(formattedInvoice)
      clearCart(true)
      setMobileCartOpen(false)
      queryClient.invalidateQueries({ queryKey: ['products'] })
      toast.success('Thanh toán đơn hàng thành công!')
    },
    onError: (err) => {
      // Xử lý kịch bản lỗi từ chối của Backend (mã lỗi 400 Bad Request do sản phẩm hết hàng hoặc số lượng tồn không đủ):
      // Bắt thông điệp lỗi từ response và hiển thị toast/alert đỏ cảnh báo rõ mặt hàng nào đang bị thiếu để thu ngân điều chỉnh lại giỏ hàng.
      const errorMsg = getCheckoutErrorMessage(err)
      setCheckoutError(errorMsg)
      toast.error(errorMsg, { duration: 6000, id: 'pos-checkout-error' })
    },
  })

  // Filter products by instant search keyword and active category tab
  const filteredProducts = useMemo(() => {
    const q = search.trim().toLowerCase()
    return allProducts.filter((product) => {
      // Active category tab check
      const matchesTab = matchesCategoryTab(product, activeTab, categoryMap)
      if (!matchesTab) return false

      // Instant keyword match (name or sku)
      if (!q) return true
      const nameMatch = product.name?.toLowerCase().includes(q)
      const skuMatch = product.sku?.toLowerCase().includes(q)
      return nameMatch || skuMatch
    })
  }, [allProducts, search, activeTab, categoryMap])

  // Count products in each tab
  const tabCounts = useMemo(() => {
    const counts = { ALL: allProducts.length }
    CATEGORY_TABS.slice(1).forEach((tab) => {
      counts[tab.id] = allProducts.filter((p) =>
        matchesCategoryTab(p, tab.id, categoryMap)
      ).length
    })
    return counts
  }, [allProducts, categoryMap])

  const handleSelectProduct = (product) => {
    if (checkoutError) setCheckoutError(null)
    addItem(product)
  }

  const handleCheckout = () => {
    if (items.length === 0) {
      toast.error('Giỏ hàng đang trống, vui lòng chọn sản phẩm trước!')
      return
    }
    saleMutation.mutate({
      cartItems: items,
      discount,
      paymentMethod,
    })
  }

  const cartPanel = (
    <PosCart
      paymentMethod={paymentMethod}
      setPaymentMethod={setPaymentMethod}
      onCheckout={handleCheckout}
      isCheckingOut={saleMutation.isPending}
      checkoutError={checkoutError}
      onClearCheckoutError={() => setCheckoutError(null)}
      cashierName={cashierName}
      cashierCode={cashierCode}
    />
  )

  return (
    <div className="space-y-6 pb-20 lg:pb-0">
      {/* Page Header */}
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Bán hàng tại quầy (POS)</h1>
          <p className="mt-0.5 text-sm text-gray-500">
            Tra cứu danh mục sản phẩm và tạo đơn hàng bán lẻ cho khách hàng
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2">
          {/* Thông tin nhân viên thu ngân trích xuất từ Auth store */}
          <div className="inline-flex items-center gap-2 rounded-xl border border-gray-200 bg-white px-3 py-1.5 text-xs shadow-xs">
            <div className="flex h-6 w-6 items-center justify-center rounded-lg bg-primary-50 text-primary-600 font-bold">
              <User size={13} />
            </div>
            <div className="text-left">
              <span className="block text-[10px] uppercase font-bold text-gray-400">Thu ngân ca trực</span>
              <span className="font-semibold text-gray-800">
                {cashierName} {cashierCode ? `(${cashierCode})` : ''}
              </span>
            </div>
          </div>

          {isError && (
            <div className="inline-flex items-center gap-1.5 rounded-lg bg-amber-50 px-3 py-1.5 text-xs text-amber-700 border border-amber-200">
              <AlertCircle size={14} />
              <span>Đang hiển thị dữ liệu mẫu do mất kết nối máy chủ</span>
              <button
                type="button"
                onClick={() => refetch()}
                className="ml-1 font-semibold underline hover:text-amber-900"
              >
                Thử lại
              </button>
            </div>
          )}
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_380px]">
        {/* Left Column: Product Catalog, Search & Tabs */}
        <div className="space-y-4">
          <Card padding="p-4 sm:p-5">
            <div className="space-y-4">
              {/* Search input */}
              <SearchInput
                id="pos-search-input"
                value={search}
                onChange={setSearch}
                placeholder="Tìm sản phẩm theo tên hoặc mã SKU..."
              />

              {/* Category tabs */}
              <div className="flex flex-wrap items-center gap-2 border-b border-gray-100 pb-3">
                {CATEGORY_TABS.map((tab) => {
                  const isActive = activeTab === tab.id
                  const count = tabCounts[tab.id] ?? 0
                  return (
                    <button
                      key={tab.id}
                      type="button"
                      onClick={() => setActiveTab(tab.id)}
                      className={`inline-flex items-center gap-1.5 rounded-xl px-4 py-2 text-xs font-semibold transition-all ${
                        isActive
                          ? 'bg-primary-500 text-white shadow-sm ring-2 ring-primary-200'
                          : 'bg-gray-100 text-gray-600 hover:bg-gray-200 hover:text-gray-900'
                      }`}
                    >
                      <span>{tab.label}</span>
                      <span
                        className={`rounded-full px-1.5 py-0.2 text-[10px] font-bold ${
                          isActive
                            ? 'bg-white/20 text-white'
                            : 'bg-gray-200 text-gray-600'
                        }`}
                      >
                        {count}
                      </span>
                    </button>
                  )
                })}
              </div>
            </div>

            {/* Product Responsive Grid */}
            <div className="mt-5">
              {isLoading ? (
                <div className="grid grid-cols-2 gap-4 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-3 2xl:grid-cols-4">
                  {Array.from({ length: 8 }).map((_, i) => (
                    <ProductSkeleton key={i} />
                  ))}
                </div>
              ) : filteredProducts.length === 0 ? (
                <EmptyState
                  title="Không tìm thấy sản phẩm"
                  description={
                    search
                      ? `Không có mặt hàng nào phù hợp với từ khóa "${search}".`
                      : 'Không có sản phẩm nào trong danh mục đã chọn.'
                  }
                  action={
                    search ? (
                      <Button
                        size="sm"
                        variant="secondary"
                        onClick={() => {
                          setSearch('')
                          setActiveTab('ALL')
                        }}
                      >
                        Xóa bộ lọc
                      </Button>
                    ) : null
                  }
                />
              ) : (
                <div className="grid grid-cols-2 gap-4 sm:grid-cols-2 md:grid-cols-3 xl:grid-cols-3 2xl:grid-cols-4">
                  {filteredProducts.map((product) => (
                    <ProductCard
                      key={product.id}
                      product={product}
                      onSelect={handleSelectProduct}
                    />
                  ))}
                </div>
              )}
            </div>

            {!isLoading && filteredProducts.length > 0 && (
              <div className="mt-4 flex items-center justify-between border-t border-gray-100 pt-3 text-xs text-gray-400">
                <span>
                  Đang hiển thị {filteredProducts.length} trên tổng số {allProducts.length} sản phẩm
                </span>
                <span>Nhấp vào thẻ sản phẩm để chọn mua</span>
              </div>
            )}
          </Card>
        </div>

        {/* Right Column: Shopping Cart Panel - Cố định bám sát đỉnh phải màn hình */}
        <div className="hidden lg:block lg:self-start sticky lg:top-4 z-20">
          <Card padding="p-5" className="shadow-lg border-gray-200/90 bg-white max-h-[calc(100vh-2rem)] flex flex-col">
            {cartPanel}
          </Card>
        </div>
      </div>

      {/* Mobile Floating Cart Trigger */}
      <button
        type="button"
        onClick={() => setMobileCartOpen(true)}
        className="fixed bottom-4 right-4 z-30 flex items-center gap-2 rounded-full bg-primary-500 px-5 py-3 text-sm font-bold text-white shadow-xl hover:bg-primary-600 transition-all lg:hidden"
      >
        <ShoppingCart size={18} />
        <span>Giỏ hàng ({items.length})</span>
        {total > 0 && <span>• {formatCurrency(total)}</span>}
      </button>

      {/* Mobile Cart Bottom Sheet */}
      {mobileCartOpen && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div
            className="absolute inset-0 bg-gray-900/50 backdrop-blur-sm transition-opacity"
            onClick={() => setMobileCartOpen(false)}
          />
          <div className="absolute inset-x-0 bottom-0 max-h-[85vh] overflow-y-auto rounded-t-3xl bg-white p-5 shadow-2xl">
            <div className="mb-3 flex items-center justify-between border-b border-gray-100 pb-2">
              <span className="text-base font-bold text-gray-900">Chi tiết giỏ hàng</span>
              <button
                type="button"
                onClick={() => setMobileCartOpen(false)}
                className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600"
              >
                <X size={20} />
              </button>
            </div>
            {cartPanel}
          </div>
        </div>
      )}

      {/* Invoice Receipt Modal after Successful Checkout */}
      <InvoiceReceiptModal
        open={Boolean(invoice)}
        onClose={() => setInvoice(null)}
        invoice={invoice}
        onNewOrder={() => {
          clearCart(true)
          setInvoice(null)
          toast.success('Đã sẵn sàng tạo đơn hàng mới!')
        }}
      />
    </div>
  )
}

export default PosPage
