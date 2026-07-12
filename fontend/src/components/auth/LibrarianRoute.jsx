import React from 'react';
import { Navigate } from 'react-router-dom';
import authService from '../../services/authService.jsx';

const LibrarianRoute = ({ children }) => {
  const isAuthenticated = authService.isAuthenticated();
  const isLibrarian = authService.isLibrarian();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (!isLibrarian) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default LibrarianRoute;
