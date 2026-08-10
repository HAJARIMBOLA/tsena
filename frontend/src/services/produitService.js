import apiClient from '../api/apiClient'

export function listerActifs() {
  return apiClient.get('/produits').then((res) => res.data)
}

export function lister() {
  return apiClient.get('/admin/produits').then((res) => res.data)
}

export function trouverParId(id) {
  return apiClient.get(`/admin/produits/${id}`).then((res) => res.data)
}

export function creer(produit) {
  return apiClient.post('/admin/produits', produit).then((res) => res.data)
}

export function modifier(id, produit) {
  return apiClient.put(`/admin/produits/${id}`, produit).then((res) => res.data)
}

export function desactiver(id) {
  return apiClient.delete(`/admin/produits/${id}`)
}
