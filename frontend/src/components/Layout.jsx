import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useSite } from '../hooks/useSite'

const lienClasses = ({ isActive }) =>
  `rounded-md px-3 py-2 text-sm font-medium transition ${
    isActive ? 'bg-emerald-600 text-white' : 'text-slate-600 hover:bg-slate-100'
  }`

export default function Layout() {
  const { utilisateur, estAdmin, deconnecter } = useAuth()
  const { siteActif, vueGlobale, reinitialiserSelection } = useSite()
  const navigate = useNavigate()

  function seDeconnecter() {
    deconnecter()
    navigate('/login', { replace: true })
  }

  function changerDeSite() {
    reinitialiserSelection()
    navigate('/sites')
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-3 px-4 py-3">
          <div className="flex flex-wrap items-center gap-6">
            <span className="text-lg font-bold text-emerald-700">TSENA</span>
            <nav className="flex flex-wrap items-center gap-1">
              <NavLink to="/dashboard" className={lienClasses}>
                Dashboard
              </NavLink>
              <NavLink to="/vente" className={lienClasses}>
                Vente
              </NavLink>
              <NavLink to="/historique" className={lienClasses}>
                Historique
              </NavLink>
              <NavLink to="/stock" className={lienClasses}>
                Stock
              </NavLink>
              {estAdmin && (
                <>
                  <span className="mx-1 h-4 w-px bg-slate-200" />
                  <NavLink to="/admin/produits" className={lienClasses}>
                    Produits
                  </NavLink>
                  <NavLink to="/admin/sites" className={lienClasses}>
                    Sites
                  </NavLink>
                  <NavLink to="/admin/utilisateurs" className={lienClasses}>
                    Utilisateurs
                  </NavLink>
                </>
              )}
            </nav>
          </div>

          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={changerDeSite}
              className="rounded-md border border-slate-200 px-3 py-1.5 text-sm text-slate-600 hover:bg-slate-100"
            >
              {vueGlobale ? 'Vue globale' : siteActif ? siteActif.nom : 'Choisir un site'}
            </button>
            <div className="text-right text-sm leading-tight">
              <p className="font-medium text-slate-700">{utilisateur.nom}</p>
              <p className="text-xs text-slate-400">{utilisateur.role}</p>
            </div>
            <button
              type="button"
              onClick={seDeconnecter}
              className="rounded-md px-3 py-1.5 text-sm text-slate-500 hover:bg-slate-100"
            >
              Deconnexion
            </button>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
