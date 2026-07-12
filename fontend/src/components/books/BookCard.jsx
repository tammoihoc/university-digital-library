// src/components/books/BookCard.jsx
import React from 'react';
import { Book, Calendar, Star, CalendarCheck } from 'lucide-react';
import './BookCard.css';

const BookCard = ({ 
  book, 
  onViewDetail,
  onReserve  // Thêm prop onReserve
}) => {
  const availability = getBookAvailability(book);
  // Số lượng còn lại có thể mượn
  const availableCount = book.physicalCopies - (book.borrowedCopies || 0);

  const handleCardClick = () => {
    if (onViewDetail) {
      onViewDetail(book.id);
    }
  };

  const handleReserveClick = (e) => {
    e.stopPropagation(); // Ngăn chặn click vào card
    if (onReserve) {
      onReserve(book.id);
    }
  };

  return (
    <div className="book-card" onClick={handleCardClick} style={{ cursor: 'pointer' }}>
      <div className="book-cover">
        <img src={book.cover} alt={book.title} />
        <div className="book-badge">
          <span className={`badge ${availability.color}`}>
            {availability.label}
          </span>
          {book.availableOnline && (
            <span className="badge online">Online</span>
          )}
        </div>
        <div className="book-rating">
          <Star size={14} className="star-icon" />
          <span>{book.rating?.toFixed(1) || '0.0'}</span>
          <span className="reads">({book.reads || 0} lượt đọc)</span>
        </div>
      </div>
      
      <div className="book-content">
        <div className="book-category">{book.category}</div>
        <h3 className="book-title">{book.title}</h3>
        <p className="book-author">{book.author}</p>
        <p className="book-description">{book.description}</p>
        
        <div className="book-stats">
          <div className="stat">
            <Book size={14} />
            <span>{book.physicalCopies || 0} bản</span>
          </div>
          <div className="stat available-count">
            <CalendarCheck size={14} />
            <span className={availableCount > 0 ? 'available' : 'unavailable'}>
              {availableCount > 0 ? `${availableCount} bản còn` : 'Hết sách'}
            </span>
          </div>
          {book.publicationYear && (
            <div className="stat">
              <Calendar size={14} />
              <span>{book.publicationYear}</span>
            </div>
          )}
        </div>
        
        {/* Nút đặt lịch mượn */}
        <div className="book-actions">
          {book.availableOnline && (
            <button 
              className="btn read-online-btn"
              onClick={(e) => {
                e.stopPropagation();
                if (onViewDetail) onViewDetail(book.id);
              }}
            >
              <span>Đọc online</span>
            </button>
          )}
          {availableCount > 0 && (
            <button 
              className="btn reserve-btn"
              onClick={handleReserveClick}
            >
              <CalendarCheck size={14} />
              <span>Đặt lịch mượn</span>
            </button>
          )}
          {!book.availableOnline && availableCount === 0 && (
            <button className="btn unavailable-btn" disabled>
              <span>Hiện không có</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

// Helper function
const getBookAvailability = (book) => {
  if (book.availableOnline) return { type: 'online', label: 'Đọc online', color: 'green' };
  const availableCount = (book.physicalCopies || 0) - (book.borrowedCopies || 0);
  if (availableCount > 0) return { type: 'physical', label: 'Có sẵn', color: 'blue' };
  return { type: 'unavailable', label: 'Hết sách', color: 'red' };
};

export default BookCard;
