import { useCart } from './context/CartContext'

// formats paisa to rupees — 14999 → ₹149.99
function formatPrice(paisa) {
  return '₹' + (paisa / 100).toLocaleString('en-IN', { minimumFractionDigits: 2 })
}

export default function ProductCard({ product }) {
  const { items, addItem, updateQuantity } = useCart()

  const inCart = items.find(i => i.productId === product.productId)
  const qty = inCart ? inCart.quantity : 0
  const soldOut = product.liveStock === 0
  const lowStock = !soldOut && product.liveStock <= 10
  const atStockLimit = qty >= product.liveStock

  const badgeClass = soldOut ? 'sold-out' : lowStock ? 'low-stock' : 'in-stock'
  const badgeText = soldOut
    ? 'Sold Out'
    : lowStock
      ? `Only ${product.liveStock} left`
      : `${product.liveStock} in stock`

  function increment() {
    if (atStockLimit) return
    if (qty === 0) addItem(product)
    else updateQuantity(product.productId, qty + 1)
  }

  function decrement() {
    updateQuantity(product.productId, qty - 1)
  }

  return (
    <div className="card">
      <span className={`card-badge ${badgeClass}`}>
        {soldOut ? '●' : '●'} {badgeText}
      </span>

      <div className="card-image">🛍️</div>

      <h3 className="card-name">{product.name}</h3>
      <p className="card-desc">{product.description}</p>

      <div className="card-footer">
        <span className="card-price">{formatPrice(product.price)}</span>

        {qty === 0 ? (
          <button className="btn btn-primary" onClick={increment} disabled={soldOut}>
            {soldOut ? 'Sold Out' : 'Add'}
          </button>
        ) : (
          <div className="stepper">
            <button className="stepper-btn" onClick={decrement}>−</button>
            <span className="stepper-qty">{qty}</span>
            <button className="stepper-btn" onClick={increment} disabled={atStockLimit}>+</button>
          </div>
        )}
      </div>
    </div>
  )
}
