import { useEffect, useMemo, useState } from 'react'
import * as stockService from '../services/stockService'
import * as produitService from '../services/produitService'
import { useAuth } from '../hooks/useAuth'
import { useSite } from '../hooks/useSite'
import { extraireMessageErreur } from '../api/apiError'
import { Alerte, ChargementPage } from '../components/PageState'
import { formaterMontant, formaterQuantite } from '../utils/format'
import { convertirQuantite, estUnitePoids, UNITES_POIDS } from '../utils/unite'
import Badge from '../components/Badge'

export default function StockPage() {
  const { estAdmin } = useAuth()
  const { siteActif, vueGlobale, sites } = useSite()
  const [stock, setStock] = useState([])
  const [produitsParId, setProduitsParId] = useState({})
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState(null)

  const [siteFormId, setSiteFormId] = useState('')
  const [produitId, setProduitId] = useState('')
  const [quantiteAjout, setQuantiteAjout] = useState('')
  const [prixUnitaireAjout, setPrixUnitaireAjout] = useState('')
  const [uniteSaisie, setUniteSaisie] = useState('')
  const [soumission, setSoumission] = useState(false)
  const [erreurFormulaire, setErreurFormulaire] = useState(null)
  const [succesFormulaire, setSuccesFormulaire] = useState(null)

  const [editionPrixId, setEditionPrixId] = useState(null)
  const [nouveauPrix, setNouveauPrix] = useState('')
  const [erreurPrix, setErreurPrix] = useState(null)
  const [soumissionPrix, setSoumissionPrix] = useState(false)

  function charger() {
    setChargement(true)
    setErreur(null)
    const requeteStock = vueGlobale ? stockService.listerTout() : stockService.listerParSite(siteActif.id)
    return Promise.all([requeteStock, produitService.listerActifs()])
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
  }, [siteActif, vueGlobale])

  const siteIdEffectif = vueGlobale ? Number(siteFormId) || null : siteActif.id

  const ligneExistante = useMemo(
    () =>
      stock.find(
        (ligne) => ligne.produitId === Number(produitId) && (!vueGlobale || ligne.siteId === siteIdEffectif),
      ),
    [stock, produitId, vueGlobale, siteIdEffectif],
  )

  const produitChoisi = produitsParId[Number(produitId)]
  const selecteurUniteVisible = Boolean(produitChoisi) && estUnitePoids(produitChoisi.unite)

  useEffect(() => {
    setUniteSaisie(produitChoisi?.unite ?? '')
    setPrixUnitaireAjout('')
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [produitChoisi?.id])

  async function soumettreReapprovisionnement(e) {
    e.preventDefault()
    setErreurFormulaire(null)
    setSuccesFormulaire(null)

    if (vueGlobale && !siteFormId) {
      setErreurFormulaire('Selectionnez un site.')
      return
    }

    if (!produitId || !quantiteAjout || Number(quantiteAjout) <= 0) {
      setErreurFormulaire('Selectionnez un produit et une quantite valide.')
      return
    }

    if (!ligneExistante && (!prixUnitaireAjout || Number(prixUnitaireAjout) <= 0)) {
      setErreurFormulaire('Indiquez un prix unitaire valide.')
      return
    }

    const quantiteEnUniteProduit = convertirQuantite(
      Number(quantiteAjout),
      uniteSaisie || produitChoisi.unite,
      produitChoisi.unite,
    )

    setSoumission(true)
    try {
      if (ligneExistante) {
        const stockMisAJour = await stockService.reapprovisionner(
          siteIdEffectif,
          Number(produitId),
          quantiteEnUniteProduit,
        )
        setStock((precedent) =>
          precedent.map((ligne) =>
            ligne.produitId === stockMisAJour.produitId && ligne.siteId === stockMisAJour.siteId
              ? stockMisAJour
              : ligne,
          ),
        )
        setSuccesFormulaire('Stock mis a jour avec succes.')
      } else {
        const nouvelleLigne = await stockService.affecter({
          siteId: siteIdEffectif,
          produitId: Number(produitId),
          quantiteDisponible: quantiteEnUniteProduit,
          prixUnitaire: Number(prixUnitaireAjout),
        })
        setStock((precedent) => [...precedent, nouvelleLigne])
        setSuccesFormulaire('Produit ajoute au stock du site.')
      }
      setProduitId('')
      setQuantiteAjout('')
      setPrixUnitaireAjout('')
    } catch (err) {
      setErreurFormulaire(extraireMessageErreur(err))
    } finally {
      setSoumission(false)
    }
  }

  function commencerEditionPrix(ligne) {
    setEditionPrixId(ligne.id)
    setNouveauPrix(String(ligne.prixUnitaire))
    setErreurPrix(null)
  }

  function annulerEditionPrix() {
    setEditionPrixId(null)
    setNouveauPrix('')
    setErreurPrix(null)
  }

  async function enregistrerPrix(ligne) {
    if (!nouveauPrix || Number(nouveauPrix) <= 0) {
      setErreurPrix('Indiquez un prix valide.')
      return
    }

    setSoumissionPrix(true)
    try {
      const ligneMiseAJour = await stockService.modifierPrix(ligne.siteId, ligne.produitId, Number(nouveauPrix))
      setStock((precedent) =>
        precedent.map((l) => (l.id === ligneMiseAJour.id ? ligneMiseAJour : l)),
      )
      annulerEditionPrix()
    } catch (err) {
      setErreurPrix(extraireMessageErreur(err))
    } finally {
      setSoumissionPrix(false)
    }
  }

  const produitsSelectionnables = Object.values(produitsParId)

  const sitesActifs = sites.filter((s) => s.actif !== false)

  const stockAffiche = useMemo(() => {
    if (!vueGlobale) return stock
    return [...stock].sort((a, b) => (a.siteNom ?? '').localeCompare(b.siteNom ?? ''))
  }, [stock, vueGlobale])

  const formulairePret = !vueGlobale || Boolean(siteFormId)

  if (chargement) {
    return <ChargementPage message="Chargement du stock..." />
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Stock</h1>
        <p className="text-sm text-slate-500">
          {vueGlobale ? 'Vue globale - tous les sites' : `Site : ${siteActif.nom}`}
        </p>
      </div>

      {erreur && <Alerte type="error">{erreur}</Alerte>}

      <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-100 text-left text-xs uppercase tracking-wide text-slate-400">
              {vueGlobale && <th className="px-4 py-3">Site</th>}
              <th className="px-4 py-3">Produit</th>
              <th className="px-4 py-3">Categorie</th>
              <th className="px-4 py-3 text-right">Quantite disponible</th>
              <th className="px-4 py-3 text-right">Prix unitaire</th>
            </tr>
          </thead>
          <tbody>
            {stockAffiche.length ? (
              stockAffiche.map((ligne) => {
                const produit = produitsParId[ligne.produitId]
                const enEdition = editionPrixId === ligne.id
                return (
                  <tr key={ligne.id} className="border-b border-slate-50 transition last:border-0 hover:bg-slate-50">
                    {vueGlobale && (
                      <td className="px-4 py-3">
                        <Badge color="emerald">{ligne.siteNom ?? `Site #${ligne.siteId}`}</Badge>
                      </td>
                    )}
                    <td className="px-4 py-3 font-medium text-slate-700">
                      {produit?.nom ?? `Produit #${ligne.produitId}`}
                    </td>
                    <td className="px-4 py-3 text-slate-500">{produit?.categorie ?? '-'}</td>
                    <td className="px-4 py-3 text-right text-slate-700">
                      {formaterQuantite(ligne.quantiteDisponible)} {produit?.unite ?? ''}
                    </td>
                    <td className="px-4 py-3 text-right">
                      {enEdition ? (
                        <div className="flex items-center justify-end gap-2">
                          <input
                            type="number"
                            min="0.01"
                            step="0.01"
                            value={nouveauPrix}
                            onChange={(e) => setNouveauPrix(e.target.value)}
                            className="w-24 rounded-lg border border-slate-300 px-2 py-1 text-right text-sm"
                          />
                          <button
                            type="button"
                            disabled={soumissionPrix}
                            onClick={() => enregistrerPrix(ligne)}
                            className="rounded-lg bg-emerald-600 px-2 py-1 text-xs font-medium text-white hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
                          >
                            OK
                          </button>
                          <button
                            type="button"
                            onClick={annulerEditionPrix}
                            className="rounded-lg border border-slate-300 px-2 py-1 text-xs text-slate-500 hover:bg-slate-50"
                          >
                            Annuler
                          </button>
                        </div>
                      ) : (
                        <div className="flex items-center justify-end gap-2">
                          <span className="text-slate-700">{formaterMontant(ligne.prixUnitaire)}</span>
                          {estAdmin && (
                            <button
                              type="button"
                              onClick={() => commencerEditionPrix(ligne)}
                              className="text-xs font-medium text-emerald-600 hover:text-emerald-700"
                            >
                              Modifier
                            </button>
                          )}
                        </div>
                      )}
                    </td>
                  </tr>
                )
              })
            ) : (
              <tr>
                <td colSpan={vueGlobale ? 5 : 4} className="px-4 py-10 text-center text-slate-400">
                  Aucun stock enregistre
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {erreurPrix && <Alerte type="error">{erreurPrix}</Alerte>}

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="mb-4 text-sm font-semibold text-slate-600">Reapprovisionner</h2>
        <form onSubmit={soumettreReapprovisionnement} className="flex flex-wrap items-end gap-3">
          {vueGlobale && (
            <div className="min-w-[200px]">
              <label className="mb-1 block text-xs font-medium text-slate-500" htmlFor="siteStock">
                Site
              </label>
              <select
                id="siteStock"
                value={siteFormId}
                onChange={(e) => {
                  setSiteFormId(e.target.value)
                  setProduitId('')
                }}
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm"
              >
                <option value="">Selectionner</option>
                {sitesActifs.map((site) => (
                  <option key={site.id} value={site.id}>
                    {site.nom}
                  </option>
                ))}
              </select>
            </div>
          )}
          <div className="min-w-[220px]">
            <label className="mb-1 block text-xs font-medium text-slate-500" htmlFor="produitStock">
              Produit
            </label>
            <select
              id="produitStock"
              value={produitId}
              onChange={(e) => setProduitId(e.target.value)}
              disabled={!formulairePret}
              className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm disabled:cursor-not-allowed disabled:bg-slate-50"
            >
              <option value="">Selectionner</option>
              {produitsSelectionnables.map((produit) => {
                const dejaEnStock = stock.some(
                  (ligne) => ligne.produitId === produit.id && (!vueGlobale || ligne.siteId === siteIdEffectif),
                )
                return (
                  <option key={produit.id} value={produit.id}>
                    {produit.nom}
                    {!dejaEnStock ? ' (nouveau)' : ''}
                  </option>
                )
              })}
            </select>
          </div>
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-500" htmlFor="quantiteAjout">
              {ligneExistante ? 'Quantite a ajouter' : 'Quantite initiale'}
            </label>
            <div className="flex gap-2">
              <input
                id="quantiteAjout"
                type="number"
                min="0.01"
                step="0.01"
                value={quantiteAjout}
                onChange={(e) => setQuantiteAjout(e.target.value)}
                disabled={!formulairePret}
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm disabled:cursor-not-allowed disabled:bg-slate-50"
              />
              {selecteurUniteVisible ? (
                <select
                  value={uniteSaisie}
                  onChange={(e) => setUniteSaisie(e.target.value)}
                  className="rounded-lg border border-slate-300 px-2 py-2 text-sm"
                >
                  {UNITES_POIDS.map((u) => (
                    <option key={u} value={u}>
                      {u}
                    </option>
                  ))}
                </select>
              ) : (
                produitChoisi && (
                  <span className="flex items-center rounded-lg bg-slate-50 px-3 text-sm text-slate-500">
                    {produitChoisi.unite}
                  </span>
                )
              )}
            </div>
          </div>
          {!ligneExistante && (
            <div>
              <label className="mb-1 block text-xs font-medium text-slate-500" htmlFor="prixUnitaireAjout">
                Prix unitaire
              </label>
              <input
                id="prixUnitaireAjout"
                type="number"
                min="0.01"
                step="0.01"
                value={prixUnitaireAjout}
                onChange={(e) => setPrixUnitaireAjout(e.target.value)}
                disabled={!formulairePret}
                className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm disabled:cursor-not-allowed disabled:bg-slate-50"
              />
            </div>
          )}
          <button
            type="submit"
            disabled={soumission || !formulairePret}
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
