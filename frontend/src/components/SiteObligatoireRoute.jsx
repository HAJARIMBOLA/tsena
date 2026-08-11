import { Navigate, Outlet, useNavigate } from 'react-router-dom'
import { useSite } from '../hooks/useSite'
import { ChargementPage } from './PageState'

export default function SiteObligatoireRoute({ adminGlobalAutorise = false }) {
  const { siteActif, vueGlobale, chargement, sites, reinitialiserSelection } = useSite()
  const navigate = useNavigate()

  if (chargement && sites.length === 0) {
    return <ChargementPage message="Chargement de vos sites..." />
  }

  if (siteActif) {
    return <Outlet />
  }

  if (vueGlobale && adminGlobalAutorise) {
    return <Outlet />
  }

  if (vueGlobale) {
    return (
      <div className="flex min-h-[40vh] flex-col items-center justify-center gap-3 text-center">
        <p className="text-slate-600">
          Cette page necessite un site specifique. Vous etes actuellement en vue globale.
        </p>
        <button
          type="button"
          onClick={() => {
            reinitialiserSelection()
            navigate('/sites')
          }}
          className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
        >
          Choisir un site
        </button>
      </div>
    )
  }

  return <Navigate to="/sites" replace />
}
