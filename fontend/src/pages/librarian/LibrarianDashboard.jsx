// src/pages/librarian/LibrarianDashboard.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import authService from '../../services/authService';
import bookService from '../../services/bookService';
import borrowService from '../../services/borrowService';
import entryExitService from '../../services/entryExitService';
import fineService from '../../services/fineService';
import reservationService from '../../services/reservationService';
import {
  BookOpen, Users, RefreshCw, DollarSign, MapPin, TrendingUp,
  LogOut, Menu, X, Bell, Search, Plus, Edit, Trash2, Eye,
  CheckCircle, FileText, Upload, UserCheck, CalendarCheck, Clock,
  Home, Library, AlertCircle, Save, XCircle, Image, File
} from 'lucide-react';
import './LibrarianDashboard.css';

// Import logo
import logoImage from '../../assets/logo.png';

const LibrarianDashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [activeTab, setActiveTab] = useState('dashboard');
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [loading, setLoading] = useState(true);
  
  // Data states
  const [books, setBooks] = useState([]);
  const [activeBorrows, setActiveBorrows] = useState([]);
  const [currentEntries, setCurrentEntries] = useState([]);
  const [fines, setFines] = useState([]);
  const [allReservations, setAllReservations] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Book Modal States
  const [showBookModal, setShowBookModal] = useState(false);
  const [editingBook, setEditingBook] = useState(null);
  const [savingBook, setSavingBook] = useState(false);
  const [coverFile, setCoverFile] = useState(null);
  const [pdfFile, setPdfFile] = useState(null);
  const [coverPreview, setCoverPreview] = useState(null);
  
  // Location Modal States
  const [showLocationModal, setShowLocationModal] = useState(false);
  const [selectedBookForLocation, setSelectedBookForLocation] = useState(null);
  const [locationData, setLocationData] = useState({
    zone: '',
    shelf: '',
    column: '',
    row: '',
    position: ''
  });
  
  // Entry/Exit States
  const [entryUserId, setEntryUserId] = useState('');
  const [selectedBranch, setSelectedBranch] = useState('B');
  const [entryLoading, setEntryLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  
  // Book Form Data
  const [bookFormData, setBookFormData] = useState({
    title: '',
    author: '',
    isbn: '',
    description: '',
    type: 'NON_FICTION',
    accessType: 'HYBRID',
    publisher: '',
    publicationYear: new Date().getFullYear(),
    pages: 0,
    totalPhysicalCopies: 0,
    department: '',
    courseCode: '',
    categories: '',
    price: 0
  });
  
  // Stats
  const [stats, setStats] = useState({
    totalBooks: 0,
    activeBorrows: 0,
    overdueCount: 0,
    currentUsers: 0,
    totalFines: 0,
    totalReservations: 0
  });

  useEffect(() => {
    const storedUser = authService.getUser();
    if (!storedUser || !authService.isLibrarian()) {
      navigate('/login');
      return;
    }
    setUser(storedUser);
    loadAllData();
  }, []);

  const loadAllData = async () => {
    setLoading(true);
    try {
      console.log('Loading librarian dashboard data...');
      
      const booksData = await bookService.getAllBooks(0, 100);
      const booksList = booksData.content || [];
      setBooks(booksList);
      
      const borrowsData = await borrowService.getAllActiveBorrows();
      setActiveBorrows(borrowsData || []);
      
      const entriesData = await entryExitService.getCurrentEntries();
      console.log('Current entries from service:', entriesData);
      setCurrentEntries(entriesData || []);
      
      const finesData = await fineService.getAllFines();
      setFines(finesData || []);
      
      const reservationsData = await reservationService.getAllReservations();
      setAllReservations(reservationsData || []);
      
      const overdueCount = (borrowsData || []).filter(b => new Date(b.dueDate) < new Date()).length;
      const totalFinesAmount = (finesData || []).reduce((sum, f) => sum + (f.amount || 0), 0);
      
      setStats({
        totalBooks: booksList.length,
        activeBorrows: (borrowsData || []).length,
        overdueCount: overdueCount,
        currentUsers: (entriesData || []).length,
        totalFines: totalFinesAmount,
        totalReservations: (reservationsData || []).filter(r => r.status === 'CONFIRMED').length
      });
      
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  // ==================== BOOK CRUD FUNCTIONS ====================
  
  const handleAddBook = () => {
    setEditingBook(null);
    setBookFormData({
      title: '',
      author: '',
      isbn: '',
      description: '',
      type: 'NON_FICTION',
      accessType: 'HYBRID',
      publisher: '',
      publicationYear: new Date().getFullYear(),
      pages: 0,
      totalPhysicalCopies: 0,
      department: '',
      courseCode: '',
      categories: '',
      price: 0
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
      price: book.price || 0
    });
    setCoverPreview(book.mainCoverImageUrl ? `http://localhost:8080${book.mainCoverImageUrl}` : null);
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
        headers: {
          'Authorization': `Bearer ${token}`,
          'X-User-Id': user?.username || 'librarian',
          'X-User-Type': 'LIBRARIAN'
        }
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
      reader.onloadend = () => {
        setCoverPreview(reader.result);
      };
      reader.readAsDataURL(file);
    }
  };

  const handlePdfChange = (e) => {
    const file = e.target.files[0];
    if (file) {
      setPdfFile(file);
    }
  };

  const handleSaveBook = async () => {
    if (!bookFormData.title || !bookFormData.author) {
      alert('Vui lòng nhập tên sách và tác giả');
      return;
    }
    
    setSavingBook(true);
    
    try {
      const token = localStorage.getItem('token');
      
      const bookData = {
        title: bookFormData.title,
        author: bookFormData.author,
        isbn: bookFormData.isbn,
        description: bookFormData.description,
        type: bookFormData.type,
        accessType: bookFormData.accessType,
        publisher: bookFormData.publisher,
        publicationYear: parseInt(bookFormData.publicationYear),
        pages: parseInt(bookFormData.pages),
        totalPhysicalCopies: parseInt(bookFormData.totalPhysicalCopies),
        department: bookFormData.department,
        courseCode: bookFormData.courseCode,
        categories: bookFormData.categories.split(',').map(c => c.trim()),
        price: parseFloat(bookFormData.price) || 0
      };
      
      let response;
      let savedBook;
      
      if (editingBook) {
        response = await fetch(`http://localhost:8080/api/books/${editingBook.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            'X-User-Id': user?.username || 'librarian',
            'X-User-Type': 'LIBRARIAN'
          },
          body: JSON.stringify(bookData)
        });
        
        if (!response.ok) throw new Error('Update failed');
        savedBook = await response.json();
        alert(`✅ Đã cập nhật sách "${savedBook.title}" thành công!`);
        
      } else {
        response = await fetch('http://localhost:8080/api/books', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
            'X-User-Id': user?.username || 'librarian',
            'X-User-Type': 'LIBRARIAN'
          },
          body: JSON.stringify(bookData)
        });
        
        if (!response.ok) throw new Error('Create failed');
        savedBook = await response.json();
        alert(`✅ Đã thêm sách "${savedBook.title}" thành công!`);
      }
      
      if (coverFile && savedBook.id) {
        const coverFormData = new FormData();
        coverFormData.append('file', coverFile);
        
        await fetch(`http://localhost:8080/api/books/${savedBook.id}/upload-cover`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`
          },
          body: coverFormData
        });
        console.log('Cover uploaded');
      }
      
      if (pdfFile && savedBook.id) {
        const pdfFormData = new FormData();
        pdfFormData.append('file', pdfFile);
        
        await fetch(`http://localhost:8080/api/books/${savedBook.id}/upload-pdf`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`
          },
          body: pdfFormData
        });
        console.log('PDF uploaded');
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

  // ==================== LOCATION FUNCTIONS ====================
  
  const handleUpdateLocation = (book) => {
    setSelectedBookForLocation(book);
    setLocationData({
      zone: book.zone || '',
      shelf: book.shelf || '',
      column: book.column || '',
      row: book.row || '',
      position: book.position || ''
    });
    setShowLocationModal(true);
  };

  const handleSaveLocation = async () => {
    if (!selectedBookForLocation) return;
    
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/books/${selectedBookForLocation.id}/location`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
          'X-User-Id': user?.username || 'librarian',
          'X-User-Type': 'LIBRARIAN'
        },
        body: JSON.stringify({
          zone: parseInt(locationData.zone) || 0,
          shelf: parseInt(locationData.shelf) || 1,
          column: parseInt(locationData.column) || 1,
          row: parseInt(locationData.row) || 1,
          position: parseInt(locationData.position) || 1
        })
      });
      
      if (response.ok) {
        alert(`✅ Đã cập nhật vị trí cho sách "${selectedBookForLocation.title}"`);
        setShowLocationModal(false);
        loadAllData();
      } else {
        throw new Error('Update failed');
      }
    } catch (error) {
      console.error('Error updating location:', error);
      alert('❌ Cập nhật vị trí thất bại');
    }
  };

  // ==================== ENTRY/EXIT FUNCTIONS ====================
  
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
      alert(`✅ Đã cập nhật danh sách, hiện có ${entriesData?.length || 0} người trong thư viện`);
    } catch (error) {
      console.error('Error refreshing entries:', error);
      alert('❌ Không thể cập nhật danh sách');
    } finally {
      setRefreshing(false);
    }
  };

  // ==================== FILTER ====================
  
  const filteredBooks = books.filter(book =>
    book.title?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    book.author?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    book.isbn?.includes(searchTerm)
  );

  // ==================== MENU ITEMS ====================
  
  const menuItems = [
    { id: 'dashboard', icon: TrendingUp, label: 'Tổng quan', color: '#0055a5' },
    { id: 'books', icon: BookOpen, label: 'Quản lý sách', color: '#10b981' },
    { id: 'borrow-return', icon: RefreshCw, label: 'Mượn/Trả sách', color: '#f59e0b' },
    { id: 'reservations', icon: CalendarCheck, label: 'Đặt lịch', color: '#3b82f6' },
    { id: 'entry-exit', icon: Users, label: 'Quản lý ra vào', color: '#8b5cf6' },
    { id: 'fines', icon: DollarSign, label: 'Quản lý phạt', color: '#ef4444' },
    { id: 'locations', icon: MapPin, label: 'Vị trí sách', color: '#06b6d4' }
  ];

  // ==================== HELPER FUNCTIONS ====================
  
  const getReservationStatus = (status) => {
    switch (status) {
      case 'CONFIRMED': return { text: '🟢 Đang giữ sách', class: 'status-active' };
      case 'COMPLETED': return { text: '✅ Đã mượn', class: 'status-returned' };
      case 'EXPIRED': return { text: '⏰ Hết hạn', class: 'status-overdue' };
      case 'CANCELLED': return { text: '❌ Đã hủy', class: 'status-cancelled' };
      default: return { text: '📅 Đang chờ', class: 'status-pending' };
    }
  };

  const getBorrowStatus = (borrow) => {
    if (borrow.returnedAt) return { text: 'Đã trả', class: 'status-returned' };
    if (new Date(borrow.dueDate) < new Date()) return { text: 'Quá hạn', class: 'status-overdue' };
    return { text: 'Đang mượn', class: 'status-active' };
  };

  // ==================== RENDER FUNCTIONS ====================
  
  const renderDashboard = () => (
    <div>
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon primary"><BookOpen size={28} /></div>
          <div className="stat-info">
            <h3>{stats.totalBooks}</h3>
            <p>Tổng số sách</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon success"><RefreshCw size={28} /></div>
          <div className="stat-info">
            <h3>{stats.activeBorrows}</h3>
            <p>Đang mượn</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon warning"><AlertCircle size={28} /></div>
          <div className="stat-info">
            <h3>{stats.overdueCount}</h3>
            <p>Quá hạn</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon danger"><DollarSign size={28} /></div>
          <div className="stat-info">
            <h3>{stats.totalFines.toLocaleString()}đ</h3>
            <p>Tổng tiền phạt</p>
          </div>
        </div>
      </div>

      <div className="recent-section">
        <div className="section-header">
          <h3>📖 Sách đang được mượn</h3>
          <span className="badge-count">{activeBorrows.length} đang mượn</span>
        </div>
        <div className="table-wrapper">
          <table className="data-table">
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
                const status = getBorrowStatus(borrow);
                return (
                  <tr key={borrow.id}>
                    <td><span className="user-code">#{borrow.id}</span></td>
                    <td>{borrow.userId}</td>
                    <td className="book-title-cell">{borrow.bookTitle || `Sách ID ${borrow.bookId}`}</td>
                    <td>{borrow.borrowedAt ? new Date(borrow.borrowedAt).toLocaleDateString('vi-VN') : 'N/A'}</td>
                    <td className={new Date(borrow.dueDate) < new Date() ? 'text-danger' : ''}>
                      {borrow.dueDate ? new Date(borrow.dueDate).toLocaleDateString('vi-VN') : 'N/A'}
                    </td>
                    <td><span className={`status-badge ${status.class}`}>{status.text}</span></td>
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

  const renderBookManagement = () => (
    <div>
      <div className="form-card">
        <div className="section-header">
          <h3><Search size={18} /> Tìm kiếm sách</h3>
          <button className="btn-primary" onClick={handleAddBook}>
            <Plus size={18} /> Thêm sách mới
          </button>
        </div>
        <input
          type="text"
          placeholder="Tìm theo tên sách, tác giả, ISBN..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ width: '100%', padding: '12px', borderRadius: '12px', border: '1px solid var(--gray-200)' }}
        />
      </div>
      
      <div className="books-grid">
        {filteredBooks.map(book => (
          <div key={book.id} className="book-card">
            <div className="book-cover">
              <img 
                src={book.mainCoverImageUrl ? `http://localhost:8080${book.mainCoverImageUrl}` : 'https://via.placeholder.com/300x200?text=No+Cover'} 
                alt={book.title}
              />
              <div className="book-badges">
                <span className={`book-badge ${book.availablePhysicalCopies > 0 ? 'badge-available' : 'badge-unavailable'}`}>
                  {book.availablePhysicalCopies > 0 ? `📖 Còn ${book.availablePhysicalCopies} bản` : '🔒 Hết sách'}
                </span>
                {book.pdfFileUrl && <span className="book-badge badge-pdf">📄 Có PDF</span>}
              </div>
            </div>
            <div className="book-info">
              <h3 className="book-title">{book.title}</h3>
              <p className="book-author">{book.author}</p>
              <div className="book-meta">
                <span>📚 {book.categories?.[0] || 'Chưa phân loại'}</span>
                <span>📅 {book.publicationYear || 'N/A'}</span>
                <span>📖 {book.pages || 'N/A'} trang</span>
              </div>
              <div className="book-stats">
                <div className="stat">
                  <span className="label">Tổng số</span>
                  <span className="value">{book.totalPhysicalCopies || 0}</span>
                </div>
                <div className="stat">
                  <span className="label">Còn lại</span>
                  <span className="value" style={{ color: book.availablePhysicalCopies > 0 ? '#10b981' : '#ef4444' }}>
                    {book.availablePhysicalCopies || 0}
                  </span>
                </div>
                <div className="stat">
                  <span className="label">Đã mượn</span>
                  <span className="value">{(book.totalPhysicalCopies || 0) - (book.availablePhysicalCopies || 0)}</span>
                </div>
              </div>
              <div className="book-actions">
                <button className="action-btn action-edit" onClick={() => handleEditBook(book)}>
                  <Edit size={14} /> Sửa
                </button>
                <button className="action-btn action-delete" onClick={() => handleDeleteBook(book)}>
                  <Trash2 size={14} /> Xóa
                </button>
                {book.pdfFileUrl && (
                  <a href={`http://localhost:8080${book.pdfFileUrl}`} target="_blank" className="action-btn action-pdf" rel="noreferrer">
                    <Eye size={14} /> PDF
                  </a>
                )}
              </div>
            </div>
          </div>
        ))}
      </div>
      {filteredBooks.length === 0 && (
        <div className="text-center" style={{ padding: '40px', color: 'var(--gray-500)' }}>
          Không tìm thấy sách nào
        </div>
      )}
      
      {/* Book Modal */}
      {showBookModal && (
        <div className="modal-overlay" onClick={() => setShowBookModal(false)}>
          <div className="modal-content modal-large" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>{editingBook ? '✏️ Sửa sách' : '📚 Thêm sách mới'}</h3>
              <button className="modal-close" onClick={() => setShowBookModal(false)}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-row">
                <div className="form-group">
                  <label>Tên sách <span className="required">*</span></label>
                  <input type="text" value={bookFormData.title} onChange={(e) => setBookFormData({...bookFormData, title: e.target.value})} placeholder="Nhập tên sách" />
                </div>
                <div className="form-group">
                  <label>Tác giả <span className="required">*</span></label>
                  <input type="text" value={bookFormData.author} onChange={(e) => setBookFormData({...bookFormData, author: e.target.value})} placeholder="Nhập tên tác giả" />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>ISBN</label>
                  <input type="text" value={bookFormData.isbn} onChange={(e) => setBookFormData({...bookFormData, isbn: e.target.value})} placeholder="Mã ISBN" />
                </div>
                <div className="form-group">
                  <label>Nhà xuất bản</label>
                  <input type="text" value={bookFormData.publisher} onChange={(e) => setBookFormData({...bookFormData, publisher: e.target.value})} placeholder="Nhà xuất bản" />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Năm xuất bản</label>
                  <input type="number" value={bookFormData.publicationYear} onChange={(e) => setBookFormData({...bookFormData, publicationYear: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Số trang</label>
                  <input type="number" value={bookFormData.pages} onChange={(e) => setBookFormData({...bookFormData, pages: e.target.value})} />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Số lượng bản</label>
                  <input type="number" value={bookFormData.totalPhysicalCopies} onChange={(e) => setBookFormData({...bookFormData, totalPhysicalCopies: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Giá (VNĐ)</label>
                  <input type="number" value={bookFormData.price} onChange={(e) => setBookFormData({...bookFormData, price: e.target.value})} />
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Loại sách</label>
                  <select value={bookFormData.type} onChange={(e) => setBookFormData({...bookFormData, type: e.target.value})}>
                    <option value="NON_FICTION">Non-Fiction</option>
                    <option value="FICTION">Fiction</option>
                    <option value="TEXTBOOK">Textbook</option>
                    <option value="REFERENCE">Reference</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Hình thức</label>
                  <select value={bookFormData.accessType} onChange={(e) => setBookFormData({...bookFormData, accessType: e.target.value})}>
                    <option value="PHYSICAL_ONLY">Chỉ mượn vật lý</option>
                    <option value="DIGITAL_ONLY">Chỉ đọc online</option>
                    <option value="HYBRID">Cả hai</option>
                  </select>
                </div>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Khoa/Viện</label>
                  <input type="text" value={bookFormData.department} onChange={(e) => setBookFormData({...bookFormData, department: e.target.value})} placeholder="VD: Công nghệ thông tin" />
                </div>
                <div className="form-group">
                  <label>Mã học phần</label>
                  <input type="text" value={bookFormData.courseCode} onChange={(e) => setBookFormData({...bookFormData, courseCode: e.target.value})} />
                </div>
              </div>
              
              <div className="form-group">
                <label>Thể loại (cách nhau bằng dấu phẩy)</label>
                <input type="text" value={bookFormData.categories} onChange={(e) => setBookFormData({...bookFormData, categories: e.target.value})} placeholder="Lập trình, Khoa học máy tính, AI" />
              </div>
              
              <div className="form-group">
                <label>Mô tả</label>
                <textarea rows="3" value={bookFormData.description} onChange={(e) => setBookFormData({...bookFormData, description: e.target.value})} placeholder="Nhập mô tả sách..."></textarea>
              </div>
              
              <div className="form-row">
                <div className="form-group">
                  <label>Ảnh bìa</label>
                  <input type="file" accept="image/*" onChange={handleCoverChange} />
                  {coverPreview && (
                    <div className="image-preview">
                      <img src={coverPreview} alt="Cover preview" style={{ width: '100px', marginTop: '10px' }} />
                    </div>
                  )}
                </div>
                <div className="form-group">
                  <label>File PDF</label>
                  <input type="file" accept=".pdf" onChange={handlePdfChange} />
                  {pdfFile && <small>Đã chọn: {pdfFile.name}</small>}
                </div>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setShowBookModal(false)}>Hủy</button>
              <button className="btn-primary" onClick={handleSaveBook} disabled={savingBook}>
                {savingBook ? 'Đang lưu...' : (editingBook ? 'Cập nhật' : 'Thêm sách')}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  const renderReservationsManagement = () => (
    <div className="recent-section">
      <div className="section-header">
        <h3>📅 Danh sách đặt lịch mượn sách</h3>
        <button className="btn-secondary" onClick={loadAllData}><RefreshCw size={16} /> Làm mới</button>
      </div>
      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Người đặt</th>
              <th>Sách</th>
              <th>Ngày đặt</th>
              <th>Ngày hẹn</th>
              <th>Hết hạn</th>
              <th>Trạng thái</th>
            </tr>
          </thead>
          <tbody>
            {allReservations.map(res => {
              const status = getReservationStatus(res.status);
              return (
                <tr key={res.id}>
                  <td><span className="user-code">#{res.id}</span></td>
                  <td><strong>{res.userId}</strong></td>
                  <td>{res.bookTitle || `Sách ID ${res.bookId}`}</td>
                  <td>{res.reservationDate ? new Date(res.reservationDate).toLocaleDateString('vi-VN') : 'N/A'}</td>
                  <td>{res.pickupDate ? new Date(res.pickupDate).toLocaleDateString('vi-VN') : 'N/A'}</td>
                  <td className={res.expiryDate && new Date(res.expiryDate) < new Date() ? 'text-danger' : ''}>
                    {res.expiryDate ? new Date(res.expiryDate).toLocaleDateString('vi-VN') : 'N/A'}
                  </td>
                  <td><span className={`status-badge ${status.class}`}>{status.text}</span></td>
                </tr>
              );
            })}
            {allReservations.length === 0 && (
              <tr><td colSpan="7" className="text-center">Chưa có đặt lịch nào</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );

  const renderBorrowReturn = () => (
    <div>
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon success"><RefreshCw size={28} /></div>
          <div className="stat-info">
            <h3>{activeBorrows.length}</h3>
            <p>Đang mượn</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon warning"><AlertCircle size={28} /></div>
          <div className="stat-info">
            <h3>{stats.overdueCount}</h3>
            <p>Quá hạn</p>
          </div>
        </div>
      </div>
      
      <div className="recent-section">
        <div className="section-header">
          <h3>📋 Danh sách mượn sách</h3>
        </div>
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã mượn</th>
                <th>Mã SV</th>
                <th>Sách</th>
                <th>Ngày mượn</th>
                <th>Hạn trả</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {activeBorrows.map(borrow => {
                const status = getBorrowStatus(borrow);
                return (
                  <tr key={borrow.id}>
                    <td><span className="user-code">#{borrow.id}</span></td>
                    <td>{borrow.userId}</td>
                    <td className="book-title-cell">{borrow.bookTitle || `Sách ID ${borrow.bookId}`}</td>
                    <td>{borrow.borrowedAt ? new Date(borrow.borrowedAt).toLocaleDateString('vi-VN') : 'N/A'}</td>
                    <td className={new Date(borrow.dueDate) < new Date() ? 'text-danger' : ''}>
                      {borrow.dueDate ? new Date(borrow.dueDate).toLocaleDateString('vi-VN') : 'N/A'}
                    </td>
                    <td><span className={`status-badge ${status.class}`}>{status.text}</span></td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );

  const renderEntryExit = () => (
    <div>
      {/* Form ghi nhận vào thư viện */}
      <div className="form-card">
        <h3>🚪 Ghi nhận vào thư viện</h3>
        <div className="form-row">
          <div className="form-group">
            <label>Mã sinh viên</label>
            <input 
              type="text" 
              placeholder="Nhập mã sinh viên" 
              value={entryUserId}
              onChange={(e) => setEntryUserId(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleRecordEntry()}
            />
          </div>
          <div className="form-group">
            <label>Khu vực</label>
            <select value={selectedBranch} onChange={(e) => setSelectedBranch(e.target.value)}>
              <option value="B">🏢 Cơ sở B - 475A Điện Biên Phủ</option>
              <option value="E">🏫 Cơ sở E - Khu Công nghệ cao Quận 9</option>
            </select>
          </div>
        </div>
        <button 
          className="btn-primary" 
          onClick={handleRecordEntry} 
          disabled={entryLoading}
        >
          {entryLoading ? 'Đang xử lý...' : 'Ghi nhận vào'}
        </button>
      </div>
      
      {/* Thông báo giờ mở cửa */}
      <div className="info-card">
        <h4>📅 Giờ mở cửa</h4>
        <div className="branch-hours">
          <div className="branch">
            <strong>🏢 Cơ sở B:</strong>
            <p>Thứ 2: 9h00 - 19h00</p>
            <p>Thứ 3 - Thứ 6: 8h00 - 19h00</p>
            <p>Thứ 7: 8h00 - 11h30</p>
            <p>Chủ nhật: Đóng cửa</p>
          </div>
          <div className="branch">
            <strong>🏫 Cơ sở E:</strong>
            <p>Thứ 2 - Thứ 6: 8h00 - 16h15</p>
            <p>Thứ 7: 8h00 - 11h15</p>
            <p>Chủ nhật: Đóng cửa</p>
          </div>
        </div>
      </div>
      
      {/* Danh sách đang trong thư viện */}
      <div className="recent-section">
        <div className="section-header">
          <h3>🟢 Đang trong thư viện ({currentEntries.length})</h3>
          <button className="btn-secondary" onClick={handleRefreshEntries} disabled={refreshing}>
            <RefreshCw size={16} /> {refreshing ? 'Đang tải...' : 'Làm mới'}
          </button>
        </div>
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã SV</th>
                <th>Họ tên</th>
                <th>Khoa/Viện</th>
                <th>Chuyên ngành</th>
                <th>Thời gian vào</th>
                <th>Khu vực</th>
                <th>Loại user</th>
              </tr>
            </thead>
            <tbody>
              {currentEntries.map(entry => (
                <tr key={entry.id}>
                  <td><span className="user-code">{entry.studentId || entry.userId}</span></td>
                  <td><strong>{entry.fullName || entry.userId}</strong></td>
                  <td>{entry.faculty || 'N/A'}</td>
                  <td>{entry.major || 'N/A'}</td>
                  <td>{entry.entryTime ? new Date(entry.entryTime).toLocaleString('vi-VN') : 'N/A'}</td>
                  <td>
                    <span className={`branch-badge ${entry.branch === 'B' ? 'branch-b' : 'branch-e'}`}>
                      {entry.branch === 'B' ? '🏢 Cơ sở B' : '🏫 Cơ sở E'}
                    </span>
                  </td>
                  <td>
                    <span className={`user-type-badge ${entry.userType?.toLowerCase() || 'student'}`}>
                      {entry.userType === 'LECTURER' ? '👨‍🏫 Giảng viên' : '🎓 Sinh viên'}
                    </span>
                  </td>
                </tr>
              ))}
              {currentEntries.length === 0 && (
                <tr>
                  <td colSpan="7" className="text-center">📭 Không có ai trong thư viện</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );

  const renderFineManagement = () => (
    <div className="recent-section">
      <div className="section-header">
        <h3>💰 Danh sách phạt</h3>
      </div>
      <div className="table-wrapper">
        <table className="data-table">
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
            {fines.map(fine => (
              <tr key={fine.id}>
                <td><span className="user-code">#{fine.id}</span></td>
                <td>{fine.userId}</td>
                <td className="text-danger">{fine.amount?.toLocaleString()}đ</td>
                <td>{fine.reason || 'Vi phạm'}</td>
                <td>{fine.createdAt ? new Date(fine.createdAt).toLocaleDateString('vi-VN') : 'N/A'}</td>
                <td>
                  <span className={`status-badge ${fine.paid ? 'status-returned' : 'status-overdue'}`}>
                    {fine.paid ? 'Đã nộp' : 'Chưa nộp'}
                  </span>
                 </td>
               </tr>
            ))}
            {fines.length === 0 && (
              <tr><td colSpan="6" className="text-center">Không có dữ liệu phạt</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );

  const renderLocationManagement = () => (
    <div>
      <div className="form-card">
        <div className="section-header">
          <h3><Search size={18} /> Tìm kiếm sách để gán vị trí</h3>
        </div>
        <input
          type="text"
          placeholder="Tìm theo tên sách, tác giả, ISBN..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ width: '100%', padding: '12px', borderRadius: '12px', border: '1px solid var(--gray-200)' }}
        />
      </div>
      
      <div className="books-grid">
        {filteredBooks.map(book => (
          <div key={book.id} className="book-card">
            <div className="book-cover">
              <img 
                src={book.mainCoverImageUrl ? `http://localhost:8080${book.mainCoverImageUrl}` : 'https://via.placeholder.com/300x200?text=No+Cover'} 
                alt={book.title}
              />
            </div>
            <div className="book-info">
              <h3 className="book-title">{book.title}</h3>
              <p className="book-author">{book.author}</p>
              <div className="book-meta">
                <span>📚 {book.categories?.[0] || 'Chưa phân loại'}</span>
                <span>📅 {book.publicationYear || 'N/A'}</span>
              </div>
              <div className="book-stats">
                <div className="stat">
                  <span className="label">Vị trí hiện tại</span>
                  <span className="value">{book.zone ? `${book.zone}-${book.shelf}-${book.column}` : 'Chưa có'}</span>
                </div>
              </div>
              <div className="book-actions">
                <button className="action-btn action-edit" onClick={() => handleUpdateLocation(book)}>
                  <MapPin size={14} /> Gán vị trí
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
      
      {/* Location Modal */}
      {showLocationModal && selectedBookForLocation && (
        <div className="modal-overlay" onClick={() => setShowLocationModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>📍 Gán vị trí cho sách: {selectedBookForLocation.title}</h3>
              <button className="modal-close" onClick={() => setShowLocationModal(false)}>×</button>
            </div>
            <div className="modal-body">
              <div className="form-row">
                <div className="form-group">
                  <label>Khu vực (Zone)</label>
                  <select value={locationData.zone} onChange={(e) => setLocationData({...locationData, zone: e.target.value})}>
                    <option value="">Chọn khu</option>
                    <option value="300">Khu 300 - Khoa học xã hội</option>
                    <option value="400">Khu 400 - Ngôn ngữ</option>
                    <option value="500">Khu 500 - Khoa học tự nhiên</option>
                    <option value="600">Khu 600 - Công nghệ</option>
                    <option value="700">Khu 700 - Nghệ thuật</option>
                    <option value="800">Khu 800 - Văn học</option>
                    <option value="900">Khu 900 - Lịch sử & Địa lý</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Kệ số</label>
                  <input type="number" placeholder="1-9" value={locationData.shelf} onChange={(e) => setLocationData({...locationData, shelf: e.target.value})} />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Cột số</label>
                  <input type="number" placeholder="1-9" value={locationData.column} onChange={(e) => setLocationData({...locationData, column: e.target.value})} />
                </div>
                <div className="form-group">
                  <label>Hàng số</label>
                  <input type="number" placeholder="1-9" value={locationData.row} onChange={(e) => setLocationData({...locationData, row: e.target.value})} />
                </div>
              </div>
              <div className="form-group">
                <label>Vị trí trên kệ</label>
                <input type="number" placeholder="1-9" value={locationData.position} onChange={(e) => setLocationData({...locationData, position: e.target.value})} />
              </div>
              <div className="location-preview">
                <strong>Vị trí đầy đủ:</strong> {locationData.zone && locationData.shelf && locationData.column ? 
                  `${locationData.zone} - Kệ ${locationData.shelf} - Cột ${locationData.column} - Hàng ${locationData.row || '?'} - Vị trí ${locationData.position || '?'}` : 'Chưa đủ thông tin'}
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn-secondary" onClick={() => setShowLocationModal(false)}>Hủy</button>
              <button className="btn-primary" onClick={handleSaveLocation}>Lưu vị trí</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );

  const renderContent = () => {
    switch (activeTab) {
      case 'dashboard': return renderDashboard();
      case 'books': return renderBookManagement();
      case 'borrow-return': return renderBorrowReturn();
      case 'reservations': return renderReservationsManagement();
      case 'entry-exit': return renderEntryExit();
      case 'fines': return renderFineManagement();
      case 'locations': return renderLocationManagement();
      default: return renderDashboard();
    }
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner"></div>
        <p>Đang tải dữ liệu...</p>
      </div>
    );
  }

  return (
    <div className="librarian-dashboard">
      <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="sidebar-header">
          <img src={logoImage} alt="HUTECH" className="sidebar-logo" />
        </div>
        
        <div className="sidebar-user">
          <div className="user-avatar">
            {user?.name?.charAt(0) || 'T'}
          </div>
          <div className="user-name">{user?.name || 'Thủ thư'}</div>
          <div className="user-role">Quản lý thư viện</div>
        </div>
        
        <nav className="sidebar-nav">
          {menuItems.map(item => (
            <button
              key={item.id}
              className={`nav-item ${activeTab === item.id ? 'active' : ''}`}
              onClick={() => setActiveTab(item.id)}
            >
              <item.icon size={20} />
              <span>{item.label}</span>
            </button>
          ))}
        </nav>
        
        <div className="sidebar-footer">
          <button className="logout-btn" onClick={handleLogout}>
            <LogOut size={18} />
            <span>Đăng xuất</span>
          </button>
        </div>
      </aside>

      <main className="main-content">
        <div className="content-header">
          <h1>
            {menuItems.find(m => m.id === activeTab)?.icon && 
              React.createElement(menuItems.find(m => m.id === activeTab).icon, { size: 24 })}
            {menuItems.find(m => m.id === activeTab)?.label || 'Tổng quan'}
          </h1>
          <div className="header-stats">
            <div className="header-stat">
              <div className="value">{stats.totalBooks}</div>
              <div className="label">Sách</div>
            </div>
            <div className="header-stat">
              <div className="value">{stats.activeBorrows}</div>
              <div className="label">Đang mượn</div>
            </div>
            <div className="header-stat">
              <div className="value">{stats.currentUsers}</div>
              <div className="label">Trong thư viện</div>
            </div>
          </div>
        </div>
        
        <div className="content-body">
          {renderContent()}
        </div>
      </main>
    </div>
  );
};

export default LibrarianDashboard;
