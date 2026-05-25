import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { CartProvider } from './context/CartContext';
import { ToastProvider } from './components/Toast';
import Navbar from './components/Navbar';
import PrivateRoute from './components/PrivateRoute';

import Home from './pages/Home';
import RestaurantMenu from './pages/RestaurantMenu';
import Login from './pages/Login';
import Register from './pages/Register';
import Cart from './pages/Cart';
import OrderConfirmation from './pages/OrderConfirmation';
import OrderHistory from './pages/OrderHistory';

import './components/Toast.css';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <ToastProvider>
            <Navbar />
            <Routes>
              {/* Public routes */}
              <Route path="/" element={<Home />} />
              <Route path="/restaurant/:id" element={<RestaurantMenu />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />

              {/* Protected routes */}
              <Route
                path="/cart"
                element={
                  <PrivateRoute>
                    <Cart />
                  </PrivateRoute>
                }
              />
              <Route
                path="/order-confirmation/:orderId"
                element={
                  <PrivateRoute>
                    <OrderConfirmation />
                  </PrivateRoute>
                }
              />
              <Route
                path="/orders"
                element={
                  <PrivateRoute>
                    <OrderHistory />
                  </PrivateRoute>
                }
              />

              {/* Fallback */}
              <Route path="*" element={<NotFound />} />
            </Routes>
          </ToastProvider>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

function NotFound() {
  return (
    <main className="page-wrapper" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <div className="empty-state">
        <span className="empty-icon">🔍</span>
        <h2>404 — Page Not Found</h2>
        <p>The page you&apos;re looking for doesn&apos;t exist.</p>
        <a href="/" className="btn btn-primary" style={{ marginTop: '8px' }}>Go Home</a>
      </div>
    </main>
  );
}
