import apiClient from '../api/apiClient'

export function mesSites() {
  return apiClient.get('/mes-sites').then((res) => res.data)
}

export function lister() {
  return apiClient.get('/admin/sites').then((res) => res.data)
}

export function creer(site) {
  return apiClient.post('/admin/sites', site).then((res) => res.data)
}

export function modifier(id, site) {
  return apiClient.put(`/admin/sites/${id}`, site).then((res) => res.data)
}

export function desactiver(id) {
  return apiClient.delete(`/admin/sites/${id}`)
}
