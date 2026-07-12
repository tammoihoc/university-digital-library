import React from 'react';
import { Search } from 'lucide-react';
import './SearchBar.css';

const SearchBar = ({ searchQuery, setSearchQuery, onSearch }) => {
  const handleSubmit = (e) => {
    e.preventDefault();
    onSearch(searchQuery);
  };

  return (
    <div className="search-filter-section">
      <div className="search-container">
        <Search className="search-icon" size={20} />
        <input
          type="text"
          placeholder="Tìm kiếm sách, tác giả, thể loại..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          onKeyPress={(e) => e.key === 'Enter' && handleSubmit(e)}
          className="search-input"
        />
        <button className="search-button" onClick={handleSubmit}>
          <Search size={18} />
          <span>Tìm kiếm</span>
        </button>
      </div>
    </div>
  );
};

export default SearchBar;
