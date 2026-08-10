import { useContext } from 'react'
import { SiteContext } from '../context/SiteContext'

export function useSite() {
  const contexte = useContext(SiteContext)
  if (!contexte) {
    throw new Error('useSite doit etre utilise a l\'interieur de SiteProvider')
  }
  return contexte
}
