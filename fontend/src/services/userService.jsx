// src/services/userService.jsx
const API_BASE_URL = 'http://localhost:8080/api';

class UserService {
  
  // Lấy avatar - CẦN TOKEN
// src/services/userService.jsx
// Thêm method này nếu chưa có

// src/services/userService.jsx
// Thêm method này (đã có nhưng cần đảm bảo)

async getAvatarUrl(username) {
  const token = localStorage.getItem('token');
  if (!token) {
    console.warn('No token found, cannot get avatar');
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`;
  }
  
  try {
    const response = await fetch(`${API_BASE_URL}/users/${username}/avatar`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
      }
    });
    
    if (response.ok) {
      const blob = await response.blob();
      return URL.createObjectURL(blob);
    } else if (response.status === 404) {
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`;
    } else {
      throw new Error(`HTTP ${response.status}`);
    }
  } catch (error) {
    console.error('Error getting avatar:', error);
    return `https://api.dicebear.com/7.x/avataaars/svg?seed=${username}`;
  }
}
  // Upload avatar - CẦN TOKEN
  async uploadAvatar(username, file) {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('No authentication token');
      }
      
      const formData = new FormData();
      formData.append('file', file);
      
      console.log(`📤 Uploading avatar for: ${username}`);
      
      const response = await fetch(`${API_BASE_URL}/users/${username}/avatar/upload`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
        },
        body: formData
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Upload failed: ${response.status} - ${errorText}`);
      }
      
      const result = await response.json();
      console.log('✅ Avatar uploaded:', result);
      
      // Clear cache
      localStorage.removeItem(`avatar_${username}`);
      localStorage.removeItem(`avatar_${username}_ts`);
      
      // Trả về URL mới
      return result.avatarUrl;
      
    } catch (error) {
      console.error('❌ Upload error:', error);
      throw error;
    }
  }

  // Xóa avatar - CẦN TOKEN
  async deleteAvatar(username) {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('No authentication token');
      }
      
      console.log(`🗑️ Deleting avatar for: ${username}`);
      
      const response = await fetch(`${API_BASE_URL}/users/${username}/avatar`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        }
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Delete failed: ${response.status} - ${errorText}`);
      }
      
      // Clear cache
      localStorage.removeItem(`avatar_${username}`);
      localStorage.removeItem(`avatar_${username}_ts`);
      
      console.log('✅ Avatar deleted');
      return true;
      
    } catch (error) {
      console.error('❌ Delete error:', error);
      throw error;
    }
  }

  // Lấy thông tin user profile - CẦN TOKEN
  async getUserProfile(username) {
    try {
      const token = localStorage.getItem('token');
      
      const headers = {
        'Content-Type': 'application/json',
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      console.log(`🔍 Fetching user profile: ${username}`);
      
      const response = await fetch(`${API_BASE_URL}/users/profile/${username}`, {
        headers
      });
      
      if (!response.ok) {
        console.warn(`User not found: ${response.status}`);
        return this.getDefaultUserProfile(username);
      }
      
      const userData = await response.json();
      console.log('✅ User profile loaded:', userData);
      
      return userData;
      
    } catch (error) {
      console.error('❌ Error fetching profile:', error);
      return this.getDefaultUserProfile(username);
    }
  }

  // Cập nhật user profile - CẦN TOKEN
  async updateUserProfile(username, userData) {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('No authentication token');
      }
      
      console.log(`✏️ Updating profile for: ${username}`, userData);
      
      const dataToSend = {
        firstName: this.extractFirstName(userData.fullName),
        lastName: this.extractLastName(userData.fullName),
        email: userData.email,
        phone: userData.phone,
        address: userData.address,
        dateOfBirth: userData.dateOfBirth,
        faculty: userData.faculty,
        major: userData.major
      };
      
      const response = await fetch(`${API_BASE_URL}/users/profile/${username}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify(dataToSend)
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`Update failed: ${response.status} - ${errorText}`);
      }
      
      const updatedUser = await response.json();
      console.log('✅ Profile updated:', updatedUser);
      
      // Clear avatar cache
      localStorage.removeItem(`avatar_${username}`);
      localStorage.removeItem(`avatar_${username}_ts`);
      
      return updatedUser;
      
    } catch (error) {
      console.error('❌ Update error:', error);
      throw error;
    }
  }

  // Lấy thông tin mượn sách - CẦN TOKEN
  async getBorrowStats(username) {
    try {
      const token = localStorage.getItem('token');
      
      const headers = {
        'Content-Type': 'application/json',
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      console.log(`📚 Fetching borrow stats for: ${username}`);
      
      const response = await fetch(`${API_BASE_URL}/users/${username}/borrow-info`, {
        headers
      });
      
      if (!response.ok) {
        console.warn('Using default borrow stats');
        return this.getDefaultBorrowStats();
      }
      
      return await response.json();
      
    } catch (error) {
      console.error('❌ Error fetching borrow stats:', error);
      return this.getDefaultBorrowStats();
    }
  }

  // Cập nhật số lượng sách đang mượn - CẦN TOKEN
  async updateBorrowCount(username, newCount) {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('No authentication token');
      }
      
      console.log(`📝 Updating borrow count for: ${username} to ${newCount}`);
      
      const response = await fetch(`${API_BASE_URL}/users/${username}/borrow-count?newCount=${newCount}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        }
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('❌ Update borrow count error:', errorText);
        return false;
      }
      
      console.log('✅ Borrow count updated successfully');
      return true;
      
    } catch (error) {
      console.error('❌ Error updating borrow count:', error);
      return false;
    }
  }

  // Helper: Lấy avatar URL cho component (async)
  async getAvatarUrlAsync(user) {
    if (!user || !user.username) {
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=guest`;
    }
    
    try {
      const url = await this.getAvatarUrl(user.username);
      return url;
    } catch (error) {
      return `https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`;
    }
  }

  // Helper: Lấy display name
  getDisplayName(userData) {
    if (!userData) return 'Người dùng';
    if (userData.fullName && userData.fullName.trim() !== '') return userData.fullName;
    if (userData.firstName && userData.lastName) return `${userData.lastName} ${userData.firstName}`;
    if (userData.firstName) return userData.firstName;
    if (userData.lastName) return userData.lastName;
    return userData.username || 'Người dùng';
  }

  extractFirstName(fullName) {
    if (!fullName) return '';
    const parts = fullName.trim().split(' ');
    return parts[parts.length - 1] || '';
  }

  extractLastName(fullName) {
    if (!fullName) return '';
    const parts = fullName.trim().split(' ');
    if (parts.length <= 1) return '';
    return parts.slice(0, -1).join(' ');
  }

  formatDateOfBirth(dateString) {
    if (!dateString) return 'Chưa cập nhật';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return 'Ngày không hợp lệ';
      return date.toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      });
    } catch {
      return 'Ngày không hợp lệ';
    }
  }

  formatDateForInput(dateString) {
    if (!dateString) return '';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return '';
      return date.toISOString().split('T')[0];
    } catch {
      return '';
    }
  }

  getDefaultUserProfile(username) {
    const today = new Date();
    const defaultBirthDate = new Date(today.getFullYear() - 18, today.getMonth(), today.getDate());
    
    return {
      username: username,
      firstName: '',
      lastName: '',
      fullName: username,
      email: `${username}@hutech.edu.vn`,
      userType: 'STUDENT',
      gender: 'OTHER',
      phone: 'Chưa cập nhật',
      address: 'Chưa cập nhật',
      dateOfBirth: defaultBirthDate.toISOString().split('T')[0],
      studentId: username,
      faculty: 'Công nghệ thông tin',
      major: 'Kỹ thuật phần mềm',
      academicYear: today.getFullYear(),
      currentBorrowed: 0,
      avatarUrl: null,
      createdAt: today.toISOString()
    };
  }

  getDefaultBorrowStats() {
    return {
      maxBorrowLimit: 5,
      currentBorrowed: 0,
      canBorrowMore: true,
      overdueBooks: 0
    };
  }
}

export default new UserService();
