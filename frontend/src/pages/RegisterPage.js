// src/pages/RegisterPage.js
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Zap, Eye, EyeOff, UserPlus } from 'lucide-react';
import './AuthPages.css';

const RegisterPage = () => {
  const { register } = useAuth();
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    firstName: '', lastName: '', username: '', email: '', password: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.username.trim()) newErrors.username = 'Username is required';
    else if (formData.username.length < 3) newErrors.username = 'Username must be at least 3 characters';
    if (!formData.email.trim()) newErrors.email = 'Email is required';
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) newErrors.email = 'Please enter a valid email';
    if (!formData.password) newErrors.password = 'Password is required';
    else if (formData.password.length < 6) newErrors.password = 'Password must be at least 6 characters';
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const validationErrors = validate();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    setLoading(true);
    try {
      const success = await register(formData);
      if (success) navigate('/login');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-bg">
        <div className="auth-bg-orb auth-bg-orb-1" />
        <div className="auth-bg-orb auth-bg-orb-2" />
        <div className="auth-bg-grid" />
      </div>

      <div className="auth-container">
        <div className="auth-logo">
          <div className="auth-logo-icon"><Zap size={24} /></div>
          <span>TaskFlow</span>
        </div>

        <div className="auth-card">
          <div className="auth-header">
            <h1>Create account</h1>
            <p>Join TaskFlow and start managing your work</p>
          </div>

          <form onSubmit={handleSubmit} className="auth-form" noValidate>
            {/* Name Row */}
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">First Name</label>
                <input
                  type="text" name="firstName"
                  className="form-input"
                  placeholder="John"
                  value={formData.firstName}
                  onChange={handleChange}
                />
              </div>
              <div className="form-group">
                <label className="form-label">Last Name</label>
                <input
                  type="text" name="lastName"
                  className="form-input"
                  placeholder="Doe"
                  value={formData.lastName}
                  onChange={handleChange}
                />
              </div>
            </div>

            {/* Username */}
            <div className="form-group">
              <label className="form-label">Username *</label>
              <input
                type="text" name="username"
                className={`form-input ${errors.username ? 'input-error' : ''}`}
                placeholder="johndoe"
                value={formData.username}
                onChange={handleChange}
                autoComplete="username"
              />
              {errors.username && <span className="form-error">{errors.username}</span>}
            </div>

            {/* Email */}
            <div className="form-group">
              <label className="form-label">Email *</label>
              <input
                type="email" name="email"
                className={`form-input ${errors.email ? 'input-error' : ''}`}
                placeholder="john@example.com"
                value={formData.email}
                onChange={handleChange}
                autoComplete="email"
              />
              {errors.email && <span className="form-error">{errors.email}</span>}
            </div>

            {/* Password */}
            <div className="form-group">
              <label className="form-label">Password *</label>
              <div className="input-with-icon">
                <input
                  type={showPassword ? 'text' : 'password'} name="password"
                  className={`form-input ${errors.password ? 'input-error' : ''}`}
                  placeholder="Minimum 6 characters"
                  value={formData.password}
                  onChange={handleChange}
                  autoComplete="new-password"
                />
                <button type="button" className="input-icon-btn"
                  onClick={() => setShowPassword(!showPassword)} tabIndex={-1}>
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <span className="form-error">{errors.password}</span>}
              {/* Password strength indicator */}
              {formData.password && (
                <div className="password-strength">
                  <div className={`strength-bar ${
                    formData.password.length >= 12 ? 'strong' :
                    formData.password.length >= 8 ? 'medium' : 'weak'
                  }`} />
                  <span>{
                    formData.password.length >= 12 ? 'Strong' :
                    formData.password.length >= 8 ? 'Medium' : 'Weak'
                  }</span>
                </div>
              )}
            </div>

            <button type="submit" className="btn btn-primary btn-lg w-full" disabled={loading}>
              {loading ? (
                <><div className="btn-spinner" />Creating account...</>
              ) : (
                <><UserPlus size={18} />Create Account</>
              )}
            </button>
          </form>

          <div className="auth-footer">
            <p>Already have an account?</p>
            <Link to="/login" className="auth-link">Sign in instead →</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;
