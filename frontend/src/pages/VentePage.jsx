import { useEffect, useMemo, useState } from 'react'
import * as produitService from '../services/produitService'
import * as venteService from '../services/venteService'
import { useSite } from '../hooks/useSite'
import { extraireMessageErreur } from '../api/apiError'
import { Alerte, ChargementPage } from '../components/PageState'
import { formaterMontant } from '../utils/format'
import { convertirQuantite, estUnitePoids, UNITES_POIDS } from '../utils/unite'

export default function VentePage() {
  const { siteActif, vueGlobale, sites } = useSite()
  const [produits, setProduits] = useState([])
  const [chargementProduits, setChargementProduits] = useState(true)
  const [erreurProduits, setErreurProduits] = useState(null)

  const [siteFormId, setSiteFormId] = useState('')
  const [produitId, setProduitId] = useState('')
  const [quantite, setQuantite] = useState('')
  const [uniteSaisie, setUniteSaisie] = useState('')
  const [soumission, setSoumission] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [succes, setSucces] = useState(null)

  useEffect(() => {
    let annule = false
    produitService
      .listerActifs()
      .then((data) => {
        if (!annule) setProduits(data)
      })
      .catch((err) => {
        if (!annule) setErreurProduits(extraireMessageErreur(err))
      })
      .finally(() => {
        if (!annule) setChargementProduits(false)
      })
    return () => {
      annule = true
    }
  }, [])

  const siteIdEffectif = vueGlobale ? Number(siteFormId) || null : siteActif.id
  const sitesActifs = sites.filter((s) => s.actif !== false)
  const formulairePret = !vueGlobale || Boolean(siteFormId)

  const produitSelectionne = useMemo(
    () => produits.find((p) => String(p.id) === String(produitId)) ?? null,
    [produits, produitId],
  )

  const selecteurUniteVisible = Boolean(produitSelectionne) && estUnitePoids(produitSelectionne.unite)

  useEffect(() => {
    setUniteSaisie(produitSelectionne?.unite ?? '')
  }, [produitSelectionne])

  const quantiteEnUniteProduit = useMemo(() => {
    if (!produitSelectionne || !quantite) return 0
    const quantiteNombre = Number(quantite)
    if (Number.isNaN(quantiteNombre)) return 0
    return convertirQuantite(quantiteNombre, uniteSaisie || produitSelectionne.unite, produitSelectionne.unite)
  }, [produitSelectionne, quantite, uniteSaisie])

  const montantEstime = useMemo(() => {
    if (!produitSelectionne) return 0
    return quantiteEnUniteProduit * Number(produitSelectionne.prixUnitaire)
  }, [produitSelectionne, quantiteEnUniteProduit])

  async function soumettre(e) {
    e.preventDefault()
    setErreur(null)
    setSucces(null)

    if (vueGlobale && !siteFormId) {
      setErreur('Selectionnez un site.')
      return
    }

    if (!produitId || !quantite || Number(quantite) <= 0) {
      setErreur('Selectionnez un produit et une quantite valide.')
      return
    }

    setSoumission(true)
    try {
      const vente = await venteService.creer({
        siteId: siteIdEffectif,
        produitId: Number(produitId),
        quantite: quantiteEnUniteProduit,
      })
      setSucces({ ...vente, quantiteSaisie: Number(quantite), uniteSaisie: uniteSaisie || produitSelectionne?.unite })
      setQuantite('')
    } catch (err) {
      setErreur(extraireMessageErreur(err))
    } finally {
      setSoumission(false)
    }
  }

  if (chargementProduits) {
    return <ChargementPage message="Chargement des produits..." />
  }

  return (
    <div className="mx-auto max-w-lg space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Nouvelle vente</h1>
        <p className="text-sm text-slate-500">
          {vueGlobale ? 'Vue globale - tous les sites' : `Site : ${siteActif.nom}`}
        </p>
      </div>

      {erreurProduits && <Alerte type="error">{erreurProduits}</Alerte>}

      <form onSubmit={soumettre} className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        {vueGlobale && (
          <div>
            <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="siteVente">
              Site
            </label>
            <select
              id="siteVente"
              value={siteFormId}
              onChange={(e) => setSiteFormId(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
            >
              <option value="">Selectionner un site</option>
              {sitesActifs.map((site) => (
                <option key={site.id} value={site.id}>
                  {site.nom}
                </option>
              ))}
            </select>
          </div>
        )}

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="produit">
            Produit
          </label>
          <select
            id="produit"
            value={produitId}
            onChange={(e) => setProduitId(e.target.value)}
            disabled={!formulairePret}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 disabled:cursor-not-allowed disabled:bg-slate-50"
          >
            <option value="">Selectionner un produit</option>
            {produits.map((p) => (
              <option key={p.id} value={p.id}>
                {p.nom} - {formaterMontant(p.prixUnitaire)} / {p.unite}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="quantite">
            Quantite
          </label>
          <div className="flex gap-2">
            <input
              id="quantite"
              type="number"
              min="0.01"
              step="0.01"
              value={quantite}
              onChange={(e) => setQuantite(e.target.value)}
              disabled={!formulairePret}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500 disabled:cursor-not-allowed disabled:bg-slate-50"
            />
            {selecteurUniteVisible ? (
              <select
                value={uniteSaisie}
                onChange={(e) => setUniteSaisie(e.target.value)}
                className="rounded-lg border border-slate-300 px-2 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
              >
                {UNITES_POIDS.map((u) => (
                  <option key={u} value={u}>
                    {u}
                  </option>
                ))}
              </select>
            ) : (
              produitSelectionne && (
                <span className="flex items-center rounded-lg bg-slate-50 px-3 text-sm text-slate-500">
                  {produitSelectionne.unite}
                </span>
              )
            )}
          </div>
        </div>

        <div className="flex items-center justify-between rounded-lg bg-slate-50 px-4 py-3">
          <span className="text-sm font-medium text-slate-500">Montant total</span>
          <span className="text-lg font-semibold text-emerald-700">{formaterMontant(montantEstime)}</span>
        </div>

        {erreur && <Alerte type="error">{erreur}</Alerte>}
        {succes && (
          <Alerte type="success">
            Vente enregistree : {formaterMontant(succes.montantTotal)} ({succes.quantiteSaisie}{' '}
            {succes.uniteSaisie ?? ''})
          </Alerte>
        )}

        <button
          type="submit"
          disabled={soumission || !formulairePret}
          className="w-full rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {soumission ? 'Enregistrement...' : 'Confirmer la vente'}
        </button>
      </form>
    </div>
  )
}
