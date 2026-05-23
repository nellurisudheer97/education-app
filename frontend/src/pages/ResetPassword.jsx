import React, { useCallback, useState, useEffect } from 'react';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { FiArrowRight, FiBookOpen, FiCheck, FiX, FiEye, FiEyeOff } from 'react-icons/fi';
import api from '../services/api';
import { validatePassword } from '../utils/validation';
import '../styles/auth.css';

export default function ResetPassword() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');
  
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [tokenValid, setTokenValid] = useState(null);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const verifyToken = useCallback(async () => {
    try {
      const response = await api.verifyResetToken({ token });
      if (response?.message === 'Token is valid') {
        setTokenValid(true);
        setError('');
      } else {
        setTokenValid(false);
        setError(response?.message || 'Reset token is invalid or expired');
      }
    } catch (err) {
      setTokenValid(false);
      setError(err.message || 'Failed to verify reset token');
    }
  }, [token]);

  useEffect(() => {
    if (!token) {
      setError('Invalid or missing reset token. Please request a new password reset.');
      setTokenValid(false);
      return;
    }

    verifyToken();
  }, [token, verifyToken]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validatePassword(password)) {
      setError('Password must be at least 8 characters.');
      return;
    }

    if (password !== confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    setLoading(true);
    setError('');

    try {
      const response = await api.resetPassword({
        token,
        newPassword: password,
        confirmPassword
      });

      if (response?.message?.includes('successful')) {
        setSuccess('Password reset successful! Redirecting to login...');
        setPassword('');
        setConfirmPassword('');
        
        setTimeout(() => {
          navigate('/login');
        }, 3000);
      } else {
        setError(response?.message || 'Failed to reset password');
      }
    } catch (err) {
      setError(err.message || 'Password reset failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (tokenValid === null) {
    return (
      <main className="auth-shell">
        <section className="auth-card" style={{ margin: 'auto' }}>
          <div style={{ textAlign: 'center', padding: '40px 20px' }}>
            <p>Verifying reset link...</p>
          </div>
        </section>
      </main>
    );
  }

  if (tokenValid === false) {
    return (
      <main className="auth-shell">
        <section className="auth-card" style={{ margin: 'auto' }}>
          <div className="auth-card-header">
            <h2>Reset Link Invalid</h2>
          </div>
          {error && <div className="error-message">{error}</div>}
          <Link to="/forgot-password" className="primary-button" style={{ display: 'block', textAlign: 'center', marginTop: '20px' }}>
            Request New Reset Link
          </Link>
        </section>
      </main>
    );
  }

  return (
    <main className="auth-shell">
      <section className="auth-panel">
        <div className="auth-brand">
          <span className="brand-mark"><FiBookOpen /></span>
          <span>EduForge</span>
        </div>
        <h1>Create new password</h1>
        <p className="auth-subtitle">Enter a new password to secure your account.</p>

        <div className="auth-highlights">
          <span><FiCheck /> Secure</span>
          <span><FiArrowRight /> Fast</span>
        </div>
      </section>

      <section className="auth-card" aria-label="Reset password form">
        <div className="auth-card-header">
          <p className="eyebrow">Set new password</p>
          <h2>Choose a strong password</h2>
        </div>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <form onSubmit={handleSubmit}>
          <label>
            New password
            <div className="password-input-wrapper">
              <input
                type={showPassword ? 'text' : 'password'}
                placeholder="Create a strong password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                minLength="8"
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

          <label>
            Confirm password
            <div className="password-input-wrapper">
              <input
                type={showConfirmPassword ? 'text' : 'password'}
                placeholder="Re-enter your password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                minLength="8"
                required
              />
              <button
                type="button"
                className="toggle-password-btn"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                aria-label={showConfirmPassword ? 'Hide password' : 'Show password'}
                title={showConfirmPassword ? 'Hide password' : 'Show password'}
              >
                {showConfirmPassword ? <FiEyeOff /> : <FiEye />}
              </button>
            </div>
          </label>

          <button type="submit" disabled={loading} className="primary-button">
            {loading ? 'Resetting password...' : 'Reset password'}
          </button>
        </form>

        <div className="auth-footer">
          <Link to="/login" className="link-button">
            <FiX /> Cancel
          </Link>
        </div>
      </section>
    </main>
  );
}
