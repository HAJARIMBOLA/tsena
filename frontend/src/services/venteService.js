import apiClient from '../api/apiClient'

export function creer(vente) {
  return apiClient.post('/ventes', vente).then((res) => res.data)
}

export function historiqueParSite(siteId, { debut, fin, page = 0, size = 20 } = {}) {
  return apiClient
    .get(`/ventes/site/${siteId}`, { params: { debut, fin, page, size } })
    .then((res) => res.data)
}

export function historiqueGlobal({ debut, fin, page = 0, size = 20 } = {}) {
  return apiClient.get('/ventes/toutes', { params: { debut, fin, page, size } }).then((res) => res.data)
}

export function mesVentes({ page = 0, size = 20 } = {}) {
  return apiClient.get('/ventes/mes-ventes', { params: { page, size } }).then((res) => res.data)
}

export function trouverParId(id) {
  return apiClient.get(`/ventes/${id}`).then((res) => res.data)
}
