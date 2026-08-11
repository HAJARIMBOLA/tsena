import { useEffect, useState } from 'react'
import * as venteService from '../services/venteService'
import * as produitService from '../services/produitService'
import * as utilisateurService from '../services/utilisateurService'
import { useAuth } from '../hooks/useAuth'
import { useSite } from '../hooks/useSite'
import { extraireMessageErreur } from '../api/apiError'
import { Alerte, ChargementPage } from '../components/PageState'
import Pagination from '../components/Pagination'
import Badge from '../components/Badge'
import { formaterDateHeure, formaterMontant, formaterQuantite } from '../utils/format'

const TAILLE_PAGE = 20

export default function HistoriquePage() {
  const { estAdmin } = useAuth()
  const { siteActif, vueGlobale } = useSite()

  const [dateDebut, setDateDebut] = useState('')
  const [dateFin, setDateFin] = useState('')
  const [page, setPage] = useState(0)

  const [ventes, setVentes] = useState(null)
  const [produitsParId, setProduitsParId] = useState({})
  const [utilisateursParId, setUtilisateursParId] = useState({})
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    produitService.listerActifs().then((donnees) => {
      setProduitsParId(Object.fromEntries(donnees.map((p) => [p.id, p.nom])))
    })
  }, [])

  useEffect(() => {
    if (estAdmin) {
      utilisateurService.lister().then((donnees) => {
        setUtilisateursParId(Object.fromEntries(donnees.map((u) => [u.id, u.nom])))
      })
    }
  }, [estAdmin])

  useEffect(() => {
    let annule = false
    setChargement(true)
    setErreur(null)

    const debut = dateDebut ? `${dateDebut}T00:00:00` : undefined
    const fin = dateFin ? `${dateFin}T23:59:59` : undefined

    const requete = vueGlobale
      ? venteService.historiqueGlobal({ debut, fin, page, size: TAILLE_PAGE })
      : venteService.historiqueParSite(siteActif.id, { debut, fin, page, size: TAILLE_PAGE })

    requete
      .then((data) => {
        if (!annule) setVentes(data)
      })
      .catch((err) => {
        if (!annule) setErreur(extraireMessageErreur(err))
      })
      .finally(() => {
        if (!annule) setChargement(false)
      })

    return () => {
      annule = true
    }
  }, [siteActif, vueGlobale, dateDebut, dateFin, page])

  function appliquerFiltres(e) {
    e.preventDefault()
    setPage(0)
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Historique des ventes</h1>
        <p className="text-sm text-slate-500">
          {vueGlobale ? 'Vue globale - tous les sites' : `Site : ${siteActif.nom}`}
        </p>
      </div>

      <form
        onSubmit={appliquerFiltres}
        className="flex flex-wrap items-end gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-500" htmlFor="dateDebut">
            Du
          </label>
          <input
            id="dateDebut"
            type="date"
            value={dateDebut}
            onChange={(e) => setDateDebut(e.target.value)}
            className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-500" htmlFor="dateFin">
            Au
          </label>
          <input
            id="dateFin"
            type="date"
            value={dateFin}
            onChange={(e) => setDateFin(e.target.value)}
            className="rounded-lg border border-slate-300 px-3 py-1.5 text-sm"
          />
        </div>
        <button
          type="submit"
          className="rounded-lg bg-emerald-600 px-4 py-1.5 text-sm font-medium text-white hover:bg-emerald-700"
        >
          Filtrer
        </button>
      </form>

      {erreur && <Alerte type="error">{erreur}</Alerte>}

      {chargement ? (
        <ChargementPage message="Chargement de l'historique..." />
      ) : (
        <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-slate-100 text-left text-xs uppercase tracking-wide text-slate-400">
                <th className="px-4 py-3">Date</th>
                {vueGlobale && <th className="px-4 py-3">Site</th>}
                <th className="px-4 py-3">Produit</th>
                <th className="px-4 py-3 text-right">Quantite</th>
                <th className="px-4 py-3 text-right">Montant</th>
                {estAdmin && <th className="px-4 py-3">Utilisateur</th>}
              </tr>
            </thead>
            <tbody>
              {ventes?.content?.length ? (
                ventes.content.map((vente) => (
                  <tr key={vente.id} className="border-b border-slate-50 transition last:border-0 hover:bg-slate-50">
                    <td className="px-4 py-3 text-slate-600">{formaterDateHeure(vente.dateVente)}</td>
                    {vueGlobale && (
                      <td className="px-4 py-3">
                        <Badge color="emerald">{vente.siteNom ?? `Site #${vente.siteId}`}</Badge>
                      </td>
                    )}
                    <td className="px-4 py-3 font-medium text-slate-700">
                      {produitsParId[vente.produitId] ?? `Produit #${vente.produitId}`}
                    </td>
                    <td className="px-4 py-3 text-right text-slate-600">{formaterQuantite(vente.quantite)}</td>
                    <td className="px-4 py-3 text-right font-medium text-slate-700">
                      {formaterMontant(vente.montantTotal)}
                    </td>
                    {estAdmin && (
                      <td className="px-4 py-3 text-slate-600">
                        {utilisateursParId[vente.utilisateurId] ?? `#${vente.utilisateurId}`}
                      </td>
                    )}
                  </tr>
                ))
              ) : (
                <tr>
                  <td
                    colSpan={4 + (estAdmin ? 1 : 0) + (vueGlobale ? 1 : 0)}
                    className="px-4 py-10 text-center text-slate-400"
                  >
                    Aucune vente sur cette periode
                  </td>
                </tr>
              )}
            </tbody>
          </table>
          {ventes && (
            <div className="px-4 py-3">
              <Pagination page={ventes.number} totalPages={ventes.totalPages} onChange={setPage} />
            </div>
          )}
        </div>
      )}
    </div>
  )
}
