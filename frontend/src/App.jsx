import { useState, useEffect } from 'react'
import ProductCard from './ProductCard'

export default function App() {
  const [products, setProducts] = useState([])
  const [loading, setLoading]   = useState(true)
  const [error, setError]       = useState(null)

  useEffect(() => {
    fetch('/api/products')
      .then(res => {
        if (!res.ok) throw new Error('Failed to fetch products')
        return res.json()
      })
      .then(data => {
        setProducts(data)
        setLoading(false)
      })
      .catch(err => {
        setError(err.message)
        setLoading(false)
      })
  }, [])  // empty array → runs once on mount

  return (
    <div style={styles.page}>
      <header style={styles.header}>
        <h1 style={styles.logo}>⚡ Stampede</h1>
        <p style={styles.tagline}>Flash Sale — Limited stock. First come, first served.</p>
      </header>

      <main style={styles.main}>
        {loading && <p style={styles.message}>Loading products...</p>}
        {error   && <p style={styles.error}>⚠️ {error} — is the backend running?</p>}

        <div style={styles.grid}>
          {products.map(product => (
            <ProductCard key={product.productId} product={product} />
          ))}
        </div>
      </main>
    </div>
  )
}

const styles = {
  page: {
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
  },
  header: {
    padding: '32px 48px',
    borderBottom: '1px solid #1a1a1a',
    background: '#0a0a0a',
  },
  logo: {
    fontSize: 32,
    fontWeight: 900,
    color: '#f97316',
    letterSpacing: -1,
  },
  tagline: {
    marginTop: 6,
    fontSize: 15,
    color: '#666',
  },
  main: {
    flex: 1,
    padding: '48px',
  },
  grid: {
    display: 'flex',
    flexWrap: 'wrap',
    gap: 24,
  },
  message: {
    color: '#666',
    marginBottom: 24,
  },
  error: {
    color: '#ef4444',
    marginBottom: 24,
  },
}
