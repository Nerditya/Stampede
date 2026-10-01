import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { ArrowRight } from './icons'

export default function Login({ onSwitchToRegister }) {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await login(email, password)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      <span className="form-kicker">Members only</span>
      <h2 className="form-title">Log in</h2>

      <div className="field">
        <label className="field-label" htmlFor="login-email">Email</label>
        <input
          id="login-email"
          className="input"
          type="email"
          autoComplete="email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          required
        />
      </div>

      <div className="field">
        <label className="field-label" htmlFor="login-password">Password</label>
        <input
          id="login-password"
          className="input"
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={e => setPassword(e.target.value)}
          required
        />
      </div>

      {error && <p className="alert-error">{error}</p>}

      <button className="btn btn-accent btn-block" type="submit" disabled={busy}>
        {busy ? 'Logging in…' : 'Enter the drop'}
        {!busy && <ArrowRight size={16} />}
      </button>

      <p className="form-switch">
        No account?{' '}
        <button type="button" className="link-btn" onClick={onSwitchToRegister}>
          Register
        </button>
      </p>
    </form>
  )
}
