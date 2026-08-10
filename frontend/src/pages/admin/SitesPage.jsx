import { useEffect, useState } from 'react'
import * as siteService from '../../services/siteService'
import { extraireMessageErreur } from '../../api/apiError'
import { Alerte, ChargementPage } from '../../components/PageState'

const FORMULAIRE_VIDE = { nom: '', localisation: '' }

export default function SitesPage() {
  const [sites, setSites] = useState([])
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState(null)

  const [formulaire, setFormulaire] = useState(FORMULAIRE_VIDE)
  const [creationEnCours, setCreationEnCours] = useState(false)
  const [erreurCreation, setErreurCreation] = useState(null)

  const [editionId, setEditionId] = useState(null)
  const [formulaireEdition, setFormulaireEdition] = useState(FORMULAIRE_VIDE)
  const [erreurEdition, setErreurEdition] = useState(null)

  function charger() {
    setChargement(true)
    setErreur(null)
    return siteService
      .lister()
      .then(setSites)
      .catch((err) => setErreur(extraireMessageErreur(err)))
      .finally(() => setChargement(false))
  }

  useEffect(() => {
    charger()
  }, [])

  async function creerSite(e) {
    e.preventDefault()
    setErreurCreation(null)
    setCreationEnCours(true)
    try {
      const site = await siteService.creer(formulaire)
      setSites((precedent) => [site, ...precedent])
      setFormulaire(FORMULAIRE_VIDE)
    } catch (err) {
      setErreurCreation(extraireMessageErreur(err))
    } finally {
      setCreationEnCours(false)
    }
  }

  function demarrerEdition(site) {
    setEditionId(site.id)
    setErreurEdition(null)
    setFormulaireEdition({ nom: site.nom, localisation: site.localisation })
  }

  async function enregistrerEdition(id) {
    setErreurEdition(null)
    try {
      const site = await siteService.modifier(id, formulaireEdition)
      setSites((precedent) => precedent.map((s) => (s.id === id ? site : s)))
      setEditionId(null)
    } catch (err) {
      setErreurEdition(extraireMessageErreur(err))
    }
  }

  async function desactiverSite(id) {
    if (!window.confirm('Desactiver ce site ?')) return
    try {
      await siteService.desactiver(id)
      setSites((precedent) => precedent.map((s) => (s.id === id ? { ...s, actif: false } : s)))
    } catch (err) {
      setErreur(extraireMessageErreur(err))
    }
  }

  if (chargement) {
    return <ChargementPage message="Chargement des sites..." />
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-800">Sites</h1>

      {erreur && <Alerte type="error">{erreur}</Alerte>}

      <form
        onSubmit={creerSite}
        className="flex flex-wrap items-end gap-3 rounded-xl border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-500">Nom</label>
          <input
            required
            value={formulaire.nom}
            onChange={(e) => setFormulaire((f) => ({ ...f, nom: e.target.value }))}
            className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>
        <div>
          <label className="mb-1 block text-xs font-medium text-slate-500">Localisation</label>
          <input
            required
            value={formulaire.localisation}
            onChange={(e) => setFormulaire((f) => ({ ...f, localisation: e.target.value }))}
            className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
          />
        </div>
        <button
          type="submit"
          disabled={creationEnCours}
          className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
        >
          {creationEnCours ? 'Creation...' : 'Ajouter'}
        </button>
      </form>
      {erreurCreation && <Alerte type="error">{erreurCreation}</Alerte>}

      <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-100 text-left text-xs uppercase text-slate-400">
              <th className="px-4 py-3">Nom</th>
              <th className="px-4 py-3">Localisation</th>
              <th className="px-4 py-3">Statut</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody>
            {sites.map((site) =>
              editionId === site.id ? (
                <tr key={site.id} className="border-b border-slate-50 bg-slate-50">
                  <td className="px-4 py-2">
                    <input
                      value={formulaireEdition.nom}
                      onChange={(e) => setFormulaireEdition((f) => ({ ...f, nom: e.target.value }))}
                      className="w-full rounded-md border border-slate-300 px-2 py-1 text-sm"
                    />
                  </td>
                  <td className="px-4 py-2">
                    <input
                      value={formulaireEdition.localisation}
                      onChange={(e) => setFormulaireEdition((f) => ({ ...f, localisation: e.target.value }))}
                      className="w-full rounded-md border border-slate-300 px-2 py-1 text-sm"
                    />
                  </td>
                  <td className="px-4 py-2" />
                  <td className="px-4 py-2">
                    <div className="flex justify-end gap-2">
                      <button
                        type="button"
                        onClick={() => enregistrerEdition(site.id)}
                        className="rounded-md bg-emerald-600 px-3 py-1 text-xs font-medium text-white hover:bg-emerald-700"
                      >
                        Enregistrer
                      </button>
                      <button
                        type="button"
                        onClick={() => setEditionId(null)}
                        className="rounded-md border border-slate-200 px-3 py-1 text-xs text-slate-500 hover:bg-slate-100"
                      >
                        Annuler
                      </button>
                    </div>
                  </td>
                </tr>
              ) : (
                <tr key={site.id} className="border-b border-slate-50 last:border-0">
                  <td className="px-4 py-3 font-medium text-slate-700">{site.nom}</td>
                  <td className="px-4 py-3 text-slate-500">{site.localisation}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                        site.actif ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                      }`}
                    >
                      {site.actif ? 'Actif' : 'Inactif'}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex justify-end gap-2">
                      <button
                        type="button"
                        onClick={() => demarrerEdition(site)}
                        className="rounded-md border border-slate-200 px-3 py-1 text-xs text-slate-600 hover:bg-slate-100"
                      >
                        Modifier
                      </button>
                      {site.actif && (
                        <button
                          type="button"
                          onClick={() => desactiverSite(site.id)}
                          className="rounded-md border border-red-200 px-3 py-1 text-xs text-red-600 hover:bg-red-50"
                        >
                          Desactiver
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ),
            )}
          </tbody>
        </table>
      </div>
      {erreurEdition && <Alerte type="error">{erreurEdition}</Alerte>}
    </div>
  )
}
