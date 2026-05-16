import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { FiArrowRight, FiBookOpen, FiMail, FiArrowLeft } from 'react-icons/fi';
import api from '../services/api';
import { validateEmail } from '../utils/validation';
import '../styles/auth.css';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateEmail(email.trim())) {
      setError('Please enter a valid email address.');
      return;
    }
    setLoading(true);
    setError('');
    setSuccess('');

    try {
      const response = await api.forgotPassword({ email: email.trim() });
      setSuccess(response?.message || 'If an account with that email exists, a reset link has been sent to your email.');
      setEmail('');
      
      // Redirect to login after 5 seconds
      setTimeout(() => {
        navigate('/login');
      }, 5000);
    } catch (err) {
      setError(err.message || 'Failed to process password reset request. Please try again.');
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
        <h1>Reset your password</h1>
        <p className="auth-subtitle">Enter your email address and we'll send you a link to reset your password.</p>

        <div className="auth-highlights">
          <span><FiMail /> Secure reset</span>
          <span><FiArrowRight /> Quick recovery</span>
        </div>
      </section>

      <section className="auth-card" aria-label="Forgot password form">
        <div className="auth-card-header">
          <p className="eyebrow">Account recovery</p>
          <h2>Find your account</h2>
        </div>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <form onSubmit={handleSubmit}>
          <label>
            Email address
            <input
              type="email"
              placeholder="your@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              autoComplete="email"
              disabled={success ? true : false}
              required
            />
          </label>

          <button
            type="submit"
            disabled={loading || success ? true : false}
            className="primary-button"
          >
            {loading ? 'Sending...' : 'Send reset link'}
          </button>
        </form>

        <div className="auth-footer">
          <Link to="/login" className="link-button">
            <FiArrowLeft /> Back to login
          </Link>
        </div>
      </section>
    </main>
  );
}
