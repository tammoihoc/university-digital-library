const API_BASE = 'http://localhost:8080/api/borrows'; // sửa thành /borrows

const reservationService = {
  // ✅ Tạo đặt trước với pickupDate
  async createReservation(bookId, pickupDate, notes) {
    try {
      const token = localStorage.getItem('token');
      if (!token) throw new Error('No token found');

      const response = await fetch(`${API_BASE}/reservations`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        credentials: 'include',
        body: JSON.stringify({ bookId, pickupDate, notes })
      });

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || 'Đặt lịch thất bại');
      }
      return await response.json();
    } catch (error) {
      console.error('Error creating reservation:', error);
      throw error;
    }
  },
// ✅ Xác nhận đặt trước (PENDING → CONFIRMED)
async approveReservation(reservationId) {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/reservations/${reservationId}/approve`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Error approving reservation:', error);
    throw error;
  }
},
  async getConfirmedReservations() {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/reservations/all`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      if (!response.ok) return [];
      const all = await response.json();
      return all.filter(r => r.status === 'CONFIRMED');
    } catch (error) {
      console.error('Error fetching confirmed reservations:', error);
      return [];
    }
  },
// ✅ Từ chối đặt trước (PENDING → CANCELLED)
async rejectReservation(reservationId, reason = '') {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/reservations/${reservationId}/reject?reason=${encodeURIComponent(reason)}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Error rejecting reservation:', error);
    throw error;
  }
},
  // ✅ Lấy danh sách đặt trước của user hiện tại
  async getMyReservations() {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/reservations/my`, {
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

  // ✅ Hủy đặt trước
  async cancelReservation(reservationId, reason = '') {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/reservations/${reservationId}?reason=${encodeURIComponent(reason)}`, {
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

  // ✅ Xác nhận nhận sách (thủ thư)
async confirmReservation(reservationId) {
  try {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/reservations/${reservationId}/confirm`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      credentials: 'include'
    });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  } catch (error) {
    console.error('Error confirming reservation:', error);
    throw error;
  }
},
  // ✅ Lấy tất cả đặt trước (admin/librarian)
  async getAllReservations() {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/reservations/all`, {
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
      console.error('Error fetching all reservations:', error);
      return [];
    }
  },

  // ✅ Lấy đặt trước theo sách
  async getReservationsByBook(bookId) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}/reservations/book/${bookId}`, {
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
