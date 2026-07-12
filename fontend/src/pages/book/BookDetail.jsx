// src/pages/book/BookDetail.jsx - CHỈ SỬA PHẦN QUAN TRỌNG
import React, { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Navbar from '../../components/common/Navbar';
import Footer from '../../components/common/Footer';
import PDFViewer from '../../components/books/PDFViewer';
import bookService from '../../services/bookService';
import authService from '../../services/authService.jsx';
import userService from '../../services/userService.jsx';
import { 
  ArrowLeft, BookOpen, Calendar, User, MapPin, 
  Globe, Download, Eye, Star, Heart, Share2,
  BookMarked, Printer, FileText, ExternalLink,
  Maximize2, Minimize2, ZoomIn, ZoomOut, CalendarCheck  // THÊM NÀY
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
  const [pdfBlobUrl, setPdfBlobUrl] = useState(''); // THAY ĐỔI: dùng blobUrl
  const [pdfLoading, setPdfLoading] = useState(false); // THÊM: trạng thái loading PDF
  const [zoomLevel, setZoomLevel] = useState(100);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [user, setUser] = useState(null);
  const [darkMode, setDarkMode] = useState(false);
  const [language, setLanguage] = useState('vi');
const [showReserveModal, setShowReserveModal] = useState(false);
const [reservePickupDate, setReservePickupDate] = useState('');
const [reserveNotes, setReserveNotes] = useState('');
const [reserving, setReserving] = useState(false);
  useEffect(() => {
    loadUserData();
    loadBookDetails();
  }, [id]);

  // Cleanup blob URL khi component unmount
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
      
      setBook(data);
      setLoading(false);
    } catch (err) {
      console.error('Error loading book details:', err);
      setError('Không thể tải thông tin sách: ' + err.message);
      setLoading(false);
    }
  };

  // HÀM MỚI: Tải PDF dưới dạng blob
  const loadPdfForEmbed = async () => {
    try {
      setPdfLoading(true);
      console.log('Loading PDF with headers...');
      
      // Tải PDF dưới dạng blob (có headers tự động)
      const blobUrl = await bookService.getPdfBlobUrl(id);
      setPdfBlobUrl(blobUrl);
      
      // Hiển thị PDF viewer
      setShowPdfEmbed(true);
      
      // Scroll xuống phần PDF viewer
      setTimeout(() => {
        document.getElementById('pdf-viewer-section')?.scrollIntoView({ 
          behavior: 'smooth',
          block: 'start'
        });
      }, 100);
      
    } catch (err) {
      console.error('Error loading PDF:', err);
      alert('Không thể tải PDF: ' + err.message);
    } finally {
      setPdfLoading(false);
    }
  };

  // HÀM MỚI: Đóng PDF viewer và cleanup
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
    setShowPdfEmbed(false);
    if (pdfBlobUrl) {
      URL.revokeObjectURL(pdfBlobUrl);
      setPdfBlobUrl('');
    }
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


  // SỬA HÀM handleReadOnline: Dùng blob URL
const handleReadOnline = async () => {
  if (!book?.pdfFileUrl) {
    alert('Sách này chưa có file PDF để đọc online.');
    return;
  }
  
  try {
    setPdfLoading(true);
    const blobUrl = await bookService.getPdfBlobUrl(id);
    
    // Mở trong tab mới
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
      
      // Cleanup blob URL khi tab đóng
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
  const handleReserveBook = () => {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  setReservePickupDate(tomorrow.toISOString().split('T')[0]);
  setReserveNotes('');
  setShowReserveModal(true);
};

const handleSubmitReservation = async () => {
  if (!reservePickupDate) {
    alert('Vui lòng chọn ngày nhận sách');
    return;
  }
  
  try {
    setReserving(true);
    const response = await fetch('http://localhost:8080/api/reservations', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-User-Id': user?.username || 'student',
        'X-User-Type': 'STUDENT'
      },
      body: JSON.stringify({
        bookId: parseInt(id),
        pickupDate: reservePickupDate,
        notes: reserveNotes
      })
    });
    
    if (response.ok) {
      const result = await response.json();
      alert(`✅ Đã đặt lịch mượn sách "${book?.title}" thành công!`);
      setShowReserveModal(false);
    } else {
      const error = await response.text();
      throw new Error(error);
    }
  } catch (error) {
    console.error('Error reserving book:', error);
    alert('Đặt lịch thất bại: ' + error.message);
  } finally {
    setReserving(false);
  }
};
  // SỬA HÀM handleDownloadPdf: Dùng service
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
};  // ... (các hàm handleZoomIn, handleZoomOut, handleResetZoom, toggleFullscreen giữ nguyên)

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

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
        {loading ? (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Đang tải thông tin sách...</p>
          </div>
        ) : error ? (
          <div className="error-container">
            <p>{error}</p>
            <button onClick={() => navigate('/dashboard')} className="back-button">
              Quay lại Dashboard
            </button>
          </div>
        ) : book ? (
          <div className="book-detail-content">
            {/* Left Column - Images */}
            <div className="book-images-section">
              <div className="main-image-container">
                <img 
                  src={book?.cover || book?.mainCoverImageUrl || '/default-book-cover.jpg'} 
                  alt={book?.title}
                  className="main-image"
                  onError={(e) => {
                    e.target.src = 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&h=500&fit=crop';
                  }}
                />
              </div>
            </div>

            {/* Right Column - Book Info */}
            <div className="book-info-section">
              <div className="book-header">
                <div className="book-category-badge">
                  <BookMarked size={16} />
                  <span>{book?.category || 'Chưa phân loại'}</span>
                </div>
                
                <h1 className="book-title">{book?.title}</h1>
                <h2 className="book-author">
                  <User size={18} />
                  <span>{book?.author}</span>
                </h2>
                
                <div className="book-rating">
                  <div className="stars">
                    {[...Array(5)].map((_, i) => (
                      <Star 
                        key={i} 
                        size={18} 
                        fill={i < Math.floor(book?.rating || 0) ? '#FFD700' : 'none'}
                      />
                    ))}
                  </div>
                  <span className="rating-text">
                    {book?.rating?.toFixed(1) || '0.0'} ({book?.reads || 0} lượt đọc)
                  </span>
                </div>
              </div>

              <div className="book-meta">
                <div className="meta-item">
                  <Calendar size={16} />
                  <span>Năm xuất bản: {book?.publicationYear || 'Không rõ'}</span>
                </div>
                
                <div className="meta-item">
                  <FileText size={16} />
                  <span>Số trang: {book?.pages || 'Không rõ'}</span>
                </div>
                
                <div className="meta-item">
                  <Globe size={16} />
                  <span>Ngôn ngữ: {book?.language || 'Tiếng Việt'}</span>
                </div>
                
                <div className="meta-item">
                  <MapPin size={16} />
                  <span>Nhà xuất bản: {book?.publisher || 'Không rõ'}</span>
                </div>
              </div>

              <div className="book-description">
                <h3>📖 Mô tả sách</h3>
                <p>{book?.description || 'Chưa có mô tả cho sách này.'}</p>
              </div>

              {/* Availability Status */}
              <div className="availability-status">
                <div className={`status-badge ${book?.availablePhysicalCopies > 0 ? 'available' : 'unavailable'}`}>
                  {book?.availablePhysicalCopies > 0 ? '🟢 Có sẵn' : '🔴 Hết sách'}
                </div>
                <span className="copies-count">
                  {book?.availablePhysicalCopies || 0} / {book?.physicalCopies || 0} bản có sẵn
                </span>
              </div>

              {/* PDF Action Buttons - SỬA */}
              <div className="action-buttons">
                {book?.physicalCopies > book?.borrowedCopies && (
    <button className="btn-primary reserve-detail" onClick={handleReserveBook}>
      <CalendarCheck size={20} />
      <span>Đặt lịch mượn</span>
    </button>
  )}
                  {book?.pdfFileUrl ? (
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
                    
                    <div className="pdf-info" style={{ marginTop: '10px', fontSize: '12px', color: '#666' }}>
                      <p>💡 <strong>Mẹo:</strong> Nhấn "Đọc online" để mở PDF trong tab mới. 
                      Nếu không hiển thị, hãy tải về và mở bằng trình đọc PDF trên máy.</p>
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
        ) : (
          <div className="error-container">
            <p>Không tìm thấy sách với ID: {id}</p>
            <button onClick={() => navigate('/dashboard')} className="back-button">
              Quay lại Dashboard
            </button>
          </div>
        )}

        {/* PDF Viewer Section - SỬA: Truyền blobUrl */}
        {showPdfEmbed && pdfBlobUrl && (
          <PDFViewer
            book={book}
            pdfUrl={pdfBlobUrl} // Dùng blobUrl thay vì URL trực tiếp
            onClose={handleClosePdf}
          />
        )}
      </div>

      <Footer />
    </div>
  );
};

export default BookDetail;
