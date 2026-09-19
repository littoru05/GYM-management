import { forwardRef } from 'react'

const Input = forwardRef(function Input(
  {
    label,
    error,
    icon: Icon,
    endIcon: EndIcon,
    onEndIconClick,
    endIconLabel,
    className = '',
    containerClassName = '',
    ...rest
  },
  ref
) {
  return (
    <div className={containerClassName}>
      {label && <label className="mb-1.5 block text-sm font-medium text-gray-700">{label}</label>}
      <div className="relative">
        {Icon && (
          <span className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-400">
            <Icon size={16} />
          </span>
        )}
        <input
          ref={ref}
          className={`w-full rounded-lg border bg-white px-3 py-2 text-sm text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-primary-200 ${
            Icon ? 'pl-9' : ''
          } ${EndIcon ? 'pr-9' : ''} ${error ? 'border-red-400' : 'border-gray-300'} ${className}`}
          {...rest}
        />
        {EndIcon && (
          <button
            type="button"
            onClick={onEndIconClick}
            aria-label={endIconLabel}
            className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            <EndIcon size={16} />
          </button>
        )}
      </div>
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  )
})

export default Input
