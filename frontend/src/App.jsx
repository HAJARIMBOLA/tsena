import { Navigate, Route, Routes } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import SiteSelectPage from './pages/SiteSelectPage'
import DashboardPage from './pages/DashboardPage'
import VentePage from './pages/VentePage'
import HistoriquePage from './pages/HistoriquePage'
import StockPage from './pages/StockPage'
import ProduitsPage from './pages/admin/ProduitsPage'
import SitesPage from './pages/admin/SitesPage'
import UtilisateursPage from './pages/admin/UtilisateursPage'
import ProtectedRoute from './components/ProtectedRoute'
import SiteObligatoireRoute from './components/SiteObligatoireRoute'
import SelectionRequiseRoute from './components/SelectionRequiseRoute'
import Layout from './components/Layout'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/sites" element={<SiteSelectPage />} />

        <Route element={<Layout />}>
          <Route element={<SelectionRequiseRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
          </Route>

          <Route element={<SiteObligatoireRoute />}>
            <Route path="/vente" element={<VentePage />} />
            <Route path="/historique" element={<HistoriquePage />} />
            <Route path="/stock" element={<StockPage />} />
          </Route>

          <Route element={<ProtectedRoute role="ADMIN" />}>
            <Route path="/admin/produits" element={<ProduitsPage />} />
            <Route path="/admin/sites" element={<SitesPage />} />
            <Route path="/admin/utilisateurs" element={<UtilisateursPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default App
