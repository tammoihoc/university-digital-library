// src/pages/librarian/tabs/FinesTab.jsx
import React from 'react';

const FinesTab = ({ fines }) => (
  <div className="ld-recent-section">
    <div className="ld-section-header">
      <h3>💰 Danh sách phạt</h3>
      <span className="ld-badge-count">{fines.length}</span>
    </div>
    <div className="ld-table-wrapper">
      <table className="ld-data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Mã SV</th>
            <th>Số tiền</th>
            <th>Lý do</th>
            <th>Ngày tạo</th>
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          {fines.map((fine) => (
            <tr key={fine.id}>
              <td>
                <span className="ld-user-code">#{fine.id}</span>
              </td>
              <td>{fine.userId}</td>
              <td className="text-danger">{fine.amount?.toLocaleString()}đ</td>
              <td>{fine.reason || 'Vi phạm'}</td>
              <td>
                {fine.createdAt
                  ? new Date(fine.createdAt).toLocaleDateString('vi-VN')
                  : 'N/A'}
              </td>
              <td>
                <span
                  className={`ld-status-badge ${fine.paid ? 'status-returned' : 'status-overdue'}`}
                >
                  {fine.paid ? 'Đã nộp' : 'Chưa nộp'}
                </span>
              </td>
            </tr>
          ))}
          {fines.length === 0 && (
            <tr>
              <td colSpan="6" className="text-center">
                Không có dữ liệu phạt
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  </div>
);

export default FinesTab;
