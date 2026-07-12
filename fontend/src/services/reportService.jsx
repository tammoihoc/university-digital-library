import axios from 'axios';

const API_BASE = 'http://localhost:8080/api/reports';

const reportService = {
  // Báo cáo mượn sách theo tháng
  getMonthlyBorrowStats: async (year) => {
    const response = await axios.get(`${API_BASE}/borrow/monthly`, { params: { year } });
    return response.data;
  },

  // Top sách được mượn nhiều nhất
  getTopBorrowedBooks: async (limit = 10) => {
    const response = await axios.get(`${API_BASE}/books/top-borrowed`, { params: { limit } });
    return response.data;
  },

  // Top người dùng mượn nhiều nhất
  getTopBorrowers: async (limit = 10) => {
    const response = await axios.get(`${API_BASE}/users/top-borrowers`, { params: { limit } });
    return response.data;
  },

  // Báo cáo doanh thu phạt
  getFineRevenue: async (startDate, endDate) => {
    const params = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    const response = await axios.get(`${API_BASE}/fines/revenue`, { params });
    return response.data;
  },

  // Thống kê tổng hợp
  getFullReport: async () => {
    const response = await axios.get(`${API_BASE}/full`);
    return response.data;
  }
};

export default reportService;
