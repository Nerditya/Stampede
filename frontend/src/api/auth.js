// ---------------------------------------------------------------------------
// Token storage + raw auth API calls. This is plumbing — no React here.
// The access token is short-lived; the refresh token is used to get a new one.
// (localStorage is simple but XSS-exposed; httpOnly cookies are safer for real
//  production. Fine for learning.)
// ---------------------------------------------------------------------------

const ACCESS_KEY = 'stampede.accessToken'
const REFRESH_KEY = 'stampede.refreshToken'

export function getAccessToken() {
  return localStorage.getItem(ACCESS_KEY)
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_KEY)
}

export function setTokens({ accessToken, refreshToken }) {
  localStorage.setItem(ACCESS_KEY, accessToken)
  localStorage.setItem(REFRESH_KEY, refreshToken)
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_KEY)
  localStorage.removeItem(REFRESH_KEY)
}

// Reads {message} from an error response body, falling back to the status text.
async function errorMessage(res) {
  try {
    const body = await res.json()
    return body.message || res.statusText
  } catch {
    return res.statusText
  }
}

export async function registerApi(name, email, password) {
  const res = await fetch('/api/auth/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ name, email, password }),
  })
  if (!res.ok) throw new Error(await errorMessage(res))
  return res.json() // { message }
}

export async function verifyEmailApi(token) {
  const res = await fetch(`/api/auth/verify?token=${encodeURIComponent(token)}`)
  if (!res.ok) throw new Error(await errorMessage(res))
  return res.json()
}

export async function loginApi(email, password) {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (!res.ok) throw new Error(await errorMessage(res))
  return res.json()
}

export async function refreshApi(refreshToken) {
  const res = await fetch('/api/auth/refresh', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
  if (!res.ok) throw new Error(await errorMessage(res))
  return res.json()
}

export async function logoutApi(refreshToken) {
  await fetch('/api/auth/logout', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  })
}
