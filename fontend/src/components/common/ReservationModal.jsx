// src/components/common/ReservationModal.jsx
import React, { useState, useEffect } from 'react';
import { X, Calendar, BookOpen, User, AlertCircle, CheckCircle, Clock } from 'lucide-react';
import './ReservationModal.css';

const ReservationModal = ({
  isOpen,
  onClose,
  book,
  onSubmit,
  isSubmitting = false,
  availableDates = []
}) => {
  const [pickupDate, setPickupDate] = useState('');
  const [notes, setNotes] = useState('');
  const [selectedDate, setSelectedDate] = useState(null);

  // Tạo danh sách ngày nếu chưa có
  const getDefaultDates = () => {
    const dates = [];
    const today = new Date();
    for (let i = 1; i <= 7; i++) {
      const date = new Date(today);
      date.setDate(today.getDate() + i);
      dates.push({
        value: date.toISOString().split('T')[0],
        label: `Ngày ${date.getDate()}/${date.getMonth() + 1}/${date.getFullYear()}`,
        day: date.toLocaleDateString('vi-VN', { weekday: 'long' })
      });
    }
    return dates;
  };

  const dates = availableDates.length > 0 ? availableDates : getDefaultDates();

  useEffect(() => {
    if (isOpen && dates.length > 0) {
      setPickupDate(dates[0].value);
      setSelectedDate(dates[0]);
    }
  }, [isOpen, dates]);

  const handleDateSelect = (date) => {
    setPickupDate(date.value);
    setSelectedDate(date);
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!pickupDate) {
      alert('Vui lòng chọn ngày nhận sách');
      return;
    }
    onSubmit(pickupDate, notes);
  };

  if (!isOpen || !book) return null;

  return (
    <div className="rm-overlay" onClick={onClose}>
      <div className="rm-modal" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="rm-header">
          <div className="rm-header-icon">
            <Calendar size={24} />
          </div>
          <h3 className="rm-title">📅 Đặt lịch mượn sách</h3>
          <button className="rm-close-btn" onClick={onClose}>
            <X size={22} />
          </button>
        </div>

        {/* Body */}
        <div className="rm-body">
          {/* Book Info */}
          <div className="rm-book-info">
            <div className="rm-book-cover-wrapper">
              <img
                src={book.cover || book.mainCoverImageUrl || 'https://via.placeholder.com/80x100?text=No+Cover'}
                alt={book.title}
                className="rm-book-cover"
                onError={(e) => {
                  e.target.src = 'https://via.placeholder.com/80x100?text=No+Cover';
                }}
              />
            </div>
            <div className="rm-book-details">
              <h4 className="rm-book-title">{book.title}</h4>
              <p className="rm-book-author">
                <User size={14} />
                <span>{book.author}</span>
              </p>
              <div className="rm-book-availability">
                <span className="rm-availability-badge available">
                  ✅ Còn {book.availablePhysicalCopies || book.physicalCopies - book.borrowedCopies || 0} bản
                </span>
              </div>
            </div>
          </div>

          {/* Pickup Date */}
          <div className="rm-form-group">
            <label className="rm-label">
              <Calendar size={16} />
              <span>Ngày dự kiến đến nhận <span className="rm-required">*</span></span>
            </label>
            <div className="rm-date-grid">
              {dates.map((date) => (
                <button
                  key={date.value}
                  type="button"
                  className={`rm-date-btn ${pickupDate === date.value ? 'active' : ''}`}
                  onClick={() => handleDateSelect(date)}
                >
                  <span className="rm-date-day">{date.day || 'Ngày'}</span>
                  <span className="rm-date-number">{date.label}</span>
                </button>
              ))}
            </div>
            <small className="rm-hint">
              <Clock size={14} />
              Bạn có 7 ngày để đến nhận sách kể từ ngày đặt. Quá hạn sẽ tự động hủy.
            </small>
          </div>

          {/* Notes */}
          <div className="rm-form-group">
            <label className="rm-label">
              <AlertCircle size={16} />
              <span>Ghi chú (tùy chọn)</span>
            </label>
            <textarea
              className="rm-textarea"
              rows="3"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Nhập ghi chú nếu có (ví dụ: thời gian dự kiến đến, yêu cầu đặc biệt...)"
            />
          </div>

          {/* Info Box */}
          <div className="rm-info-box">
            <div className="rm-info-icon">
              <CheckCircle size={18} />
            </div>
            <div className="rm-info-content">
              <p className="rm-info-title">📌 Lưu ý quan trọng:</p>
              <ul className="rm-info-list">
                <li>Đặt lịch thành công sẽ <strong>tự động giữ sách</strong> cho bạn trong <strong>7 ngày</strong></li>
                <li>Vui lòng đến thư viện trong vòng <strong>7 ngày</strong> kể từ ngày đặt</li>
                <li>Quá 7 ngày không đến nhận, đặt lịch sẽ <strong>tự động hủy</strong></li>
                <li>Khi đến nhận, vui lòng thông báo mã đặt lịch cho thủ thư</li>
                <li>Bạn có thể hủy đặt lịch bất kỳ lúc nào trước khi hết hạn</li>
              </ul>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="rm-footer">
          <button className="rm-btn rm-btn-secondary" onClick={onClose} type="button">
            Hủy
          </button>
          <button
            className="rm-btn rm-btn-primary"
            onClick={handleSubmit}
            disabled={isSubmitting || !pickupDate}
          >
            {isSubmitting ? (
              <>
                <span className="rm-spinner"></span>
                Đang xử lý...
              </>
            ) : (
              <>
                <Calendar size={18} />
                Xác nhận đặt lịch
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ReservationModal;
