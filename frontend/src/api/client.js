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

let refreshPromise

function expireSession() {
  clearTokens()
  window.dispatchEvent(new Event('stampede:session-expired'))
}

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
  if (res.status === 401) {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      expireSession()
      return res
    }

    try {
      refreshPromise ??= refreshApi(refreshToken).finally(() => {
        refreshPromise = undefined
      })
      const tokens = await refreshPromise
      setTokens(tokens)
      res = await fetch(url, withAuth(options, tokens.accessToken))
    } catch {
      expireSession()
    }
  }

  return res
}
