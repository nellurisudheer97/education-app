import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FiArrowRight, FiBookOpen, FiEye, FiEyeOff, FiShield, FiTrendingUp } from 'react-icons/fi';
import api from '../services/api';
import { saveSession } from '../services/session';
import { validateEmail, validateRequired } from '../utils/validation';
import '../styles/auth.css';

export default function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateEmail(email.trim())) {
      setError('Please enter a valid email address.');
      return;
    }
    if (!validateRequired(password)) {
      setError('Password is required.');
      return;
    }
    setLoading(true);
    setError('');

    try {
      const response = await api.login({ email: email.trim(), password });
      if (response?.token) {
        saveSession(response);
        navigate('/dashboard', { replace: true });
        return;
      }

      setError(response?.message || 'We could not sign you in with those details.');
    } catch (err) {
      setError(err.message || 'Sign in failed. Please check your connection and try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-panel">
        <div className="auth-brand">
          <span className="brand-mark"><FiBookOpen /></span>
          <span>EduForge</span>
        </div>
        <h1>Welcome back</h1>
        <p className="auth-subtitle">Continue learning, building courses, and tracking progress from one polished workspace.</p>

        <div className="auth-highlights">
          <span><FiShield /> Secure access</span>
          <span><FiTrendingUp /> Progress focused</span>
        </div>
      </section>

      <section className="auth-card" aria-label="Sign in form">
        <div className="auth-card-header">
          <p className="eyebrow">Sign in</p>
          <h2>Access your classroom</h2>
        </div>

        {error && <div className="error-message">{error}</div>}

        <form onSubmit={handleSubmit}>
          <label>
            Email address
            <input
              type="email"
              placeholder="student@eduapp.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              required
            />
          </label>

          <label>
            Password
            <div className="password-input-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
                required
              />
              <button
                type="button"
                className="toggle-password-btn"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
                title={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <FiEyeOff /> : <FiEye />}
              </button>
            </div>
          </label>

          <Link to="/forgot-password" className="forgot-password-link" style={{ display: 'block', textAlign: 'right', fontSize: '0.9rem', marginBottom: '20px', color: '#0066cc', textDecoration: 'none' }}>
            Forgot password?
          </Link>

          <button className="btn-primary auth-submit" type="submit" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign in'}
            {!loading && <FiArrowRight />}
          </button>
        </form>

        <p className="auth-switch">
          New to EduForge? <Link to="/register">Create an account</Link>
        </p>
      </section>
    </main>
  );
}
