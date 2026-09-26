// src/pages/librarian/tabs/ViolationManagement.jsx
import React, { useState, useEffect } from 'react';
import { ShieldAlert, Search, RefreshCw, CheckCircle } from 'lucide-react';

const ViolationManagement = () => {
  const [penalties, setPenalties] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadPenalties();
  }, []);

  const loadPenalties = async () => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/fines/penalties', {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        const data = await response.json();
        setPenalties(data);
      }
    } catch (error) {
      console.error('Error loading penalties:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleResolvePenalty = async (penaltyId) => {
    if (!window.confirm('Bạn có chắc muốn giải quyết vi phạm này?')) return;
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/fines/penalties/${penaltyId}/resolve`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        alert('✅ Đã giải quyết vi phạm');
        loadPenalties();
      }
    } catch (error) {
      console.error('Error resolving penalty:', error);
      alert('❌ Giải quyết thất bại');
    }
  };

  const getPenaltyTypeLabel = (type) => {
    const types = {
      LATE_RETURN: '📖 Trả sách trễ',
      DAMAGED: '💔 Hư hỏng sách',
      LOST: '❌ Mất sách',
      MULTIPLE_VIOLATIONS: '⚠️ Nhiều vi phạm',
    };
    return types[type] || type;
  };

  const getLevelLabel = (level) => {
    const levels = {
      WARNING: '⚠️ Cảnh cáo',
      MINOR: '🟡 Nhẹ',
      MODERATE: '🟠 Trung bình',
      SEVERE: '🔴 Nặng',
      PERMANENT: '⛔ Vĩnh viễn',
    };
    return levels[level] || level;
  };

  const filteredPenalties = penalties.filter(
    (p) =>
      p.userId?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      p.reason?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="ld-loading-screen">
        <div className="ld-loading-spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="ld-toolbar">
        <div className="ld-search-box">
          <Search size={18} className="ld-search-icon" />
          <input
            type="text"
            placeholder="Tìm theo tên người dùng hoặc lý do..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <button className="ld-btn-secondary" onClick={loadPenalties}>
          <RefreshCw size={16} /> Làm mới
        </button>
      </div>

      <div className="ld-recent-section">
        <div className="ld-section-header">
          <h3>
            <ShieldAlert size={18} /> Quản lý vi phạm
          </h3>
          <span className="ld-badge-count">{filteredPenalties.length} vi phạm</span>
        </div>
        <div className="ld-table-wrapper">
          <table className="ld-data-table">
            <thead>
              <tr>
                <th>Người dùng</th>
                <th>Loại vi phạm</th>
                <th>Mức độ</th>
                <th>Lý do</th>
                <th>Ngày tạo</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {filteredPenalties.map((p) => (
                <tr key={p.id}>
                  <td>
                    <span className="ld-user-code">{p.userId}</span>
                  </td>
                  <td>{getPenaltyTypeLabel(p.penaltyType)}</td>
                  <td>
                    <span className="ld-status-badge status-overdue">{getLevelLabel(p.level)}</span>
                  </td>
                  <td>{p.reason || 'N/A'}</td>
                  <td>
                    {p.createdAt
                      ? new Date(p.createdAt).toLocaleDateString('vi-VN')
                      : 'N/A'}
                  </td>
                  <td>
                    <span
                      className={`ld-status-badge ${p.isActive ? 'status-active' : 'status-returned'}`}
                    >
                      {p.isActive ? '🟡 Đang xử lý' : '✅ Đã giải quyết'}
                    </span>
                  </td>
                  <td>
                    {p.isActive && (
                      <button className="ld-action-btn edit" onClick={() => handleResolvePenalty(p.id)}>
                        <CheckCircle size={14} /> Giải quyết
                      </button>
                    )}
                  </td>
                </tr>
              ))}
              {filteredPenalties.length === 0 && (
                <tr>
                  <td colSpan="7" className="text-center">
                    Không có vi phạm nào
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default ViolationManagement;
