import { Navigate, Outlet } from 'react-router-dom'
import { useSite } from '../hooks/useSite'
import { ChargementPage } from './PageState'

export default function SelectionRequiseRoute() {
  const { selectionEffectuee, chargement, sites } = useSite()

  if (chargement && sites.length === 0) {
    return <ChargementPage message="Chargement de vos sites..." />
  }

  if (!selectionEffectuee) {
    return <Navigate to="/sites" replace />
  }

  return <Outlet />
}
