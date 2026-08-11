import { useState } from 'react'
import * as compteService from '../services/compteService'
import { useAuth } from '../hooks/useAuth'
import { extraireMessageErreur } from '../api/apiError'
import { Alerte } from '../components/PageState'
import { evaluerRobustesse } from '../utils/motDePasse'

export default function ParametresPage() {
  const { utilisateur } = useAuth()

  const [motDePasseActuel, setMotDePasseActuel] = useState('')
  const [nouveauMotDePasse, setNouveauMotDePasse] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [soumission, setSoumission] = useState(false)
  const [erreur, setErreur] = useState(null)
  const [succes, setSucces] = useState(false)

  const { robuste, manquants } = evaluerRobustesse(nouveauMotDePasse)
  const confirmationValide = confirmation.length === 0 || confirmation === nouveauMotDePasse
  const formulaireValide =
    motDePasseActuel.length > 0 && robuste && confirmation.length > 0 && confirmation === nouveauMotDePasse

  async function soumettre(e) {
    e.preventDefault()
    setErreur(null)
    setSucces(false)

    if (!formulaireValide) {
      return
    }

    setSoumission(true)
    try {
      await compteService.changerMotDePasse(motDePasseActuel, nouveauMotDePasse)
      setSucces(true)
      setMotDePasseActuel('')
      setNouveauMotDePasse('')
      setConfirmation('')
    } catch (err) {
      setErreur(extraireMessageErreur(err))
    } finally {
      setSoumission(false)
    }
  }

  return (
    <div className="mx-auto max-w-lg space-y-6">
      <div>
        <h1 className="text-xl font-semibold text-slate-800">Parametres</h1>
        <p className="text-sm text-slate-500">Compte de {utilisateur.nom}</p>
      </div>

      <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="mb-3 text-sm font-semibold text-slate-600">Profil</h2>
        <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-2 text-sm">
          <dt className="text-slate-400">Nom</dt>
          <dd className="text-slate-700">{utilisateur.nom}</dd>
          <dt className="text-slate-400">Email</dt>
          <dd className="text-slate-700">{utilisateur.email}</dd>
          <dt className="text-slate-400">Role</dt>
          <dd className="text-slate-700">{utilisateur.role}</dd>
        </dl>
      </div>

      <form onSubmit={soumettre} className="space-y-4 rounded-xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="text-sm font-semibold text-slate-600">Changer le mot de passe</h2>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="motDePasseActuel">
            Mot de passe actuel
          </label>
          <input
            id="motDePasseActuel"
            type="password"
            required
            autoComplete="current-password"
            value={motDePasseActuel}
            onChange={(e) => setMotDePasseActuel(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="nouveauMotDePasse">
            Nouveau mot de passe
          </label>
          <input
            id="nouveauMotDePasse"
            type="password"
            required
            autoComplete="new-password"
            value={nouveauMotDePasse}
            onChange={(e) => setNouveauMotDePasse(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
          />
          {nouveauMotDePasse.length > 0 && !robuste && (
            <div className="mt-2">
              <Alerte type="error">Mot de passe pas assez robuste : il manque {manquants.join(', ')}.</Alerte>
            </div>
          )}
          {nouveauMotDePasse.length > 0 && robuste && (
            <div className="mt-2">
              <Alerte type="success">Mot de passe robuste.</Alerte>
            </div>
          )}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="confirmation">
            Confirmer le nouveau mot de passe
          </label>
          <input
            id="confirmation"
            type="password"
            required
            autoComplete="new-password"
            value={confirmation}
            onChange={(e) => setConfirmation(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
          />
          {!confirmationValide && (
            <div className="mt-2">
              <Alerte type="error">La confirmation ne correspond pas au nouveau mot de passe.</Alerte>
            </div>
          )}
        </div>

        {erreur && <Alerte type="error">{erreur}</Alerte>}
        {succes && <Alerte type="success">Mot de passe modifie avec succes.</Alerte>}

        <button
          type="submit"
          disabled={soumission || !formulaireValide}
          className="w-full rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {soumission ? 'Enregistrement...' : 'Changer le mot de passe'}
        </button>
      </form>
    </div>
  )
}
