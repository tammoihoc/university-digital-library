// src/pages/auth/Register.jsx
import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import authService from '../../services/authService';
import './Register.css';

// Import ảnh
import logo from '../../assets/logo.png';

const Register = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    userType: 'STUDENT',
    faculty: '',
    major: ''
  });

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!formData.username || !formData.password) {
      setError('Vui lòng điền đầy đủ thông tin');
      return;
    }
    
    if (formData.password !== formData.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return;
    }
    
    if (formData.password.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự');
      return;
    }
    
    setLoading(true);
    setError('');
    
    try {
      const registerData = {
        username: formData.username,
        password: formData.password,
        roles: [formData.userType],
        userProfile: {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          phone: formData.phone,
          faculty: formData.faculty,
          major: formData.major,
          userType: formData.userType
        }
      };
      
      await authService.register(registerData);
      
      setError('Đăng ký thành công! Đang chuyển hướng...');
      
      setTimeout(() => {
        navigate('/dashboard');
      }, 1500);
      
    } catch (err) {
      console.error('Register error:', err);
      setError(err.message || 'Đăng ký thất bại. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="register-container">
      <div className="register-card">
        <div className="register-header">
          <img src={logo} alt="HUTECH Logo" className="register-logo" />
          <h2>Đăng ký tài khoản</h2>
          <p>Thư viện số HUTECH</p>
        </div>
        
        {error && (
          <div className={`register-message ${error.includes('thành công') ? 'success' : 'error'}`}>
            {error}
          </div>
        )}
        
        <form onSubmit={handleSubmit} className="register-form">
          <div className="form-row">
            <div className="form-group">
              <label>Tên đăng nhập *</label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                placeholder="Nhập tên đăng nhập"
                required
              />
            </div>
            <div className="form-group">
              <label>Email</label>
              <input
                type="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="email@hutech.edu.vn"
              />
            </div>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label>Họ và tên lót</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                placeholder="Họ và tên lót"
              />
            </div>
            <div className="form-group">
              <label>Tên</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                placeholder="Tên"
              />
            </div>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label>Mật khẩu *</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder="Nhập mật khẩu"
                required
              />
            </div>
            <div className="form-group">
              <label>Xác nhận mật khẩu *</label>
              <input
                type="password"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder="Nhập lại mật khẩu"
                required
              />
            </div>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label>Số điện thoại</label>
              <input
                type="tel"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                placeholder="Số điện thoại"
              />
            </div>
            <div className="form-group">
              <label>Loại tài khoản *</label>
              <select name="userType" value={formData.userType} onChange={handleChange} required>
                <option value="STUDENT">Sinh viên</option>
                <option value="LECTURER">Giảng viên</option>
              </select>
            </div>
          </div>
          
          <div className="form-row">
            <div className="form-group">
              <label>Khoa/Viện</label>
              <input
                type="text"
                name="faculty"
                value={formData.faculty}
                onChange={handleChange}
                placeholder="VD: Công nghệ thông tin"
              />
            </div>
            <div className="form-group">
              <label>Chuyên ngành</label>
              <input
                type="text"
                name="major"
                value={formData.major}
                onChange={handleChange}
                placeholder="VD: Kỹ thuật phần mềm"
              />
            </div>
          </div>
          
          <button type="submit" className="register-btn" disabled={loading}>
            {loading ? 'Đang xử lý...' : 'Đăng ký'}
          </button>
        </form>
        
        <div className="register-footer">
          <span>Đã có tài khoản?</span>
          <Link to="/login">Đăng nhập ngay</Link>
        </div>
      </div>
    </div>
  );
};

export default Register;
