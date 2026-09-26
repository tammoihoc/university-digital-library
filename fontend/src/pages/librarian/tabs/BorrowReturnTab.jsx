import React, { useState, useEffect } from 'react';
import {
  Search, User, BookOpen, Clock, CheckCircle,
  RefreshCw, Plus, AlertCircle, Loader, X, CalendarCheck
} from 'lucide-react';
import bookService from '../../../services/bookService';
import borrowService from '../../../services/borrowService';
import userService from '../../../services/userService';
import reservationService from '../../../services/reservationService';
import './BorrowReturnTab.css';

const BorrowReturnTab = ({ onRefresh }) => {
  const [loading, setLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState(null);

  // Tất cả borrows (hiển thị hết)
  const [allBorrows, setAllBorrows] = useState([]);
  const [borrowBookCache, setBorrowBookCache] = useState({});
  const [borrowUserCache, setBorrowUserCache] = useState({});
  const [loadingBorrows, setLoadingBorrows] = useState(false);

  // Confirmed reservations (chờ xác nhận mượn)
  const [confirmedReservations, setConfirmedReservations] = useState([]);
  const [loadingReservations, setLoadingReservations] = useState(false);

  // Modal "Thêm mượn"
  const [showAddModal, setShowAddModal] = useState(false);
  const [searchInput, setSearchInput] = useState('');
  const [foundUser, setFoundUser] = useState(null);
  const [loadingUser, setLoadingUser] = useState(false);
  const [bookSearch, setBookSearch] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const [searchingBook, setSearchingBook] = useState(false);
  const [borrowing, setBorrowing] = useState(false);

  // ---------- Load data ----------
  const loadData = async () => {
    setLoading(true);
    await Promise.all([loadAllBorrows(), loadConfirmedReservations()]);
    setLoading(false);
  };

  const loadAllBorrows = async () => {
    setLoadingBorrows(true);
    try {
      const borrows = await borrowService.getAllBorrows();
      setAllBorrows(borrows || []);
      for (const borrow of borrows) {
        if (borrow.bookId && !borrowBookCache[borrow.bookId]) {
          try {
            const book = await bookService.getBookById(borrow.bookId);
            setBorrowBookCache(prev => ({ ...prev, [borrow.bookId]: book }));
          } catch (e) { /* ignore */ }
        }
        if (borrow.userId && !borrowUserCache[borrow.userId]) {
          try {
            const profile = await userService.getUserProfile(borrow.userId);
            setBorrowUserCache(prev => ({ ...prev, [borrow.userId]: profile }));
          } catch (e) { /* ignore */ }
        }
      }
    } catch (error) {
      console.error('Error loading borrows:', error);
    } finally {
      setLoadingBorrows(false);
    }
  };

  const loadConfirmedReservations = async () => {
    setLoadingReservations(true);
    try {
      const data = await reservationService.getConfirmedReservations();
      setConfirmedReservations(data);
    } catch (error) {
      console.error('Error loading confirmed reservations:', error);
    } finally {
      setLoadingReservations(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // ---------- Handlers ----------
  const handleReturnBook = async (borrowId) => {
    if (!window.confirm('Xác nhận trả sách này?')) return;
    setActionLoading(borrowId);
    try {
      await borrowService.returnBook(borrowId);
      alert('✅ Trả sách thành công!');
      await loadData();
      if (onRefresh) onRefresh();
    } catch (error) {
      alert('❌ Lỗi trả sách: ' + error.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleConfirmBorrow = async (reservationId) => {
    if (!window.confirm('Xác nhận mượn sách cho người dùng này?')) return;
    setActionLoading(reservationId);
    try {
      await reservationService.confirmReservation(reservationId);
      alert('✅ Xác nhận mượn thành công!');
      await loadData();
      if (onRefresh) onRefresh();
    } catch (error) {
      alert('❌ Lỗi: ' + error.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleOpenAddModal = () => {
    setShowAddModal(true);
    setSearchInput('');
    setFoundUser(null);
    setBookSearch('');
    setSearchResults([]);
  };

  const handleCloseAddModal = () => {
    setShowAddModal(false);
    setFoundUser(null);
    setSearchInput('');
    setBookSearch('');
    setSearchResults([]);
  };

  const handleSearchUser = async () => {
    const input = searchInput.trim();
    if (!input) {
      alert('Vui lòng nhập username hoặc MSSV');
      return;
    }
    setLoadingUser(true);
    try {
      let profile = null;
      try {
        const response = await fetch(`http://localhost:8080/api/users/student/${encodeURIComponent(input)}`, {
          headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
        });
        if (response.ok) profile = await response.json();
      } catch (e) { /* ignore */ }

      if (!profile) {
        profile = await userService.getUserProfile(input);
      }

      if (!profile || !profile.username) {
        alert('Không tìm thấy người dùng');
        setFoundUser(null);
        return;
      }

      setFoundUser(profile);
      const borrows = await borrowService.getUserBorrows(profile.username);
      const active = borrows.filter(b => !b.returnedAt);
      if (active.length >= (profile.maxBorrowLimit || 5)) {
        alert('Người dùng đã đạt giới hạn mượn tối đa!');
      }
    } catch (error) {
      alert('Lỗi tìm user: ' + error.message);
    } finally {
      setLoadingUser(false);
    }
  };

  const handleSearchBook = async () => {
    if (!bookSearch.trim()) return;
    setSearchingBook(true);
    try {
      const result = await bookService.searchBooks(bookSearch.trim(), 0, 20);
      setSearchResults(result.content || []);
    } catch (error) {
      alert('Lỗi tìm sách: ' + error.message);
    } finally {
      setSearchingBook(false);
    }
  };

  const handleBorrowBook = async (bookId) => {
    if (!foundUser) {
      alert('Chưa tìm thấy người dùng');
      return;
    }
    setBorrowing(true);
    try {
      await borrowService.borrowBook({
        userId: foundUser.username,
        bookId: bookId
      });
      alert('✅ Mượn sách thành công!');
      handleCloseAddModal();
      await loadData();
      if (onRefresh) onRefresh();
    } catch (error) {
      alert('❌ Mượn sách thất bại:\n' + error.message);
    } finally {
      setBorrowing(false);
    }
  };

  // ---------- Format helpers ----------
  const formatDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('vi-VN') + ' ' +
           new Date(dateStr).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
  };

  const formatShortDate = (dateStr) => {
    if (!dateStr) return '—';
    return new Date(dateStr).toLocaleDateString('vi-VN');
  };

  const getStatusBadge = (borrow) => {
    if (borrow.returnedAt) return { text: 'Đã trả', cls: 'badge-success' };
    if (new Date(borrow.dueDate) < new Date()) return { text: 'Quá hạn', cls: 'badge-danger' };
    return { text: 'Đang mượn', cls: 'badge-primary' };
  };

  const getUserDisplay = (userId) => {
    const profile = borrowUserCache[userId];
    if (profile) {
      return {
        fullName: profile.fullName || profile.username,
        studentId: profile.studentId || ''
      };
    }
    return { fullName: userId, studentId: '' };
  };

  const getBookTitle = (bookId) => {
    const book = borrowBookCache[bookId];
    return book ? book.title : `Sách #${bookId}`;
  };

  return (
    <div className="borrow-return-tab">
      {/* ====== Bảng: Đặt lịch chờ xác nhận mượn ====== */}
      <div className="br-section">
        <div className="br-section-header">
          <h3><CalendarCheck size={18} /> Đặt lịch chờ xác nhận mượn</h3>
          <button className="br-btn-secondary" onClick={loadConfirmedReservations} disabled={loadingReservations}>
            <RefreshCw size={16} className={loadingReservations ? 'spin' : ''} /> Làm mới
          </button>
        </div>
        {loadingReservations ? (
          <div className="br-loading">Đang tải...</div>
        ) : confirmedReservations.length === 0 ? (
          <div className="br-empty-state">
            <CheckCircle size={32} />
            <p>Không có đặt lịch nào đang chờ xác nhận mượn</p>
          </div>
        ) : (
          <div className="br-table-wrapper">
            <table className="br-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Người đặt</th>
                  <th>Sách</th>
                  <th>Ngày đặt</th>
                  <th>Hạn nhận</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {confirmedReservations.map((res) => (
                  <tr key={res.id}>
                    <td>#{res.id}</td>
                    <td>{res.userId}</td>
                    <td>{res.bookTitle || `Sách #${res.bookId}`}</td>
                    <td>{formatShortDate(res.reservationDate)}</td>
                    <td>{formatShortDate(res.expiryDate)}</td>
                    <td>
                      <button
                        className="br-btn-confirm"
                        onClick={() => handleConfirmBorrow(res.id)}
                        disabled={actionLoading === res.id}
                      >
                        {actionLoading === res.id ? <Loader size={14} className="spin" /> : <CheckCircle size={14} />}
                        Xác nhận mượn
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ====== Bảng: Danh sách mượn/trả ====== */}
      <div className="br-section">
        <div className="br-section-header">
          <h3><BookOpen size={18} /> Danh sách mượn/trả</h3>
          <div className="br-header-actions">
            <span className="br-count">{allBorrows.length} lượt</span>
            <button className="br-btn-success" onClick={handleOpenAddModal}>
              <Plus size={16} /> Thêm mượn
            </button>
            <button className="br-btn-secondary" onClick={loadData} disabled={loadingBorrows}>
              <RefreshCw size={16} className={loadingBorrows ? 'spin' : ''} /> Làm mới
            </button>
          </div>
        </div>

        {loadingBorrows ? (
          <div className="br-loading">Đang tải danh sách...</div>
        ) : allBorrows.length === 0 ? (
          <div className="br-empty-state">
            <BookOpen size={32} />
            <p>Không có lượt mượn nào</p>
          </div>
        ) : (
          <div className="br-table-wrapper">
            <table className="br-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Sách</th>
                  <th>Người mượn</th>
                  <th>Ngày mượn</th>
                  <th>Hạn trả</th>
                  <th>Trạng thái</th>
                  <th>Hành động</th>
                </tr>
              </thead>
              <tbody>
                {allBorrows.map((borrow) => {
                  const status = getStatusBadge(borrow);
                  const userInfo = getUserDisplay(borrow.userId);
                  const bookTitle = getBookTitle(borrow.bookId);
                  return (
                    <tr key={borrow.id}>
                      <td>#{borrow.id}</td>
                      <td className="br-book-title">{bookTitle}</td>
                      <td>
                        <div className="br-user-cell">
                          <span className="br-user-name">{userInfo.fullName}</span>
                          {userInfo.studentId && (
                            <span className="br-user-id">(MSSV: {userInfo.studentId})</span>
                          )}
                        </div>
                      </td>
                      <td>{formatDate(borrow.borrowedAt)}</td>
                      <td className={status.cls === 'badge-danger' ? 'text-danger' : ''}>
                        {formatDate(borrow.dueDate)}
                      </td>
                      <td><span className={`badge ${status.cls}`}>{status.text}</span></td>
                      <td>
                        {!borrow.returnedAt ? (
                          <button
                            className="br-btn-return"
                            onClick={() => handleReturnBook(borrow.id)}
                            disabled={actionLoading === borrow.id}
                          >
                            {actionLoading === borrow.id ? <Loader size={14} className="spin" /> : <CheckCircle size={14} />}
                            Trả sách
                          </button>
                        ) : (
                          <span className="text-muted">Đã trả</span>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ====== Modal thêm mượn ====== */}
      {showAddModal && (
        <div className="br-modal-overlay" onClick={handleCloseAddModal}>
          <div className="br-modal" onClick={(e) => e.stopPropagation()}>
            <div className="br-modal-header">
              <h3>📚 Thêm lượt mượn mới</h3>
              <button className="br-modal-close" onClick={handleCloseAddModal}>×</button>
            </div>
            <div className="br-modal-body">
              {/* Bước 1: Tìm người dùng */}
              <div className="br-search-block">
                <label className="br-label">Người mượn</label>
                <div className="br-search-box">
                  <input
                    type="text"
                    placeholder="Nhập username hoặc MSSV..."
                    value={searchInput}
                    onChange={(e) => setSearchInput(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleSearchUser()}
                  />
                  <button className="br-btn-primary" onClick={handleSearchUser} disabled={loadingUser}>
                    {loadingUser ? <Loader size={18} className="spin" /> : <User size={18} />}
                    Tìm
                  </button>
                </div>
                {foundUser && (
                  <div className="br-user-info found">
                    <div className="br-user-avatar">
                      <img
                        src={foundUser.avatarUrl || `https://api.dicebear.com/7.x/avataaars/svg?seed=${foundUser.username}`}
                        alt={foundUser.fullName}
                        onError={(e) => e.target.style.display = 'none'}
                      />
                    </div>
                    <div className="br-user-details">
                      <h4>{foundUser.fullName || foundUser.username}</h4>
                      <div className="br-user-meta">
                        <span><User size={14} /> {foundUser.username}</span>
                        {foundUser.studentId && <span><BookOpen size={14} /> MSSV: {foundUser.studentId}</span>}
                      </div>
                    </div>
                    <button className="br-btn-close" onClick={() => setFoundUser(null)}>
                      <X size={18} />
                    </button>
                  </div>
                )}
              </div>

              {/* Bước 2: Chọn sách */}
              {foundUser && (
                <div className="br-search-block">
                  <label className="br-label">Sách mượn</label>
                  <div className="br-search-book">
                    <input
                      type="text"
                      placeholder="Nhập tên sách, tác giả, ISBN..."
                      value={bookSearch}
                      onChange={(e) => setBookSearch(e.target.value)}
                      onKeyPress={(e) => e.key === 'Enter' && handleSearchBook()}
                    />
                    <button className="br-btn-primary" onClick={handleSearchBook} disabled={searchingBook}>
                      {searchingBook ? <Loader size={18} className="spin" /> : <Search size={18} />}
                      Tìm sách
                    </button>
                  </div>
                  {searchResults.length > 0 && (
                    <div className="br-book-results">
                      {searchResults.map(book => (
                        <div key={book.id} className="br-book-item">
                          <img
                            src={bookService.processImageUrl(book.coverImageUrl)}
                            alt={book.title}
                            className="br-book-cover"
                            onError={(e) => e.target.src = 'https://via.placeholder.com/40x50?text=No+Image'}
                          />
                          <div className="br-book-info">
                            <div className="br-book-title">{book.title}</div>
                            <div className="br-book-meta">
                              <span>{book.author}</span>
                              <span>Còn {book.availablePhysicalCopies || 0} bản</span>
                            </div>
                          </div>
                          <button
                            className="br-btn-borrow"
                            onClick={() => handleBorrowBook(book.id)}
                            disabled={borrowing || (book.availablePhysicalCopies || 0) <= 0}
                          >
                            {borrowing ? <Loader size={14} className="spin" /> : 'Mượn'}
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                  {searchResults.length === 0 && bookSearch && !searchingBook && (
                    <div className="br-empty-state small">
                      <AlertCircle size={24} />
                      <p>Không tìm thấy sách</p>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default BorrowReturnTab;
