import { useEffect, useState } from 'react';
import api from '../api/axiosConfig';
import { useToast } from '../components/Toast';
import './OrderHistory.css';

// Only PLACED, CONFIRMED, CANCELLED remain
const STATUS_CONFIG = {
  PLACED:     { label: 'Placed',     class: 'badge-placed',     icon: '📦' },
  CONFIRMED:  { label: 'Confirmed',  class: 'badge-confirmed',  icon: '✅' },
  CANCELLED:  { label: 'Cancelled',  class: 'badge-cancelled',  icon: '❌' },
};

// Orders that can still be cancelled
const CANCELLABLE = new Set(['PLACED', 'CONFIRMED']);

function getStatusInfo(status) {
  return STATUS_CONFIG[status?.toUpperCase()] || { label: status || 'Unknown', class: '', icon: '📋' };
}

function formatDate(isoStr) {
  try {
    return new Intl.DateTimeFormat('en-IN', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(isoStr));
  } catch {
    return isoStr || '-';
  }
}

function OrderCard({ order, onCancelled }) {
  const addToast = useToast();
  const [expanded, setExpanded]   = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [status, setStatus]       = useState(order.status);

  const statusInfo  = getStatusInfo(status);
  const orderId     = order.orderId || order.id;
  const restaurant  = order.restaurantName || order.restaurant?.name || 'Restaurant';
  const total       = order.totalAmount || order.total || 0;
  const date        = order.orderedAt || order.createdAt || order.placedAt || '';
  const items       = order.items || order.orderItems || [];
  const canCancel   = CANCELLABLE.has(status?.toUpperCase());

  const handleCancel = async () => {
    if (!window.confirm(`Cancel order #${orderId}?`)) return;
    setCancelling(true);
    try {
      const res = await api.put(`/orders/cancel/${orderId}`);
      const updated = res.data?.data || res.data;
      const newStatus = updated?.status || 'CANCELLED';
      setStatus(newStatus);
      addToast(`Order #${orderId} cancelled successfully`, 'info');
      if (onCancelled) onCancelled(orderId);
    } catch (err) {
      addToast(err.response?.data?.message || 'Failed to cancel order', 'error');
    } finally {
      setCancelling(false);
    }
  };

  return (
    <article className="order-card" aria-label={`Order #${orderId}`}>
      <div className="order-card-header">
        <div className="order-card-main">
          <span className="order-number">#{orderId}</span>
          <h3 className="order-restaurant">{restaurant}</h3>
          <div className="order-meta">
            <span className="order-date">🕐 {formatDate(date)}</span>
            <span className="order-total">₹{parseFloat(total).toFixed(2)}</span>
          </div>
        </div>
        <div className="order-card-actions">
          <span
            className={`badge ${statusInfo.class}`}
            role="status"
            aria-label={`Status: ${statusInfo.label}`}
          >
            {statusInfo.icon} {statusInfo.label}
          </span>

          {items.length > 0 && (
            <button
              className="toggle-btn"
              onClick={() => setExpanded((v) => !v)}
              aria-expanded={expanded}
              aria-controls={`order-items-${orderId}`}
              id={`toggle-order-${orderId}`}
            >
              {expanded ? '▲ Hide' : '▼ Details'}
            </button>
          )}

          {canCancel && (
            <button
              id={`cancel-order-${orderId}`}
              className={`btn btn-danger btn-sm cancel-order-btn ${cancelling ? 'loading' : ''}`}
              onClick={handleCancel}
              disabled={cancelling}
              aria-label={`Cancel order #${orderId}`}
            >
              {cancelling ? <span className="btn-spinner" /> : '✕ Cancel'}
            </button>
          )}
        </div>
      </div>

      {expanded && items.length > 0 && (
        <div
          className="order-items-list"
          id={`order-items-${orderId}`}
          role="region"
          aria-label="Order items"
        >
          <ul>
            {items.map((item, idx) => {
              // OrderItemDTO uses 'itemName'
              const name  = item.itemName || item.name || item.menuItemName || item.menuItem?.name || `Item ${idx + 1}`;
              const qty   = item.quantity || 1;
              const price = item.price || item.unitPrice || item.menuItem?.price || 0;
              return (
                <li key={idx} className="order-item-row">
                  <span className="oi-name">{name}</span>
                  <span className="oi-qty">× {qty}</span>
                  <span className="oi-price">₹{(price * qty).toFixed(2)}</span>
                </li>
              );
            })}
          </ul>
        </div>
      )}
    </article>
  );
}

export default function OrderHistory() {
  const [orders, setOrders]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState(null);
  const [filter, setFilter]   = useState('ALL');

  useEffect(() => {
    const controller = new AbortController();
    api
      .get('/orders/history', { signal: controller.signal })
      .then((res) => {
        const data = res.data?.data || res.data || [];
        setOrders(Array.isArray(data) ? data : []);
      })
      .catch((err) => {
        if (err.name !== 'CanceledError' && err.name !== 'AbortError') {
          setError(err.response?.data?.message || 'Failed to load order history.');
        }
      })
      .finally(() => setLoading(false));

    return () => controller.abort();
  }, []);

  // When an order is cancelled, update its status in local state
  const handleCancelled = (orderId) => {
    setOrders((prev) =>
      prev.map((o) =>
        (o.orderId || o.id) === orderId ? { ...o, status: 'CANCELLED' } : o
      )
    );
  };

  const statuses = ['ALL', ...Object.keys(STATUS_CONFIG)];
  const filtered = filter === 'ALL'
    ? orders
    : orders.filter((o) => (o.status || '').toUpperCase() === filter);

  return (
    <main className="history-page page-wrapper">
      <div className="container">
        <div className="history-header">
          <h1 className="history-title">📋 Order History</h1>
          <p className="history-subtitle">
            {!loading && `${orders.length} total order${orders.length !== 1 ? 's' : ''}`}
          </p>
        </div>

        {/* Status Filter Tabs */}
        {!loading && !error && orders.length > 0 && (
          <div className="status-filters" role="tablist" aria-label="Filter by status">
            {statuses.map((s) => (
              <button
                key={s}
                role="tab"
                aria-selected={filter === s}
                className={`filter-tab ${filter === s ? 'active' : ''}`}
                onClick={() => setFilter(s)}
                id={`filter-tab-${s.toLowerCase()}`}
              >
                {s === 'ALL' ? '🌐 All' : `${STATUS_CONFIG[s].icon} ${STATUS_CONFIG[s].label}`}
              </button>
            ))}
          </div>
        )}

        {loading && (
          <div className="spinner-overlay">
            <div className="spinner" />
            <p className="spinner-text">Loading your orders…</p>
          </div>
        )}

        {!loading && error && (
          <div className="error-state" role="alert">
            <span className="error-icon">😕</span>
            <h3>Something went wrong</h3>
            <p>{error}</p>
          </div>
        )}

        {!loading && !error && orders.length === 0 && (
          <div className="empty-state">
            <span className="empty-icon">🍽️</span>
            <h3>No orders yet</h3>
            <p>Looks like you haven&apos;t placed any orders. Start exploring!</p>
          </div>
        )}

        {!loading && !error && filtered.length === 0 && orders.length > 0 && (
          <div className="empty-state">
            <span className="empty-icon">🔍</span>
            <h3>No {filter.toLowerCase()} orders</h3>
            <p>You don&apos;t have any orders with this status.</p>
          </div>
        )}

        {!loading && !error && filtered.length > 0 && (
          <div className="orders-list" aria-label="Orders list">
            {filtered.map((order) => (
              <OrderCard
                key={order.orderId || order.id}
                order={order}
                onCancelled={handleCancelled}
              />
            ))}
          </div>
        )}
      </div>
    </main>
  );
}
