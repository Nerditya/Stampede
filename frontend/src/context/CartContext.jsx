import { createContext, useContext, useState } from 'react'
import { authFetch } from '../api/client'

// Holds the cart and exposes operations to add/remove/checkout.
// Cart item shape:  { productId, name, price, quantity }
const CartContext = createContext(null)

export function useCart() {
  return useContext(CartContext)
}

export function CartProvider({ children }) {
  const [items, setItems] = useState([])

  // add one unit; if the product is already in the cart, bump its quantity
  function addItem(product) {
    setItems(prev => {
      const existing = prev.find(i => i.productId === product.productId)
      if (existing) {
        return prev.map(i =>
          i.productId === product.productId ? { ...i, quantity: i.quantity + 1 } : i
        )
      }
      return [
        ...prev,
        { productId: product.productId, name: product.name, price: product.price, quantity: 1 },
      ]
    })
  }

  function removeItem(productId) {
    setItems(prev => prev.filter(i => i.productId !== productId))
  }

  // set an exact quantity; dropping to 0 removes the line
  function updateQuantity(productId, quantity) {
    if (quantity < 1) {
      removeItem(productId)
      return
    }
    setItems(prev =>
      prev.map(i => (i.productId === productId ? { ...i, quantity } : i))
    )
  }

  function clearCart() {
    setItems([])
  }

  // send the whole cart to the backend as one transactional order
  async function checkout() {
    const res = await authFetch('/api/orders/order', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        items: items.map(i => ({ productId: i.productId, quantity: i.quantity })),
      }),
    })

    if (!res.ok) {
      let message = 'Checkout failed'
      try {
        const body = await res.json()
        message = body.message || message // 409 body names what sold out
      } catch {
        /* ignore */
      }
      throw new Error(message)
    }

    const order = await res.json()
    clearCart()
    return order
  }

  const value = { items, addItem, removeItem, updateQuantity, clearCart, checkout }
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}
