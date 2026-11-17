import axios from 'axios'

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10_000,
  withCredentials: true,
  withXSRFToken: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

interface CsrfTokenResponse {
  headerName: string
  token: string
}

let csrfToken: CsrfTokenResponse | null = null

http.interceptors.request.use(async (config) => {
  const isStateChanging = ['post', 'put', 'patch', 'delete'].includes(config.method?.toLowerCase() || '')
  if (!isStateChanging) return config

  if (!csrfToken) {
    const response = await http.get<CsrfTokenResponse>('/admin/csrf')
    csrfToken = response.data
  }
  config.headers.set(csrfToken.headerName, csrfToken.token)
  return config
})

http.interceptors.response.use(
  (response) => {
    if (response.config.url?.endsWith('/admin/logout')) csrfToken = null
    return response
  },
  (error: unknown) => {
    if (
      axios.isAxiosError(error)
      && (error.response?.status === 401 || error.response?.status === 403)
      && error.config?.url?.startsWith('/admin/')
      && !['/admin/login', '/admin/session', '/admin/csrf'].some((path) => error.config?.url?.endsWith(path))
    ) {
      csrfToken = null
      if (typeof window !== 'undefined') {
        window.dispatchEvent(new Event('vsblog:session-expired'))
      }
    }
    return Promise.reject(error)
  },
)