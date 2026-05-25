import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { useCart } from '../context/CartContext';
import { useToast } from '../components/Toast';
import './Cart.css';

export default function Cart() {
  const navigate = useNavigate();
  const { refreshCart } = useCart();
  const addToast = useToast();

  const [cartItems, setCartItems]   = useState([]);
  const [loading, setLoading]       = useState(true);
  const [error, setError]           = useState(null);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [updatingItem, setUpdatingItem] = useState(null); // menuItemId being updated

  // ── Fetch cart ────────────────────────────────────────────────────────────
  const fetchCart = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/cart');
      const cartDto = res.data?.data || res.data || {};
      const items = cartDto.items || [];
      setCartItems(Array.isArray(items) ? items : []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load cart. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCart(); }, []);

  const grandTotal = cartItems.reduce((sum, item) => {
    const price = item.price || 0;
    const qty   = item.quantity || 1;
    return sum + price * qty;
  }, 0);

  // ── Update quantity (uses stable menuItemId endpoint) ─────────────────────
  const handleUpdateQty = async (item, delta) => {
    const menuItemId = item.menuItemId;
    const newQty     = (item.quantity || 1) + delta;
    setUpdatingItem(menuItemId);
    try {
      await api.put(`/cart/update-by-menu/${menuItemId}`, { quantity: newQty });
      // Optimistically update local list
      if (newQty <= 0) {
        setCartItems((prev) => prev.filter((i) => i.menuItemId !== menuItemId));
      } else {
        setCartItems((prev) =>
          prev.map((i) =>
            i.menuItemId === menuItemId
              ? { ...i, quantity: newQty, itemTotal: (i.price || 0) * newQty }
              : i
          )
        );
      }
      // Sync global context so RestaurantMenu/Navbar badge updates
      await refreshCart();
      if (newQty <= 0) addToast('Item removed from cart', 'info');
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to update cart', 'error');
      // Re-fetch to get accurate state
      await fetchCart();
      await refreshCart();
    } finally {
      setUpdatingItem(null);
    }
  };

  // ── Clear cart ────────────────────────────────────────────────────────────
  const handleClearCart = async () => {
    if (!window.confirm('Clear all items from cart?')) return;
    setLoading(true);
    try {
      await api.delete('/cart/clear');
      setCartItems([]);
      await refreshCart();
      addToast('Cart cleared', 'info');
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to clear cart', 'error');
    } finally {
      setLoading(false);
    }
  };

  // ── Place order ───────────────────────────────────────────────────────────
  const handlePlaceOrder = async () => {
    if (cartItems.length === 0) { addToast('Your cart is empty', 'error'); return; }
    setPlacingOrder(true);
    try {
      const res       = await api.post('/orders/place');
      const orderData = res.data?.data || res.data;
      const orderId   = orderData?.orderId || orderData?.id || 'unknown';
      setCartItems([]);
      // Sync context → resets RestaurantMenu add buttons immediately
      await refreshCart();
      addToast('Order placed successfully! 🎉', 'success');
      navigate(`/order-confirmation/${orderId}`, { state: { order: orderData } });
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to place order. Please try again.', 'error');
    } finally {
      setPlacingOrder(false);
    }
  };

  // ── Loading / Error states ────────────────────────────────────────────────
  if (loading) {
    return (
      <div className="page-wrapper">
        <div className="spinner-overlay">
          <div className="spinner" />
          <p className="spinner-text">Loading your cart…</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-wrapper">
        <div className="error-state container" role="alert">
          <span className="error-icon">😕</span>
          <h3>Failed to load cart</h3>
          <p>{error}</p>
          <button className="btn btn-primary" onClick={fetchCart}>Retry</button>
        </div>
      </div>
    );
  }

  // ── Render ────────────────────────────────────────────────────────────────
  return (
    <main className="cart-page page-wrapper">
      <div className="container">
        <div className="cart-header">
          <h1 className="cart-title">🛒 Your Cart</h1>
          {cartItems.length > 0 && (
            <button
              id="clear-cart-btn"
              className="btn btn-outline btn-sm"
              onClick={handleClearCart}
            >
              🗑 Clear Cart
            </button>
          )}
        </div>

        {cartItems.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">🛒</span>
            <h3>Your cart is empty</h3>
            <p>Looks like you haven&apos;t added anything yet!</p>
            <button className="btn btn-primary" onClick={() => navigate('/')}>
              Browse Restaurants
            </button>
          </div>
        ) : (
          <div className="cart-layout">
            {/* Items */}
            <section className="cart-items" aria-label="Cart items">
              {cartItems.map((item) => {
                const menuItemId = item.menuItemId;
                const name  = item.itemName || item.name || 'Unknown Item';
                const price = item.price || 0;
                const qty   = item.quantity || 1;
                const isUpdating = updatingItem === menuItemId;

                return (
                  <article key={menuItemId} className="cart-item">
                    <div className="cart-item-info">
                      <h3 className="cart-item-name">{name}</h3>
                      <div className="cart-item-meta">
                        <span className="cart-item-price">₹{parseFloat(price).toFixed(2)}</span>
                        <span className="cart-item-subtotal">
                          = ₹{(price * qty).toFixed(2)}
                        </span>
                      </div>
                    </div>

                    {/* Quantity controls + Remove */}
                    <div className="cart-item-controls">
                      <div className="qty-control" role="group" aria-label={`Quantity of ${name}`}>
                        <button
                          className="qty-btn qty-minus"
                          onClick={() => handleUpdateQty(item, -1)}
                          disabled={isUpdating}
                          aria-label={`Decrease ${name}`}
                        >
                          {qty === 1 ? '🗑' : '−'}
                        </button>
                        <span className="qty-value">
                          {isUpdating ? <span className="qty-spinner" /> : qty}
                        </span>
                        <button
                          className="qty-btn qty-plus"
                          onClick={() => handleUpdateQty(item, 1)}
                          disabled={isUpdating}
                          aria-label={`Increase ${name}`}
                        >
                          +
                        </button>
                      </div>
                    </div>
                  </article>
                );
              })}
            </section>

            {/* Order Summary */}
            <aside className="order-summary">
              <div className="summary-card">
                <h2 className="summary-title">Order Summary</h2>

                <div className="summary-rows">
                  <div className="summary-row">
                    <span>Subtotal ({cartItems.length} item{cartItems.length !== 1 ? 's' : ''})</span>
                    <span>₹{grandTotal.toFixed(2)}</span>
                  </div>
                  <div className="summary-row">
                    <span>Delivery</span>
                    <span className="free-label">Free</span>
                  </div>
                  <div className="summary-divider" />
                  <div className="summary-row total-row">
                    <span>Total</span>
                    <span className="grand-total">₹{grandTotal.toFixed(2)}</span>
                  </div>
                </div>

                <button
                  id="place-order-btn"
                  className={`btn btn-primary btn-lg place-order-btn ${placingOrder ? 'loading' : ''}`}
                  onClick={handlePlaceOrder}
                  disabled={placingOrder || cartItems.length === 0}
                  aria-label="Place your order"
                >
                  {placingOrder ? (
                    <>
                      <span className="btn-spinner" />
                      Placing Order…
                    </>
                  ) : (
                    '🎉 Place Order'
                  )}
                </button>

                <p className="secure-note">🔒 Secure checkout</p>
              </div>
            </aside>
          </div>
        )}
      </div>
    </main>
  );
}
