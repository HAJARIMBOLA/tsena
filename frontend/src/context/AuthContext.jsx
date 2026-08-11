import { createContext, useCallback, useEffect, useState } from 'react'
import * as authService from '../services/authService'
import { setAuthToken, setUnauthorizedHandler } from '../api/apiClient'

export const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [utilisateur, setUtilisateur] = useState(null)
  const [initialise, setInitialise] = useState(false)

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setAuthToken(null)
      setUtilisateur(null)
    })
    setInitialise(true)
  }, [])

  const appliquerSession = useCallback((reponse) => {
    setAuthToken(reponse.token)
    setUtilisateur({
      id: reponse.id,
      nom: reponse.nom,
      email: reponse.email,
      role: reponse.role,
      siteIds: reponse.siteIds ?? [],
    })
    return reponse
  }, [])

  const connecter = useCallback(
    async (email, motDePasse) => appliquerSession(await authService.login(email, motDePasse)),
    [appliquerSession],
  )

  const enregistrer = useCallback(
    async (nom, email, motDePasse) => appliquerSession(await authService.register(nom, email, motDePasse)),
    [appliquerSession],
  )

  const deconnecter = useCallback(() => {
    setAuthToken(null)
    setUtilisateur(null)
  }, [])

  const valeur = {
    utilisateur,
    estConnecte: Boolean(utilisateur),
    estAdmin: utilisateur?.role === 'ADMIN',
    connecter,
    enregistrer,
    deconnecter,
  }

  if (!initialise) {
    return null
  }

  return <AuthContext.Provider value={valeur}>{children}</AuthContext.Provider>
}
