export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null

  return (
    <div className="flex items-center justify-between border-t border-slate-100 pt-3 text-sm">
      <button
        type="button"
        onClick={() => onChange(page - 1)}
        disabled={page <= 0}
        className="rounded-md border border-slate-200 px-3 py-1.5 text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
      >
        Precedent
      </button>
      <span className="text-slate-500">
        Page {page + 1} / {totalPages}
      </span>
      <button
        type="button"
        onClick={() => onChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="rounded-md border border-slate-200 px-3 py-1.5 text-slate-600 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
      >
        Suivant
      </button>
    </div>
  )
}
