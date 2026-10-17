function Card({ children, className = '', padding = 'p-5', as: Component = 'div', ...rest }) {
  return (
    <Component className={`rounded-2xl border border-gray-100 bg-white shadow-card ${padding} ${className}`} {...rest}>
      {children}
    </Component>
  )
}

export default Card
