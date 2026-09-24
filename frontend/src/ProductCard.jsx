import { useState } from 'react'

// formats paisa to rupees — 14999 → ₹149.99
function formatPrice(paisa) {
  return '₹' + (paisa / 100).toLocaleString('en-IN', { minimumFractionDigits: 2 })
}

export default function ProductCard({ product }) {
  const [stock, setStock]   = useState(product.liveStock)
  const [status, setStatus] = useState('idle')  // idle | loading | success | soldout | error

  async function handleBuy() {
    setStatus('loading')

    try {
      const res = await fetch('/api/orders/order', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          personId:  'user-' + Math.random().toString(36).slice(2, 8), // random guest id
          productId: product.productId,
          quantity:  1,
        }),
      })

      if (res.ok) {
        // refresh live stock from backend
        const updated = await fetch(`/api/products/${product.productId}`)
        const data    = await updated.json()
        setStock(data.liveStock)
        setStatus('success')
        setTimeout(() => setStatus('idle'), 2000)  // reset after 2s

      } else if (res.status === 409) {
        setStock(0)
        setStatus('soldout')

      } else {
        setStatus('error')
        setTimeout(() => setStatus('idle'), 2000)
      }

    } catch {
      setStatus('error')
      setTimeout(() => setStatus('idle'), 2000)
    }
  }

  const soldOut = stock === 0

  return (
    <div style={styles.card}>
      <div style={styles.badge}>
        {soldOut ? '🔴 Sold Out' : `🟢 ${stock} left`}
      </div>

      <div style={styles.imagePlaceholder}>
        🛍️
      </div>

      <h2 style={styles.name}>{product.name}</h2>
      <p style={styles.description}>{product.description}</p>
      <p style={styles.price}>{formatPrice(product.price)}</p>

      <button
        style={{
          ...styles.button,
          ...(soldOut || status === 'loading' ? styles.buttonDisabled : {}),
          ...(status === 'success' ? styles.buttonSuccess : {}),
        }}
        onClick={handleBuy}
        disabled={soldOut || status === 'loading'}
      >
        {status === 'loading' ? 'Placing order...'
          : status === 'success' ? '✅ Order placed!'
          : status === 'soldout' ? 'Sold Out'
          : status === 'error'   ? 'Error — try again'
          : soldOut              ? 'Sold Out'
          : 'Buy Now'}
      </button>
    </div>
  )
}

const styles = {
  card: {
    background: '#1a1a1a',
    border: '1px solid #2a2a2a',
    borderRadius: 12,
    padding: 24,
    display: 'flex',
    flexDirection: 'column',
    gap: 12,
    position: 'relative',
    width: 280,
  },
  badge: {
    fontSize: 13,
    fontWeight: 600,
    color: '#aaa',
  },
  imagePlaceholder: {
    fontSize: 64,
    textAlign: 'center',
    padding: '16px 0',
  },
  name: {
    fontSize: 20,
    fontWeight: 700,
  },
  description: {
    fontSize: 14,
    color: '#888',
    lineHeight: 1.5,
  },
  price: {
    fontSize: 22,
    fontWeight: 700,
    color: '#f97316',
  },
  button: {
    marginTop: 8,
    padding: '12px 0',
    borderRadius: 8,
    border: 'none',
    background: '#f97316',
    color: '#fff',
    fontSize: 16,
    fontWeight: 700,
    cursor: 'pointer',
    transition: 'background 0.2s',
  },
  buttonDisabled: {
    background: '#333',
    color: '#666',
    cursor: 'not-allowed',
  },
  buttonSuccess: {
    background: '#16a34a',
  },
}
