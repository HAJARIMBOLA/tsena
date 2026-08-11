import apiClient from '../api/apiClient'

export function creer(utilisateur) {
  return apiClient.post('/admin/utilisateurs', utilisateur).then((res) => res.data)
}

export function lister() {
  return apiClient.get('/admin/utilisateurs').then((res) => res.data)
}

export function trouverParId(id) {
  return apiClient.get(`/admin/utilisateurs/${id}`).then((res) => res.data)
}

export function modifierSites(id, siteIds) {
  return apiClient.put(`/admin/utilisateurs/${id}/sites`, siteIds).then((res) => res.data)
}

export function desactiver(id) {
  return apiClient.put(`/admin/utilisateurs/${id}/desactiver`)
}

export function reactiver(id) {
  return apiClient.put(`/admin/utilisateurs/${id}/reactiver`)
}

export function supprimer(id) {
  return apiClient.delete(`/admin/utilisateurs/${id}`)
}
