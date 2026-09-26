import React, { useState, useEffect } from 'react';
import { CalendarCheck, CheckCircle, XCircle, BookOpen, RefreshCw, Search } from 'lucide-react';
import reservationService from '../../../services/reservationService';
import bookService from '../../../services/bookService';
import userService from '../../../services/userService';

// Component con: hiển thị avatar + tên + MSSV
const UserCell = ({ userId }) => {
  const [avatarUrl, setAvatarUrl] = useState(null);
  const [userInfo, setUserInfo] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUser = async () => {
      if (!userId) return;
      try {
        setLoading(true);
        const profile = await userService.getUserProfile(userId);
        setUserInfo(profile);
        const avatar = await userService.getAvatarUrl(userId);
        setAvatarUrl(avatar);
      } catch (err) {
        console.warn('Không thể tải thông tin user:', userId, err);
      } finally {
        setLoading(false);
      }
    };
    fetchUser();
  }, [userId]);

  if (loading) {
    return <span className="user-loading">Đang tải...</span>;
  }

  const displayName = userInfo?.fullName || userId || 'Người dùng';
  const studentId = userInfo?.studentId || '';

  return (
    <div className="user-cell">
      <div className="user-avatar-wrapper">
        {avatarUrl ? (
          <img src={avatarUrl} alt={displayName} className="user-avatar-small" />
        ) : (
          <div className="user-avatar-placeholder">{displayName.charAt(0).toUpperCase()}</div>
        )}
      </div>
      <div className="user-info-text">
        <div className="user-fullname">{displayName}</div>
        {studentId && <div className="user-studentid">MSSV: {studentId}</div>}
      </div>
    </div>
  );
};

const ReservationsTab = ({ reservations = [], onRefresh }) => {
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(null);
  const [filterStatus, setFilterStatus] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [bookCache, setBookCache] = useState({});

  // Load thông tin sách vào cache
  useEffect(() => {
    const fetchBooks = async () => {
      const newCache = {};
      for (const res of reservations) {
        if (res.bookId && !bookCache[res.bookId]) {
          try {
            const book = await bookService.getBookById(res.bookId);
            newCache[res.bookId] = book;
          } catch (e) {
            console.warn('Không tải được sách', res.bookId);
          }
        }
      }
      if (Object.keys(newCache).length > 0) {
        setBookCache(prev => ({ ...prev, ...newCache }));
      }
    };
    if (reservations.length > 0) fetchBooks();
  }, [reservations]);

  const handleApprove = async (id) => {
    if (!window.confirm('Xác nhận đặt trước này?')) return;
    setActionLoading(id);
    try {
      await reservationService.approveReservation(id);
      alert('✅ Đã xác nhận đặt trước!');
      if (onRefresh) onRefresh();
    } catch (error) {
      alert('❌ Lỗi: ' + error.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async (id) => {
    const reason = window.prompt('Lý do từ chối (tùy chọn):');
    if (reason === null) return;
    setActionLoading(id);
    try {
      await reservationService.rejectReservation(id, reason);
      alert('✅ Đã từ chối đặt trước.');
      if (onRefresh) onRefresh();
    } catch (error) {
      alert('❌ Lỗi: ' + error.message);
    } finally {
      setActionLoading(null);
    }
  };

  const getBookInfo = (res) => {
    const book = bookCache[res.bookId];
    if (book) {
      return {
        title: book.title || `Sách #${res.bookId}`,
        cover: book.coverImageUrl || book.mainCoverImageUrl || null,
      };
    }
    return {
      title: res.bookTitle || `Sách #${res.bookId}`,
      cover: null,
    };
  };

  const filtered = reservations.filter(res => {
    const statusMatch = filterStatus === 'all' || res.status === filterStatus;
    const book = getBookInfo(res);
    const searchMatch =
      res.id.toString().includes(searchTerm) ||
      res.userId?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      book.title?.toLowerCase().includes(searchTerm.toLowerCase());
    return statusMatch && searchMatch;
  });

  const getStatusBadge = (status) => {
    const map = {
      CONFIRMED: { label: 'Chờ nhận', cls: 'badge-warning' },
      COMPLETED: { label: 'Đã nhận', cls: 'badge-success' },
      CANCELLED: { label: 'Đã hủy', cls: 'badge-danger' },
      EXPIRED: { label: 'Hết hạn', cls: 'badge-secondary' },
      PENDING: { label: 'Đang xử lý', cls: 'badge-info' },
    };
    const s = map[status] || { label: status, cls: 'badge-secondary' };
    return <span className={`badge ${s.cls}`}>{s.label}</span>;
  };

  const formatShortDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('vi-VN');
  };

  return (
    <div className="reservations-tab">
      <div className="ld-toolbar">
        <div className="ld-toolbar-left">
          <h2>📋 Quản lý đặt lịch</h2>
          <span className="badge badge-primary">{reservations.length} đặt lịch</span>
        </div>
        <div className="ld-toolbar-right">
          <div className="ld-search-box">
            <Search size={18} className="ld-search-icon" />
            <input
              type="text"
              placeholder="Tìm theo ID, người dùng, tên sách..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            className="ld-select"
          >
            <option value="all">Tất cả trạng thái</option>
            <option value="PENDING">Đang xử lý</option>
            <option value="CONFIRMED">Chờ nhận</option>
            <option value="COMPLETED">Đã nhận</option>
            <option value="CANCELLED">Đã hủy</option>
            <option value="EXPIRED">Hết hạn</option>
          </select>
          <button className="ld-btn-secondary" onClick={onRefresh} disabled={loading}>
            <RefreshCw size={18} className={loading ? 'rotate' : ''} />
            Làm mới
          </button>
        </div>
      </div>

      {filtered.length === 0 ? (
        <div className="ld-empty-state">
          <CalendarCheck size={48} />
          <p>Không có đặt lịch nào</p>
        </div>
      ) : (
        <div className="ld-table-responsive">
          <table className="ld-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Người dùng</th>
                <th>Sách</th>
                <th>Ngày đặt</th>
                <th>Ngày nhận dự kiến</th>
                <th>Hết hạn</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((res) => {
                const book = getBookInfo(res);
                const coverUrl = book.cover ? bookService.processImageUrl(book.cover) : null;
                return (
                  <tr key={res.id}>
                    <td>{res.id}</td>
                    <td><UserCell userId={res.userId} /></td>
                    <td>
                      <div className="book-info">
                        {coverUrl ? (
                          <img src={coverUrl} alt={book.title} className="book-thumbnail" />
                        ) : (
                          <BookOpen size={16} className="book-icon" />
                        )}
                        <span>{book.title}</span>
                      </div>
                    </td>
                    <td>{formatShortDate(res.reservationDate)}</td>
                    <td>{formatShortDate(res.pickupDate)}</td>
                    <td>{formatShortDate(res.expiryDate)}</td>
                    <td>{getStatusBadge(res.status)}</td>
                    <td>
                      <div className="action-buttons">
                        {res.status === 'PENDING' && (
                          <>
                            <button
                              className="ld-action-btn approve"
                              onClick={() => handleApprove(res.id)}
                              disabled={actionLoading === res.id}
                            >
                              <CheckCircle size={16} /> Xác nhận
                            </button>
                            <button
                              className="ld-action-btn reject"
                              onClick={() => handleReject(res.id)}
                              disabled={actionLoading === res.id}
                            >
                              <XCircle size={16} /> Từ chối
                            </button>
                          </>
                        )}
                        {res.status === 'CONFIRMED' && (
                          <span className="text-warning">⏳ Chờ nhận</span>
                        )}
                        {res.status === 'COMPLETED' && <span className="text-success">✅ Đã nhận</span>}
                        {(res.status === 'CANCELLED' || res.status === 'EXPIRED') && (
                          <span className="text-muted">{res.status}</span>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
          <div className="ld-table-footer">
            Hiển thị {filtered.length} trên {reservations.length} đặt lịch
          </div>
        </div>
      )}
    </div>
  );
};

export default ReservationsTab;
