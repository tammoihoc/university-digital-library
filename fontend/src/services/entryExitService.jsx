// src/services/entryExitService.js
const API_BASE = 'http://localhost:8080/api/entry-exit';

const entryExitService = {
  /**
   * Ghi nhận vào thư viện
   * @param {string} userId - Mã sinh viên/người dùng
   * @param {string} branch - 'B' (Cơ sở B) hoặc 'E' (Cơ sở E)
   */
  recordEntry: async (userId, branch = 'B') => {
    try {
      const token = localStorage.getItem('token');
      
      if (!token) {
        throw new Error('Vui lòng đăng nhập lại');
      }
      
      const response = await fetch(`${API_BASE}/entry`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'X-Branch': branch
        },
        credentials: 'include'
      });
      
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || 'Ghi nhận thất bại');
      }
      
      const data = await response.json();
      console.log('✅ Entry recorded:', data);
      return data;
      
    } catch (error) {
      console.error('Error recording entry:', error.message);
      throw error;
    }
  },

  /**
   * Lấy danh sách người đang trong thư viện (chỉ ADMIN/LIBRARIAN)
   */
  getCurrentEntries: async () => {
    try {
      const token = localStorage.getItem('token');
      
      if (!token) {
        console.warn('No token found');
        return [];
      }
      
      console.log('Fetching current entries from:', `${API_BASE}/current`);
      
      const response = await fetch(`${API_BASE}/current`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });
      
      console.log('Response status:', response.status);
      
      if (!response.ok) {
        if (response.status === 403) {
          console.warn('User is not authorized to view current entries');
          return [];
        }
        throw new Error(`HTTP ${response.status}`);
      }
      
      const data = await response.json();
      console.log('✅ Current entries:', data);
      return data || [];
      
    } catch (error) {
      console.error('Error fetching current entries:', error.message);
      return [];
    }
  },

  /**
   * Lấy lịch sử ra vào (chỉ ADMIN/LIBRARIAN)
   * @param {string} startDate - Ngày bắt đầu (YYYY-MM-DD)
   * @param {string} endDate - Ngày kết thúc (YYYY-MM-DD)
   */
  getEntryHistory: async (startDate, endDate) => {
    try {
      const token = localStorage.getItem('token');
      
      if (!token) {
        return [];
      }
      
      const params = new URLSearchParams();
      if (startDate) params.append('startDate', startDate);
      if (endDate) params.append('endDate', endDate);
      
      const response = await fetch(`${API_BASE}/history?${params.toString()}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });
      
      if (!response.ok) {
        if (response.status === 403) return [];
        throw new Error(`HTTP ${response.status}`);
      }
      
      return await response.json();
      
    } catch (error) {
      console.error('Error fetching entry history:', error.message);
      return [];
    }
  },

  /**
   * Lấy lịch sử ra vào của user hiện tại
   */
  getMyHistory: async () => {
    try {
      const token = localStorage.getItem('token');
      
      if (!token) {
        return [];
      }
      
      const response = await fetch(`${API_BASE}/my-history`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });
      
      if (!response.ok) {
        if (response.status === 403) return [];
        throw new Error(`HTTP ${response.status}`);
      }
      
      return await response.json();
      
    } catch (error) {
      console.error('Error fetching my history:', error.message);
      return [];
    }
  },

  /**
   * Lấy lịch sử ra vào của user cụ thể (chỉ ADMIN/LIBRARIAN)
   * @param {string} userId - Mã người dùng
   */
  getUserHistory: async (userId) => {
    try {
      const token = localStorage.getItem('token');
      
      if (!token) {
        return [];
      }
      
      const response = await fetch(`${API_BASE}/user/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });
      
      if (!response.ok) {
        if (response.status === 403) return [];
        throw new Error(`HTTP ${response.status}`);
      }
      
      return await response.json();
      
    } catch (error) {
      console.error('Error fetching user history:', error.message);
      return [];
    }
  },

  /**
   * Lấy thống kê trong ngày (chỉ ADMIN/LIBRARIAN)
   */
  getDailyStats: async () => {
    try {
      const token = localStorage.getItem('token');
      
      if (!token) {
        return { todayEntries: 0 };
      }
      
      const response = await fetch(`${API_BASE}/stats/daily`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });
      
      if (!response.ok) {
        if (response.status === 403) return { todayEntries: 0 };
        throw new Error(`HTTP ${response.status}`);
      }
      
      return await response.json();
      
    } catch (error) {
      console.error('Error fetching daily stats:', error.message);
      return { todayEntries: 0 };
    }
  },

  /**
   * Lấy số lượng người đang trong thư viện (chỉ ADMIN/LIBRARIAN)
   */
  getCurrentCount: async () => {
    try {
      const token = localStorage.getItem('token');
      
      if (!token) {
        return 0;
      }
      
      const response = await fetch(`${API_BASE}/count`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include'
      });
      
      if (!response.ok) {
        if (response.status === 403) return 0;
        throw new Error(`HTTP ${response.status}`);
      }
      
      const data = await response.json();
      return data.count || 0;
      
    } catch (error) {
      console.error('Error fetching current count:', error.message);
      return 0;
    }
  },

  /**
   * Lấy giờ mở cửa của thư viện
   * @param {string} branch - 'B' hoặc 'E'
   */
  getOpeningHours: async (branch = 'B') => {
    try {
      const token = localStorage.getItem('token');
      
      const response = await fetch(`${API_BASE}/hours/${branch}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      
      return await response.json();
      
    } catch (error) {
      console.error('Error fetching opening hours:', error.message);
      return {
        branch: branch,
        openingHours: 'Không có thông tin',
        isOpenNow: false
      };
    }
  },

  /**
   * Kiểm tra xem user có thể vào thư viện không (dựa trên giờ mở cửa)
   * @param {string} branch - 'B' hoặc 'E'
   */
  canEnterLibrary: async (branch = 'B') => {
    try {
      const hours = await entryExitService.getOpeningHours(branch);
      return hours.isOpenNow === true;
    } catch (error) {
      console.error('Error checking library hours:', error);
      return false;
    }
  },

  /**
   * Lấy danh sách chi nhánh thư viện
   */
  getBranches: () => {
    return [
      { 
        id: 'B', 
        name: '🏢 Cơ sở B - 475A Điện Biên Phủ', 
        address: '475A Điện Biên Phủ, P.25, Q.Bình Thạnh, TP.HCM',
        hours: 'Thứ 2: 9h-19h | Thứ 3-6: 8h-19h | Thứ 7: 8h-11h30 | CN: Đóng cửa'
      },
      { 
        id: 'E', 
        name: '🏫 Cơ sở E - Khu Công nghệ cao Quận 9', 
        address: 'Tòa nhà E3, Khu Công nghệ cao, Quận 9, TP.HCM',
        hours: 'Thứ 2-6: 8h-16h15 | Thứ 7: 8h-11h15 | CN: Đóng cửa'
      }
    ];
  }
};

export default entryExitService;
