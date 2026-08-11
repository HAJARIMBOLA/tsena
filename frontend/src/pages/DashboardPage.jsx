import { useEffect, useState } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import * as dashboardService from '../services/dashboardService'
import { useSite } from '../hooks/useSite'
import { extraireMessageErreur } from '../api/apiError'
import { Alerte, ChargementPage } from '../components/PageState'
import StatTile from '../components/StatTile'
import PeriodeFilter from '../components/PeriodeFilter'
import { formaterDate, formaterMontant, formaterQuantite } from '../utils/format'

export default function DashboardPage() {
  const { siteActif, vueGlobale } = useSite()
  const [periode, setPeriode] = useState('jour')
  const [donnees, setDonnees] = useState(null)
  const [chargement, setChargement] = useState(true)
  const [erreur, setErreur] = useState(null)

  useEffect(() => {
    let annule = false
    setChargement(true)
    setErreur(null)

    const requete = vueGlobale
      ? dashboardService.dashboardGlobal(periode)
      : dashboardService.dashboardSite(siteActif.id, periode)

    requete
      .then((data) => {
        if (!annule) setDonnees(data)
      })
      .catch((err) => {
        if (!annule) setErreur(extraireMessageErreur(err))
      })
      .finally(() => {
        if (!annule) setChargement(false)
      })

    return () => {
      annule = true
    }
  }, [vueGlobale, siteActif, periode])

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-xl font-semibold text-slate-800">
            {vueGlobale ? 'Dashboard global' : `Dashboard - ${siteActif.nom}`}
          </h1>
          <p className="text-sm text-slate-500">Vue d'ensemble des ventes</p>
        </div>
        <PeriodeFilter valeur={periode} onChange={setPeriode} />
      </div>

      {erreur && <Alerte type="error">{erreur}</Alerte>}

      {chargement ? (
        <ChargementPage message="Chargement du dashboard..." />
      ) : donnees ? (
        <>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <StatTile
              label="Chiffre d'affaires"
              value={formaterMontant(donnees.chiffreAffairesTotal)}
              accent="text-emerald-600"
            />
            <StatTile label="Nombre de ventes" value={formaterQuantite(donnees.nombreVentes)} />
            <StatTile label="Poids vendu" value={`${formaterQuantite(donnees.quantiteVenduePoidsKg)} kg`} />
            {Number(donnees.quantiteVendueSacs) > 0 && (
              <StatTile label="Sacs vendus" value={formaterQuantite(donnees.quantiteVendueSacs)} />
            )}
          </div>

          <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
            <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
              <h2 className="mb-3 text-sm font-semibold text-slate-600">Top produits</h2>
              {donnees.topProduits?.length ? (
                <ResponsiveContainer width="100%" height={260}>
                  <BarChart data={donnees.topProduits}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="produitNom" tick={{ fontSize: 12 }} />
                    <YAxis tick={{ fontSize: 12 }} />
                    <Tooltip formatter={(value) => formaterMontant(value)} />
                    <Bar dataKey="montantTotal" fill="#059669" radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              ) : (
                <p className="py-10 text-center text-sm text-slate-400">Aucune vente sur cette periode</p>
              )}
            </div>

            <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
              <h2 className="mb-3 text-sm font-semibold text-slate-600">Evolution des ventes</h2>
              {donnees.evolution?.length ? (
                <ResponsiveContainer width="100%" height={260}>
                  <LineChart data={donnees.evolution}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} />
                    <XAxis dataKey="date" tickFormatter={formaterDate} tick={{ fontSize: 12 }} />
                    <YAxis tick={{ fontSize: 12 }} />
                    <Tooltip labelFormatter={formaterDate} formatter={(value) => formaterMontant(value)} />
                    <Line type="monotone" dataKey="montant" stroke="#059669" strokeWidth={2} dot={false} />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <p className="py-10 text-center text-sm text-slate-400">Aucune vente sur cette periode</p>
              )}
            </div>
          </div>

          {vueGlobale && donnees.classementSites?.length > 0 && (
            <div className="rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
              <h2 className="mb-3 text-sm font-semibold text-slate-600">Classement des sites</h2>
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b border-slate-100 text-left text-xs uppercase text-slate-400">
                    <th className="pb-2">#</th>
                    <th className="pb-2">Site</th>
                    <th className="pb-2 text-right">Chiffre d'affaires</th>
                  </tr>
                </thead>
                <tbody>
                  {donnees.classementSites.map((site, index) => (
                    <tr key={site.siteId} className="border-b border-slate-50 last:border-0">
                      <td className="py-2 text-slate-400">{index + 1}</td>
                      <td className="py-2 font-medium text-slate-700">{site.siteNom}</td>
                      <td className="py-2 text-right text-slate-600">{formaterMontant(site.chiffreAffaires)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      ) : null}
    </div>
  )
}
