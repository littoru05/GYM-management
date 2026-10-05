import { useMemo, useState } from 'react'
import { Search, Plus, PackagePlus, ClipboardCheck } from 'lucide-react'
import { useProductsQuery, useCategoriesQuery } from '../../hooks/useProducts'
import Button from '../../components/common/Button'
import AddProductModal from './AddProductModal'
import ImportStockModal from './ImportStockModal'
import AdjustStockModal from './AdjustStockModal'
import Input from '../../components/common/Input'
import Select from '../../components/common/Select'
import Badge from '../../components/common/Badge'
import TableSkeleton from '../../components/common/TableSkeleton'
import EmptyState from '../../components/common/EmptyState'
import { formatCurrency } from '../../utils/formatters'
import { getStockStatus } from '../../utils/inventory'

function InventoryPage() {
  const [keyword, setKeyword] = useState('')
  const [category, setCategory] = useState('')
  const [addModalOpen, setAddModalOpen] = useState(false)
  const [importModal, setImportModal] = useState({ open: false, product: null })
  const [adjustTarget, setAdjustTarget] = useState(null)

  const { data: products, isLoading, isError, error, refetch, isFetching } = useProductsQuery()
  const { data: categories } = useCategoriesQuery()

  const categoryLabel = useMemo(
    () => Object.fromEntries((categories || []).map((c) => [c.code, c.name])),
    [categories]
  )

  const categoryOptions = [
    { value: '', label: 'Tất cả danh mục' },
    ...(categories || []).map((c) => ({ value: c.code, label: c.name })),
  ]

  // Lọc phía client để kết quả cập nhật tức thời theo từng phím gõ
  const filteredProducts = useMemo(() => {
    const q = keyword.trim().toLowerCase()
    return (products || []).filter((p) => {
      const matchesKeyword = !q || p.name.toLowerCase().includes(q) || p.sku.toLowerCase().includes(q)
      const matchesCategory = !category || p.category === category
      return matchesKeyword && matchesCategory
    })
  }, [products, keyword, category])

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Quản lý kho hàng</h1>
          <p className="mt-1 text-sm text-gray-500">Theo dõi tồn kho và cảnh báo sản phẩm sắp hết hàng.</p>
        </div>
        <div className="flex gap-3">
          <Button variant="secondary" icon={PackagePlus} onClick={() => setImportModal({ open: true, product: null })}>
            Nhập hàng
          </Button>
          <Button icon={Plus} onClick={() => setAddModalOpen(true)}>
            Thêm sản phẩm
          </Button>
        </div>
      </div>

      <div className="mt-6 flex flex-wrap items-center gap-3 rounded-xl bg-white p-4 shadow-card">
        <Input
          icon={Search}
          placeholder="Tìm theo tên hàng hoặc SKU..."
          aria-label="Tìm kiếm sản phẩm"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          containerClassName="min-w-[280px] flex-1"
        />
        <Select
          options={categoryOptions}
          aria-label="Lọc theo danh mục"
          value={category}
          onChange={(e) => setCategory(e.target.value)}
          containerClassName="w-52"
        />
      </div>

      <div className="mt-4 overflow-hidden rounded-xl bg-white shadow-card">
        {/* Cuộn ngang ở màn hình hẹp thay vì bóp méo các cột */}
        <div className="overflow-x-auto">
        <table className="w-full min-w-[960px] text-left text-sm">
          <thead className="bg-gray-50 text-xs uppercase text-gray-500">
            <tr>
              <th className="px-4 py-3">SKU</th>
              <th className="px-4 py-3">Tên hàng</th>
              <th className="px-4 py-3">Danh mục</th>
              <th className="px-4 py-3 text-right">Đơn giá</th>
              <th className="px-4 py-3 text-right">Tồn kho</th>
              <th className="px-4 py-3 text-right">Min Stock</th>
              <th className="px-4 py-3">Trạng thái</th>
              <th className="px-4 py-3 text-right">Thao tác</th>
            </tr>
          </thead>
          {isLoading ? (
            <TableSkeleton columns={8} />
          ) : (
            <tbody>
              {filteredProducts.map((product) => {
                const status = getStockStatus(product.stock, product.minStock)
                return (
                  <tr key={product.id} className="border-b border-gray-100 hover:bg-gray-50">
                    <td className="whitespace-nowrap px-4 py-3 font-mono text-xs text-gray-600">{product.sku}</td>
                    <td className="px-4 py-3 font-medium text-gray-800">{product.name}</td>
                    <td className="px-4 py-3 text-gray-600">{categoryLabel[product.category] || product.category}</td>
                    <td className="whitespace-nowrap px-4 py-3 text-right text-gray-600">{formatCurrency(product.price)}</td>
                    <td className="px-4 py-3 text-right font-semibold text-gray-900">{product.stock}</td>
                    <td className="px-4 py-3 text-right text-gray-600">{product.minStock}</td>
                    <td className="whitespace-nowrap px-4 py-3">
                      <Badge color={status.color}>{status.label}</Badge>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex justify-end gap-2">
                        <Button
                          size="sm"
                          variant="ghost"
                          icon={PackagePlus}
                          onClick={() => setImportModal({ open: true, product })}
                        >
                          Nhập
                        </Button>
                        <Button size="sm" variant="ghost" icon={ClipboardCheck} onClick={() => setAdjustTarget(product)}>
                          Kiểm kê
                        </Button>
                      </div>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          )}
        </table>
        </div>

        {isError && (
          <EmptyState
            title="Không tải được dữ liệu kho"
            description={error?.response?.data?.message || 'Vui lòng kiểm tra kết nối tới máy chủ.'}
            action={
              <Button variant="secondary" size="sm" loading={isFetching} onClick={() => refetch()}>
                Thử lại
              </Button>
            }
          />
        )}

        {!isLoading && !isError && filteredProducts.length === 0 && (
          <EmptyState title="Không tìm thấy sản phẩm phù hợp" />
        )}
      </div>

      {!isLoading && !isError && (
        <p className="mt-2 text-xs text-gray-400">
          Hiển thị {filteredProducts.length} / {(products || []).length} sản phẩm
        </p>
      )}

      <AddProductModal open={addModalOpen} onClose={() => setAddModalOpen(false)} categories={categories} />

      <ImportStockModal
        open={importModal.open}
        product={importModal.product}
        products={products}
        onClose={() => setImportModal({ open: false, product: null })}
      />

      <AdjustStockModal open={Boolean(adjustTarget)} product={adjustTarget} onClose={() => setAdjustTarget(null)} />
    </div>
  )
}

export default InventoryPage
