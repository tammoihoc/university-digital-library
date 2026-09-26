// src/pages/librarian/tabs/EntryExitTab.jsx
import React, { useState, useEffect } from 'react';
import { RefreshCw } from 'lucide-react';
import entryExitService from '../../../services/entryExitService';

const EntryExitTab = ({ currentEntries, entryUserId, setEntryUserId, selectedBranch, setSelectedBranch, onRecordEntry, onRefresh, entryLoading, refreshing }) => {
  const [history, setHistory] = useState([]);

  useEffect(() => {
    loadHistory();
  }, []);

  const loadHistory = async () => {
    try {
      const data = await entryExitService.getMyHistory();
      setHistory(data || []);
    } catch (e) {
      console.warn('Cannot load history');
    }
  };

  return (
    <div>
      {/* Form check-in */}
      <div className="ld-form-card">
        <h3>📥 Ghi nhận vào thư viện</h3>
        <div className="ld-form-row">
          <div className="ld-form-group">
            <label>Mã sinh viên / Tên đăng nhập</label>
            <input
              type="text"
              placeholder="Nhập username hoặc studentId..."
              value={entryUserId}
              onChange={(e) => setEntryUserId(e.target.value)}
            />
          </div>
          <div className="ld-form-group">
            <label>Chi nhánh</label>
            <select value={selectedBranch} onChange={(e) => setSelectedBranch(e.target.value)}>
              <option value="B">🏢 Cơ sở B</option>
              <option value="E">🏫 Cơ sở E</option>
            </select>
          </div>
        </div>
        <button className="ld-btn-primary" onClick={onRecordEntry} disabled={entryLoading}>
          {entryLoading ? 'Đang xử lý...' : 'Ghi nhận vào'}
        </button>
      </div>

      {/* Danh sách check-in hôm nay */}
      <div className="ld-recent-section" style={{ marginTop: '20px' }}>
        <div className="ld-section-header">
          <h3>📋 Người đang trong thư viện</h3>
          <button className="ld-btn-secondary" onClick={onRefresh} disabled={refreshing}>
            <RefreshCw size={18} className={refreshing ? 'rotate' : ''} /> Làm mới
          </button>
        </div>
        <div className="ld-table-wrapper">
          <table className="ld-data-table">
            <thead>
              <tr>
                <th>MSSV</th>
                <th>Họ tên</th>
                <th>Giờ vào</th>
                <th>Chi nhánh</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {currentEntries.length === 0 ? (
                <tr><td colSpan="5" className="text-center">Chưa có ai trong thư viện</td></tr>
              ) : (
                currentEntries.map(entry => (
                  <tr key={entry.id}>
                    <td>{entry.studentId || entry.userId}</td>
                    <td>{entry.fullName || entry.userId}</td>
                    <td>{new Date(entry.entryTime).toLocaleTimeString('vi-VN')}</td>
                    <td>{entry.branch === 'B' ? '🏢 Cơ sở B' : '🏫 Cơ sở E'}</td>
                    <td><span className="ld-status-badge status-active">✅ Đang ở trong</span></td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default EntryExitTab;
