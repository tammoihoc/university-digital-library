const API_BASE = 'http://localhost:8080/api/borrows';

const borrowService = {
  async getAllActiveBorrows() {
    try {
      const token = localStorage.getItem('token');
      console.log('Fetching active borrows from:', `${API_BASE}/active`);
      
      const response = await fetch(`${API_BASE}/active`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      
      const data = await response.json();
      console.log('Active borrows response:', data);
      return data;
    } catch (error) {
      console.error('Error fetching active borrows:', error.message);
      return [
        {
          id: 1,
          userId: "SV001",
          bookTitle: "Clean Code",
          borrowedAt: new Date().toISOString(),
          dueDate: new Date(Date.now() + 7*24*60*60*1000).toISOString(),
          status: "ACTIVE"
        },
        {
          id: 2,
          userId: "SV002", 
          bookTitle: "Introduction to Algorithms",
          borrowedAt: new Date().toISOString(),
          dueDate: new Date(Date.now() - 2*24*60*60*1000).toISOString(),
          status: "OVERDUE"
        }
      ];
    }
  },

  async getOverdueBorrows() {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/overdue`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) return [];
      return await response.json();
    } catch (error) {
      console.error('Error fetching overdue borrows:', error.message);
      return [];
    }
  },

  async returnBook(borrowId) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/${borrowId}/return`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error('Error returning book:', error.message);
      throw error;
    }
  },
async getAllBorrows() {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/all`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include'
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Error fetching all borrows:', error);
    return [];
  }
},
  async getUserBorrows(userId) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/user/${userId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) return [];
      return await response.json();
    } catch (error) {
      console.error('Error fetching user borrows:', error.message);
      return [];
    }
  },

  async borrowBook(data) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include',
        body: JSON.stringify(data)
      });
      
      if (!response.ok) {
        // Lấy message lỗi chi tiết từ server
        let errorMessage = `HTTP ${response.status}`;
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorData.error || errorMessage;
        } catch (e) {
          // Nếu response không phải JSON, lấy text
          errorMessage = await response.text() || errorMessage;
        }
        throw new Error(errorMessage);
      }
      
      return await response.json();
    } catch (error) {
      console.error('❌ Error borrowing book:', error.message);
      throw error;
    }
  }
};

export default borrowService;
