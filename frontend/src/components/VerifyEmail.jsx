import { useEffect, useState } from 'react'
import { verifyEmailApi } from '../api/auth'

export default function VerifyEmail() {
  const [status, setStatus] = useState('loading')
  const [message, setMessage] = useState('Verifying your email…')

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get('token')
    if (!token) {
      setStatus('error')
      setMessage('This verification link is missing its token.')
      return
    }

    verifyEmailApi(token)
      .then(response => {
        setStatus('success')
        setMessage(response.message)
      })
      .catch(error => {
        setStatus('error')
        setMessage(error.message)
      })
  }, [])

  return (
    <div className="auth-split">
      <section className="auth-hero">
        <span className="kicker">Stampede // Live Drop</span>
        <h1 className="auth-hero-title">
          Email<br /><em>verified.</em>
        </h1>
      </section>
      <section className="auth-form-side">
        <div className="form">
          <span className="form-kicker">{status === 'loading' ? 'One moment' : 'Account access'}</span>
          <h2 className="form-title">{status === 'success' ? "You're in" : status === 'error' ? 'Link problem' : 'Verifying'}</h2>
          <p className={status === 'error' ? 'alert-error' : 'alert-success'}>{message}</p>
          {status !== 'loading' && (
            <a className="btn btn-accent btn-block" href="/">Continue to login</a>
          )}
        </div>
      </section>
    </div>
  )
}