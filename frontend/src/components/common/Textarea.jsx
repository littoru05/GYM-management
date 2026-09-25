import { forwardRef } from 'react'

const Textarea = forwardRef(function Textarea(
  { label, error, className = '', containerClassName = '', ...rest },
  ref
) {
  return (
    <div className={containerClassName}>
      {label && <label className="mb-1.5 block text-sm font-medium text-gray-700">{label}</label>}
      <textarea
        ref={ref}
        className={`w-full rounded-lg border bg-white px-3 py-2 text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-200 ${
          error ? 'border-red-400' : 'border-gray-300'
        } ${className}`}
        {...rest}
      />
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  )
})

export default Textarea
