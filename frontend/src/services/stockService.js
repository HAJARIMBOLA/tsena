import apiClient from '../api/apiClient'

export function affecter(stock) {
  return apiClient.post('/admin/stock', stock).then((res) => res.data)
}

export function reapprovisionner(siteId, produitId, quantite) {
  return apiClient.put(`/stock/${siteId}/${produitId}`, { quantite }).then((res) => res.data)
}

export function modifierPrix(siteId, produitId, prixUnitaire) {
  return apiClient.put(`/admin/stock/${siteId}/${produitId}/prix`, { prixUnitaire }).then((res) => res.data)
}

export function listerParSite(siteId) {
  return apiClient.get(`/stock/site/${siteId}`).then((res) => res.data)
}

export function listerTout() {
  return apiClient.get('/stock').then((res) => res.data)
}
