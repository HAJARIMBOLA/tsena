import apiClient from '../api/apiClient'

export function moi() {
  return apiClient.get('/moi').then((res) => res.data)
}

export function changerMotDePasse(motDePasseActuel, nouveauMotDePasse) {
  return apiClient.put('/moi/mot-de-passe', { motDePasseActuel, nouveauMotDePasse })
}
