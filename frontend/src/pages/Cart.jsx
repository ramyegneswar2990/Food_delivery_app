import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { useCart } from '../context/CartContext';
import { useToast } from '../components/Toast';
import './Cart.css';

export default function Cart() {
  const navigate = useNavigate();
  const { setCartCount } = useCart();
  const addToast = useToast();

  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [removing, setRemoving] = useState(null);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [clearing, setClearing] = useState(false);

  const fetchCart = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get('/cart');
      const data = res.data?.data || res.data || [];
      setCartItems(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load cart. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const grandTotal = cartItems.reduce((sum, item) => {
    const price = item.price || item.menuItem?.price || 0;
    const qty = item.quantity || 1;
    return sum + price * qty;
  }, 0);

  const handleRemove = async (cartItemId) => {
    setRemoving(cartItemId);
    try {
      await api.delete(`/cart/remove/${cartItemId}`);
      setCartItems((prev) => prev.filter((i) => (i.cartItemId || i.id) !== cartItemId));
      setCartCount((c) => Math.max(0, c - 1));
      addToast('Item removed from cart', 'info');
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to remove item', 'error');
    } finally {
      setRemoving(null);
    }
  };

  const handleClearCart = async () => {
    if (!window.confirm('Clear all items from cart?')) return;
    setClearing(true);
    try {
      // Remove each item individually if no bulk clear endpoint exists
      await Promise.all(
        cartItems.map((item) =>
          api.delete(`/cart/remove/${item.cartItemId || item.id}`)
        )
      );
      setCartItems([]);
      setCartCount(0);
      addToast('Cart cleared', 'info');
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to clear cart', 'error');
    } finally {
      setClearing(false);
    }
  };

  const handlePlaceOrder = async () => {
    if (cartItems.length === 0) {
      addToast('Your cart is empty', 'error');
      return;
    }
    setPlacingOrder(true);
    try {
      const res = await api.post('/orders/place');
      const orderData = res.data?.data || res.data;
      const orderId = orderData?.orderId || orderData?.id || 'unknown';
      setCartItems([]);
      setCartCount(0);
      addToast('Order placed successfully! 🎉', 'success');
      navigate(`/order-confirmation/${orderId}`, { state: { order: orderData } });
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to place order. Please try again.', 'error');
    } finally {
      setPlacingOrder(false);
    }
  };

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
              disabled={clearing}
            >
              {clearing ? 'Clearing…' : '🗑 Clear Cart'}
            </button>
          )}
        </div>

        {cartItems.length === 0 ? (
          <div className="empty-state">
            <span className="empty-icon">🛒</span>
            <h3>Your cart is empty</h3>
            <p>Looks like you haven&apos;t added anything yet!</p>
            <button
              className="btn btn-primary"
              onClick={() => navigate('/')}
            >
              Browse Restaurants
            </button>
          </div>
        ) : (
          <div className="cart-layout">
            {/* Items */}
            <section className="cart-items" aria-label="Cart items">
              {cartItems.map((item) => {
                const itemId = item.cartItemId || item.id;
                const name = item.name || item.menuItem?.name || 'Item';
                const price = item.price || item.menuItem?.price || 0;
                const qty = item.quantity || 1;

                return (
                  <article key={itemId} className="cart-item">
                    <div className="cart-item-info">
                      <h3 className="cart-item-name">{name}</h3>
                      <div className="cart-item-meta">
                        <span className="cart-item-price">₹{parseFloat(price).toFixed(2)}</span>
                        <span className="cart-item-qty">× {qty}</span>
                        <span className="cart-item-total">
                          = ₹{(price * qty).toFixed(2)}
                        </span>
                      </div>
                    </div>
                    <button
                      id={`remove-item-${itemId}`}
                      className={`btn btn-danger btn-sm remove-btn ${removing === itemId ? 'loading' : ''}`}
                      onClick={() => handleRemove(itemId)}
                      disabled={removing === itemId}
                      aria-label={`Remove ${name} from cart`}
                    >
                      {removing === itemId ? (
                        <span className="btn-spinner" />
                      ) : (
                        'Remove'
                      )}
                    </button>
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
                    <span>Subtotal ({cartItems.length} items)</span>
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
