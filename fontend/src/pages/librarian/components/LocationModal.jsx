// src/pages/librarian/components/LocationModal.jsx
import React from 'react';
import { X, MapPin } from 'lucide-react';

const LocationModal = ({ book, locationData, setLocationData, onSave, onClose }) => {
  return (
    <div className="ld-modal-overlay" onClick={onClose}>
      <div className="ld-modal" onClick={(e) => e.stopPropagation()}>
        <div className="ld-modal-header">
          <h3>📍 Vị trí sách: {book.title}</h3>
          <button className="ld-modal-close" onClick={onClose}>×</button>
        </div>
        <div className="ld-modal-body">
          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Chi nhánh</label>
              <select value={locationData.branchId} onChange={(e) => setLocationData({...locationData, branchId: e.target.value})}>
                <option value="1">🏢 Cơ sở B</option>
                <option value="2">🏫 Cơ sở E</option>
              </select>
            </div>
          </div>
          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Khu vực</label>
              <select value={locationData.zone} onChange={(e) => setLocationData({...locationData, zone: e.target.value})}>
                <option value="">Chọn khu</option>
                <option value="300">Khu 300 - Khoa học xã hội</option>
                <option value="400">Khu 400 - Ngôn ngữ</option>
                <option value="500">Khu 500 - Khoa học tự nhiên</option>
                <option value="600">Khu 600 - Công nghệ</option>
                <option value="700">Khu 700 - Nghệ thuật</option>
                <option value="800">Khu 800 - Văn học</option>
                <option value="900">Khu 900 - Lịch sử & Địa lý</option>
              </select>
            </div>
            <div className="ld-form-group">
              <label>Kệ số</label>
              <input type="number" placeholder="1-9" value={locationData.shelf} onChange={(e) => setLocationData({...locationData, shelf: e.target.value})} />
            </div>
          </div>
          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Cột số</label>
              <input type="number" placeholder="1-9" value={locationData.column} onChange={(e) => setLocationData({...locationData, column: e.target.value})} />
            </div>
            <div className="ld-form-group">
              <label>Hàng số</label>
              <input type="number" placeholder="1-9" value={locationData.row} onChange={(e) => setLocationData({...locationData, row: e.target.value})} />
            </div>
          </div>
          <div className="ld-form-group">
            <label>Vị trí trên kệ</label>
            <input type="number" placeholder="1-9" value={locationData.position} onChange={(e) => setLocationData({...locationData, position: e.target.value})} />
          </div>
          <div className="ld-location-preview">
            <strong>Vị trí đầy đủ:</strong> {locationData.zone && locationData.shelf && locationData.column ?
              `[${locationData.branchId === 1 ? 'Cơ sở B' : 'Cơ sở E'}] ${locationData.zone} - Kệ ${locationData.shelf} - Cột ${locationData.column} - Hàng ${locationData.row || '?'} - Vị trí ${locationData.position || '?'}` : 'Chưa đủ thông tin'}
          </div>
        </div>
        <div className="ld-modal-footer">
          <button className="ld-btn-secondary" onClick={onClose}>Hủy</button>
          <button className="ld-btn-primary" onClick={onSave}>Lưu vị trí</button>
        </div>
      </div>
    </div>
  );
};

export default LocationModal;
