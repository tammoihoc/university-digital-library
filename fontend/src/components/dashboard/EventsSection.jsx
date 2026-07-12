import React from 'react';
import { Calendar } from 'lucide-react';
import './EventsSection.css';

const EventsSection = () => {
  const events = [
    { title: 'Workshop: Clean Code', date: '15/12/2024', speaker: 'TS. Nguyễn Văn B' },
    { title: 'Seminar: AI in Education', date: '20/12/2024', speaker: 'PGS.TS. Trần Thị C' },
    { title: 'Book Launch: Digital Transformation', date: '25/12/2024', speaker: 'Nhà xuất bản HUTECH' },
  ];

  return (
    <section className="events-section">
      <div className="section-header">
        <h2 className="section-title">Sự kiện sắp tới</h2>
        <button className="view-all-btn">Xem tất cả →</button>
      </div>
      <div className="events-list">
        {events.map((event, index) => (
          <div key={index} className="event-card">
            <Calendar className="event-icon" />
            <div className="event-content">
              <h3>{event.title}</h3>
              <p className="event-date">{event.date}</p>
              <p className="event-speaker">Diễn giả: {event.speaker}</p>
            </div>
            <button className="register-btn">Đăng ký</button>
          </div>
        ))}
      </div>
    </section>
  );
};

export default EventsSection;
