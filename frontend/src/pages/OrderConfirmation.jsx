import { Link, useParams, useLocation, useNavigate } from 'react-router-dom';
import './OrderConfirmation.css';

export default function OrderConfirmation() {
  const { orderId } = useParams();
  const { state } = useLocation();
  const navigate = useNavigate();

  const order = state?.order;

  const items = order?.items || order?.orderItems || [];
  const restaurantName = order?.restaurantName || order?.restaurant?.name || 'Restaurant';
  const total = order?.totalAmount || order?.total || 0;
  const status = order?.status || 'PLACED';
  const placedAt = order?.createdAt || order?.placedAt || new Date().toISOString();

  const formatDate = (isoStr) => {
    try {
      return new Intl.DateTimeFormat('en-IN', {
        dateStyle: 'medium',
        timeStyle: 'short',
      }).format(new Date(isoStr));
    } catch {
      return isoStr;
    }
  };

  return (
    <main className="confirmation-page page-wrapper">
      <div className="container">
        <div className="confirmation-card">
          {/* Success Animation */}
          <div className="success-icon-wrapper" aria-hidden="true">
            <div className="success-ring" />
            <span className="success-icon">✓</span>
          </div>

          <h1 className="confirmation-title">Order Placed!</h1>
          <p className="confirmation-subtitle">
            Your order is being prepared. Sit back and relax! 🍔
          </p>

          {/* Order Details */}
          <div className="order-details-card">
            <div className="order-detail-row">
              <span className="detail-label">Order ID</span>
              <span className="detail-value order-id-val">#{orderId}</span>
            </div>
            <div className="order-detail-row">
              <span className="detail-label">Restaurant</span>
              <span className="detail-value">{restaurantName}</span>
            </div>
            <div className="order-detail-row">
              <span className="detail-label">Status</span>
              <span className={`badge badge-${status.toLowerCase()}`}>{status}</span>
            </div>
            <div className="order-detail-row">
              <span className="detail-label">Placed at</span>
              <span className="detail-value">{formatDate(placedAt)}</span>
            </div>
          </div>

          {/* Items */}
          {items.length > 0 && (
            <div className="confirmation-items">
              <h2 className="items-heading">Items Ordered</h2>
              <ul className="items-list">
                {items.map((item, idx) => {
                  const name = item.name || item.menuItemName || item.menuItem?.name || `Item ${idx + 1}`;
                  const qty = item.quantity || 1;
                  const price = item.price || item.unitPrice || item.menuItem?.price || 0;
                  return (
                    <li key={idx} className="item-row">
                      <span className="item-row-name">{name}</span>
                      <span className="item-row-qty">× {qty}</span>
                      <span className="item-row-price">₹{(price * qty).toFixed(2)}</span>
                    </li>
                  );
                })}
              </ul>
              <div className="items-total">
                <span>Total</span>
                <span className="items-total-amount">₹{parseFloat(total).toFixed(2)}</span>
              </div>
            </div>
          )}

          {/* Actions */}
          <div className="confirmation-actions">
            <Link
              to="/orders"
              id="view-history-link"
              className="btn btn-outline"
            >
              📋 View Order History
            </Link>
            <button
              id="go-home-btn"
              className="btn btn-primary"
              onClick={() => navigate('/')}
            >
              🏠 Go Home
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}
