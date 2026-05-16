// src/App.js
// ==========================================
// Main App Component with Routing
// ==========================================

import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';

// Page imports
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import TasksPage from './pages/TasksPage';
import ProfilePage from './pages/ProfilePage';

// Layout
import Layout from './components/layout/Layout';

import './styles/global.css';

/**
 * ProtectedRoute - Wraps routes that require authentication.
 * Redirects to /login if user is not logged in.
 */
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  // Show loading spinner while checking auth status
  if (loading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner"></div>
        <p>Loading TaskFlow...</p>
      </div>
    );
  }

  // Redirect to login if not authenticated
  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

/**
 * PublicRoute - Wraps routes that should only be accessible when NOT logged in.
 * Redirects to dashboard if user is already logged in.
 */
const PublicRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();

  if (loading) return null;

  // Redirect to dashboard if already authenticated
  return !isAuthenticated ? children : <Navigate to="/dashboard" replace />;
};

/**
 * AppRoutes - Defines all application routes.
 * Separated so it can access the AuthContext from AuthProvider.
 */
const AppRoutes = () => {
  return (
    <Routes>
      {/* Default route: redirect to dashboard */}
      <Route path="/" element={<Navigate to="/dashboard" replace />} />

      {/* Public routes - only accessible when NOT logged in */}
      <Route
        path="/login"
        element={
          <PublicRoute>
            <LoginPage />
          </PublicRoute>
        }
      />
      <Route
        path="/register"
        element={
          <PublicRoute>
            <RegisterPage />
          </PublicRoute>
        }
      />

      {/* Protected routes - require authentication */}
      {/* Layout wraps all protected pages with the sidebar/navbar */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Layout>
              <DashboardPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/tasks"
        element={
          <ProtectedRoute>
            <Layout>
              <TasksPage />
            </Layout>
          </ProtectedRoute>
        }
      />
      <Route
        path="/profile"
        element={
          <ProtectedRoute>
            <Layout>
              <ProfilePage />
            </Layout>
          </ProtectedRoute>
        }
      />

      {/* 404 - catch all unknown routes */}
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
};

/**
 * App - Root component.
 * Wraps everything with AuthProvider and Router.
 */
function App() {
  return (
    <AuthProvider>
      <Router>
        {/* Toast notification container */}
        <Toaster
          position="top-right"
          toastOptions={{
            duration: 3000,
            style: {
              background: '#1e2028',
              color: '#f0f0f5',
              border: '1px solid #2d3142',
              borderRadius: '12px',
              fontSize: '14px',
            },
            success: {
              iconTheme: { primary: '#4ade80', secondary: '#1e2028' },
            },
            error: {
              iconTheme: { primary: '#f87171', secondary: '#1e2028' },
            },
          }}
        />
        <AppRoutes />
      </Router>
    </AuthProvider>
  );
}

export default App;
