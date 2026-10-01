import { useCart } from './context/CartContext'
import { Plus, Minus, ArrowRight } from './components/icons'

// formats paisa to rupees — 14999 → ₹149.99
function formatPrice(paisa) {
  return '₹' + (paisa / 100).toLocaleString('en-IN', { minimumFractionDigits: 2 })
}

export default function ProductCard({ product, index = 0 }) {
  const { items, addItem, updateQuantity } = useCart()

  const inCart = items.find(i => i.productId === product.productId)
  const qty = inCart ? inCart.quantity : 0
  const soldOut = product.liveStock === 0
  const lowStock = !soldOut && product.liveStock <= 10
  const atStockLimit = qty >= product.liveStock

  const num = String(index + 1).padStart(2, '0')
  const sku = `STMP-${product.productId.toUpperCase()}`
  const pct = product.initialStock
    ? Math.max(0, Math.round((product.liveStock / product.initialStock) * 100))
    : 0

  const tagClass = soldOut ? 'out' : lowStock ? 'low' : 'in'
  const tagText = soldOut ? 'Sold Out' : lowStock ? `Low · ${product.liveStock}` : 'In Stock'

  function increment() {
    if (atStockLimit) return
    if (qty === 0) addItem(product)
    else updateQuantity(product.productId, qty + 1)
  }
  function decrement() {
    updateQuantity(product.productId, qty - 1)
  }

  return (
    <article className="card reveal" style={{ animationDelay: `${index * 80}ms` }}>
      <div className="card-top">
        <span className="card-index">{num} / DROP</span>
        <span className={`tag ${tagClass}`}>{tagText}</span>
      </div>

      <div className="card-visual">
        <span className="card-visual-num">{num}</span>
      </div>

      <div className="card-meta">
        <span className="card-sku">{sku}</span>
        <h3 className="card-name">{product.name}</h3>
        <p className="card-desc">{product.description}</p>
      </div>

      <div className="card-stock">
        <div className="meter">
          <div className="meter-fill" style={{ width: `${pct}%` }} />
        </div>
        <span className="card-stock-num">
          {soldOut ? 'Out of stock' : `${product.liveStock} / ${product.initialStock} remaining`}
        </span>
      </div>

      <div className="card-buy">
        <span className="card-price">{formatPrice(product.price)}</span>

        {qty === 0 ? (
          <button className="btn btn-accent" onClick={increment} disabled={soldOut}>
            {soldOut ? 'Sold Out' : 'Add'}
            {!soldOut && <ArrowRight size={16} />}
          </button>
        ) : (
          <div className="stepper">
            <button className="stepper-btn" onClick={decrement} aria-label="Decrease quantity">
              <Minus size={16} />
            </button>
            <span className="stepper-qty">{qty}</span>
            <button className="stepper-btn" onClick={increment} disabled={atStockLimit} aria-label="Increase quantity">
              <Plus size={16} />
            </button>
          </div>
        )}
      </div>
    </article>
  )
}
