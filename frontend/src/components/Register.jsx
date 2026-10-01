import { useState } from 'react'
import { useAuth } from '../context/AuthContext'
import { ArrowRight } from './icons'

export default function Register({ onSwitchToLogin }) {
  const { register } = useAuth()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [message, setMessage] = useState(null)
  const [busy, setBusy] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      const response = await register(name, email, password)
      setMessage(response.message)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (message) {
    return (
      <div className="form">
        <span className="form-kicker">Almost there</span>
        <h2 className="form-title">Check your email</h2>
        <p className="alert-success">{message}</p>
        <p className="form-switch">
          Already verified?{' '}
          <button type="button" className="link-btn" onClick={onSwitchToLogin}>
            Log in
          </button>
        </p>
      </div>
    )
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      <span className="form-kicker">Get on the list</span>
      <h2 className="form-title">Register</h2>

      <div className="field">
        <label className="field-label" htmlFor="reg-name">Name</label>
        <input
          id="reg-name"
          className="input"
          type="text"
          autoComplete="name"
          value={name}
          onChange={e => setName(e.target.value)}
          required
        />
      </div>

      <div className="field">
        <label className="field-label" htmlFor="reg-email">Email</label>
        <input
          id="reg-email"
          className="input"
          type="email"
          autoComplete="email"
          value={email}
          onChange={e => setEmail(e.target.value)}
          required
        />
      </div>

      <div className="field">
        <label className="field-label" htmlFor="reg-password">Password</label>
        <input
          id="reg-password"
          className="input"
          type="password"
          autoComplete="new-password"
          placeholder="Min 8 characters"
          value={password}
          onChange={e => setPassword(e.target.value)}
          minLength={8}
          required
        />
      </div>

      {error && <p className="alert-error">{error}</p>}

      <button className="btn btn-accent btn-block" type="submit" disabled={busy}>
        {busy ? 'Creating…' : 'Create account'}
        {!busy && <ArrowRight size={16} />}
      </button>

      <p className="form-switch">
        Already in?{' '}
        <button type="button" className="link-btn" onClick={onSwitchToLogin}>
          Log in
        </button>
      </p>
    </form>
  )
}
