import React from 'react';
import { Filter, BookOpen, Eye, BookText, BookMarked, Users, Library } from 'lucide-react';
import './BookFilters.css';

const BookFilters = ({ selectedFilter, onFilterChange }) => {
  const filters = [
    { id: 'all', label: 'Tất cả sách', icon: BookOpen },
    { id: 'online', label: 'Đọc online được', icon: Eye },
    { id: 'physical', label: 'Sách vật lý', icon: BookText },
    { id: 'available', label: 'Có sẵn', icon: BookMarked },
    { id: 'borrowed', label: 'Đã mượn', icon: Users },
    { id: 'programming', label: 'Lập trình', icon: BookOpen },
    { id: 'science', label: 'Khoa học', icon: Library },
  ];

  return (
    <div className="filter-container">
      <div className="filter-header">
        <Filter size={20} />
        <h3>Lọc sách</h3>
      </div>
      <div className="filter-grid">
        {filters.map((filter) => {
          const Icon = filter.icon;
          return (
            <button
              key={filter.id}
              className={`filter-btn ${selectedFilter === filter.id ? 'active' : ''}`}
              onClick={() => onFilterChange(filter.id)}
            >
              <Icon size={18} />
              <span>{filter.label}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default BookFilters;
