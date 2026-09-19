function SectionHeading({ eyebrow, title, description, center = true }) {
  return (
    <div className={`max-w-2xl ${center ? 'mx-auto text-center' : ''}`}>
      {eyebrow && <p className="text-sm font-semibold uppercase tracking-wide text-primary-600">{eyebrow}</p>}
      <h2 className="mt-2 text-3xl font-extrabold tracking-tight text-gray-900 sm:text-4xl">{title}</h2>
      {description && <p className="mt-4 text-base text-gray-500">{description}</p>}
    </div>
  )
}

export default SectionHeading
