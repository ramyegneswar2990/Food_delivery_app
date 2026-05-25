import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../components/Toast';
import './Auth.css';

export default function Login() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const addToast = useToast();

  const [form, setForm] = useState({ email: '', password: '' });
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
    if (!form.email.trim()) errs.email = 'Email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) errs.email = 'Enter a valid email';
    if (!form.password) errs.password = 'Password is required';
    return errs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) { setErrors(errs); return; }

    setLoading(true);
    setApiError('');
    try {
      const res = await api.post('/auth/login', form);
      const data = res.data?.data || res.data;
      const token = data?.token || data?.accessToken;
      const userData = {
        name: data?.name || data?.username || form.email.split('@')[0],
        email: data?.email || form.email,
        userId: data?.userId || data?.id,
      };
      login(userData, token);
      addToast(`Welcome back, ${userData.name}! 🎉`, 'success');
      navigate('/');
    } catch (err) {
      setApiError(err.response?.data?.message || 'Invalid email or password. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-page page-wrapper">
      <div className="auth-container">
        <div className="auth-card">
          {/* Branding */}
          <div className="auth-brand">
            <span className="auth-logo">🍔</span>
            <h1 className="auth-title">Welcome Back</h1>
            <p className="auth-subtitle">Sign in to your FoodApp account</p>
          </div>

          {apiError && (
            <div className="alert alert-error" role="alert" aria-live="polite">
              ⚠️ {apiError}
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate aria-label="Login form">
            <div className="form-group">
              <label htmlFor="login-email">Email address</label>
              <input
                id="login-email"
                name="email"
                type="email"
                className={`form-input ${errors.email ? 'input-error' : ''}`}
                placeholder="you@example.com"
                value={form.email}
                onChange={handleChange}
                autoComplete="email"
                aria-describedby={errors.email ? 'email-err' : undefined}
              />
              {errors.email && (
                <span id="email-err" className="form-error">{errors.email}</span>
              )}
            </div>

            <div className="form-group">
              <label htmlFor="login-password">Password</label>
              <input
                id="login-password"
                name="password"
                type="password"
                className={`form-input ${errors.password ? 'input-error' : ''}`}
                placeholder="Your password"
                value={form.password}
                onChange={handleChange}
                autoComplete="current-password"
                aria-describedby={errors.password ? 'password-err' : undefined}
              />
              {errors.password && (
                <span id="password-err" className="form-error">{errors.password}</span>
              )}
            </div>

            <button
              id="login-submit-btn"
              type="submit"
              className={`btn btn-primary btn-lg submit-btn ${loading ? 'loading' : ''}`}
              disabled={loading}
              aria-busy={loading}
            >
              {loading ? (
                <>
                  <span className="btn-spinner" />
                  Signing in…
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          <p className="auth-footer-text">
            Don&apos;t have an account?{' '}
            <Link to="/register" id="go-to-register-link">Create one</Link>
          </p>
        </div>

        {/* Decorative panel */}
        <div className="auth-decoration" aria-hidden="true">
          <div className="deco-card">
            <span>🍕</span><span>🍜</span><span>🌮</span>
            <span>🍣</span><span>🍔</span><span>🥗</span>
            <h2>Order your<br />favorite food</h2>
            <p>Fast delivery to your doorstep</p>
          </div>
        </div>
      </div>
    </main>
  );
}
