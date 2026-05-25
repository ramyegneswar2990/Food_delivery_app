import { useEffect, useState, useMemo } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import { useToast } from '../components/Toast';
import './RestaurantMenu.css';

export default function RestaurantMenu() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { cartItems, cartCount, refreshCart } = useCart();
  const addToast = useToast();

  const [restaurant, setRestaurant] = useState(null);
  const [menuItems, setMenuItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [loadingItem, setLoadingItem] = useState(null); // item being added/changed
  const [showLoginModal, setShowLoginModal] = useState(false);
  const [activeCategory, setActiveCategory] = useState(null);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);

    Promise.all([
      api.get(`/restaurants/${id}`, { signal: controller.signal }),
      api.get(`/restaurants/${id}/menu`, { signal: controller.signal }),
    ])
      .then(([restaurantRes, menuRes]) => {
        const rData = restaurantRes.data?.data || restaurantRes.data;
        const mData = menuRes.data?.data || menuRes.data || [];
        setRestaurant(rData);
        setMenuItems(Array.isArray(mData) ? mData : []);
      })
      .catch((err) => {
        if (err.name !== 'CanceledError' && err.name !== 'AbortError') {
          setError(err.response?.data?.message || 'Failed to load menu. Please try again.');
        }
      })
      .finally(() => setLoading(false));

    return () => controller.abort();
  }, [id]);

  // Build a map: menuItemId → cartItem (so we know quantity per item)
  const cartMap = useMemo(() => {
    const map = {};
    cartItems.forEach((ci) => {
      const mid = ci.menuItemId || ci.menuItem?.id;
      if (mid) map[mid] = ci;
    });
    return map;
  }, [cartItems]);

  // Group menu items by category
  const categorized = menuItems.reduce((acc, item) => {
    const cat = item.category || 'Other';
    if (!acc[cat]) acc[cat] = [];
    acc[cat].push(item);
    return acc;
  }, {});

  const categories = Object.keys(categorized);

  useEffect(() => {
    if (categories.length > 0 && !activeCategory) {
      setActiveCategory(categories[0]);
    }
  }, [categories, activeCategory]);

  // ── Add to cart ──────────────────────────────────────────────────────────
  const handleAddToCart = async (item) => {
    if (!isAuthenticated) {
      setShowLoginModal(true);
      return;
    }
    const itemId = item.id || item.menuItemId;
    setLoadingItem(itemId);
    try {
      await api.post('/cart/add', { menuItemId: itemId, quantity: 1 });
      await refreshCart();
      addToast(`${item.name} added to cart! 🛒`, 'success');
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to add item to cart', 'error');
    } finally {
      setLoadingItem(null);
    }
  };

  // ── Update quantity ──────────────────────────────────────────────────────
  const handleUpdateQuantity = async (item, delta) => {
    if (!isAuthenticated) return;
    const menuItemId = item.id || item.menuItemId;
    const cartItem = cartMap[menuItemId];
    if (!cartItem) return;

    const newQty = (cartItem.quantity || 1) + delta;
    setLoadingItem(menuItemId);
    try {
      if (newQty <= 0) {
        // Remove via stable menuItemId endpoint
        await api.put(`/cart/update-by-menu/${menuItemId}`, { quantity: 0 });
        addToast(`${item.name} removed from cart`, 'info');
      } else {
        // Update via stable menuItemId endpoint
        await api.put(`/cart/update-by-menu/${menuItemId}`, { quantity: newQty });
      }
      await refreshCart();
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to update cart', 'error');
    } finally {
      setLoadingItem(null);
    }
  };

  if (loading) {
    return (
      <div className="page-wrapper">
        <div className="spinner-overlay" role="status">
          <div className="spinner" />
          <p className="spinner-text">Loading menu…</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-wrapper">
        <div className="error-state container" role="alert">
          <span className="error-icon">😕</span>
          <h3>Failed to load</h3>
          <p>{error}</p>
          <button className="btn btn-primary" onClick={() => navigate('/')}>
            ← Back to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <main className="menu-page page-wrapper">
      {/* Restaurant Header */}
      <section className="restaurant-header">
        <div className="container restaurant-header-inner">
          <button
            className="back-btn"
            onClick={() => navigate('/')}
            aria-label="Go back to restaurants"
          >
            ← Back
          </button>
          <div className="restaurant-info">
            <h1 className="restaurant-name">{restaurant?.name}</h1>
            <div className="restaurant-meta">
              {restaurant?.cuisine && (
                <span className="meta-tag">🍽️ {restaurant.cuisine}</span>
              )}
              {restaurant?.rating && (
                <span className="meta-tag">⭐ {restaurant.rating}</span>
              )}
              {restaurant?.address && (
                <span className="meta-tag address-meta">📍 {restaurant.address}</span>
              )}
            </div>
          </div>
        </div>
      </section>

      <div className="menu-layout container">
        {/* Category Sidebar */}
        {categories.length > 1 && (
          <aside className="category-sidebar" aria-label="Menu categories">
            <h2 className="sidebar-title">Categories</h2>
            <nav>
              {categories.map((cat) => (
                <button
                  key={cat}
                  className={`category-btn ${activeCategory === cat ? 'active' : ''}`}
                  onClick={() => setActiveCategory(cat)}
                  aria-current={activeCategory === cat ? 'true' : undefined}
                >
                  {cat}
                  <span className="category-count">
                    {categorized[cat].length}
                  </span>
                </button>
              ))}
            </nav>
          </aside>
        )}

        {/* Menu Items */}
        <section className="menu-content" aria-label="Menu items">
          {menuItems.length === 0 ? (
            <div className="empty-state">
              <span className="empty-icon">📋</span>
              <h3>No menu items</h3>
              <p>This restaurant hasn&apos;t added any items yet.</p>
            </div>
          ) : (
            categories.map((cat) => (
              <div
                key={cat}
                className="menu-category"
                style={{ display: !activeCategory || activeCategory === cat ? 'block' : 'none' }}
                id={`category-${cat.toLowerCase().replace(/\s+/g, '-')}`}
              >
                <h2 className="category-title">{cat}</h2>
                <div className="menu-items-grid">
                  {categorized[cat].map((item) => {
                    const itemId = item.id || item.menuItemId;
                    const cartItem = cartMap[itemId];
                    const inCart = !!cartItem;
                    const qty = cartItem?.quantity || 0;
                    const isLoading = loadingItem === itemId;

                    return (
                      <article key={itemId} className="menu-item-card">
                        {item.imageUrl && (
                          <img
                            src={item.imageUrl}
                            alt={item.name}
                            className="item-image"
                            loading="lazy"
                            onError={(e) => { e.target.style.display = 'none'; }}
                          />
                        )}
                        <div className="item-info">
                          <h3 className="item-name">{item.name}</h3>
                          {item.description && (
                            <p className="item-description">{item.description}</p>
                          )}
                          <span className="item-price">
                            ₹{parseFloat(item.price || 0).toFixed(2)}
                          </span>
                        </div>

                        {/* ── Quantity control / Add button ─────────────── */}
                        <div className="item-action">
                          {isLoading ? (
                            <button className="btn btn-primary btn-sm add-btn loading" disabled>
                              <span className="btn-spinner" />
                            </button>
                          ) : inCart ? (
                            <div className="qty-control" role="group" aria-label={`Quantity of ${item.name}`}>
                              <button
                                className="qty-btn qty-minus"
                                onClick={() => handleUpdateQuantity(item, -1)}
                                aria-label={`Decrease quantity of ${item.name}`}
                              >
                                −
                              </button>
                              <span className="qty-value">{qty}</span>
                              <button
                                className="qty-btn qty-plus"
                                onClick={() => handleUpdateQuantity(item, 1)}
                                aria-label={`Increase quantity of ${item.name}`}
                              >
                                +
                              </button>
                            </div>
                          ) : (
                            <button
                              id={`add-to-cart-${itemId}`}
                              className="btn btn-primary btn-sm add-btn"
                              onClick={() => handleAddToCart(item)}
                              aria-label={`Add ${item.name} to cart`}
                            >
                              + Add
                            </button>
                          )}
                        </div>
                      </article>
                    );
                  })}
                </div>
              </div>
            ))
          )}
        </section>
      </div>

      {/* ── Floating Go to Cart Bar ─────────────────────────────────────────── */}
      {isAuthenticated && cartCount > 0 && (
        <div className="floating-cart-bar" role="complementary" aria-label="Cart summary">
          <span className="floating-cart-info">
            🛒 {cartCount} item{cartCount !== 1 ? 's' : ''} in cart
          </span>
          <button
            id="go-to-cart-btn"
            className="btn btn-light btn-sm"
            onClick={() => navigate('/cart')}
          >
            Go to Cart →
          </button>
        </div>
      )}

      {/* Login Modal */}
      {showLoginModal && (
        <div
          className="modal-overlay"
          role="dialog"
          aria-modal="true"
          aria-labelledby="login-modal-title"
          onClick={(e) => { if (e.target === e.currentTarget) setShowLoginModal(false); }}
        >
          <div className="modal">
            <span className="modal-icon" aria-hidden="true">🔒</span>
            <h3 id="login-modal-title">Login Required</h3>
            <p>Please login to add items to your cart and place orders.</p>
            <div className="modal-actions">
              <button
                className="btn btn-outline btn-sm"
                onClick={() => setShowLoginModal(false)}
              >
                Cancel
              </button>
              <button
                id="modal-login-btn"
                className="btn btn-primary btn-sm"
                onClick={() => navigate('/login')}
              >
                Login
              </button>
            </div>
          </div>
        </div>
      )}
    </main>
  );
}
