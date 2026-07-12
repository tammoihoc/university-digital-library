const API_BASE_URL = 'http://localhost:8080/api';

class AuthService {
  async login(credentials) {
    try {
      console.log('🔐 Attempting login with:', credentials.username);
      
      const response = await fetch(`${API_BASE_URL}/auth/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
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
      
      const userInfo = await this.fetchUserInfo(credentials.username, data.token);
      
      if (userInfo) {
        const userRole = this.determineUserRole(credentials.username, userInfo);
        userInfo.userType = userRole;
        userInfo.role = userRole;
        
        this.saveUser(userInfo);
        console.log('✅ User saved with role:', userRole);
      }
      
      return { token: data.token, user: userInfo };
      
    } catch (error) {
      console.error('❌ Login error:', error);
      throw error;
    }
  }

  async register(registerData) {
    try {
      console.log('📝 Attempting registration for:', registerData.username);
      
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify(registerData),
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Đăng ký thất bại');
      }

      const result = await response.text();
      console.log('✅ Registration successful:', result);
      
      return await this.login({
        username: registerData.username,
        password: registerData.password
      });
      
    } catch (error) {
      console.error('❌ Registration error:', error);
      throw error;
    }
  }

  async fetchUserInfo(username, token) {
    try {
      const response = await fetch(`${API_BASE_URL}/users/profile/${username}`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
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
    if (userInfo?.userType === 'LIBRARIAN' || userInfo?.userType === 'ADMIN') {
      return userInfo.userType;
    }
    if (userInfo?.role === 'LIBRARIAN' || userInfo?.role === 'ADMIN') {
      return userInfo.role;
    }
    if (username === 'librarian' || username === 'admin') {
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
    return user.userType || user.role || 'STUDENT';
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
    return role === 'ADMIN';
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('token_timestamp');
    localStorage.removeItem('user');
  }
}

export default new AuthService();
