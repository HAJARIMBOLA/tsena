import Spinner from './Spinner'

export function ChargementPage({ message = 'Chargement...' }) {
  return (
    <div className="flex min-h-[40vh] flex-col items-center justify-center gap-3 text-slate-500">
      <Spinner taille="h-8 w-8" />
      <p>{message}</p>
    </div>
  )
}

export function ErreurPage({ message }) {
  return (
    <div className="flex min-h-[40vh] items-center justify-center">
      <div className="rounded-lg border border-red-200 bg-red-50 px-6 py-4 text-red-700">
        {message}
      </div>
    </div>
  )
}

export function Alerte({ type = 'error', children }) {
  const styles = {
    error: 'border-red-200 bg-red-50 text-red-700',
    success: 'border-emerald-200 bg-emerald-50 text-emerald-700',
    info: 'border-sky-200 bg-sky-50 text-sky-700',
  }

  return (
    <div className={`rounded-lg border px-4 py-3 text-sm ${styles[type] ?? styles.info}`}>
      {children}
    </div>
  )
}
