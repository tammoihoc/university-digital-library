// src/pages/librarian/tabs/LocationsTab.jsx
import React from 'react';
import { Search, MapPin } from 'lucide-react';

const LocationsTab = ({ books, searchTerm, setSearchTerm, onUpdateLocation }) => (
  <div>
    <div className="ld-toolbar">
      <div className="ld-search-box">
        <Search size={18} className="ld-search-icon" />
        <input
          type="text"
          placeholder="Tìm theo tên sách, tác giả..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>
    </div>

    <div className="ld-books-grid">
      {books.map((book) => (
        <div key={book.id} className="ld-book-card">
          <div className="ld-book-cover">
            <img
              src={
                book.mainCoverImageUrl
                  ? `http://localhost:8080${book.mainCoverImageUrl}`
                  : 'https://via.placeholder.com/300x200?text=No+Cover'
              }
              alt={book.title}
              onError={(e) => {
                e.target.src = 'https://via.placeholder.com/300x200?text=No+Cover';
              }}
            />
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
                <span className="label">Vị trí</span>
                <span className="value">
                  {book.zone ? `${book.zone}-${book.shelf}-${book.column}` : 'Chưa có'}
                </span>
              </div>
            </div>
            <div className="ld-book-actions">
              <button className="ld-action-btn edit" onClick={() => onUpdateLocation(book)}>
                <MapPin size={14} /> Gán vị trí
              </button>
            </div>
          </div>
        </div>
      ))}
      {books.length === 0 && <div className="ld-empty-state">Không tìm thấy sách nào</div>}
    </div>
  </div>
);

export default LocationsTab;
