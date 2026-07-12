import React from 'react';
import { BookOpen, Eye, Users, Clock } from 'lucide-react';
import './StatsSection.css';

const StatsSection = ({ stats }) => {
  return (
    <section className="quick-stats">
      <div className="stat-card">
        <div className="stat-icon">
          <BookOpen size={24} />
        </div>
        <div className="stat-content">
          <h3>{stats.totalBooks}</h3>
          <p>Tổng số sách</p>
        </div>
      </div>
      
      <div className="stat-card">
        <div className="stat-icon">
          <Eye size={24} />
        </div>
        <div className="stat-content">
          <h3>{stats.onlineBooks}</h3>
          <p>Sách đọc online</p>
        </div>
      </div>
      
      <div className="stat-card">
        <div className="stat-icon">
          <Users size={24} />
        </div>
        <div className="stat-content">
          <h3>{stats.activeUsers}</h3>
          <p>Sinh viên online</p>
        </div>
      </div>
      
      <div className="stat-card">
        <div className="stat-icon">
          <Clock size={24} />
        </div>
        <div className="stat-content">
          <h3>{stats.monthlyReads}</h3>
          <p>Lượt đọc/tháng</p>
        </div>
      </div>
    </section>
  );
};

export default StatsSection;
