import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { extraireMessageErreur } from '../api/apiError'
import { Alerte } from '../components/PageState'

export default function LoginPage() {
  const { connecter } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [motDePasse, setMotDePasse] = useState('')
  const [chargement, setChargement] = useState(false)
  const [erreur, setErreur] = useState(null)

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
    <div className="flex min-h-screen items-center justify-center bg-slate-100 px-4">
      <div className="w-full max-w-sm rounded-2xl bg-white p-8 shadow-sm">
        <h1 className="mb-1 text-2xl font-semibold text-slate-800">TSENA</h1>
        <p className="mb-6 text-sm text-slate-500">Connexion a votre espace</p>

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
      </div>
    </div>
  )
}
