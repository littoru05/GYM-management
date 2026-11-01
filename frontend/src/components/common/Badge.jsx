const COLOR_STYLES = {
  green: 'bg-green-100 text-green-700',
  emerald: 'bg-emerald-100 text-emerald-700',
  yellow: 'bg-yellow-100 text-yellow-700',
  red: 'bg-red-100 text-red-700',
  danger: 'bg-red-100 text-red-700',
  darkRed: 'bg-red-700 text-white',
  amber: 'bg-amber-100 text-amber-800',
  gray: 'bg-gray-100 text-gray-600',
  blue: 'bg-blue-100 text-blue-700',
  purple: 'bg-purple-100 text-purple-700',
  primary: 'bg-primary-100 text-primary-700',
}

const DOT_STYLES = {
  green: 'bg-green-500',
  emerald: 'bg-emerald-500',
  yellow: 'bg-yellow-500',
  red: 'bg-red-500',
  danger: 'bg-red-500',
  darkRed: 'bg-white',
  amber: 'bg-amber-500',
  gray: 'bg-gray-400',
  blue: 'bg-blue-500',
  purple: 'bg-purple-500',
  primary: 'bg-primary-500',
}

function Badge({ color, variant, dot = false, className = '', children }) {
  const activeColor = color || variant || 'gray'
  const style = COLOR_STYLES[activeColor] || COLOR_STYLES.gray
  const dotStyle = DOT_STYLES[activeColor] || DOT_STYLES.gray

  return (
    <span
      className={`inline-flex items-center whitespace-nowrap rounded-full px-2.5 py-0.5 text-xs font-medium ${style} ${className}`}
    >
      {dot && <span className={`mr-1.5 h-1.5 w-1.5 rounded-full ${dotStyle}`} />}
      {children}
    </span>
  )
}

export default Badge

