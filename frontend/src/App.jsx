import { useState, useEffect, useCallback } from 'react'
import ProductCard from './ProductCard'
import Cart from './components/Cart'
import AuthPage from './components/AuthPage'
import { Bag, LogOut, Close } from './components/icons'
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

  if (!isAuthenticated) {
    return <AuthPage />
  }

  return (
    <>
      <div className="ticker" aria-hidden="true">
        <div className="ticker-track">
          <span>Live drop</span>·<span>Limited stock</span>·<span>No resellers</span>·<span>One shot</span>
          <span>Live drop</span>·<span>Limited stock</span>·<span>No resellers</span>·<span>One shot</span>
        </div>
      </div>

      <header className="header">
        <div className="brand">
          <span className="brand-mark">STAMPEDE<b>_</b></span>
          <span className="brand-sub">Est. 2026</span>
        </div>

        <div className="header-actions">
          <button className="btn btn-outline" onClick={logout}>
            <LogOut size={16} /> Logout
          </button>
          <button className="btn btn-accent cart-toggle" onClick={() => setCartOpen(true)}>
            <Bag size={16} /> Cart
            {cartCount > 0 && <span className="cart-count">{cartCount}</span>}
          </button>
        </div>
      </header>

      <main className="main">
        <div className="hero reveal">
          <span className="kicker">Drop 001 — Live now</span>
          <h1 className="hero-title">The <em>Limited</em> Drop</h1>
          <p className="hero-sub">
            Limited stock. Thousands of buyers. A database that refuses to oversell — every unit is accounted for, to the last one.
          </p>
        </div>

        {loading && <p className="status-msg">Loading drop…</p>}
        {error && <p className="status-error">{error} — is the backend running?</p>}

        <div className="grid">
          {products.map((product, i) => (
            <ProductCard key={product.productId} product={product} index={i} />
          ))}
        </div>
      </main>

      {cartOpen && (
        <>
          <div className="overlay" onClick={() => setCartOpen(false)} />
          <aside className="drawer" role="dialog" aria-label="Shopping cart">
            <div className="drawer-head">
              <span className="drawer-title">Your Cart</span>
              <button className="icon-btn" onClick={() => setCartOpen(false)} aria-label="Close cart">
                <Close size={18} />
              </button>
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
