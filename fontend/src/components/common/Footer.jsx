// src/components/common/Footer.jsx
import React from 'react';
import './Footer.css';
import logoImage from '../../assets/logo.png'; // Điều chỉnh đường dẫn

const Footer = () => {
  const handleLogoClick = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <footer className="footer">
      <div className="footer-content">
        <div className="footer-section">
          <div className="footer-logo">
            <img 
              src={logoImage} // Sử dụng biến đã import
              alt="HUTECH Logo" 
              className="footer-logo-img"
              onClick={handleLogoClick}
              onError={(e) => {
                console.error('Footer logo failed to load');
                e.target.style.display = 'none';
                e.target.parentElement.innerHTML = '<div class="footer-logo-fallback">HUTECH</div>';
              }}
            />
          </div>
          <p className="footer-description">
            Cung cấp nguồn tài liệu học tập và nghiên cứu chất lượng cao 
            cho sinh viên và giảng viên HUTECH
          </p>
        </div>
        
        <div className="footer-links">
          <div className="link-group">
            <h4>Thư viện</h4>
            <a href="/about">Giới thiệu</a>
            <a href="/books">Danh mục sách</a>
            <a href="/guide">Hướng dẫn sử dụng</a>
            <a href="/rules">Quy định mượn sách</a>
          </div>
          
          <div className="link-group">
            <h4>Hỗ trợ</h4>
            <a href="/faq">Câu hỏi thường gặp</a>
            <a href="/contact">Liên hệ thủ thư</a>
            <a href="/report">Báo lỗi</a>
            <a href="/suggest">Đề xuất sách mới</a>
          </div>
          
          <div className="link-group">
            <h4>Kết nối</h4>
            <a href="https://facebook.com/hutech" target="_blank" rel="noopener noreferrer">Facebook</a>
            <a href="https://youtube.com/hutech" target="_blank" rel="noopener noreferrer">Youtube</a>
            <a href="mailto:thuvien@hutech.edu.vn">Email: thuvien@hutech.edu.vn</a>
            <a href="tel:02835120789">Hotline: 028 3512 0789</a>
          </div>
        </div>
      </div>
      
      <div className="footer-bottom">
        <p>© 2024 HUTECH Digital Library. Bản quyền thuộc về Đại học Công nghệ TP.HCM</p>
        <p>Địa chỉ: 475A Điện Biên Phủ, P.25, Q.Bình Thạnh, TP.HCM</p>
      </div>
    </footer>
  );
};

export default Footer;
