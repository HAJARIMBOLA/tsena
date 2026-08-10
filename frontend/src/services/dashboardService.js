import apiClient from '../api/apiClient'

export function dashboardSite(siteId, periode = 'jour') {
  return apiClient.get(`/dashboard/site/${siteId}`, { params: { periode } }).then((res) => res.data)
}

export function dashboardGlobal(periode = 'jour') {
  return apiClient.get('/dashboard/global', { params: { periode } }).then((res) => res.data)
}
