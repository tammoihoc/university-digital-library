const API_BASE = 'http://localhost:8080/api';

const librarianService = {
  async createBook(bookData) {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/books`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include',
      body: JSON.stringify(bookData)
    });
    
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  },

  async updateBook(bookId, bookData) {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/books/${bookId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include',
      body: JSON.stringify(bookData)
    });
    
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  },

  async deleteBook(bookId) {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/books/${bookId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include'
    });
    
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  },

  async uploadCover(bookId, file) {
    const token = localStorage.getItem('token');
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`${API_BASE}/books/${bookId}/upload-cover`, {
      method: 'POST',
      headers: {
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include',
      body: formData
    });
    
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  },

  async uploadPdf(bookId, file) {
    const token = localStorage.getItem('token');
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch(`${API_BASE}/books/${bookId}/upload-pdf`, {
      method: 'POST',
      headers: {
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include',
      body: formData
    });
    
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  },

  async getDashboardStats() {
    try {
      const token = localStorage.getItem('token');
      
      const [booksRes, borrowsRes, entriesRes, finesRes] = await Promise.all([
        fetch(`${API_BASE}/books`, {
          headers: { ...(token && { 'Authorization': `Bearer ${token}` }) },
          credentials: 'include'
        }),
        fetch(`${API_BASE}/borrows/active`, {
          headers: { ...(token && { 'Authorization': `Bearer ${token}` }) },
          credentials: 'include'
        }),
        fetch(`${API_BASE}/entry-exit/current`, {
          headers: { ...(token && { 'Authorization': `Bearer ${token}` }) },
          credentials: 'include'
        }),
        fetch(`${API_BASE}/fines/stats`, {
          headers: { ...(token && { 'Authorization': `Bearer ${token}` }) },
          credentials: 'include'
        })
      ]);
      
      const booksData = booksRes.ok ? await booksRes.json() : { totalElements: 0 };
      const borrowsData = borrowsRes.ok ? await borrowsRes.json() : [];
      const entriesData = entriesRes.ok ? await entriesRes.json() : [];
      const finesData = finesRes.ok ? await finesRes.json() : { totalUnpaidAmount: 0 };
      
      return {
        totalBooks: booksData.totalElements || booksData.content?.length || 0,
        activeBorrows: borrowsData?.length || 0,
        currentUsers: entriesData?.length || 0,
        totalFines: finesData?.totalUnpaidAmount || 0
      };
    } catch (error) {
      console.error('Error fetching dashboard stats:', error);
      return {
        totalBooks: 0,
        activeBorrows: 0,
        currentUsers: 0,
        totalFines: 0
      };
    }
  }
};

export default librarianService;
