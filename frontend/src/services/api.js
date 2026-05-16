// src/services/api.js
// ==========================================
// Axios HTTP Client Configuration
// ==========================================
// This file sets up Axios with base URL, headers, and JWT interceptors.
// All API calls in the app go through this configured instance.

import axios from 'axios';

// Base URL for all API calls
// In development, React's package.json proxy forwards /api/* to localhost:8080
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';

/**
 * Create a configured Axios instance.
 * All components import this instead of using `axios` directly.
 */
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

// ==========================================
// Request Interceptor
// ==========================================
// Automatically adds the JWT token to every outgoing request.
// This means we don't have to add "Authorization: Bearer xxx" in every API call.
api.interceptors.request.use(
  (config) => {
    // Get JWT token from localStorage
    const token = localStorage.getItem('token');

    // If token exists, add it to the Authorization header
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// ==========================================
// Response Interceptor
// ==========================================
// Handles global error scenarios like token expiration.
api.interceptors.response.use(
  // Pass through successful responses unchanged
  (response) => response,

  // Handle error responses
  (error) => {
    if (error.response) {
      // 401 Unauthorized - token expired or invalid
      if (error.response.status === 401) {
        // Clear stored credentials and redirect to login
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }

      // 403 Forbidden - user doesn't have permission
      if (error.response.status === 403) {
        console.error('Access denied:', error.response.data?.message);
      }
    }

    return Promise.reject(error);
  }
);

// ==========================================
// Auth API Functions
// ==========================================

export const authAPI = {
  /**
   * Register a new user account.
   * @param {Object} userData - { username, email, password, firstName, lastName }
   */
  register: (userData) => api.post('/api/auth/register', userData),

  /**
   * Login and get JWT token.
   * @param {Object} credentials - { username, password }
   */
  login: (credentials) => api.post('/api/auth/login', credentials),
};

// ==========================================
// Tasks API Functions
// ==========================================

export const tasksAPI = {
  /** Get all tasks with optional filters */
  getAll: (params) => api.get('/api/tasks', { params }),

  /** Get dashboard statistics */
  getDashboard: () => api.get('/api/tasks/dashboard'),

  /** Get a single task by ID */
  getById: (id) => api.get(`/api/tasks/${id}`),

  /** Create a new task */
  create: (taskData) => api.post('/api/tasks', taskData),

  /** Update an existing task */
  update: (id, taskData) => api.put(`/api/tasks/${id}`, taskData),

  /** Mark a task as completed */
  complete: (id) => api.patch(`/api/tasks/${id}/complete`),

  /** Delete a task */
  delete: (id) => api.delete(`/api/tasks/${id}`),

  /** Search tasks by keyword */
  search: (keyword) => api.get('/api/tasks', { params: { search: keyword } }),
};

// ==========================================
// Users API Functions
// ==========================================

export const usersAPI = {
  /** Get current user's profile */
  getProfile: () => api.get('/api/users/profile'),

  /** Update profile information */
  updateProfile: (profileData) => api.put('/api/users/profile', profileData),

  /** Change password */
  changePassword: (passwordData) => api.patch('/api/users/change-password', passwordData),

  /** Admin: Get all users */
  getAllUsers: () => api.get('/api/users'),
};

export default api;
