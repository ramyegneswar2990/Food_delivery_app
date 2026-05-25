import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../api/axiosConfig';
import { useAuth } from './AuthContext';

const CartContext = createContext(null);

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [cartCount, setCartCount] = useState(0);
  // Full items list so RestaurantMenu can read which items are already in cart
  const [cartItems, setCartItems] = useState([]);

  const fetchCart = useCallback(async () => {
    if (!isAuthenticated) {
      setCartCount(0);
      setCartItems([]);
      return;
    }
    try {
      const res = await api.get('/cart');
      // Backend returns ApiResponse<CartDTO>
      // CartDTO has { items: [...] } where each item is a CartItemDTO
      const cartDto = res.data?.data || res.data || {};
      const items = cartDto.items || [];
      setCartItems(Array.isArray(items) ? items : []);
      // Count total quantity across all cart items
      const totalQty = Array.isArray(items)
        ? items.reduce((sum, i) => sum + (i.quantity || 1), 0)
        : 0;
      setCartCount(totalQty);
    } catch {
      setCartCount(0);
      setCartItems([]);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const refreshCart = () => fetchCart();

  return (
    <CartContext.Provider value={{ cartCount, setCartCount, cartItems, refreshCart }}>
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
