// src/services/authService.jsx
import { generateSignatureHeaders } from '../utils/cryptoUtils.jsx';

const API_BASE_URL = 'http://localhost:8080/api';

class AuthService {
  async login(credentials) {
    try {
      console.log('🔐 Attempting login with:', credentials.username);
      
      const sigHeaders = await generateSignatureHeaders('POST', '/api/auth/login');
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...sigHeaders
        },
        credentials: 'include',
        body: JSON.stringify(credentials),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Đăng nhập thất bại');
      }

      const data = await response.json();
      
      if (data.token) {
        this.saveToken(data.token);
      }
      
      // ✅ Parse role từ token ngay lập tức
      const rolesFromToken = this.getRolesFromToken(data.token);
      const userRole = this.determineUserRole(credentials.username, { roles: rolesFromToken });
      
      // Tạo user object với role đã xác định
      const userInfo = {
        username: credentials.username,
        userType: userRole,
        role: userRole,
        name: credentials.username,
      };
      
      // Cố gắng lấy thông tin chi tiết từ user-service (không bắt buộc)
      try {
        const profile = await this.fetchUserInfo(credentials.username, data.token);
        if (profile) {
          Object.assign(userInfo, profile);
          if (profile.userType) {
            userInfo.userType = profile.userType;
            userInfo.role = profile.userType;
          }
        }
      } catch (e) {
        console.warn('Could not fetch user profile, using token data');
      }
      
      this.saveUser(userInfo);
      console.log('✅ User saved with role:', userInfo.role);
      
      return { token: data.token, user: userInfo };
      
    } catch (error) {
      console.error('❌ Login error:', error);
      throw error;
    }
  }

  // ✅ Lấy roles từ JWT token
  getRolesFromToken(token) {
    try {
      if (!token) return [];
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      const payload = JSON.parse(jsonPayload);
      console.log('🔍 Token payload:', payload);
      return payload.roles || payload.role || payload.authorities || [];
    } catch (e) {
      console.error('Failed to parse JWT token:', e);
      return [];
    }
  }

  async fetchUserInfo(username, token) {
    try {
      const path = `/api/users/profile/${username}`;
      const sigHeaders = await generateSignatureHeaders('GET', path);
      const response = await fetch(`${API_BASE_URL}/users/profile/${username}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
          ...sigHeaders
        },
        credentials: 'include'
      });
      
      if (response.ok) {
        return await response.json();
      }
    } catch (error) {
      console.error('Error fetching user info:', error);
    }
    return null;
  }

  determineUserRole(username, userInfo) {
    // Ưu tiên từ userInfo.roles (token)
    if (userInfo?.roles && Array.isArray(userInfo.roles) && userInfo.roles.length > 0) {
      // ✅ Strip ROLE_ prefix
      const cleanedRoles = userInfo.roles.map(r => r.startsWith('ROLE_') ? r.substring(5) : r);
      const role = cleanedRoles.find(r => r === 'ADMIN' || r === 'LIBRARIAN' || r === 'STUDENT' || r === 'LECTURER');
      if (role) return role;
    }
    if (userInfo?.userType) {
      return userInfo.userType;
    }
    if (userInfo?.role) {
      return userInfo.role;
    }
    if (username === 'admin' || username === 'librarian') {
      return 'LIBRARIAN';
    }
    return 'STUDENT';
  }

  saveToken(token) {
    localStorage.setItem('token', token);
    localStorage.setItem('token_timestamp', Date.now().toString());
  }

  saveUser(user) {
    const userToSave = {
      username: user.username,
      name: user.fullName || user.name || user.username,
      fullName: user.fullName || user.name,
      email: user.email,
      userType: user.userType || user.role || 'STUDENT',
      role: user.role || user.userType || 'STUDENT',
      studentId: user.studentId,
      faculty: user.faculty,
      major: user.major,
      avatar: user.avatar,
      currentBorrowed: user.currentBorrowed || 0,
      maxBorrowLimit: user.maxBorrowLimit || 5,
      phone: user.phone,
      address: user.address,
      dateOfBirth: user.dateOfBirth
    };
    localStorage.setItem('user', JSON.stringify(userToSave));
  }

  getUser() {
    const userStr = localStorage.getItem('user');
    if (!userStr) return null;
    try {
      return JSON.parse(userStr);
    } catch {
      return null;
    }
  }

  getToken() {
    return localStorage.getItem('token');
  }

  isTokenExpired() {
    const token = this.getToken();
    const timestamp = localStorage.getItem('token_timestamp');
    if (!token || !timestamp) return true;
    const tokenAge = Date.now() - parseInt(timestamp);
    return tokenAge > 28800000;
  }

  isAuthenticated() {
    return !this.isTokenExpired();
  }

  getUserRole() {
    const user = this.getUser();
    if (!user) return null;
    let role = user.role || user.userType || 'STUDENT';
    // Loại bỏ tiền tố ROLE_ nếu có
    if (role && role.startsWith('ROLE_')) {
      role = role.substring(5);
    }
    console.log('🔍 getUserRole() =', role);
    return role;
  }

  isLibrarian() {
    const role = this.getUserRole();
    return role === 'LIBRARIAN' || role === 'ADMIN';
  }

  isStudent() {
    const role = this.getUserRole();
    return role === 'STUDENT';
  }

  isAdmin() {
    const role = this.getUserRole();
    const result = role === 'ADMIN';
    console.log('🔍 isAdmin() =', result, 'role:', role);
    return result;
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('token_timestamp');
    localStorage.removeItem('user');
  }
}

export default new AuthService();
