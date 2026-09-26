// src/pages/librarian/LibrarianDashboard.jsx
import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../../services/authService';
import bookService from '../../services/bookService';
import borrowService from '../../services/borrowService';
import entryExitService from '../../services/entryExitService';
import fineService from '../../services/fineService';
import reservationService from '../../services/reservationService';
import {
  LayoutDashboard, BookOpen, RefreshCw, CalendarCheck, Users, DollarSign, MapPin,
  LogOut, Menu, X, Bell, Search, Plus, Edit, Trash2, Eye,
  UserCog, ShieldAlert, Camera
} from 'lucide-react';
import './LibrarianDashboard.css';
import logoImage from '../../assets/logo.png';

// Import các tab
import DashboardTab from './tabs/DashboardTab';
import BooksTab from './tabs/BooksTab';
import BorrowReturnTab from './tabs/BorrowReturnTab';
import ReservationsTab from './tabs/ReservationsTab';
import EntryExitTab from './tabs/EntryExitTab';
import FinesTab from './tabs/FinesTab';
import LocationsTab from './tabs/LocationsTab';
import AccountManagement from './tabs/AccountManagement';
import ViolationManagement from './tabs/ViolationManagement';

// Import các modal
import BookModal from './components/BookModal';
import LocationModal from './components/LocationModal';

const LibrarianDashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [activeTab, setActiveTab] = useState('dashboard');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [loading, setLoading] = useState(true);
  const [isAdmin, setIsAdmin] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [showNotifications, setShowNotifications] = useState(false);

  // Avatar
  const [avatarPreview, setAvatarPreview] = useState(null);
  const [uploadingAvatar, setUploadingAvatar] = useState(false);
  const avatarInputRef = useRef(null);

  // Data
  const [books, setBooks] = useState([]);
  const [activeBorrows, setActiveBorrows] = useState([]);
  const [currentEntries, setCurrentEntries] = useState([]);
  const [fines, setFines] = useState([]);
  const [allReservations, setAllReservations] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [stats, setStats] = useState({
    totalBooks: 0, activeBorrows: 0, overdueCount: 0,
    currentUsers: 0, totalFines: 0, totalReservations: 0
  });

  // Book Modal
  const [showBookModal, setShowBookModal] = useState(false);
  const [editingBook, setEditingBook] = useState(null);
  const [savingBook, setSavingBook] = useState(false);
  const [coverFile, setCoverFile] = useState(null);
  const [pdfFile, setPdfFile] = useState(null);
  const [coverPreview, setCoverPreview] = useState(null);
  const [bookFormData, setBookFormData] = useState({
    title: '', author: '', isbn: '', description: '',
    type: 'NON_FICTION', accessType: 'HYBRID',
    publisher: '', publicationYear: new Date().getFullYear(),
    pages: 0, totalPhysicalCopies: 0,
    department: '', courseCode: '', categories: '', price: 0,
    language: 'vi', libraryBranchId: 1 // ✅ THÊM VERSION
  });

  // Location Modal (tạm thời không dùng)
  const [showLocationModal, setShowLocationModal] = useState(false);
  const [selectedBookForLocation, setSelectedBookForLocation] = useState(null);
  const [locationData, setLocationData] = useState({
    branchId: '1', zone: '', shelf: '', column: '', row: '', position: ''
  });

  // Entry/Exit
  const [entryUserId, setEntryUserId] = useState('');
  const [selectedBranch, setSelectedBranch] = useState('B');
  const [entryLoading, setEntryLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);

  const menuItems = useMemo(() => {
    const base = [
      { id: 'dashboard', icon: LayoutDashboard, label: 'Tổng quan' },
      { id: 'books', icon: BookOpen, label: 'Quản lý sách' },
      { id: 'borrow-return', icon: RefreshCw, label: 'Mượn/Trả sách' },
      { id: 'reservations', icon: CalendarCheck, label: 'Đặt lịch' },
      { id: 'entry-exit', icon: Users, label: 'Quản lý ra vào' },
      { id: 'fines', icon: DollarSign, label: 'Quản lý phạt' },
      { id: 'locations', icon: MapPin, label: 'Vị trí sách' },
    ];
    if (isAdmin) {
      base.push(
        { id: 'accounts', icon: UserCog, label: 'Quản lý tài khoản' },
        { id: 'violations', icon: ShieldAlert, label: 'Quản lý vi phạm' }
      );
    }
    return base;
  }, [isAdmin]);

  const displayName = user?.name?.trim() ? user.name : (isAdmin ? 'ADMIN' : 'LIBRARIAN');

  // ========== LOAD DATA ==========
  const loadAvatar = async (username) => {
    if (!username) return;
    try {
      const token = localStorage.getItem('token');
      if (!token) return;
      const response = await fetch(`http://localhost:8080/api/users/${username}/avatar`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        const blob = await response.blob();
        setAvatarPreview(URL.createObjectURL(blob));
      }
    } catch (e) {
      console.warn('Cannot load avatar', e);
    }
  };

const loadAllData = async () => {
  setLoading(true);
  try {
    const [booksData, borrowsData, entriesData, finesData, reservationsData] = await Promise.all([
      bookService.getAllBooks(0, 100).catch(() => ({ content: [] })),
      borrowService.getAllActiveBorrows().catch(() => []),
      entryExitService.getCurrentEntries().catch(() => []),
      fineService.getAllFines().catch(() => []),
      reservationService.getAllReservations().catch(() => []) // ✅ lấy tất cả
    ]);

    setBooks(booksData.content || []);
    setActiveBorrows(borrowsData || []);
    setCurrentEntries(entriesData || []);
    setFines(finesData || []);
    setAllReservations(reservationsData || []);
    // ... tính stats
  } catch (error) {
    console.error('Error loading data:', error);
  } finally {
    setLoading(false);
  }
};

  useEffect(() => {
    const storedUser = authService.getUser();
    if (!storedUser || !authService.isLibrarian()) {
      navigate('/login');
      return;
    }
    setUser(storedUser);
    setIsAdmin(authService.isAdmin());
    loadAvatar(storedUser?.username);
    loadAllData();
  }, []);

  // ========== AVATAR UPLOAD ==========
  const handleAvatarClick = () => avatarInputRef.current?.click();

  const handleAvatarChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onloadend = () => setAvatarPreview(reader.result);
    reader.readAsDataURL(file);

    setUploadingAvatar(true);
    try {
      const token = localStorage.getItem('token');
      const fd = new FormData();
      fd.append('file', file);
      const username = user?.username;
      if (!username) return;
      const response = await fetch(`http://localhost:8080/api/users/${username}/avatar/upload`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
        body: fd
      });
      if (response.ok) {
        const result = await response.json();
        const avatarUrl = result.avatarUrl || result.url || result;
        const fullUrl = avatarUrl.startsWith('http') ? avatarUrl : `http://localhost:8080${avatarUrl}`;
        setAvatarPreview(fullUrl);
        const storedUser = authService.getUser();
        const newUser = { ...storedUser, avatarUrl: avatarUrl };
        localStorage.setItem('user', JSON.stringify(newUser));
        setUser(newUser);
        loadAvatar(username);
      } else {
        const err = await response.text();
        alert('Upload avatar thất bại: ' + err);
      }
    } catch (error) {
      console.error('Error uploading avatar:', error);
      alert('Lỗi khi upload avatar');
    } finally {
      setUploadingAvatar(false);
      e.target.value = '';
    }
  };

  // ========== HANDLERS ==========
  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const toggleNotifications = () => {
    setShowNotifications(!showNotifications);
  };

  // ========== BOOK CRUD ==========
  const handleAddBook = () => {
    setEditingBook(null);
    setBookFormData({
      title: '', author: '', isbn: '', description: '',
      type: 'NON_FICTION', accessType: 'HYBRID',
      publisher: '', publicationYear: new Date().getFullYear(),
      pages: 0, totalPhysicalCopies: 0,
      department: '', courseCode: '', categories: '', price: 0,
      language: 'vi', libraryBranchId: 1
    });
    setCoverFile(null);
    setPdfFile(null);
    setCoverPreview(null);
    setShowBookModal(true);
  };

  const handleEditBook = (book) => {
    setEditingBook(book);
    setBookFormData({
      title: book.title || '',
      author: book.author || '',
      isbn: book.isbn || '',
      description: book.description || '',
      type: book.type || 'NON_FICTION',
      accessType: book.accessType || 'HYBRID',
      publisher: book.publisher || '',
      publicationYear: book.publicationYear || new Date().getFullYear(),
      pages: book.pages || 0,
      totalPhysicalCopies: book.totalPhysicalCopies || 0,
      department: book.department || '',
      courseCode: book.courseCode || '',
      categories: book.categories?.join(', ') || '',
      price: book.price || 0,
      language: book.language || 'vi',
      libraryBranchId: book.libraryBranchId || 1   // ✅ LẤY VERSION
    });
    // Lấy URL ảnh
    const coverUrl = book.mainCoverImageUrl || book.coverImageUrl || null;
    setCoverPreview(coverUrl ? `http://localhost:8080${coverUrl}` : null);
    setCoverFile(null);
    setPdfFile(null);
    setShowBookModal(true);
  };

  const handleDeleteBook = async (book) => {
    if (!window.confirm(`Bạn có chắc muốn xóa sách "${book.title}"?`)) return;
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/books/${book.id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (response.ok) {
        alert(`✅ Đã xóa sách "${book.title}" thành công!`);
        loadAllData();
      } else {
        throw new Error('Delete failed');
      }
    } catch (error) {
      console.error('Error deleting book:', error);
      alert('❌ Xóa sách thất bại');
    }
  };

  const handleCoverChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setCoverFile(file);
      const reader = new FileReader();
      reader.onloadend = () => setCoverPreview(reader.result);
      reader.readAsDataURL(file);
    }
  };

  const handlePdfChange = (e) => {
    const file = e.target.files[0];
    if (file) setPdfFile(file);
  };

  // ✅ HÀM handleSaveBook – CÓ GỬI VERSION
  const handleSaveBook = async () => {
    if (!bookFormData.title || !bookFormData.author) {
      alert('Vui lòng nhập tên sách và tác giả');
      return;
    }
    setSavingBook(true);
    try {
      const token = localStorage.getItem('token');

      const bookData = {
        title: bookFormData.title.trim(),
        author: bookFormData.author.trim(),
        isbn: bookFormData.isbn?.trim() || '',
        description: bookFormData.description?.trim() || '',
        type: bookFormData.type || 'NON_FICTION',
        accessType: bookFormData.accessType || 'HYBRID',
        publisher: bookFormData.publisher?.trim() || 'Unknown Publisher',
        publicationYear: parseInt(bookFormData.publicationYear) || new Date().getFullYear(),
        pages: parseInt(bookFormData.pages) || 0,
        totalPhysicalCopies: parseInt(bookFormData.totalPhysicalCopies) || 0,
        department: bookFormData.department?.trim() || '',
        courseCode: bookFormData.courseCode?.trim() || '',
        categories: bookFormData.categories
          ? bookFormData.categories.split(',').map(c => c.trim()).filter(c => c !== '')
          : ['General'],
        price: parseFloat(bookFormData.price) || 0,
        language: bookFormData.language || 'vi',
        libraryBranchId: parseInt(bookFormData.libraryBranchId) || 1 // ✅ GỬI VERSION LÊN SERVER
      };

      console.log('📤 Sending book data:', JSON.stringify(bookData, null, 2));

      let response, savedBook;
      if (editingBook) {
        response = await fetch(`http://localhost:8080/api/books/${editingBook.id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
          body: JSON.stringify(bookData)
        });
        if (!response.ok) {
          const errorText = await response.text();
          console.error('❌ Update failed:', errorText);
          throw new Error(`Update failed: ${errorText}`);
        }
        savedBook = await response.json();
        alert(`✅ Đã cập nhật sách "${savedBook.title}"!`);
      } else {
        response = await fetch('http://localhost:8080/api/books', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
          body: JSON.stringify(bookData)
        });
        if (!response.ok) {
          const errorText = await response.text();
          console.error('❌ Create failed:', errorText);
          throw new Error(`Create failed: ${errorText}`);
        }
        savedBook = await response.json();
        alert(`✅ Đã thêm sách "${savedBook.title}"!`);
      }

      // Upload ảnh bìa nếu có file mới
      if (coverFile && savedBook.id) {
        const fd = new FormData();
        fd.append('file', coverFile);
        await fetch(`http://localhost:8080/api/books/${savedBook.id}/upload-cover`, {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${token}` },
          body: fd
        });
      }
      // Upload PDF nếu có file mới
      if (pdfFile && savedBook.id) {
        const fd = new FormData();
        fd.append('file', pdfFile);
        await fetch(`http://localhost:8080/api/books/${savedBook.id}/upload-pdf`, {
          method: 'POST',
          headers: { 'Authorization': `Bearer ${token}` },
          body: fd
        });
      }

      setShowBookModal(false);
      loadAllData();
    } catch (error) {
      console.error('Error saving book:', error);
      alert('❌ Lưu sách thất bại: ' + error.message);
    } finally {
      setSavingBook(false);
    }
  };

  // ========== LOCATION (tạm thời không dùng) ==========
  const handleUpdateLocation = (book) => {
    console.log('📍 Location feature coming soon');
  };

  const handleSaveLocation = async () => {
    // Tạm thời chưa dùng
  };

  // ========== ENTRY/EXIT ==========
  const handleRecordEntry = async () => {
    if (!entryUserId.trim()) {
      alert('Vui lòng nhập mã sinh viên');
      return;
    }
    setEntryLoading(true);
    try {
      const result = await entryExitService.recordEntry(entryUserId.trim(), selectedBranch);
      alert(`✅ Đã ghi nhận ${result.fullName || entryUserId} vào thư viện ${selectedBranch === 'B' ? 'Cơ sở B' : 'Cơ sở E'}`);
      setEntryUserId('');
      await loadAllData();
    } catch (error) {
      console.error('Error recording entry:', error);
      alert('❌ Lỗi: ' + (error.message || 'Không thể ghi nhận'));
    } finally {
      setEntryLoading(false);
    }
  };

  const handleRefreshEntries = async () => {
    setRefreshing(true);
    try {
      const entriesData = await entryExitService.getCurrentEntries();
      setCurrentEntries(entriesData || []);
      alert(`✅ Đã cập nhật, hiện có ${entriesData?.length || 0} người trong thư viện`);
    } catch (error) {
      console.error('Error refreshing entries:', error);
      alert('❌ Không thể cập nhật danh sách');
    } finally {
      setRefreshing(false);
    }
  };

  // ========== FILTER ==========
  const filteredBooks = books.filter(book =>
    book.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    book.author?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    book.isbn?.includes(searchTerm)
  );

  // ========== RENDER ==========
  if (loading) {
    return (
      <div className="ld-loading-screen">
        <div className="ld-loading-spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>
    );
  }

  return (
    <div className="ld-dashboard">
      {/* Sidebar */}
      <aside className={`ld-sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="ld-sidebar-header">
          <div className="ld-sidebar-brand">
            <img src={logoImage} alt="HUTECH Logo" className="ld-sidebar-logo" />
          </div>
          <button className="ld-sidebar-toggle" onClick={() => setSidebarOpen(!sidebarOpen)}>
            {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>

        <div className="ld-sidebar-user">
          <div className="ld-avatar-wrapper" onClick={handleAvatarClick} title="Đổi ảnh đại diện">
            {avatarPreview ? (
              <img src={avatarPreview} alt={displayName} className="ld-avatar-img" />
            ) : (
              <div className="ld-avatar">{displayName.charAt(0).toUpperCase()}</div>
            )}
            <div className="ld-avatar-overlay">
              <Camera size={18} />
              <span>{uploadingAvatar ? 'Đang tải...' : 'Đổi ảnh'}</span>
            </div>
            <div className="ld-avatar-camera-badge">
              <Camera size={14} />
            </div>
            <input
              type="file"
              accept="image/*"
              ref={avatarInputRef}
              onChange={handleAvatarChange}
              style={{ display: 'none' }}
            />
          </div>
          <div className="ld-user-name">{displayName}</div>
          <div className="ld-user-role">{isAdmin ? '🛡️ Quản trị viên' : '📖 Thủ thư'}</div>
        </div>

        <nav className="ld-sidebar-nav">
          {menuItems.map(item => (
            <button
              key={item.id}
              className={`ld-nav-item ${activeTab === item.id ? 'active' : ''}`}
              onClick={() => setActiveTab(item.id)}
            >
              <item.icon size={20} />
              <span>{item.label}</span>
              {activeTab === item.id && <div className="ld-nav-indicator" />}
            </button>
          ))}
        </nav>

        <div className="ld-sidebar-footer">
          <button className="ld-logout-btn" onClick={handleLogout}>
            <LogOut size={18} />
            <span>Đăng xuất</span>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="ld-main">
        <div className="ld-topbar">
          <div className="ld-topbar-left">
            <h1 className="ld-page-title">
              {menuItems.find(m => m.id === activeTab)?.label || 'Tổng quan'}
            </h1>
            <span className="ld-page-subtitle">
              {isAdmin ? 'Quản trị viên' : 'Thủ thư'} • HUTECH Digital Library
            </span>
          </div>
          <div className="ld-topbar-right">
            <div className="ld-topbar-stats">
              <div className="ld-top-stat">
                <span className="value">{stats.totalBooks}</span>
                <span className="label">Sách</span>
              </div>
              <div className="ld-top-stat">
                <span className="value">{stats.activeBorrows}</span>
                <span className="label">Đang mượn</span>
              </div>
              <div className="ld-top-stat">
                <span className="value">{stats.currentUsers}</span>
                <span className="label">Trong thư viện</span>
              </div>
            </div>
            <div className="ld-notification-wrapper">
              <button className="ld-notification-btn" onClick={toggleNotifications}>
                <Bell size={20} />
                {notifications.filter(n => !n.read).length > 0 && (
                  <span className="ld-notification-badge">{notifications.filter(n => !n.read).length}</span>
                )}
              </button>
              {showNotifications && (
                <div className="ld-notification-dropdown">
                  <div className="ld-notification-header">
                    <span>Thông báo</span>
                    <button onClick={() => setNotifications([])}>Đánh dấu đã đọc</button>
                  </div>
                  {notifications.length === 0 ? (
                    <div className="ld-notification-empty">Không có thông báo</div>
                  ) : (
                    notifications.map(n => (
                      <div key={n.id} className={`ld-notification-item ${!n.read ? 'unread' : ''}`}>
                        <p>{n.text}</p>
                        <span className="time">{n.time}</span>
                      </div>
                    ))
                  )}
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="ld-content">
          {activeTab === 'dashboard' && <DashboardTab stats={stats} activeBorrows={activeBorrows} />}
          {activeTab === 'books' && (
            <BooksTab
              books={filteredBooks}
              searchTerm={searchTerm}
              setSearchTerm={setSearchTerm}
              onAdd={handleAddBook}
              onEdit={handleEditBook}
              onDelete={handleDeleteBook}
            />
          )}
{activeTab === 'borrow-return' && (
  <BorrowReturnTab onRefresh={loadAllData} />
)}
{activeTab === 'reservations' && (
  <ReservationsTab
    reservations={allReservations}
    onRefresh={loadAllData}
  />
)}
          {activeTab === 'entry-exit' && (
            <EntryExitTab
              currentEntries={currentEntries}
              entryUserId={entryUserId}
              setEntryUserId={setEntryUserId}
              selectedBranch={selectedBranch}
              setSelectedBranch={setSelectedBranch}
              onRecordEntry={handleRecordEntry}
              onRefresh={handleRefreshEntries}
              entryLoading={entryLoading}
              refreshing={refreshing}
            />
          )}
          {activeTab === 'fines' && <FinesTab fines={fines} />}
          {activeTab === 'locations' && (
            <LocationsTab
              books={filteredBooks}
              searchTerm={searchTerm}
              setSearchTerm={setSearchTerm}
              onUpdateLocation={handleUpdateLocation}
            />
          )}
          {activeTab === 'accounts' && <AccountManagement />}
          {activeTab === 'violations' && <ViolationManagement />}
        </div>
      </main>

      {/* Book Modal */}
      {showBookModal && (
        <BookModal
          editingBook={editingBook}
          bookFormData={bookFormData}
          setBookFormData={setBookFormData}
          coverPreview={coverPreview}
          coverFile={coverFile}
          pdfFile={pdfFile}
          handleCoverChange={handleCoverChange}
          handlePdfChange={handlePdfChange}
          handleSaveBook={handleSaveBook}
          savingBook={savingBook}
          onClose={() => setShowBookModal(false)}
        />
      )}

      {/* Location Modal (tạm thời ẩn) */}
      {showLocationModal && selectedBookForLocation && (
        <LocationModal
          book={selectedBookForLocation}
          locationData={locationData}
          setLocationData={setLocationData}
          onSave={handleSaveLocation}
          onClose={() => setShowLocationModal(false)}
        />
      )}
    </div>
  );
};

export default LibrarianDashboard;
