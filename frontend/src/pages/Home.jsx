import { useEffect, useMemo, useState } from 'react';
import api from '../api/axiosConfig';
import RestaurantCard from '../components/RestaurantCard';
import './Home.css';

const RATING_OPTIONS = [
  { label: 'All Ratings', value: 0 },
  { label: '4.5+ ⭐', value: 4.5 },
  { label: '4.0+ ⭐', value: 4.0 },
  { label: '3.5+ ⭐', value: 3.5 },
];

const SORT_OPTIONS = [
  { label: 'Default',        value: 'default' },
  { label: 'Rating ↓',      value: 'rating-desc' },
  { label: 'Name A–Z',      value: 'name-asc' },
];

export default function Home() {
  const [restaurants, setRestaurants] = useState([]);
  const [loading,     setLoading]     = useState(true);
  const [error,       setError]       = useState(null);

  // Filters
  const [search,     setSearch]     = useState('');
  const [cuisine,    setCuisine]    = useState('All');
  const [minRating,  setMinRating]  = useState(0);
  const [sortBy,     setSortBy]     = useState('default');

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
          setError(err.response?.data?.message || 'Failed to load restaurants.');
        }
      })
      .finally(() => setLoading(false));
    return () => controller.abort();
  }, []);

  // Derive unique cuisines list
  const cuisines = useMemo(() => {
    const set = new Set(restaurants.map((r) => r.cuisine).filter(Boolean));
    return ['All', ...Array.from(set).sort()];
  }, [restaurants]);

  // Apply all filters + sort
  const filtered = useMemo(() => {
    let list = restaurants;

    if (search.trim()) {
      const q = search.toLowerCase();
      list = list.filter(
        (r) =>
          (r.name    || '').toLowerCase().includes(q) ||
          (r.cuisine || '').toLowerCase().includes(q) ||
          (r.address || '').toLowerCase().includes(q)
      );
    }

    if (cuisine !== 'All') {
      list = list.filter((r) => r.cuisine === cuisine);
    }

    if (minRating > 0) {
      list = list.filter((r) => (r.rating || 0) >= minRating);
    }

    if (sortBy === 'rating-desc') {
      list = [...list].sort((a, b) => (b.rating || 0) - (a.rating || 0));
    } else if (sortBy === 'name-asc') {
      list = [...list].sort((a, b) => (a.name || '').localeCompare(b.name || ''));
    }

    return list;
  }, [restaurants, search, cuisine, minRating, sortBy]);

  const hasActiveFilters = search || cuisine !== 'All' || minRating > 0 || sortBy !== 'default';

  const clearAll = () => {
    setSearch('');
    setCuisine('All');
    setMinRating(0);
    setSortBy('default');
  };

  return (
    <main className="home-page page-wrapper">
      {/* ── Hero ── */}
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

      {/* ── Restaurants Section ── */}
      <section className="restaurants-section container" aria-label="Restaurants">

        {/* Filter Bar */}
        {!loading && !error && restaurants.length > 0 && (
          <div className="filter-bar" aria-label="Filters">

            {/* Cuisine chips */}
            <div className="cuisine-chips" role="group" aria-label="Filter by cuisine">
              {cuisines.map((c) => (
                <button
                  key={c}
                  id={`cuisine-chip-${c.toLowerCase().replace(/\s+/g, '-')}`}
                  className={`cuisine-chip ${cuisine === c ? 'active' : ''}`}
                  onClick={() => setCuisine(c)}
                  aria-pressed={cuisine === c}
                >
                  {c}
                </button>
              ))}
            </div>

            {/* Right controls: Rating + Sort + Clear */}
            <div className="filter-controls">
              <select
                id="rating-filter"
                className="filter-select"
                value={minRating}
                onChange={(e) => setMinRating(Number(e.target.value))}
                aria-label="Minimum rating filter"
              >
                {RATING_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>

              <select
                id="sort-select"
                className="filter-select"
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                aria-label="Sort restaurants"
              >
                {SORT_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>

              {hasActiveFilters && (
                <button
                  id="clear-filters-btn"
                  className="clear-filters-btn"
                  onClick={clearAll}
                  aria-label="Clear all filters"
                >
                  ✕ Clear
                </button>
              )}
            </div>
          </div>
        )}

        {/* Section heading */}
        <div className="section-header">
          <h2 className="section-title">
            {search
              ? `Results for "${search}"`
              : cuisine !== 'All'
              ? `${cuisine} Restaurants`
              : 'All Restaurants'}
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
            <button className="btn btn-primary" onClick={() => window.location.reload()}>
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && filtered.length === 0 && (
          <div className="empty-state" role="status">
            <span className="empty-icon">🍽️</span>
            <h3>No restaurants found</h3>
            <p>
              {hasActiveFilters
                ? 'No results match your filters.'
                : 'No restaurants available right now.'}
            </p>
            {hasActiveFilters && (
              <button className="btn btn-outline" onClick={clearAll}>
                Clear Filters
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
