import { useEffect, useState } from 'react'
import * as stockService from '../services/stockService'
import * as produitService from '../services/produitService'
import { useSite } from '../hooks/useSite'
import { extraireMessageErreur } from '../api/apiError'
import { Alerte, ChargementPage } from '../components/PageState'
import { formaterQuantite } from '../utils/format'

export default function StockPage() {
  const { siteActif } = useSite()
  const [stock, setStock] = useState([])
  const [produitsParId, setProduitsParId] = useState({})
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState(null)

  const [produitId, setProduitId] = useState('')
  const [quantiteAjout, setQuantiteAjout] = useState('')
  const [soumission, setSoumission] = useState(false)
  const [erreurFormulaire, setErreurFormulaire] = useState(null)
  const [succesFormulaire, setSuccesFormulaire] = useState(null)

  function charger() {
    setChargement(true)
    setErreur(null)
    return Promise.all([stockService.listerParSite(siteActif.id), produitService.listerActifs()])
      .then(([stockData, produitsData]) => {
        setStock(stockData)
        setProduitsParId(Object.fromEntries(produitsData.map((p) => [p.id, p])))
      })
      .catch((err) => setErreur(extraireMessageErreur(err)))
      .finally(() => setChargement(false))
  }

  useEffect(() => {
    charger()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [siteActif])

  async function soumettreReapprovisionnement(e) {
    e.preventDefault()
    setErreurFormulaire(null)
    setSuccesFormulaire(null)

    if (!produitId || !quantiteAjout || Number(quantiteAjout) <= 0) {
      setErreurFormulaire('Selectionnez un produit et une quantite valide.')
      return
    }

    setSoumission(true)
    try {
      const stockMisAJour = await stockService.reapprovisionner(
        siteActif.id,
        Number(produitId),
        Number(quantiteAjout),
      )
      setStock((precedent) =>
        precedent.map((ligne) => (ligne.produitId === stockMisAJour.produitId ? stockMisAJour : ligne)),
      )
      setSuccesFormulaire('Stock mis a jour avec succes.')
      setQuantiteAjout('')
    } catch (err) {
      setErreurFormulaire(extraireMessageErreur(err))
    } finally {
      setSoumission(false)
    }
  }

  if (chargement) {
    return <ChargementPage message="Chargement du stock..." />
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Stock</h1>
        <p className="text-sm text-slate-500">Site : {siteActif.nom}</p>
      </div>

      {erreur && <Alerte type="error">{erreur}</Alerte>}

      <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-100 text-left text-xs uppercase text-slate-400">
              <th className="px-4 py-3">Produit</th>
              <th className="px-4 py-3">Categorie</th>
              <th className="px-4 py-3 text-right">Quantite disponible</th>
            </tr>
          </thead>
          <tbody>
            {stock.length ? (
              stock.map((ligne) => {
                const produit = produitsParId[ligne.produitId]
                return (
                  <tr key={ligne.id} className="border-b border-slate-50 last:border-0">
                    <td className="px-4 py-3 font-medium text-slate-700">
                      {produit?.nom ?? `Produit #${ligne.produitId}`}
                    </td>
                    <td className="px-4 py-3 text-slate-500">{produit?.categorie ?? '-'}</td>
                    <td className="px-4 py-3 text-right text-slate-700">
                      {formaterQuantite(ligne.quantiteDisponible)} {produit?.unite ?? ''}
                    </td>
                  </tr>
                )
              })
            ) : (
              <tr>
                <td colSpan={3} className="px-4 py-10 text-center text-slate-400">
                  Aucun stock enregistre pour ce site
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-sm font-semibold text-slate-600">Reapprovisionner</h2>
        <form onSubmit={soumettreReapprovisionnement} className="flex flex-wrap items-end gap-3">
          <div className="min-w-[220px]">
            <label className="mb-1 block text-xs font-medium text-slate-500" htmlFor="produitStock">
              Produit
            </label>
            <select
              id="produitStock"
              value={produitId}
              onChange={(e) => setProduitId(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="">Selectionner</option>
              {stock.map((ligne) => (
                <option key={ligne.produitId} value={ligne.produitId}>
                  {produitsParId[ligne.produitId]?.nom ?? `Produit #${ligne.produitId}`}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-500" htmlFor="quantiteAjout">
              Quantite a ajouter
            </label>
            <input
              id="quantiteAjout"
              type="number"
              min="0.01"
              step="0.01"
              value={quantiteAjout}
              onChange={(e) => setQuantiteAjout(e.target.value)}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <button
            type="submit"
            disabled={soumission}
            className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
          >
            {soumission ? 'Envoi...' : 'Ajouter au stock'}
          </button>
        </form>

        {erreurFormulaire && (
          <div className="mt-4">
            <Alerte type="error">{erreurFormulaire}</Alerte>
          </div>
        )}
        {succesFormulaire && (
          <div className="mt-4">
            <Alerte type="success">{succesFormulaire}</Alerte>
          </div>
        )}
      </div>
    </div>
  )
}
