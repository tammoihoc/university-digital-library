import React from 'react';
import { Search, Plus, Edit, Trash2, Eye } from 'lucide-react';
import bookService from '../../../services/bookService';

const BooksTab = ({ books, searchTerm, setSearchTerm, onAdd, onEdit, onDelete }) => {

  // Xử lý mở PDF với token
  const handleViewPdf = async (bookId) => {
    if (!bookId) {
      alert('Không có ID sách');
      return;
    }
    try {
      const blob = await bookService.getPdfBlob(bookId);
      const url = window.URL.createObjectURL(blob);
      window.open(url, '_blank');
      setTimeout(() => window.URL.revokeObjectURL(url), 60000);
    } catch (error) {
      alert('Không thể mở PDF: ' + error.message);
    }
  };

  return (
    <div>
      <div className="ld-toolbar">
        <div className="ld-search-box">
          <Search size={18} className="ld-search-icon" />
          <input
            type="text"
            placeholder="Tìm theo tên sách, tác giả, ISBN..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <button className="ld-btn-primary" onClick={onAdd}>
          <Plus size={18} /> Thêm sách
        </button>
      </div>

      <div className="ld-books-grid">
        {books.map((book) => {
          const coverUrl = bookService.processImageUrl(book.mainCoverImageUrl || book.coverImageUrl);
          return (
            <div key={book.id} className="ld-book-card">
              <div className="ld-book-cover">
                <img
                  src={coverUrl}
                  alt={book.title}
                  onError={(e) => {
                    e.target.src = 'https://via.placeholder.com/300x200?text=No+Cover';
                  }}
                />
                <div className="ld-book-badges">
                  <span
                    className={`ld-book-badge ${
                      book.availablePhysicalCopies > 0 ? 'badge-available' : 'badge-unavailable'
                    }`}
                  >
                    {book.availablePhysicalCopies > 0
                      ? `📖 Còn ${book.availablePhysicalCopies}`
                      : '🔒 Hết sách'}
                  </span>
                  {book.pdfFileUrl && <span className="ld-book-badge badge-pdf">📄 PDF</span>}
                </div>
              </div>
              <div className="ld-book-info">
                <h3 className="ld-book-title">{book.title}</h3>
                <p className="ld-book-author">{book.author}</p>
                <div className="ld-book-meta">
                  <span>📚 {book.categories?.[0] || 'Chưa phân loại'}</span>
                  <span>📅 {book.publicationYear || 'N/A'}</span>
                  <span>🏢 {book.libraryBranchId === 1 ? 'Cơ sở B' : 'Cơ sở E'}</span>
                </div>
                <div className="ld-book-stats">
                  <div className="stat">
                    <span className="label">Tổng</span>
                    <span className="value">{book.totalPhysicalCopies || 0}</span>
                  </div>
                  <div className="stat">
                    <span className="label">Còn</span>
                    <span
                      className="value"
                      style={{ color: book.availablePhysicalCopies > 0 ? '#10b981' : '#ef4444' }}
                    >
                      {book.availablePhysicalCopies || 0}
                    </span>
                  </div>
                  <div className="stat">
                    <span className="label">Đã mượn</span>
                    <span className="value">
                      {(book.totalPhysicalCopies || 0) - (book.availablePhysicalCopies || 0)}
                    </span>
                  </div>
                </div>
                <div className="ld-book-actions">
                  <button className="ld-action-btn edit" onClick={() => onEdit(book)}>
                    <Edit size={14} /> Sửa
                  </button>
                  <button className="ld-action-btn delete" onClick={() => onDelete(book)}>
                    <Trash2 size={14} /> Xóa
                  </button>
                  {/* ✅ PDF với token */}
                  {book.pdfFileUrl && (
                    <button
                      className="ld-action-btn pdf"
                      onClick={() => handleViewPdf(book.id)}
                    >
                      <Eye size={14} /> PDF
                    </button>
                  )}
                </div>
              </div>
            </div>
          );
        })}
        {books.length === 0 && <div className="ld-empty-state">Không tìm thấy sách nào</div>}
      </div>
    </div>
  );
};

export default BooksTab;
