// src/utils/apiInterceptor.jsx
import authService from '../services/authService.jsx';
import { generateSignatureHeaders } from './cryptoUtils.jsx';

export const apiRequest = async (url, options = {}) => {
  const token = authService.getToken();
  
  let path = url;
  if (url.startsWith('http://') || url.startsWith('https://')) {
    try {
      const urlObj = new URL(url);
      path = urlObj.pathname + urlObj.search;
    } catch (e) {
      console.warn('URL parsing failed in apiInterceptor', e);
    }
  }

  const method = options.method || 'GET';
  const sigHeaders = await generateSignatureHeaders(method, path);

  const headers = {
    'Content-Type': 'application/json',
    ...sigHeaders,
    ...options.headers,
  };

  if (token && authService.isAuthenticated()) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, config);
    
    if (response.status === 401) {
      // token hết hạn
      authService.logout();
      window.location.href = '/login';
      throw new Error('Phiên đăng nhập đã hết hạn');
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('API request error:', error);
    throw error;
  }
};

