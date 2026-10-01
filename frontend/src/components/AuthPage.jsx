import { useState } from 'react'
import Login from './Login'
import Register from './Register'

// Full-screen auth gate. Shown whenever the user is not logged in.
// Toggles between the Login and Register "pages". On success, AuthContext flips
// isAuthenticated → true and App swaps this out for the shop automatically.
export default function AuthPage() {
  const [view, setView] = useState('login') // 'login' | 'register'

  return (
    <div className="auth-page">
      <div className="auth-brand">
        <span className="auth-logo">⚡ Stampede</span>
        <p className="auth-tagline">Flash Sale — limited stock, first come, first served.</p>
      </div>

      <div className="auth-card">
        {view === 'login' ? (
          <Login onSwitchToRegister={() => setView('register')} onSuccess={() => {}} />
        ) : (
          <Register onSwitchToLogin={() => setView('login')} onSuccess={() => {}} />
        )}
      </div>
    </div>
  )
}
