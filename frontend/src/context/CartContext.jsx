import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api/axiosConfig';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [cartCount, setCartCount] = useState(0);

  const fetchCartCount = useCallback(async () => {
    if (!isAuthenticated) {
      setCartCount(0);
      return;
    }
    try {
      const res = await api.get('/cart');
      const items = res.data?.data || res.data || [];
      setCartCount(Array.isArray(items) ? items.length : 0);
    } catch {
      setCartCount(0);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    fetchCartCount();
  }, [fetchCartCount]);

  const refreshCart = () => fetchCartCount();

  return (
    <CartContext.Provider value={{ cartCount, setCartCount, refreshCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
}
