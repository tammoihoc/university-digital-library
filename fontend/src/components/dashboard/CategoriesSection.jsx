import React from 'react';
import './CategoriesSection.css';

const CategoriesSection = () => {
  const categories = [
    { name: 'Lập trình', count: '4,231', color: 'category-1' },
    { name: 'Khoa học máy tính', count: '3,456', color: 'category-2' },
    { name: 'AI & Machine Learning', count: '1,890', color: 'category-3' },
    { name: 'Khoa học dữ liệu', count: '2,345', color: 'category-4' },
    { name: 'Kỹ thuật phần mềm', count: '3,123', color: 'category-5' },
    { name: 'Mạng máy tính', count: '1,567', color: 'category-6' },
  ];

  return (
    <section className="categories-section">
      <h2 className="section-title">Thể loại nổi bật</h2>
      <div className="categories-grid">
        {categories.map((cat, index) => (
          <div key={index} className={`category-card ${cat.color}`}>
            <h3>{cat.name}</h3>
            <p>{cat.count} sách</p>
            <button className="explore-btn">Khám phá →</button>
          </div>
        ))}
      </div>
    </section>
  );
};

export default CategoriesSection;
