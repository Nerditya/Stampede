import { useState } from 'react'
import { useCart } from '../context/CartContext'
import { useAuth } from '../context/AuthContext'
import { Plus, Minus, Trash, ArrowRight } from './icons'

// formats paisa → rupees, e.g. 14999 → ₹149.99
function formatPrice(paisa) {
  return '₹' + (paisa / 100).toLocaleString('en-IN', { minimumFractionDigits: 2 })
}

export default function Cart({ onOrderPlaced, onRequireLogin }) {
  const { items, removeItem, updateQuantity, checkout } = useCart()
  const { isAuthenticated } = useAuth()
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(null)
  const [busy, setBusy] = useState(false)

  const total = items.reduce((sum, i) => sum + i.price * i.quantity, 0)

  async function handleCheckout() {
    if (!isAuthenticated) {
      onRequireLogin?.()
      return
    }
    setBusy(true)
    setError(null)
    setSuccess(null)
    try {
      const order = await checkout()
      setSuccess(order.orderId)
      onOrderPlaced?.(order)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <div className="drawer-body">
        {items.length === 0 ? (
          <p className="cart-empty">
            {success ? `Order ${success} placed` : 'Cart is empty'}
          </p>
        ) : (
          items.map(item => (
            <div key={item.productId} className="cart-line">
              <div>
                <div className="cart-line-name">{item.name}</div>
                <div className="cart-line-price">
                  {formatPrice(item.price)} × {item.quantity} = {formatPrice(item.price * item.quantity)}
                </div>
              </div>
              <div className="cart-line-actions">
                <div className="stepper">
                  <button className="stepper-btn" onClick={() => updateQuantity(item.productId, item.quantity - 1)} aria-label="Decrease">
                    <Minus size={15} />
                  </button>
                  <span className="stepper-qty">{item.quantity}</span>
                  <button className="stepper-btn" onClick={() => updateQuantity(item.productId, item.quantity + 1)} aria-label="Increase">
                    <Plus size={15} />
                  </button>
                </div>
                <button className="cart-remove" onClick={() => removeItem(item.productId)} aria-label="Remove item">
                  <Trash size={17} />
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {items.length > 0 && (
        <div className="drawer-foot">
          <div className="cart-total">
            <span className="cart-total-label">Total</span>
            <span className="cart-total-value">{formatPrice(total)}</span>
          </div>

          {error && <p className="alert-error">{error}</p>}
          {success && <p className="alert-success">Order {success} placed</p>}

          <button className="btn btn-accent btn-block" onClick={handleCheckout} disabled={busy}>
            {busy ? 'Placing order…' : isAuthenticated ? 'Checkout' : 'Log in to checkout'}
            {!busy && <ArrowRight size={16} />}
          </button>
        </div>
      )}
    </>
  )
}
