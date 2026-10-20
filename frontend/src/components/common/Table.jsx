import TableSkeleton from './TableSkeleton'
import EmptyState from './EmptyState'

/**
 * Generic data table. `columns` = [{ key, header, render?, className? }]
 * `data` = array of row objects. Renders a horizontally scrollable table on
 * small screens instead of collapsing into cards, matching common admin UX.
 */
function Table({ columns, data, loading = false, emptyMessage = 'Không tìm thấy dữ liệu', rowKey = 'id' }) {
  if (loading) {
    return (
      <div className="overflow-x-auto p-4">
        <table className="w-full min-w-[640px] text-left text-sm">
          <TableSkeleton columns={columns.length} />
        </table>
      </div>
    )
  }

  if (!data || data.length === 0) {
    return (
      <div className="p-8">
        <EmptyState title={emptyMessage} />
      </div>
    )
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[640px] text-left text-sm">
        <thead>
          <tr className="border-b border-gray-100 bg-gray-50 text-xs font-semibold uppercase tracking-wider text-gray-500">
            {columns.map((col) => {
              const alignment = col.className?.includes('text-right')
                ? 'text-right'
                : col.className?.includes('text-center')
                ? 'text-center'
                : 'text-left'
              return (
                <th
                  key={col.key}
                  className={`whitespace-nowrap px-4 py-3 ${alignment} ${col.headerClassName || ''}`}
                >
                  {col.header}
                </th>
              )
            })}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100 bg-white">
          {data.map((row, idx) => (
            <tr key={row[rowKey] ?? idx} className="transition-colors hover:bg-gray-50/80">
              {columns.map((col) => (
                <td key={col.key} className={`px-4 py-3.5 text-gray-700 ${col.className || ''}`}>
                  {col.render ? col.render(row) : row[col.key]}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default Table
