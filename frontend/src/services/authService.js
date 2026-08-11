import apiClient from '../api/apiClient'

export function login(email, motDePasse) {
  return apiClient.post('/auth/login', { email, motDePasse }).then((res) => res.data)
}

export function register(nom, email, motDePasse) {
  return apiClient.post('/auth/register', { nom, email, motDePasse }).then((res) => res.data)
}
