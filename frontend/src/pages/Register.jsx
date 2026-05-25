import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { useToast } from '../components/Toast';
import './Auth.css';

export default function Register() {
  const navigate = useNavigate();
  const addToast = useToast();

  const [form, setForm] = useState({ name: '', email: '', password: '', address: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [apiError, setApiError] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((f) => ({ ...f, [name]: value }));
    if (errors[name]) setErrors((e) => ({ ...e, [name]: '' }));
    setApiError('');
  };

  const validate = () => {
    const errs = {};
    if (!form.name.trim()) errs.name = 'Full name is required';
    if (!form.email.trim()) errs.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) errs.email = 'Enter a valid email address';
    if (!form.address.trim()) errs.address = 'Delivery address is required';
    if (!form.password) errs.password = 'Password is required';
    else if (form.password.length < 6) errs.password = 'Password must be at least 6 characters';
    return errs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }

    setLoading(true);
    setApiError('');
    try {
      await api.post('/auth/register', form);
      addToast('Registration successful! Please login. 🎉', 'success', 5000);
      navigate('/login', { state: { registered: true } });
    } catch (err) {
      setApiError(err.response?.data?.message || 'Registration failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page page-wrapper">
      <div className="auth-container auth-container-reverse">
        {/* Decorative panel */}
        <div className="auth-decoration" aria-hidden="true">
          <div className="deco-card">
            <span>🍕</span><span>🍜</span><span>🌮</span>
            <span>🍣</span><span>🍔</span><span>🥗</span>
            <h2>Join FoodApp<br />today!</h2>
            <p>Thousands of restaurants at your fingertips</p>
          </div>
        </div>

        <div className="auth-card">
          <div className="auth-brand">
            <span className="auth-logo">🍔</span>
            <h1 className="auth-title">Create Account</h1>
            <p className="auth-subtitle">Join FoodApp and start ordering</p>
          </div>

          {apiError && (
            <div className="alert alert-error" role="alert" aria-live="polite">
              ⚠️ {apiError}
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate aria-label="Registration form">
            <div className="form-group">
              <label htmlFor="reg-name">Full name</label>
              <input
                id="reg-name"
                name="name"
                type="text"
                className={`form-input ${errors.name ? 'input-error' : ''}`}
                placeholder="John Doe"
                value={form.name}
                onChange={handleChange}
                autoComplete="name"
              />
              {errors.name && <span className="form-error">{errors.name}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="reg-email">Email address</label>
              <input
                id="reg-email"
                name="email"
                type="email"
                className={`form-input ${errors.email ? 'input-error' : ''}`}
                placeholder="you@example.com"
                value={form.email}
                onChange={handleChange}
                autoComplete="email"
              />
              {errors.email && <span className="form-error">{errors.email}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="reg-address">Delivery address</label>
              <textarea
                id="reg-address"
                name="address"
                className={`form-input ${errors.address ? 'input-error' : ''}`}
                placeholder="123 Main St, Apt 4B, City"
                value={form.address}
                onChange={handleChange}
                rows="2"
                style={{ resize: 'vertical' }}
              />
              {errors.address && <span className="form-error">{errors.address}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="reg-password">Password</label>
              <input
                id="reg-password"
                name="password"
                type="password"
                className={`form-input ${errors.password ? 'input-error' : ''}`}
                placeholder="Min. 6 characters"
                value={form.password}
                onChange={handleChange}
                autoComplete="new-password"
              />
              {errors.password && <span className="form-error">{errors.password}</span>}
              {!errors.password && form.password && (
                <div className="password-strength">
                  <div
                    className="strength-bar"
                    style={{
                      width: `${Math.min((form.password.length / 12) * 100, 100)}%`,
                      background: form.password.length < 6
                        ? 'var(--status-cancelled)'
                        : form.password.length < 10
                        ? 'var(--status-preparing)'
                        : 'var(--status-delivered)',
                    }}
                  />
                </div>
              )}
            </div>

            <button
              id="register-submit-btn"
              type="submit"
              className={`btn btn-primary btn-lg submit-btn ${loading ? 'loading' : ''}`}
              disabled={loading}
              aria-busy={loading}
            >
              {loading ? (
                <>
                  <span className="btn-spinner" />
                  Creating account…
                </>
              ) : (
                'Create Account'
              )}
            </button>
          </form>

          <p className="auth-footer-text">
            Already have an account?{' '}
            <Link to="/login" id="go-to-login-link">Sign in</Link>
          </p>
        </div>
      </div>
    </main>
  );
}
