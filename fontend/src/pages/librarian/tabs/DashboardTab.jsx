// src/pages/librarian/tabs/DashboardTab.jsx
import React from 'react';
import { BookOpen, RefreshCw, AlertCircle, DollarSign } from 'lucide-react';

const DashboardTab = ({ stats, activeBorrows }) => {
  const getStatus = (borrow) => {
    if (borrow.returnedAt) return { text: 'Đã trả', class: 'status-returned' };
    if (new Date(borrow.dueDate) < new Date()) return { text: 'Quá hạn', class: 'status-overdue' };
    return { text: 'Đang mượn', class: 'status-active' };
  };

  return (
    <div className="ld-tab-dashboard">
      <div className="ld-stats-grid">
        <div className="ld-stat-card">
          <div className="ld-stat-icon primary"><BookOpen size={28} /></div>
          <div className="ld-stat-info">
            <h3>{stats.totalBooks}</h3>
            <p>Tổng số sách</p>
          </div>
        </div>
        <div className="ld-stat-card">
          <div className="ld-stat-icon success"><RefreshCw size={28} /></div>
          <div className="ld-stat-info">
            <h3>{stats.activeBorrows}</h3>
            <p>Đang mượn</p>
          </div>
        </div>
        <div className="ld-stat-card">
          <div className="ld-stat-icon warning"><AlertCircle size={28} /></div>
          <div className="ld-stat-info">
            <h3>{stats.overdueCount}</h3>
            <p>Quá hạn</p>
          </div>
        </div>
        <div className="ld-stat-card">
          <div className="ld-stat-icon danger"><DollarSign size={28} /></div>
          <div className="ld-stat-info">
            <h3>{stats.totalFines.toLocaleString()}đ</h3>
            <p>Tổng tiền phạt</p>
          </div>
        </div>
      </div>

      <div className="ld-recent-section">
        <div className="ld-section-header">
          <h3>📖 Sách đang được mượn</h3>
          <span className="ld-badge-count">{activeBorrows.length}</span>
        </div>
        <div className="ld-table-wrapper">
          <table className="ld-data-table">
            <thead>
              <tr>
                <th>Mã mượn</th>
                <th>Mã sinh viên</th>
                <th>Tên sách</th>
                <th>Ngày mượn</th>
                <th>Hạn trả</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {activeBorrows.slice(0, 10).map(borrow => {
                const status = getStatus(borrow);
                return (
                  <tr key={borrow.id}>
                    <td><span className="ld-user-code">#{borrow.id}</span></td>
                    <td>{borrow.userId}</td>
                    <td className="ld-book-title-cell">{borrow.bookTitle || `Sách ID ${borrow.bookId}`}</td>
                    <td>{borrow.borrowedAt ? new Date(borrow.borrowedAt).toLocaleDateString('vi-VN') : 'N/A'}</td>
                    <td className={new Date(borrow.dueDate) < new Date() ? 'text-danger' : ''}>
                      {borrow.dueDate ? new Date(borrow.dueDate).toLocaleDateString('vi-VN') : 'N/A'}
                    </td>
                    <td><span className={`ld-status-badge ${status.class}`}>{status.text}</span></td>
                  </tr>
                );
              })}
              {activeBorrows.length === 0 && (
                <tr><td colSpan="6" className="text-center">Không có dữ liệu</td></tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default DashboardTab;
