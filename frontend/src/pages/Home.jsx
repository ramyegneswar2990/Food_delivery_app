import { useEffect, useState } from 'react';
import api from '../api/axiosConfig';
import RestaurantCard from '../components/RestaurantCard';
import './Home.css';

export default function Home() {
  const [restaurants, setRestaurants] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);

    api
      .get('/restaurants', { signal: controller.signal })
      .then((res) => {
        const data = res.data?.data || res.data || [];
        setRestaurants(Array.isArray(data) ? data : []);
      })
      .catch((err) => {
        if (err.name !== 'CanceledError' && err.name !== 'AbortError') {
          setError(err.response?.data?.message || 'Failed to load restaurants. Please try again.');
        }
      })
      .finally(() => setLoading(false));

    return () => controller.abort();
  }, []);

  const filtered = restaurants.filter((r) => {
    const q = search.toLowerCase();
    return (
      (r.name || '').toLowerCase().includes(q) ||
      (r.cuisine || '').toLowerCase().includes(q) ||
      (r.address || '').toLowerCase().includes(q)
    );
  });

  return (
    <main className="home-page page-wrapper">
      {/* Hero Section */}
      <section className="hero-section" aria-label="Hero">
        <div className="hero-content container">
          <div className="hero-badge">🔥 Hot Deals Today</div>
          <h1 className="hero-title">
            Delicious Food,<br />
            <span className="hero-highlight">Delivered Fast</span>
          </h1>
          <p className="hero-subtitle">
            Order from the best local restaurants with super-fast delivery
          </p>
          <div className="search-bar" role="search">
            <span className="search-icon" aria-hidden="true">🔍</span>
            <input
              id="restaurant-search"
              type="search"
              className="search-input"
              placeholder="Search restaurants, cuisines…"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              aria-label="Search restaurants"
            />
            {search && (
              <button
                className="search-clear"
                onClick={() => setSearch('')}
                aria-label="Clear search"
              >
                ×
              </button>
            )}
          </div>
        </div>
        <div className="hero-decorations" aria-hidden="true">
          <span className="deco-emoji deco-1">🍕</span>
          <span className="deco-emoji deco-2">🍜</span>
          <span className="deco-emoji deco-3">🌮</span>
          <span className="deco-emoji deco-4">🍣</span>
          <span className="deco-emoji deco-5">🍔</span>
        </div>
      </section>

      {/* Restaurant Grid */}
      <section className="restaurants-section container" aria-label="Restaurants">
        <div className="section-header">
          <h2 className="section-title">
            {search ? `Results for "${search}"` : 'All Restaurants'}
          </h2>
          {!loading && !error && (
            <span className="restaurant-count">
              {filtered.length} {filtered.length === 1 ? 'place' : 'places'}
            </span>
          )}
        </div>

        {loading && (
          <div className="spinner-overlay" role="status" aria-label="Loading restaurants">
            <div className="spinner" />
            <p className="spinner-text">Finding the best spots for you…</p>
          </div>
        )}

        {!loading && error && (
          <div className="error-state" role="alert">
            <span className="error-icon">😕</span>
            <h3>Oops!</h3>
            <p>{error}</p>
            <button
              className="btn btn-primary"
              onClick={() => window.location.reload()}
            >
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && filtered.length === 0 && (
          <div className="empty-state" role="status">
            <span className="empty-icon">🍽️</span>
            <h3>No restaurants found</h3>
            <p>
              {search
                ? `No results for "${search}". Try a different search.`
                : 'No restaurants available right now. Check back soon!'}
            </p>
            {search && (
              <button className="btn btn-outline" onClick={() => setSearch('')}>
                Clear Search
              </button>
            )}
          </div>
        )}

        {!loading && !error && filtered.length > 0 && (
          <div className="restaurant-grid" aria-label="Restaurant list">
            {filtered.map((restaurant) => (
              <RestaurantCard
                key={restaurant.id || restaurant.restaurantId}
                restaurant={restaurant}
              />
            ))}
          </div>
        )}
      </section>
    </main>
  );
}
