// src/pages/book/BookDetail.jsx
import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../../components/common/Navbar';
import Footer from '../../components/common/Footer';
import PDFViewer from '../../components/books/PDFViewer';
import ReservationModal from '../../components/common/ReservationModal';
import bookService from '../../services/bookService';
import authService from '../../services/authService';
import userService from '../../services/userService';
import reservationService from '../../services/reservationService';
import {
  ArrowLeft,
  BookOpen,
  Calendar,
  User,
  MapPin,
  Globe,
  Download,
  Eye,
  Star,
  Heart,
  Share2,
  BookMarked,
  Printer,
  FileText,
  ExternalLink,
  CalendarCheck
} from 'lucide-react';
import './BookDetail.css';

const BookDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [isFavorite, setIsFavorite] = useState(false);
  const [showPdfEmbed, setShowPdfEmbed] = useState(false);
  const [pdfBlobUrl, setPdfBlobUrl] = useState('');
  const [pdfLoading, setPdfLoading] = useState(false);
  const [zoomLevel, setZoomLevel] = useState(100);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [user, setUser] = useState(null);
  const [darkMode, setDarkMode] = useState(false);
  const [language, setLanguage] = useState('vi');
  const [showReserveModal, setShowReserveModal] = useState(false);
  const [reserving, setReserving] = useState(false);

  const getAvailablePickupDates = () => {
    const dates = [];
    const today = new Date();
    for (let i = 1; i <= 7; i++) {
      const date = new Date(today);
      date.setDate(today.getDate() + i);
      dates.push({
        value: date.toISOString().split('T')[0],
        label: `Ngày ${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`,
        day: date.toLocaleDateString('vi-VN', { weekday: 'long' })
      });
    }
    return dates;
  };

  const availableDates = getAvailablePickupDates();

  useEffect(() => {
    loadUserData();
    loadBookDetails();
  }, [id]);

  useEffect(() => {
    return () => {
      if (pdfBlobUrl) {
        URL.revokeObjectURL(pdfBlobUrl);
      }
    };
  }, [pdfBlobUrl]);

  const loadUserData = async () => {
    const storedUser = authService.getUser();
    if (storedUser) {
      setUser(storedUser);
    }
  };

  const loadBookDetails = async () => {
    try {
      setLoading(true);
      setError('');
      console.log(`Loading book details for ID: ${id}`);
      const data = await bookService.getBookById(id);
      console.log('Book data received:', data);

      // Xử lý ảnh bìa
      if (data) {
        let coverUrl = data.mainCoverImageUrl || data.coverImageUrl || null;
        if (coverUrl) {
          coverUrl = bookService.processImageUrl(coverUrl);
        } else {
          coverUrl = `https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&h=500&fit=crop&random=${data.id || 1}`;
        }
        data.cover = coverUrl;
        data.category = data.categories?.[0] || data.department || 'Chung';
        data.physicalCopies = data.totalPhysicalCopies || 0;
        data.borrowedCopies = (data.totalPhysicalCopies || 0) - (data.availablePhysicalCopies || 0);
        data.reads = data.viewCount || 0;
        data.rating = data.averageRating || 4.5;
      }

      setBook(data);
      setLoading(false);
    } catch (err) {
      console.error('Error loading book details:', err);
      setError('Không thể tải thông tin sách: ' + err.message);
      setLoading(false);
    }
  };

  const handleClosePdf = () => {
    setShowPdfEmbed(false);
    if (pdfBlobUrl) {
      URL.revokeObjectURL(pdfBlobUrl);
      setPdfBlobUrl('');
    }
  };

  const handleTogglePdfView = async () => {
    if (!book?.pdfFileUrl) {
      alert('Sách này chưa có file PDF để đọc online.');
      return;
    }
    if (showPdfEmbed) {
      handleClosePdf();
    } else {
      try {
        setPdfLoading(true);
        const blobUrl = await bookService.getPdfBlobUrl(id);
        setPdfBlobUrl(blobUrl);
        setShowPdfEmbed(true);
        setTimeout(() => {
          document.getElementById('pdf-viewer-section')?.scrollIntoView({
            behavior: 'smooth',
            block: 'start'
          });
        }, 100);
      } catch (error) {
        alert('Lỗi khi tải PDF: ' + error.message);
      } finally {
        setPdfLoading(false);
      }
    }
  };

  const handleReadOnline = async () => {
    if (!book?.pdfFileUrl) {
      alert('Sách này chưa có file PDF để đọc online.');
      return;
    }
    try {
      setPdfLoading(true);
      const blobUrl = await bookService.getPdfBlobUrl(id);
      const newWindow = window.open('', '_blank');
      if (newWindow) {
        newWindow.document.write(`
          <!DOCTYPE html>
          <html>
          <head>
            <title>${book.title} - PDF Viewer</title>
            <style>
              body { margin: 0; padding: 0; }
              embed { width: 100%; height: 100vh; }
            </style>
          </head>
          <body>
            <embed src="${blobUrl}#toolbar=0" type="application/pdf" />
          </body>
          </html>
        `);
        newWindow.document.close();
        newWindow.onbeforeunload = () => {
          URL.revokeObjectURL(blobUrl);
        };
      }
    } catch (error) {
      alert('Lỗi khi mở PDF: ' + error.message);
    } finally {
      setPdfLoading(false);
    }
  };

  const handleDownloadPdf = async () => {
    if (!book?.pdfFileUrl) {
      alert('Không có file PDF để tải về.');
      return;
    }
    try {
      setPdfLoading(true);
      const blob = await bookService.getPdfBlob(id);
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `${book.title.replace(/[^a-z0-9]/gi, '_')}.pdf`;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      setTimeout(() => {
        window.URL.revokeObjectURL(url);
      }, 100);
    } catch (error) {
      alert('Lỗi khi tải PDF: ' + error.message);
    } finally {
      setPdfLoading(false);
    }
  };

  const handleReserveBook = () => {
    setShowReserveModal(true);
  };

const handleSubmitReservation = async (pickupDate, notes) => {
  setReserving(true);
  try {
    const result = await reservationService.createReservation(parseInt(id), pickupDate, notes);
    alert(`✅ Đặt lịch thành công! Sách sẽ được giữ đến ${new Date(result.expiryDate).toLocaleDateString('vi-VN')}`);
    setShowReserveModal(false);
  } catch (error) {
    alert('❌ Đặt lịch thất bại: ' + error.message);
  } finally {
    setReserving(false);
  }
};

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  if (loading) {
    return (
      <div className="loading-container">
        <div className="loading-spinner"></div>
        <p>Đang tải thông tin sách...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-container">
        <p>{error}</p>
        <button onClick={() => navigate('/dashboard')} className="back-button">
          Quay lại Dashboard
        </button>
      </div>
    );
  }

  if (!book) {
    return (
      <div className="error-container">
        <p>Không tìm thấy sách với ID: {id}</p>
        <button onClick={() => navigate('/dashboard')} className="back-button">
          Quay lại Dashboard
        </button>
      </div>
    );
  }

  return (
    <div className={`dashboard ${darkMode ? 'dark' : ''}`}>
      <Navbar
        user={user}
        onLogout={handleLogout}
        darkMode={darkMode}
        toggleDarkMode={() => setDarkMode(!darkMode)}
        toggleLanguage={() => setLanguage(language === 'vi' ? 'en' : 'vi')}
        language={language}
        onAvatarClick={() => navigate('/profile')}
      />

      <div className="book-detail-container">
        {/* Navigation */}
        <div className="book-detail-nav">
          <button onClick={() => navigate('/dashboard')} className="nav-back">
            <ArrowLeft size={20} />
            <span>Quay lại Dashboard</span>
          </button>
          <div className="nav-actions">
            <button
              className={`favorite-btn ${isFavorite ? 'active' : ''}`}
              onClick={() => setIsFavorite(!isFavorite)}
            >
              <Heart size={18} fill={isFavorite ? 'currentColor' : 'none'} />
            </button>
            <button className="share-btn">
              <Share2 size={18} />
            </button>
            <button className="print-btn">
              <Printer size={18} />
            </button>
          </div>
        </div>

        {/* Main Content */}
        <div className="book-detail-content">
          {/* Left Column - Images */}
          <div className="book-images-section">
            <div className="main-image-container">
              <img
                src={book.cover || book.mainCoverImageUrl || book.coverImageUrl}
                alt={book.title}
                className="main-image"
                onError={(e) => {
                  e.target.src =
                    'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&h=500&fit=crop';
                }}
              />
            </div>
          </div>

          {/* Right Column - Book Info */}
          <div className="book-info-section">
            <div className="book-header">
              <div className="book-category-badge">
                <BookMarked size={16} />
                <span>{book.category || 'Chưa phân loại'}</span>
              </div>
              <h1 className="book-title">{book.title}</h1>
              <h2 className="book-author">
                <User size={18} />
                <span>{book.author}</span>
              </h2>
              <div className="book-rating">
                <div className="stars">
                  {[...Array(5)].map((_, i) => (
                    <Star
                      key={i}
                      size={18}
                      fill={i < Math.floor(book.rating || 0) ? '#FFD700' : 'none'}
                    />
                  ))}
                </div>
                <span className="rating-text">
                  {book.rating?.toFixed(1) || '0.0'} ({book.reads || 0} lượt đọc)
                </span>
              </div>
            </div>

            <div className="book-meta">
              <div className="meta-item">
                <Calendar size={16} />
                <span>Năm xuất bản: {book.publicationYear || 'Không rõ'}</span>
              </div>
              <div className="meta-item">
                <FileText size={16} />
                <span>Số trang: {book.pages || 'Không rõ'}</span>
              </div>
              <div className="meta-item">
                <Globe size={16} />
                <span>Ngôn ngữ: {book.language || 'Tiếng Việt'}</span>
              </div>
              <div className="meta-item">
                <MapPin size={16} />
                <span>Nhà xuất bản: {book.publisher || 'Không rõ'}</span>
              </div>
            </div>

            <div className="book-description">
              <h3>📖 Mô tả sách</h3>
              <p>{book.description || 'Chưa có mô tả cho sách này.'}</p>
            </div>

            {/* Availability */}
            <div className="availability-status">
              <div
                className={`status-badge ${
                  book.availablePhysicalCopies > 0 ? 'available' : 'unavailable'
                }`}
              >
                {book.availablePhysicalCopies > 0 ? '🟢 Có sẵn' : '🔴 Hết sách'}
              </div>
              <span className="copies-count">
                {book.availablePhysicalCopies || 0} / {book.physicalCopies || 0} bản có sẵn
              </span>
            </div>

            {/* Action Buttons */}
            <div className="action-buttons">
              {book.physicalCopies > book.borrowedCopies && (
                <button className="btn-primary reserve-detail" onClick={handleReserveBook}>
                  <CalendarCheck size={20} />
                  <span>Đặt lịch mượn</span>
                </button>
              )}
              {book.pdfFileUrl ? (
                <>
                  <button
                    className="btn-primary"
                    onClick={handleTogglePdfView}
                    disabled={pdfLoading}
                  >
                    {pdfLoading ? (
                      <>
                        <span className="spinner"></span> Đang tải...
                      </>
                    ) : (
                      <>
                        <BookOpen size={20} />
                        <span>{showPdfEmbed ? 'Ẩn PDF' : 'Xem PDF ngay trên trang'}</span>
                      </>
                    )}
                  </button>

                  <button
                    className="btn-secondary"
                    onClick={handleReadOnline}
                    disabled={pdfLoading}
                  >
                    <ExternalLink size={20} />
                    <span>Mở tab mới</span>
                  </button>

                  <div
                    className="pdf-info"
                    style={{ marginTop: '10px', fontSize: '12px', color: '#666' }}
                  >
                    <p>
                      💡 <strong>Mẹo:</strong> Nhấn "Đọc online" để mở PDF trong tab mới. Nếu không
                      hiển thị, hãy tải về và mở bằng trình đọc PDF trên máy.
                    </p>
                  </div>

                  <button
                    className="btn-outline"
                    onClick={handleDownloadPdf}
                    disabled={pdfLoading}
                  >
                    <Download size={20} />
                    <span>Tải về máy</span>
                  </button>
                </>
              ) : (
                <button className="btn-primary borrow-btn">
                  <BookMarked size={20} />
                  <span>Mượn sách</span>
                </button>
              )}
            </div>
          </div>
        </div>

        {/* PDF Viewer */}
        {showPdfEmbed && pdfBlobUrl && (
          <PDFViewer book={book} pdfUrl={pdfBlobUrl} onClose={handleClosePdf} />
        )}
      </div>

      <ReservationModal
        isOpen={showReserveModal}
        onClose={() => setShowReserveModal(false)}
        book={book}
        onSubmit={handleSubmitReservation}
        isSubmitting={reserving}
        availableDates={availableDates}
      />

      <Footer />
    </div>
  );
};

export default BookDetail;
