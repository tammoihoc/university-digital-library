// src/components/books/BookGrid.jsx
import React from 'react';
import BookCard from './BookCard';
import { BookOpen } from 'lucide-react';
import './BookGrid.css';

const BookGrid = ({ 
  books, 
  loading, 
  selectedFilter, 
  onBorrow, 
  onReadOnline,
  onViewDetail,
  onReserve  // Thêm prop onReserve
}) => {
  const filteredBooks = books.filter(book => {
    if (selectedFilter === 'all') return true;
    if (selectedFilter === 'online' && book.availableOnline) return true;
    if (selectedFilter === 'physical' && book.availablePhysical) return true;
    const availableCount = (book.physicalCopies || 0) - (book.borrowedCopies || 0);
    if (selectedFilter === 'available' && availableCount > 0) return true;
    if (selectedFilter === 'borrowed' && (book.borrowedCopies || 0) > 0) return true;
    if (selectedFilter === 'programming' && 
        (book.category?.includes('Lập trình') || 
         book.category?.includes('Programming') ||
         book.department === 'Công nghệ thông tin')) return true;
    if (selectedFilter === 'science' && 
        (book.category?.includes('Khoa học') || 
         book.category?.includes('Science') ||
         book.department === 'Khoa học')) return true;
    return false;
  });

  if (loading) {
    return (
      <div className="books-loading">
        <div className="books-spinner"></div>
        <p>Đang tải danh sách sách...</p>
      </div>
    );
  }

  if (filteredBooks.length === 0) {
    return (
      <div className="empty-state">
        <BookOpen size={64} className="empty-icon" />
        <h3>Không tìm thấy sách phù hợp</h3>
        <p>Thử tìm kiếm với từ khóa khác hoặc chọn bộ lọc khác</p>
      </div>
    );
  }

  return (
    <div className="books-grid">
      {filteredBooks.map((book) => (
        <BookCard
          key={book.id}
          book={book}
          onBorrow={onBorrow}
          onReadOnline={onReadOnline}
          onViewDetail={onViewDetail}
          onReserve={onReserve}  // Truyền onReserve
        />
      ))}
    </div>
  );
};

export default BookGrid;
