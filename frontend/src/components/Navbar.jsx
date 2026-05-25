import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useCart } from '../context/CartContext';
import './Navbar.css';

export default function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const { cartCount } = useCart();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="navbar" role="banner">
      <div className="navbar-inner container">
        {/* Logo */}
        <Link to="/" className="navbar-logo" aria-label="FoodApp Home">
          <span className="logo-icon">🍔</span>
          <span className="logo-text">FoodApp</span>
        </Link>

        {/* Right Side */}
        <nav className="navbar-actions" aria-label="Main navigation">
          {/* Cart */}
          <Link to="/cart" className="cart-btn" aria-label={`Cart, ${cartCount} items`}>
            <span className="cart-icon">🛒</span>
            {cartCount > 0 && (
              <span className="cart-badge" aria-hidden="true">
                {cartCount > 99 ? '99+' : cartCount}
              </span>
            )}
          </Link>

          {isAuthenticated ? (
            <>
              <Link to="/orders" className="nav-link orders-link">
                📋 Orders
              </Link>
              <div className="user-info">
                <span className="user-avatar">
                  {user?.name?.charAt(0)?.toUpperCase() || 'U'}
                </span>
                <span className="user-greeting">Hi, {user?.name?.split(' ')[0] || 'User'}</span>
              </div>
              <button
                id="logout-btn"
                className="btn btn-outline btn-sm logout-btn"
                onClick={handleLogout}
              >
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/login" className="btn btn-outline btn-sm" id="nav-login-btn">
                Login
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm" id="nav-register-btn">
                Register
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
