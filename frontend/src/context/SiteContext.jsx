import { createContext, useCallback, useEffect, useState } from 'react'
import * as siteService from '../services/siteService'
import { extraireMessageErreur } from '../api/apiError'
import { useAuth } from '../hooks/useAuth'

export const SiteContext = createContext(null)

export function SiteProvider({ children }) {
  const { estConnecte, estAdmin } = useAuth()
  const [sites, setSites] = useState([])
  const [siteActifId, setSiteActifId] = useState(null)
  const [vueGlobale, setVueGlobale] = useState(false)
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)

  const chargerSites = useCallback(async () => {
    setChargement(true)
    setErreur(null)
    try {
      const donnees = await siteService.mesSites()
      setSites(donnees)
      return donnees
    } catch (e) {
      setErreur(extraireMessageErreur(e))
      return []
    } finally {
      setChargement(false)
    }
  }, [])

  useEffect(() => {
    if (estConnecte) {
      chargerSites()
    } else {
      setSites([])
      setSiteActifId(null)
      setVueGlobale(false)
    }
  }, [estConnecte, chargerSites])

  useEffect(() => {
    if (!estAdmin && sites.length === 1 && !siteActifId) {
      setSiteActifId(sites[0].id)
    }
  }, [estAdmin, sites, siteActifId])

  const selectionnerSite = useCallback((siteId) => {
    setSiteActifId(siteId)
    setVueGlobale(false)
  }, [])

  const selectionnerVueGlobale = useCallback(() => {
    setVueGlobale(true)
    setSiteActifId(null)
  }, [])

  const reinitialiserSelection = useCallback(() => {
    setSiteActifId(null)
    setVueGlobale(false)
  }, [])

  const siteActif = sites.find((s) => s.id === siteActifId) ?? null

  const valeur = {
    sites,
    siteActif,
    vueGlobale,
    chargement,
    erreur,
    selectionEffectuee: Boolean(siteActif) || vueGlobale,
    selectionnerSite,
    selectionnerVueGlobale,
    reinitialiserSelection,
    rechargerSites: chargerSites,
  }

  return <SiteContext.Provider value={valeur}>{children}</SiteContext.Provider>
}
