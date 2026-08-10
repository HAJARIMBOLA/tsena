import { Navigate, Outlet } from 'react-router-dom'
import { useSite } from '../hooks/useSite'
import { ChargementPage } from './PageState'

export default function SiteObligatoireRoute() {
  const { siteActif, chargement, sites } = useSite()

  if (chargement && sites.length === 0) {
    return <ChargementPage message="Chargement de vos sites..." />
  }

  if (!siteActif) {
    return <Navigate to="/sites" replace />
  }

  return <Outlet />
}
