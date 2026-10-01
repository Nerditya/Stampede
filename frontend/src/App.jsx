import { useState, useEffect, useCallback } from 'react'
import ProductCard from './ProductCard'
import Cart from './components/Cart'
import AuthPage from './components/AuthPage'
import { useCart } from './context/CartContext'
import { useAuth } from './context/AuthContext'

export default function App() {
  const { isAuthenticated, logout } = useAuth()
  const { items } = useCart()

  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [cartOpen, setCartOpen] = useState(false)

  const cartCount = items.reduce((sum, item) => sum + item.quantity, 0)

  // load the live catalog; re-called after checkout so stock counts stay fresh
  const loadProducts = useCallback(async () => {
    try {
      setLoading(true)
      const res = await fetch('/api/products')
      if (!res.ok) throw new Error('Failed to load products')
      setProducts(await res.json())
      setError(null)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    if (isAuthenticated) loadProducts()
  }, [isAuthenticated, loadProducts])

  // not logged in → show the login / register pages
  if (!isAuthenticated) {
    return <AuthPage />
  }

  return (
    <>
      <header className="header">
        <div className="brand">
          <span className="brand-logo">⚡ Stampede</span>
          <span className="brand-tagline">Flash Sale — first come, first served</span>
        </div>

        <div className="header-actions">
          <button className="btn btn-ghost" onClick={logout}>Logout</button>
          <button className="btn btn-primary cart-toggle" onClick={() => setCartOpen(true)}>
            🛒 Cart
            {cartCount > 0 && <span className="cart-count">{cartCount}</span>}
          </button>
        </div>
      </header>

      <main className="main">
        <h1 className="page-title">Limited Drop</h1>
        <p className="page-sub">Limited stock. Thousands of buyers. Zero overselling.</p>

        {loading && <p className="status-msg">Loading products…</p>}
        {error && <p className="status-error">⚠️ {error} — is the backend running?</p>}

        <div className="grid">
          {products.map(product => (
            <ProductCard key={product.productId} product={product} />
          ))}
        </div>
      </main>

      {cartOpen && (
        <>
          <div className="overlay" onClick={() => setCartOpen(false)} />
          <aside className="drawer">
            <div className="drawer-head">
              <span className="drawer-title">Your Cart</span>
              <button className="drawer-close" onClick={() => setCartOpen(false)}>✕</button>
            </div>
            <Cart
              onOrderPlaced={() => loadProducts()}
              onRequireLogin={() => setCartOpen(false)}
            />
          </aside>
        </>
      )}
    </>
  )
}
