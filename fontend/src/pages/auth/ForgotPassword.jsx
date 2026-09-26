// src/pages/auth/ForgotPassword.jsx
import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowLeft, Mail, Phone, Calendar, Key, Check } from 'lucide-react';
import logo from '../../assets/logo.png';
import './ForgotPassword.css';

const ForgotPassword = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const [formData, setFormData] = useState({
    studentId: '',
    phone: '',
    dateOfBirth: '',
    newPassword: '',
    confirmPassword: ''
  });

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const handleVerify = (e) => {
    e.preventDefault();
    if (!formData.studentId || !formData.phone || !formData.dateOfBirth) {
      setError('Vui lòng điền đầy đủ thông tin');
      return;
    }
    
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      // Giả lập xác thực thành công (Backend chưa có API)
      setStep(2);
      setSuccess('Xác thực thành công! Vui lòng nhập mật khẩu mới.');
    }, 1000);
  };

  const handleResetPassword = (e) => {
    e.preventDefault();
    if (formData.newPassword.length < 6) {
      setError('Mật khẩu phải có ít nhất 6 ký tự');
      return;
    }
    if (formData.newPassword !== formData.confirmPassword) {
      setError('Mật khẩu xác nhận không khớp');
      return;
    }
    
    setLoading(true);
    setTimeout(() => {
      setLoading(false);
      setSuccess('Đổi mật khẩu thành công! Đang chuyển hướng...');
      setTimeout(() => navigate('/login'), 1500);
    }, 1000);
  };

  return (
    <div className="forgot-password-container">
      <div className="forgot-password-card">
        <div className="forgot-header">
          <img src={logo} alt="HUTECH" className="forgot-logo" />
          <h2>Quên mật khẩu</h2>
          <p className="forgot-subtitle">
            {step === 1 ? 'Nhập thông tin để xác thực tài khoản' : 'Nhập mật khẩu mới'}
          </p>
        </div>

        {error && (
          <div className="forgot-error">
            <span>⚠️</span> {error}
          </div>
        )}

        {success && (
          <div className="forgot-success">
            <span>✅</span> {success}
          </div>
        )}

        {step === 1 ? (
          <form onSubmit={handleVerify} className="forgot-form">
            <div className="form-group">
              <label>Mã số sinh viên / giảng viên</label>
              <div className="input-with-icon">
                <Mail size={18} />
                <input
                  type="text"
                  name="studentId"
                  value={formData.studentId}
                  onChange={handleChange}
                  placeholder="VD: SV-001 hoặc GV-001"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>Số điện thoại</label>
              <div className="input-with-icon">
                <Phone size={18} />
                <input
                  type="tel"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  placeholder="Nhập số điện thoại đã đăng ký"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label>Ngày tháng năm sinh</label>
              <div className="input-with-icon">
                <Calendar size={18} />
                <input
                  type="date"
                  name="dateOfBirth"
                  value={formData.dateOfBirth}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <button type="submit" className="forgot-btn" disabled={loading}>
              {loading ? 'Đang xác thực...' : 'Xác thực'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleResetPassword} className="forgot-form">
            <div className="form-group">
              <label>Mật khẩu mới</label>
              <div className="input-with-icon">
                <Key size={18} />
                <input
                  type="password"
                  name="newPassword"
                  value={formData.newPassword}
                  onChange={handleChange}
                  placeholder="Ít nhất 6 ký tự"
                  required
                  minLength="6"
                />
              </div>
            </div>

            <div className="form-group">
              <label>Xác nhận mật khẩu</label>
              <div className="input-with-icon">
                <Check size={18} />
                <input
                  type="password"
                  name="confirmPassword"
                  value={formData.confirmPassword}
                  onChange={handleChange}
                  placeholder="Nhập lại mật khẩu mới"
                  required
                />
              </div>
            </div>

            <button type="submit" className="forgot-btn" disabled={loading}>
              {loading ? 'Đang xử lý...' : 'Đổi mật khẩu'}
            </button>
          </form>
        )}

        <div className="forgot-footer">
          <Link to="/login" className="forgot-back">
            <ArrowLeft size={16} /> Quay lại đăng nhập
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
