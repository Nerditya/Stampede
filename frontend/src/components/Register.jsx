import { useState } from 'react'
import { useAuth } from '../context/AuthContext'

export default function Register({ onSwitchToLogin, onSuccess }) {
  const { register } = useAuth()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await register(name, email, password)
      onSuccess?.()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="form" onSubmit={handleSubmit}>
      <h2 className="form-title">Create account</h2>
      <p className="form-sub">Join the stampede.</p>

      <input
        className="input"
        type="text"
        placeholder="Name"
        value={name}
        onChange={e => setName(e.target.value)}
        required
      />
      <input
        className="input"
        type="email"
        placeholder="Email"
        value={email}
        onChange={e => setEmail(e.target.value)}
        required
      />
      <input
        className="input"
        type="password"
        placeholder="Password (min 8 characters)"
        value={password}
        onChange={e => setPassword(e.target.value)}
        minLength={8}
        required
      />

      {error && <p className="alert-error">{error}</p>}

      <button className="btn btn-primary btn-block" type="submit" disabled={busy}>
        {busy ? 'Creating…' : 'Register'}
      </button>

      <p className="form-switch">
        Have an account?{' '}
        <button type="button" className="link-btn" onClick={onSwitchToLogin}>
          Log in
        </button>
      </p>
    </form>
  )
}
