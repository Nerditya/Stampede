import { createContext, useContext, useEffect, useState } from 'react'
import {
  getAccessToken,
  getRefreshToken,
  setTokens,
  clearTokens,
  loginApi,
  registerApi,
  logoutApi,
} from '../api/auth'

// Holds "am I logged in?" and exposes login/register/logout to the whole app.
const AuthContext = createContext(null)

export function useAuth() {
  return useContext(AuthContext)
}

export function AuthProvider({ children }) {
  // seed from storage so a page refresh (F5) keeps you logged in
  const [isAuthenticated, setIsAuthenticated] = useState(() => !!getAccessToken())

  useEffect(() => {
    function handleSessionExpired() {
      clearTokens()
      setIsAuthenticated(false)
    }
    window.addEventListener('stampede:session-expired', handleSessionExpired)
    return () => window.removeEventListener('stampede:session-expired', handleSessionExpired)
  }, [])

  async function login(email, password) {
    const tokens = await loginApi(email, password)
    setTokens(tokens)
    setIsAuthenticated(true)
  }

  async function register(name, email, password) {
    return registerApi(name, email, password)
  }

  async function logout() {
    try {
      const refreshToken = getRefreshToken()
      if (refreshToken) await logoutApi(refreshToken)
    } catch {
      /* clear local state regardless of network result */
    }
    clearTokens()
    setIsAuthenticated(false)
  }

  const value = { isAuthenticated, login, register, logout }
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
