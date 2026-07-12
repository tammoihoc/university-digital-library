const API_BASE = 'http://localhost:8080/api/fines';

const fineService = {
  async getAllFines() {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/`, {
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
      console.error('Error fetching fines:', error);
      return [];
    }
  },

  async getUserFines(userId) {
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
      console.error('Error fetching user fines:', error);
      return [];
    }
  },

  async createFine(fineData) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include',
        body: JSON.stringify(fineData)
      });
      
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error('Error creating fine:', error);
      throw error;
    }
  },

  async payFine(fineId) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/${fineId}/pay`, {
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
      console.error('Error paying fine:', error);
      throw error;
    }
  },

  async getFineStats() {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/stats`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) {
        return { totalUnpaidAmount: 0, totalFines: 0 };
      }
      
      return await response.json();
    } catch (error) {
      console.error('Error fetching fine stats:', error);
      return { totalUnpaidAmount: 0, totalFines: 0 };
    }
  }
};

export default fineService;
