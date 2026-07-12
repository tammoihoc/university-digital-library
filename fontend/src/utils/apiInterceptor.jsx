// src/utils/apiinterceptor.js
import authservice from '../services/authservice.jsx';

export const apirequest = async (url, options = {}) => {
  const token = authservice.gettoken();
  
  const headers = {
    'content-type': 'application/json',
    ...options.headers,
  };

  if (token && authservice.isauthenticated()) {
    headers['authorization'] = `bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(url, config);
    
    if (response.status === 401) {
      // token hết hạn
      authservice.removetoken();
      window.location.href = '/login';
      throw new error('phiên đăng nhập đã hết hạn');
    }

    if (!response.ok) {
      throw new error(`http error! status: ${response.status}`);
    }

    return await response.json();
  } catch (error) {
    console.error('api request error:', error);
    throw error;
  }
};
