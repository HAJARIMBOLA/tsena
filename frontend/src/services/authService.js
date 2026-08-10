import apiClient from '../api/apiClient'

export function login(email, motDePasse) {
  return apiClient.post('/auth/login', { email, motDePasse }).then((res) => res.data)
}
