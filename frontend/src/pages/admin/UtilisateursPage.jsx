import { useEffect, useState } from 'react'
import * as utilisateurService from '../../services/utilisateurService'
import * as siteService from '../../services/siteService'
import { useAuth } from '../../hooks/useAuth'
import { extraireMessageErreur } from '../../api/apiError'
import { Alerte, ChargementPage } from '../../components/PageState'

const FORMULAIRE_VIDE = { nom: '', email: '', motDePasse: '', role: 'EMPLOYE', siteIds: [] }

export default function UtilisateursPage() {
  const { utilisateur: compteConnecte } = useAuth()
  const [utilisateurs, setUtilisateurs] = useState([])
  const [sites, setSites] = useState([])
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState(null)

  const [formulaire, setFormulaire] = useState(FORMULAIRE_VIDE)
  const [creationEnCours, setCreationEnCours] = useState(false)
  const [erreurCreation, setErreurCreation] = useState(null)

  const [editionSitesId, setEditionSitesId] = useState(null)
  const [sitesSelectionnes, setSitesSelectionnes] = useState([])
  const [erreurEdition, setErreurEdition] = useState(null)

  function charger() {
    setChargement(true)
    setErreur(null)
    return Promise.all([utilisateurService.lister(), siteService.lister()])
      .then(([utilisateursData, sitesData]) => {
        setUtilisateurs(utilisateursData)
        setSites(sitesData)
      })
      .catch((err) => setErreur(extraireMessageErreur(err)))
      .finally(() => setChargement(false))
  }

  useEffect(() => {
    charger()
  }, [])

  async function creerUtilisateur(e) {
    e.preventDefault()
    setErreurCreation(null)
    setCreationEnCours(true)
    try {
      const utilisateur = await utilisateurService.creer(formulaire)
      setUtilisateurs((precedent) => [utilisateur, ...precedent])
      setFormulaire(FORMULAIRE_VIDE)
    } catch (err) {
      setErreurCreation(extraireMessageErreur(err))
    } finally {
      setCreationEnCours(false)
    }
  }

  function demarrerEditionSites(utilisateur) {
    setEditionSitesId(utilisateur.id)
    setErreurEdition(null)
    setSitesSelectionnes(utilisateur.siteIds ?? [])
  }

  function basculerSiteEdition(siteId) {
    setSitesSelectionnes((precedent) =>
      precedent.includes(siteId) ? precedent.filter((id) => id !== siteId) : [...precedent, siteId],
    )
  }

  async function enregistrerSites(id) {
    setErreurEdition(null)
    try {
      const utilisateur = await utilisateurService.modifierSites(id, sitesSelectionnes)
      setUtilisateurs((precedent) => precedent.map((u) => (u.id === id ? utilisateur : u)))
      setEditionSitesId(null)
    } catch (err) {
      setErreurEdition(extraireMessageErreur(err))
    }
  }

  async function desactiverUtilisateur(id) {
    if (!window.confirm('Desactiver ce compte ?')) return
    try {
      await utilisateurService.desactiver(id)
      setUtilisateurs((precedent) => precedent.map((u) => (u.id === id ? { ...u, actif: false } : u)))
    } catch (err) {
      setErreur(extraireMessageErreur(err))
    }
  }

  async function reactiverUtilisateur(id) {
    try {
      await utilisateurService.reactiver(id)
      setUtilisateurs((precedent) => precedent.map((u) => (u.id === id ? { ...u, actif: true } : u)))
    } catch (err) {
      setErreur(extraireMessageErreur(err))
    }
  }

  async function supprimerUtilisateur(id) {
    if (!window.confirm('Supprimer definitivement ce compte ? Cette action est irreversible.')) return
    try {
      await utilisateurService.supprimer(id)
      setUtilisateurs((precedent) => precedent.filter((u) => u.id !== id))
    } catch (err) {
      setErreur(extraireMessageErreur(err))
    }
  }

  function nomsSites(siteIds) {
    if (!siteIds?.length) return '-'
    return siteIds
      .map((id) => sites.find((s) => s.id === id)?.nom ?? `#${id}`)
      .join(', ')
  }

  if (chargement) {
    return <ChargementPage message="Chargement des utilisateurs..." />
  }

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-semibold text-slate-800">Utilisateurs</h1>

      {erreur && <Alerte type="error">{erreur}</Alerte>}

      <form
        onSubmit={creerUtilisateur}
        className="space-y-4 rounded-xl border border-slate-200 bg-white p-4 shadow-sm"
      >
        <div className="flex flex-wrap items-end gap-3">
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
            <label className="mb-1 block text-xs font-medium text-slate-500">Email</label>
            <input
              required
              type="email"
              value={formulaire.email}
              onChange={(e) => setFormulaire((f) => ({ ...f, email: e.target.value }))}
              className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-500">Mot de passe</label>
            <input
              required
              type="password"
              value={formulaire.motDePasse}
              onChange={(e) => setFormulaire((f) => ({ ...f, motDePasse: e.target.value }))}
              className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-medium text-slate-500">Role</label>
            <select
              value={formulaire.role}
              onChange={(e) => setFormulaire((f) => ({ ...f, role: e.target.value }))}
              className="rounded-lg border border-slate-300 px-3 py-2 text-sm"
            >
              <option value="EMPLOYE">EMPLOYE</option>
              <option value="ADMIN">ADMIN</option>
            </select>
          </div>
          <button
            type="submit"
            disabled={creationEnCours}
            className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700 disabled:opacity-60"
          >
            {creationEnCours ? 'Creation...' : 'Creer le compte'}
          </button>
        </div>

        {formulaire.role === 'EMPLOYE' && (
          <p className="text-xs text-slate-400">
            Les sites autorises se configurent apres la creation, via "Modifier les sites" dans le tableau
            ci-dessous.
          </p>
        )}
      </form>
      {erreurCreation && <Alerte type="error">{erreurCreation}</Alerte>}

      <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white shadow-sm">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-slate-100 text-left text-xs uppercase text-slate-400">
              <th className="px-4 py-3">Nom</th>
              <th className="px-4 py-3">Email</th>
              <th className="px-4 py-3">Role</th>
              <th className="px-4 py-3">Sites autorises</th>
              <th className="px-4 py-3">Statut</th>
              <th className="px-4 py-3" />
            </tr>
          </thead>
          <tbody>
            {utilisateurs.map((utilisateur) => (
              <tr key={utilisateur.id} className="border-b border-slate-50 last:border-0 align-top">
                <td className="px-4 py-3 font-medium text-slate-700">{utilisateur.nom}</td>
                <td className="px-4 py-3 text-slate-500">{utilisateur.email}</td>
                <td className="px-4 py-3 text-slate-500">{utilisateur.role}</td>
                <td className="px-4 py-3 text-slate-500">
                  {editionSitesId === utilisateur.id ? (
                    <div className="space-y-2">
                      <div className="flex flex-wrap gap-2">
                        {sites.map((site) => (
                          <label
                            key={site.id}
                            className="flex items-center gap-1.5 rounded-lg border border-slate-200 px-2 py-1 text-xs"
                          >
                            <input
                              type="checkbox"
                              checked={sitesSelectionnes.includes(site.id)}
                              onChange={() => basculerSiteEdition(site.id)}
                            />
                            {site.nom}
                          </label>
                        ))}
                      </div>
                      {erreurEdition && <Alerte type="error">{erreurEdition}</Alerte>}
                      <div className="flex gap-2">
                        <button
                          type="button"
                          onClick={() => enregistrerSites(utilisateur.id)}
                          className="rounded-md bg-emerald-600 px-3 py-1 text-xs font-medium text-white hover:bg-emerald-700"
                        >
                          Enregistrer
                        </button>
                        <button
                          type="button"
                          onClick={() => setEditionSitesId(null)}
                          className="rounded-md border border-slate-200 px-3 py-1 text-xs text-slate-500 hover:bg-slate-100"
                        >
                          Annuler
                        </button>
                      </div>
                    </div>
                  ) : utilisateur.role === 'ADMIN' ? (
                    'Tous les sites (acces global)'
                  ) : (
                    nomsSites(utilisateur.siteIds)
                  )}
                </td>
                <td className="px-4 py-3">
                  <span
                    className={`rounded-full px-2 py-0.5 text-xs font-medium ${
                      utilisateur.actif ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                    }`}
                  >
                    {utilisateur.actif ? 'Actif' : 'Inactif'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex justify-end gap-2">
                    {utilisateur.role === 'EMPLOYE' && editionSitesId !== utilisateur.id && (
                      <button
                        type="button"
                        onClick={() => demarrerEditionSites(utilisateur)}
                        className="rounded-md border border-slate-200 px-3 py-1 text-xs text-slate-600 hover:bg-slate-100"
                      >
                        Modifier les sites
                      </button>
                    )}
                    {utilisateur.actif && utilisateur.id !== compteConnecte?.id && (
                      <button
                        type="button"
                        onClick={() => desactiverUtilisateur(utilisateur.id)}
                        className="rounded-md border border-red-200 px-3 py-1 text-xs text-red-600 hover:bg-red-50"
                      >
                        Desactiver
                      </button>
                    )}
                    {!utilisateur.actif && (
                      <button
                        type="button"
                        onClick={() => reactiverUtilisateur(utilisateur.id)}
                        className="rounded-md border border-emerald-200 px-3 py-1 text-xs text-emerald-600 hover:bg-emerald-50"
                      >
                        Reactiver
                      </button>
                    )}
                    {utilisateur.role === 'EMPLOYE' && (
                      <button
                        type="button"
                        onClick={() => supprimerUtilisateur(utilisateur.id)}
                        className="rounded-md border border-red-200 px-3 py-1 text-xs text-red-600 hover:bg-red-50"
                      >
                        Supprimer
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
