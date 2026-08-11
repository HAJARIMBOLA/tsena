import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { extraireMessageErreur } from '../api/apiError'
import { Alerte } from '../components/PageState'
import { evaluerRobustesse } from '../utils/motDePasse'

export default function LoginPage() {
  const [vue, setVue] = useState('login')
  const [messageBascule, setMessageBascule] = useState(null)

  if (vue === 'setup') {
    return (
      <SetupAdminForm
        onRetourConnexion={() => setVue('login')}
        onSetupDejaEffectue={(message) => {
          setMessageBascule(message)
          setVue('login')
        }}
      />
    )
  }

  return (
    <FormulaireLogin onCreerCompte={() => setVue('setup')} erreurInitiale={messageBascule} />
  )
}

function FormulaireLogin({ onCreerCompte, erreurInitiale }) {
  const { connecter } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [motDePasse, setMotDePasse] = useState('')
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(erreurInitiale ?? null)

  const destination = location.state?.from?.pathname ?? '/sites'

  async function soumettre(e) {
    e.preventDefault()
    setErreur(null)
    setChargement(true)
    try {
      await connecter(email, motDePasse)
      navigate(destination, { replace: true })
    } catch (err) {
      setErreur(extraireMessageErreur(err))
    } finally {
      setChargement(false)
    }
  }

  return (
    <PageAuth titre="TSENA" sousTitre="Connexion a votre espace">
      <form onSubmit={soumettre} className="space-y-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="email">
            Email
          </label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
            autoComplete="username"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="motDePasse">
            Mot de passe
          </label>
          <input
            id="motDePasse"
            type="password"
            required
            value={motDePasse}
            onChange={(e) => setMotDePasse(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
            autoComplete="current-password"
          />
        </div>

        {erreur && <Alerte type="error">{erreur}</Alerte>}

        <button
          type="submit"
          disabled={chargement}
          className="w-full rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {chargement ? 'Connexion...' : 'Se connecter'}
        </button>
      </form>

      <div className="mt-6 border-t border-slate-200 pt-4 text-center">
        <button
          type="button"
          onClick={onCreerCompte}
          className="text-sm font-medium text-emerald-600 hover:text-emerald-700"
        >
          Creer un compte administrateur
        </button>
      </div>
    </PageAuth>
  )
}

function SetupAdminForm({ onRetourConnexion, onSetupDejaEffectue }) {
  const { enregistrer } = useAuth()
  const navigate = useNavigate()
  const [nom, setNom] = useState('')
  const [email, setEmail] = useState('')
  const [motDePasse, setMotDePasse] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)

  const { robuste, manquants } = evaluerRobustesse(motDePasse)
  const confirmationValide = confirmation.length === 0 || confirmation === motDePasse
  const formulaireValide =
    nom.trim().length > 0 && email.trim().length > 0 && robuste && confirmation.length > 0 && confirmation === motDePasse

  async function soumettre(e) {
    e.preventDefault()
    setErreur(null)

    if (!formulaireValide) {
      return
    }

    setChargement(true)
    try {
      await enregistrer(nom, email, motDePasse)
      navigate('/sites', { replace: true })
    } catch (err) {
      const message = extraireMessageErreur(err)
      if (err?.response?.status === 403) {
        onSetupDejaEffectue(message)
      } else {
        setErreur(message)
      }
    } finally {
      setChargement(false)
    }
  }

  return (
    <PageAuth titre="TSENA" sousTitre="Configurer le compte administrateur">
      <form onSubmit={soumettre} className="space-y-4">
        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="nom">
            Nom
          </label>
          <input
            id="nom"
            type="text"
            required
            value={nom}
            onChange={(e) => setNom(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
            autoComplete="name"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="email">
            Email
          </label>
          <input
            id="email"
            type="email"
            required
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
            autoComplete="username"
          />
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="motDePasse">
            Mot de passe
          </label>
          <input
            id="motDePasse"
            type="password"
            required
            value={motDePasse}
            onChange={(e) => setMotDePasse(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
            autoComplete="new-password"
          />
          {motDePasse.length > 0 && !robuste && (
            <div className="mt-2">
              <Alerte type="error">Mot de passe pas assez robuste : il manque {manquants.join(', ')}.</Alerte>
            </div>
          )}
        </div>

        <div>
          <label className="mb-1 block text-sm font-medium text-slate-600" htmlFor="confirmation">
            Confirmer le mot de passe
          </label>
          <input
            id="confirmation"
            type="password"
            required
            value={confirmation}
            onChange={(e) => setConfirmation(e.target.value)}
            className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-emerald-500 focus:outline-none focus:ring-1 focus:ring-emerald-500"
            autoComplete="new-password"
          />
          {!confirmationValide && (
            <div className="mt-2">
              <Alerte type="error">La confirmation ne correspond pas au mot de passe.</Alerte>
            </div>
          )}
        </div>

        {erreur && <Alerte type="error">{erreur}</Alerte>}

        <button
          type="submit"
          disabled={chargement || !formulaireValide}
          className="w-full rounded-lg bg-emerald-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-emerald-700 disabled:cursor-not-allowed disabled:opacity-60"
        >
          {chargement ? 'Creation du compte...' : 'Creer le compte administrateur'}
        </button>
      </form>

      <div className="mt-6 border-t border-slate-200 pt-4 text-center">
        <button
          type="button"
          onClick={onRetourConnexion}
          className="text-sm font-medium text-emerald-600 hover:text-emerald-700"
        >
          Se connecter
        </button>
      </div>
    </PageAuth>
  )
}

function PageAuth({ titre, sousTitre, children }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <div className="w-full max-w-sm rounded-2xl bg-white p-8 shadow-sm">
        <h1 className="mb-1 text-2xl font-semibold text-slate-800">{titre}</h1>
        <p className="mb-6 text-sm text-slate-500">{sousTitre}</p>
        {children}
      </div>
    </div>
  )
}
