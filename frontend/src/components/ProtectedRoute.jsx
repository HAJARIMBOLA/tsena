import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

export default function ProtectedRoute({ role }) {
  const { estConnecte, utilisateur } = useAuth()

  if (!estConnecte) {
    return <Navigate to="/login" replace />
  }

  if (role && utilisateur.role !== role) {
    return <Navigate to="/dashboard" replace />
  }

  return <Outlet />
}
