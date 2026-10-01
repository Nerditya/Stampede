// ---------------------------------------------------------------------------
// authFetch: like fetch(), but attaches the access token and, on a 401,
// transparently tries the refresh token ONCE, then retries the request.
// This is the single choke point every authenticated call should go through.
// ---------------------------------------------------------------------------

import {
  getAccessToken,
  getRefreshToken,
  setTokens,
  clearTokens,
  refreshApi,
} from './auth'

function withAuth(options, token) {
  return {
    ...options,
    headers: {
      ...(options.headers || {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  }
}

export async function authFetch(url, options = {}) {
  const token = getAccessToken()
  let res = await fetch(url, withAuth(options, token))

  // access token expired/invalid → attempt a single refresh, then retry
  if (res.status === 401 && getRefreshToken()) {
    try {
      const tokens = await refreshApi(getRefreshToken())
      setTokens(tokens)
      res = await fetch(url, withAuth(options, tokens.accessToken))
    } catch {
      clearTokens() // refresh failed → force re-login
    }
  }

  return res
}
