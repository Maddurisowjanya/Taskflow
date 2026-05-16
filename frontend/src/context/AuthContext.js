// src/context/AuthContext.js
// ==========================================
// Authentication Context
// ==========================================
// React Context provides a way to share state across all components
// without prop drilling (passing props through many layers).
// AuthContext stores the logged-in user's data and provides
// login/logout functions available anywhere in the app.

import React, { createContext, useContext, useState, useEffect } from 'react';
import { authAPI } from '../services/api';
import toast from 'react-hot-toast';

// Create the context object
// Other components will import and use this via useAuth() hook
const AuthContext = createContext(null);

/**
 * AuthProvider - Wraps the entire app to provide auth state globally.
 *
 * Provides:
 * - user: the logged-in user object (null if not logged in)
 * - isAuthenticated: boolean
 * - login(credentials): logs user in
 * - logout(): logs user out
 * - loading: true while checking for existing session
 */
export const AuthProvider = ({ children }) => {
  // User state - null means not logged in
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true); // True while checking localStorage

  /**
   * On component mount (app startup), check if user is already logged in
   * by looking for a stored token and user in localStorage.
   * This maintains the session across page refreshes.
   */
  useEffect(() => {
    const token = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');

    if (token && storedUser) {
      try {
        setUser(JSON.parse(storedUser));
      } catch (e) {
        // Invalid stored data - clear it
        localStorage.removeItem('token');
        localStorage.removeItem('user');
      }
    }

    setLoading(false); // Done checking - show the app
  }, []);

  /**
   * Login function.
   * Called when user submits the login form.
   *
   * @param {Object} credentials - { username, password }
   * @returns {boolean} - true if login successful
   */
  const login = async (credentials) => {
    try {
      const response = await authAPI.login(credentials);
      const { data: apiResponse } = response;

      if (apiResponse.success) {
        const userData = apiResponse.data;

        // Store token and user data in localStorage for session persistence
        localStorage.setItem('token', userData.token);
        localStorage.setItem('user', JSON.stringify(userData));

        // Update React state
        setUser(userData);

        toast.success(`Welcome back, ${userData.firstName || userData.username}! 👋`);
        return true;
      }
    } catch (error) {
      const message = error.response?.data?.message || 'Login failed. Please check your credentials.';
      toast.error(message);
      return false;
    }
  };

  /**
   * Register function.
   * Called when user submits the registration form.
   *
   * @param {Object} userData - { username, email, password, firstName, lastName }
   * @returns {boolean} - true if registration successful
   */
  const register = async (userData) => {
    try {
      const response = await authAPI.register(userData);
      if (response.data.success) {
        toast.success('Account created! Please log in. 🎉');
        return true;
      }
    } catch (error) {
      const errorData = error.response?.data;
      const message = errorData?.message || 'Registration failed. Please try again.';

      // If validation errors, show them
      if (errorData?.data && typeof errorData.data === 'object') {
        Object.values(errorData.data).forEach(err => toast.error(err));
      } else {
        toast.error(message);
      }
      return false;
    }
  };

  /**
   * Logout function.
   * Clears localStorage and resets user state.
   */
  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    toast.success('Logged out successfully');
  };

  /**
   * Check if current user has admin role.
   */
  const isAdmin = () => {
    return user?.roles?.includes('ROLE_ADMIN') || false;
  };

  // Values provided to all components that consume this context
  const value = {
    user,
    isAuthenticated: !!user,  // Convert to boolean
    loading,
    login,
    register,
    logout,
    isAdmin,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/**
 * useAuth - Custom hook to access auth context.
 *
 * Usage in any component:
 * const { user, login, logout, isAuthenticated } = useAuth();
 *
 * Throws an error if used outside of AuthProvider.
 */
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
