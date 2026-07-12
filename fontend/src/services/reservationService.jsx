const API_BASE = 'http://localhost:8080/api/reservations';

const reservationService = {
  async createReservation(bookId, notes) {
    try {
      const token = localStorage.getItem('token');
      const userStr = localStorage.getItem('user');
      const user = userStr ? JSON.parse(userStr) : {};
      
      const response = await fetch(`${API_BASE}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': user?.username || 'student',
          'X-User-Type': user?.userType || 'STUDENT',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include',
        body: JSON.stringify({ bookId, notes })
      });
      
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error('Error creating reservation:', error);
      throw error;
    }
  },

  async getMyReservations() {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/my`, {
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
      console.error('Error fetching my reservations:', error);
      return [];
    }
  },

  async cancelReservation(reservationId, reason = '') {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/${reservationId}?reason=${encodeURIComponent(reason)}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      return await response.json();
    } catch (error) {
      console.error('Error cancelling reservation:', error);
      throw error;
    }
  },

  async pickupBook(reservationId) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/${reservationId}/pickup`, {
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
      console.error('Error picking up book:', error);
      throw error;
    }
  },

  async getAllReservations() {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/all`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'X-User-Id': 'librarian',
          'X-User-Type': 'LIBRARIAN',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) return [];
      const data = await response.json();
      console.log('All reservations response:', data);
      return data;
    } catch (error) {
      console.error('Error fetching all reservations:', error);
      return [];
    }
  },

  async getReservationsByBook(bookId) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/book/${bookId}`, {
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
      console.error('Error fetching reservations by book:', error);
      return [];
    }
  }
};

export default reservationService;
