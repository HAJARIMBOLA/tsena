export default function Spinner({ taille = 'h-6 w-6', className = '' }) {
  return (
    <div
      role="status"
      aria-label="Chargement"
      className={`animate-spin rounded-full border-2 border-slate-300 border-t-emerald-600 ${taille} ${className}`}
    />
  )
}
