// src/pages/dashboard/Dashboard.jsx
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/common/Navbar';
import Footer from '../../components/common/Footer';
import HeroSection from '../../components/dashboard/HeroSection';
import SearchBar from '../../components/common/SearchBar';
import BookFilters from '../../components/books/BookFilters';
import BookGrid from '../../components/books/BookGrid';
import StatsSection from '../../components/dashboard/StatsSection';
import CategoriesSection from '../../components/dashboard/CategoriesSection';
import EventsSection from '../../components/dashboard/EventsSection';
import ReservationModal from '../../components/common/ReservationModal';
import authService from '../../services/authService';
import bookService from '../../services/bookService';
import userService from '../../services/userService';
import reservationService from '../../services/reservationService';
import './Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const [user, setUser] = useState(null);
  const [darkMode, setDarkMode] = useState(false);
  const [language, setLanguage] = useState('vi');
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedFilter, setSelectedFilter] = useState('all');
  const [loading, setLoading] = useState(true);
  const [books, setBooks] = useState([]);
  const [loadingBooks, setLoadingBooks] = useState(false);
  const [apiError, setApiError] = useState('');
  const [stats, setStats] = useState({
    totalBooks: '0',
    onlineBooks: '0',
    physicalBooks: '0',
    activeUsers: '8,742',
    monthlyReads: '156,234'
  });

  // Reservation Modal States
  const [showReserveModal, setShowReserveModal] = useState(false);
  const [selectedReserveBook, setSelectedReserveBook] = useState(null);
  const [reserving, setReserving] = useState(false);

  // Get available pickup dates
  const getAvailablePickupDates = () => {
    const dates = [];
    const today = new Date();
    for (let i = 1; i <= 7; i++) {
      const date = new Date(today);
      date.setDate(today.getDate() + i);
      dates.push({
        value: date.toISOString().split('T')[0],
        label: `${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`,
        day: date.toLocaleDateString('vi-VN', { weekday: 'long' })
      });
    }
    return dates;
  };

  const availableDates = getAvailablePickupDates();

  useEffect(() => {
    const initializeDashboard = async () => {
      try {
        await loadUserData();
        await loadBooks();
        setLoading(false);
      } catch (error) {
        console.error('Dashboard initialization error:', error);
        setLoading(false);
      }
    };

    initializeDashboard();

    if (darkMode) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [darkMode]);

  const loadUserData = async () => {
    try {
      const storedUser = authService.getUser();
      
      if (!storedUser) {
        setUser({
          name: "Van A Nguyen",
          username: "vana",
          studentId: "ST2024001",
          faculty: "Công nghệ thông tin",
          avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=vana`
        });
        return;
      }
      
      const initialUser = {
        name: storedUser.name || storedUser.username || 'Van A Nguyen',
        username: storedUser.username || 'vana',
        studentId: storedUser.studentId || storedUser.username || 'ST2024001',
        faculty: storedUser.department || storedUser.faculty || 'Công nghệ thông tin',
        email: storedUser.email,
        avatar: userService.getAvatarUrl(storedUser) || `https://api.dicebear.com/7.x/avataaars/svg?seed=${storedUser.username || 'vana'}`
      };
      
      setUser(initialUser);
      
      if (storedUser.username) {
        userService.getUserProfile(storedUser.username)
          .then(freshData => {
            if (freshData && freshData.username) {
              const updatedUser = {
                ...initialUser,
                name: userService.getDisplayName ? userService.getDisplayName(freshData) : 
                      (freshData.fullName || freshData.name || freshData.username),
                avatar: userService.getAvatarUrl(freshData) || initialUser.avatar
              };
              setUser(updatedUser);
              authService.saveUser({
                ...storedUser,
                ...freshData
              });
            }
          })
          .catch(error => {
            console.log('Using stored user data');
          });
      }
      
    } catch (error) {
      console.error('Error loading user:', error);
      setUser({
        name: "Van A Nguyen",
        username: "vana",
        studentId: "ST2024001",
        faculty: "Công nghệ thông tin",
        avatar: `https://api.dicebear.com/7.x/avataaars/svg?seed=vana`
      });
    }
  };

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };

  const toggleLanguage = () => {
    setLanguage(language === 'vi' ? 'en' : 'vi');
  };

  const handleAvatarClick = () => {
    navigate('/profile');
  };

  const loadBooks = async (keyword = '') => {
    try {
      setLoadingBooks(true);
      setApiError('');
      
      let booksData;
      
      if (keyword.trim() === '') {
        booksData = await bookService.getAllBooks(0, 20, 'createdAt', 'desc');
      } else {
        booksData = await bookService.searchBooks(keyword, 0, 20);
      }
      
      if (!booksData) {
        throw new Error('No data received from server');
      }
      
      let booksArray = [];
      
      if (booksData.content && Array.isArray(booksData.content)) {
        booksArray = booksData.content;
      } else if (Array.isArray(booksData)) {
        booksArray = booksData;
      } else if (booksData.books && Array.isArray(booksData.books)) {
        booksArray = booksData.books;
      } else {
        booksArray = [];
      }
      
      const formattedBooks = formatBooksData(booksArray);
      setBooks(formattedBooks);
      calculateStatistics(formattedBooks);
      
    } catch (error) {
      console.error('Error loading books:', error);
      setApiError(`Không thể tải dữ liệu sách: ${error.message}`);
      const fallbackBooks = bookService.getFallbackBooks ? bookService.getFallbackBooks() : [];
      setBooks(fallbackBooks);
      calculateStatistics(fallbackBooks);
    } finally {
      setLoadingBooks(false);
    }
  };

  const formatBooksData = (booksData) => {
    const booksArray = Array.isArray(booksData) ? booksData : [];
    
    return booksArray.map((book, index) => {
      return {
        id: book.id || index + 1,
        title: book.title || `Sách ${index + 1}`,
        author: book.author || "Không rõ tác giả",
        category: book.categories?.[0] || book.department || book.type || "Chung",
        rating: book.averageRating || book.rating || 4.5 + (Math.random() * 0.5),
        reads: book.viewCount || book.reads || Math.floor(Math.random() * 2000) + 500,
        cover: bookService.processImageUrl ? bookService.processImageUrl(book.mainCoverImageUrl || book.coverImageUrl) : 
               `https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&h=500&fit=crop&v=${index}`,
        availableOnline: book.canReadOnline || 
                        (book.accessType === 'FULL_DIGITAL' || book.accessType === 'HYBRID') ||
                         false,
        availablePhysical: book.canBorrowPhysical || 
                          (book.availablePhysicalCopies > 0 && 
                           (book.accessType === 'PHYSICAL_ONLY' || book.accessType === 'HYBRID')) ||
                          false,
        physicalCopies: book.totalPhysicalCopies || book.physicalCopies || 0,
        availablePhysicalCopies: book.availablePhysicalCopies || 0,
        borrowedCopies: (book.totalPhysicalCopies || 0) - (book.availablePhysicalCopies || 0),
        pdfFileUrl: book.pdfFileUrl ? bookService.processPdfUrl ? bookService.processPdfUrl(book.pdfFileUrl) : book.pdfFileUrl : null,
        description: book.description || `Mô tả cho sách "${book.title || `Sách ${index + 1}`}"`,
        publicationYear: book.publicationYear,
        pages: book.pages,
        publisher: book.publisher,
        department: book.department,
        mainCoverImageUrl: book.mainCoverImageUrl || book.coverImageUrl
      };
    });
  };

  const calculateStatistics = (booksList) => {
    const totalBooks = booksList.length;
    const onlineBooks = booksList.filter(book => book.availableOnline).length;
    const physicalBooks = booksList.filter(book => book.availablePhysical).length;
    
    setStats(prev => ({
      ...prev,
      totalBooks: totalBooks.toLocaleString('vi-VN'),
      onlineBooks: onlineBooks.toLocaleString('vi-VN'),
      physicalBooks: physicalBooks.toLocaleString('vi-VN')
    }));
  };

  const handleSearch = (e) => {
    if (e) e.preventDefault();
    loadBooks(searchQuery);
  };

  const handleReserveBook = (bookId) => {
    const book = books.find(b => b.id === bookId);
    if (book) {
      setSelectedReserveBook(book);
      setShowReserveModal(true);
    }
  };

const handleSubmitReservation = async (pickupDate, notes) => {
  if (!selectedReserveBook) {
    alert('Không có sách được chọn');
    return;
  }
  setReserving(true);
  try {
    const result = await reservationService.createReservation(
      selectedReserveBook.id,  // ✅ bookId
      pickupDate,
      notes
    );
    alert(`✅ Đặt lịch thành công! Sách "${selectedReserveBook.title}" sẽ được giữ đến ${new Date(result.expiryDate).toLocaleDateString('vi-VN')}`);
    setShowReserveModal(false);
    setSelectedReserveBook(null);
    await loadBooks();
  } catch (error) {
    alert('❌ Đặt lịch thất bại: ' + error.message);
  } finally {
    setReserving(false);
  }
};

  const handleReadOnline = (bookId) => {
    const book = books.find(b => b.id === bookId);
    if (book && book.pdfFileUrl) {
      window.open(book.pdfFileUrl, '_blank');
    } else if (book && book.availableOnline) {
      alert(`Sách "${book.title}" có sẵn để đọc online nhưng chưa có file PDF`);
    } else {
      alert('Sách này không có sẵn để đọc online');
    }
  };

  const handleViewDetail = (bookId) => {
    navigate(`/book/${bookId}`);
  };

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner">
          <div className="spinner-ring"></div>
          <div className="loading-logo-fallback">HUTECH</div>
        </div>
        <p className="loading-text">Đang tải thư viện HUTECH...</p>
      </div>
    );
  }

  return (
    <div className={`dashboard ${darkMode ? 'dark' : ''}`}>
      <Navbar
        user={user}
        onLogout={handleLogout}
        darkMode={darkMode}
        toggleDarkMode={toggleDarkMode}
        toggleLanguage={toggleLanguage}
        language={language}
        onAvatarClick={handleAvatarClick}
      />

      <HeroSection stats={stats} />

      <main className="main-content">
        <SearchBar 
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          onSearch={handleSearch}
        />
        
        <BookFilters 
          selectedFilter={selectedFilter}
          onFilterChange={setSelectedFilter}
        />

        {apiError && (
          <div className="api-error-message">
            {apiError}
          </div>
        )}

        <BookGrid 
          books={books}
          loading={loadingBooks}
          selectedFilter={selectedFilter}
          onReadOnline={handleReadOnline}
          onViewDetail={handleViewDetail}
          onReserve={handleReserveBook}
        />

        <StatsSection stats={stats} />
        <CategoriesSection />
        <EventsSection />
      </main>

      <ReservationModal
        isOpen={showReserveModal}
        onClose={() => setShowReserveModal(false)}
        book={selectedReserveBook}
        onSubmit={handleSubmitReservation}
        isSubmitting={reserving}
        availableDates={availableDates}
      />

      <Footer />
    </div>
  );
};

export default Dashboard;
