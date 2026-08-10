import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useSite } from '../hooks/useSite'
import { Alerte, ChargementPage } from '../components/PageState'

export default function SiteSelectPage() {
  const { estAdmin } = useAuth()
  const { sites, siteActif, vueGlobale, chargement, erreur, selectionnerSite, selectionnerVueGlobale } =
    useSite()
  const navigate = useNavigate()

  useEffect(() => {
    if (siteActif || vueGlobale) {
      navigate('/dashboard', { replace: true })
    }
  }, [siteActif, vueGlobale, navigate])

  if (chargement) {
    return <ChargementPage message="Chargement de vos sites..." />
  }

  function choisirSite(site) {
    selectionnerSite(site.id)
    navigate('/dashboard', { replace: true })
  }

  function choisirVueGlobale() {
    selectionnerVueGlobale()
    navigate('/dashboard', { replace: true })
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-sm">
        <h1 className="mb-1 text-xl font-semibold text-slate-800">Choisir un site</h1>
        <p className="mb-6 text-sm text-slate-500">
          Selectionnez le site sur lequel vous souhaitez travailler.
        </p>

        {erreur && (
          <div className="mb-4">
            <Alerte type="error">{erreur}</Alerte>
          </div>
        )}

        <div className="space-y-2">
          {estAdmin && (
            <button
              type="button"
              onClick={choisirVueGlobale}
              className="w-full rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-left text-sm font-medium text-emerald-700 hover:bg-emerald-100"
            >
              Vue globale (tous les sites)
            </button>
          )}

          {sites.length === 0 && !erreur && (
            <p className="text-sm text-slate-400">Aucun site ne vous est assigne pour le moment.</p>
          )}

          {sites.map((site) => (
            <button
              key={site.id}
              type="button"
              onClick={() => choisirSite(site)}
              className="w-full rounded-lg border border-slate-200 px-4 py-3 text-left text-sm hover:bg-slate-50"
            >
              <span className="font-medium text-slate-700">{site.nom}</span>
              <span className="ml-2 text-slate-400">{site.localisation}</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  )
}
