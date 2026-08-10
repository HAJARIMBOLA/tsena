import { useContext } from 'react'
import { AuthContext } from '../context/AuthContext'

export function useAuth() {
  const contexte = useContext(AuthContext)
  if (!contexte) {
    throw new Error('useAuth doit etre utilise a l\'interieur de AuthProvider')
  }
  return contexte
}
