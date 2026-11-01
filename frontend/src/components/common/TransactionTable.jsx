import Table from './Table.jsx'
import Badge from './Badge.jsx'
import { formatCurrency, formatDate } from '../../utils/formatters.js'
import { PAYMENT_METHOD_LABEL, TRANSACTION_TYPE_LABEL } from '../../utils/constants.js'

const TYPE_VARIANT = {
  MEMBERSHIP: 'primary',
  RETAIL: 'blue',
}

function TransactionTable({ transactions = [], loading = false, emptyMessage = 'Chưa có giao dịch nào' }) {
  const columns = [
    {
      key: 'id',
      header: 'Mã GD',
      render: (row) => <span className="font-mono text-xs font-semibold text-gray-700">{row.id}</span>,
    },
    {
      key: 'date',
      header: 'Ngày',
      render: (row) => <span className="text-xs text-gray-600">{formatDate(row.date)}</span>,
    },
    {
      key: 'memberName',
      header: 'Khách hàng',
      render: (row) => (
        <span className="font-medium text-gray-900">{row.memberName || 'Khách lẻ'}</span>
      ),
    },
    {
      key: 'description',
      header: 'Nội dung',
      render: (row) => <span className="text-xs text-gray-600">{row.description}</span>,
    },
    {
      key: 'type',
      header: 'Phân loại',
      render: (row) => (
        <Badge variant={TYPE_VARIANT[row.type] || 'gray'}>
          {TRANSACTION_TYPE_LABEL[row.type] || row.type}
        </Badge>
      ),
    },
    {
      key: 'paymentMethod',
      header: 'Hình thức',
      render: (row) => (
        <span className="text-xs text-gray-600">
          {PAYMENT_METHOD_LABEL[row.paymentMethod] || row.paymentMethod || 'Tiền mặt'}
        </span>
      ),
    },
    {
      key: 'amount',
      header: 'Số tiền',
      className: 'text-right font-bold text-gray-900',
      render: (row) => formatCurrency(row.amount),
    },
  ]

  return (
    <Table
      columns={columns}
      data={transactions}
      loading={loading}
      rowKey="id"
      emptyMessage={emptyMessage}
    />
  )
}

export default TransactionTable
