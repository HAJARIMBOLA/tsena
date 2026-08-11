const styles = {
  slate: 'bg-slate-100 text-slate-600',
  emerald: 'bg-emerald-50 text-emerald-700',
}

export default function Badge({ children, color = 'slate' }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${
        styles[color] ?? styles.slate
      }`}
    >
      {children}
    </span>
  )
}
