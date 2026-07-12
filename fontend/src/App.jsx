// src/App.jsx
import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LibrarianRoute from './components/auth/LibrarianRoute';
import Login from './pages/auth/Login';
import Dashboard from './pages/dashboard/Dashboard';
import BookDetail from './pages/book/BookDetail';
import Profile from './pages/profile/Profile';
import LibrarianDashboard from './pages/librarian/LibrarianDashboard';
import './index.css';

// Error Boundary Component
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-boundary">
          <div className="error-content">
            <h2>Đã có lỗi xảy ra</h2>
            <p>{this.state.error?.message || 'Vui lòng thử lại sau'}</p>
            <button onClick={() => window.location.reload()}>Tải lại trang</button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}

function App() {
  return (
    <ErrorBoundary>
      <Router>
        <Toaster position="top-right" toastOptions={{ duration: 3000 }} />
        <div className="App">
          <Routes>
            <Route path="/login" element={<Login />} />
            
            {/* Student Routes */}
            <Route 
              path="/dashboard" 
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/book/:id" 
              element={
                <ProtectedRoute>
                  <BookDetail />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/profile" 
              element={
                <ProtectedRoute>
                  <Profile />
                </ProtectedRoute>
              } 
            />
            
            {/* Librarian Routes */}
            <Route 
              path="/librarian" 
              element={
                <LibrarianRoute>
                  <LibrarianDashboard />
                </LibrarianRoute>
              } 
            />
            <Route 
              path="/librarian/*" 
              element={
                <LibrarianRoute>
                  <LibrarianDashboard />
                </LibrarianRoute>
              } 
            />
            
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </Router>
    </ErrorBoundary>
  );
}

export default App;
