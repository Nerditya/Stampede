import { useState } from 'react'
import Login from './Login'
import Register from './Register'

// Full-screen auth gate: an editorial brand panel beside the login / register form.
export default function AuthPage() {
  const [view, setView] = useState('login') // 'login' | 'register'

  return (
    <div className="auth-split">
      <section className="auth-hero">
        <span className="kicker">Stampede // Live Drop</span>
        <h1 className="auth-hero-title">
          First<br /><em>come</em>,<br />first served.
        </h1>
        <p className="auth-hero-foot">Limited stock · No resellers · One shot</p>
      </section>

      <section className="auth-form-side">
        {view === 'login' ? (
          <Login onSwitchToRegister={() => setView('register')} />
        ) : (
          <Register onSwitchToLogin={() => setView('login')} />
        )}
      </section>
    </div>
  )
}
