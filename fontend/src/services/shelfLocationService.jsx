const API_BASE = 'http://localhost:8080/api/shelf-locations';

const shelfLocationService = {
  async getAllLocations() {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${API_BASE}`, {
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
      console.error('Error fetching locations:', error);
      return [];
    }
  },

  async getBookLocation(bookId) {
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
      
      if (!response.ok) return null;
      return await response.json();
    } catch (error) {
      console.error('Error fetching book location:', error);
      return null;
    }
  },

  async assignLocation(bookId, locationData) {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/assign`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include',
      body: JSON.stringify({
        bookId,
        zone: locationData.zone,
        rackNumber: locationData.rackNumber,
        columnNumber: locationData.columnNumber,
        rowPosition: locationData.rowPosition,
        position: locationData.position
      })
    });
    
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  },

  async updateLocation(locationId, locationData) {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/${locationId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include',
      body: JSON.stringify(locationData)
    });
    
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    return await response.json();
  },

  async removeLocation(locationId) {
    const token = localStorage.getItem('token');
    const response = await fetch(`${API_BASE}/${locationId}`, {
      method: 'DELETE',
      headers: {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` })
      },
      credentials: 'include'
    });
    
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
  }
};

export default shelfLocationService;
