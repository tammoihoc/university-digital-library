import React from 'react';
import './HeroSection.css';

const HeroSection = ({ stats }) => {
  return (
    <section className="hero-section">
      <div className="hero-container">
        <div className="hero-content">
          <h2 className="hero-title">Thư Viện Số HUTECH</h2>
          <p className="hero-subtitle">Kho tri thức không giới hạn cho sinh viên thời đại 4.0</p>
          <div className="hero-stats">
            <div className="stat-item">
              <span className="stat-number">{stats.totalBooks}</span>
              <span className="stat-label">ĐẦU SÁCH</span>
            </div>
            <div className="stat-divider"></div>
            <div className="stat-item">
              <span className="stat-number">{stats.onlineBooks}</span>
              <span className="stat-label">SÁCH ONLINE</span>
            </div>
            <div className="stat-divider"></div>
            <div className="stat-item">
              <span className="stat-number">{stats.activeUsers}</span>
              <span className="stat-label">SINH VIÊN ONLINE</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default HeroSection;
