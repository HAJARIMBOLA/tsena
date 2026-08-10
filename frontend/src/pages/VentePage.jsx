import { useEffect, useMemo, useState } from 'react'
import * as produitService from '../services/produitService'
import * as venteService from '../services/venteService'
import { useSite } from '../hooks/useSite'
import { extraireMessageErreur } from '../api/apiError'
import { Alerte, ChargementPage } from '../components/PageState'
import { formaterMontant } from '../utils/format'

export default function VentePage() {
  const { siteActif } = useSite()
  const [produits, setProduits] = useState([])
  const [chargementProduits, setChargementProduits] = useState(true)
  const [erreurProduits, setErreurProduits] = useState(null)

  const [produitId, setProduitId] = useState('')
  const [quantite, setQuantite] = useState('')
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

  const produitSelectionne = useMemo(
    () => produits.find((p) => String(p.id) === String(produitId)) ?? null,
    [produits, produitId],
  )

  const montantEstime = useMemo(() => {
    if (!produitSelectionne || !quantite) return 0
    const quantiteNombre = Number(quantite)
    if (Number.isNaN(quantiteNombre)) return 0
    return quantiteNombre * Number(produitSelectionne.prixUnitaire)
  }, [produitSelectionne, quantite])

  async function soumettre(e) {
    e.preventDefault()
    setErreur(null)
    setSucces(null)

    if (!produitId || !quantite || Number(quantite) <= 0) {
      setErreur('Selectionnez un produit et une quantite valide.')
      return
    }

    setSoumission(true)
    try {
      const vente = await venteService.creer({
        siteId: siteActif.id,
        produitId: Number(produitId),
        quantite: Number(quantite),
      })
      setSucces(vente)
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
        <p className="text-sm text-slate-500">Site : {siteActif.nom}</p>
      </div>

      {erreurProduits && <Alerte type="error">{erreurProduits}</Alerte>}

      <form onSubmit={soumettre} className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="produit">
            Produit
          </label>
          <select
            id="produit"
            value={produitId}
            onChange={(e) => setProduitId(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
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
          <input
            id="quantite"
            type="number"
            min="0.01"
            step="0.01"
            value={quantite}
            onChange={(e) => setQuantite(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
          />
        </div>

        <div className="flex items-center justify-between rounded-lg bg-slate-50 px-4 py-3">
          <span className="text-sm font-medium text-slate-500">Montant total</span>
          <span className="text-lg font-semibold text-emerald-700">{formaterMontant(montantEstime)}</span>
        </div>

        {erreur && <Alerte type="error">{erreur}</Alerte>}
        {succes && (
          <Alerte type="success">
            Vente enregistree : {formaterMontant(succes.montantTotal)} ({succes.quantite}{' '}
            {produitSelectionne?.unite ?? ''})
          </Alerte>
        )}

        <button
          type="submit"
          disabled={soumission}
          className="w-full rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {soumission ? 'Enregistrement...' : 'Confirmer la vente'}
        </button>
      </form>
    </div>
  )
}
