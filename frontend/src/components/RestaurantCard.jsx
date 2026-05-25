import { Link } from 'react-router-dom';
import './RestaurantCard.css';

const CUISINE_IMAGES = {
  indian:   'https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=400&q=80',
  chinese:  'https://images.unsplash.com/photo-1563245372-f21724e3856d?w=400&q=80',
  italian:  'https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=400&q=80',
  pizza:    'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400&q=80',
  burger:   'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400&q=80',
  mexican:  'https://images.unsplash.com/photo-1565299585323-38d6b0865b47?w=400&q=80',
  thai:     'https://images.unsplash.com/photo-1559314809-0d155014e29e?w=400&q=80',
  sushi:    'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=400&q=80',
  default:  'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=400&q=80',
};

function getImageUrl(restaurant) {
  if (restaurant.image) return restaurant.image;
  const cuisine = (restaurant.cuisine || '').toLowerCase();
  for (const key of Object.keys(CUISINE_IMAGES)) {
    if (cuisine.includes(key)) return CUISINE_IMAGES[key];
  }
  return CUISINE_IMAGES.default;
}

function StarRating({ rating = 0 }) {
  const max = 5;
  return (
    <span className="stars" aria-label={`${rating} out of 5 stars`}>
      {Array.from({ length: max }, (_, i) => (
        <span key={i} className={`star ${i < Math.round(rating) ? 'filled' : ''}`}>
          ★
        </span>
      ))}
      <span className="rating-number">({rating})</span>
    </span>
  );
}

export default function RestaurantCard({ restaurant }) {
  const {
    id,
    restaurantId,
    name = 'Restaurant',
    cuisine = 'Various',
    rating = 0,
    address = '',
  } = restaurant;

  const restaurantIdentifier = id || restaurantId;
  const imageUrl = getImageUrl(restaurant);

  return (
    <article className="restaurant-card" aria-label={`${name} restaurant card`}>
      <div className="card-image-wrapper">
        <img
          src={imageUrl}
          alt={`${name} restaurant`}
          className="card-image"
          loading="lazy"
          onError={(e) => { e.target.src = CUISINE_IMAGES.default; }}
        />
        <span className="cuisine-tag">{cuisine}</span>
      </div>

      <div className="card-body">
        <h3 className="card-title">{name}</h3>
        <div className="card-rating">
          <StarRating rating={rating} />
        </div>
        {address && (
          <p className="card-address">
            <span className="address-icon">📍</span> {address}
          </p>
        )}
      </div>

      <div className="card-footer">
        <Link
          to={`/restaurant/${restaurantIdentifier}`}
          className="btn btn-primary btn-sm view-menu-btn"
          id={`view-menu-${restaurantIdentifier}`}
        >
          View Menu →
        </Link>
      </div>
    </article>
  );
}
