import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/common/Navbar';
import Footer from '../../components/common/Footer';
import authService from '../../services/authService';
import userService from '../../services/userService';
import { 
  ArrowLeft, User, Mail, Phone, MapPin, Calendar,
  Edit, Save, X, Camera, Key, Book, 
  GraduationCap, Building, Users,
  Upload, Loader, CheckCircle, AlertCircle
} from 'lucide-react';
import './Profile.css';

const Profile = () => {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);
  
  const [user, setUser] = useState(null);
  const [darkMode, setDarkMode] = useState(false);
  const [language, setLanguage] = useState('vi');
  const [isEditing, setIsEditing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [borrowStats, setBorrowStats] = useState(null);
  
  const [editForm, setEditForm] = useState({
    fullName: '',
    email: '',
    phone: '',
    address: '',
    dateOfBirth: '',
    faculty: '',
    major: ''
  });

  const loadUserData = async () => {
    try {
      setLoading(true);
      
      const storedUser = authService.getUser();
      
      if (!storedUser) {
        navigate('/login');
        return;
      }
      
      const freshData = await userService.getUserProfile(storedUser.username);
      const statsData = await userService.getBorrowStats(storedUser.username);
      
      console.log('📊 Borrow stats from API:', statsData);
      
      if (freshData) {
        const userData = {
          ...storedUser,
          ...freshData,
          avatar: await userService.getAvatarUrl(freshData.username),
          fullName: userService.getDisplayName(freshData),
          displayName: userService.getDisplayName(freshData),
          firstName: freshData.firstName || '',
          lastName: freshData.lastName || '',
          email: freshData.email || storedUser.email || '',
          phone: freshData.phone || '',
          address: freshData.address || '',
          dateOfBirth: freshData.dateOfBirth ? userService.formatDateForInput(freshData.dateOfBirth) : '',
          faculty: freshData.faculty || 'Công nghệ thông tin',
          major: freshData.major || 'Kỹ thuật phần mềm',
          studentId: freshData.studentId || storedUser.username,
          userType: freshData.userType || storedUser.userType || 'STUDENT',
          academicYear: freshData.academicYear || new Date().getFullYear(),
          currentBorrowed: statsData?.currentBorrowed || 0,
          maxBorrowLimit: statsData?.maxBorrowLimit || 5,
          canBorrowMore: statsData?.canBorrowMore !== false
        };
        
        console.log('📊 User data loaded:', {
          fullName: userData.fullName,
          currentBorrowed: userData.currentBorrowed,
          maxBorrowLimit: userData.maxBorrowLimit,
          canBorrowMore: userData.canBorrowMore
        });
        
        setUser(userData);
        setBorrowStats(statsData);
        
        setEditForm({
          fullName: userData.fullName,
          email: userData.email || '',
          phone: userData.phone || '',
          address: userData.address || '',
          dateOfBirth: userData.dateOfBirth || '',
          faculty: userData.faculty || '',
          major: userData.major || ''
        });
        
        authService.saveUser(userData);
      } else {
        setUser(storedUser);
        setBorrowStats({
          maxBorrowLimit: 5,
          currentBorrowed: 0,
          canBorrowMore: true,
          overdueBooks: 0
        });
      }
      
      setLoading(false);
    } catch (error) {
      console.error('Error loading user profile:', error);
      setErrorMessage('Không thể tải thông tin hồ sơ');
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUserData();

    // Tự động reload khi quay lại tab
    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        loadUserData();
      }
    };
    document.addEventListener('visibilitychange', handleVisibilityChange);
    
    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  const toggleDarkMode = () => {
    setDarkMode(!darkMode);
  };

  const toggleLanguage = () => {
    setLanguage(language === 'vi' ? 'en' : 'vi');
  };

  const handleEditChange = (e) => {
    const { name, value } = e.target;
    setEditForm(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSaveProfile = async () => {
    try {
      const updateData = {
        fullName: editForm.fullName,
        email: editForm.email,
        phone: editForm.phone,
        address: editForm.address,
        dateOfBirth: editForm.dateOfBirth,
        faculty: editForm.faculty,
        major: editForm.major
      };
      
      console.log('📤 Sending update data:', updateData);
      
      const updatedData = await userService.updateUserProfile(user.username, updateData);
      
      const updatedUser = {
        ...user,
        ...updatedData,
        avatar: userService.getAvatarUrl(updatedData),
        fullName: updateData.fullName,
        dateOfBirth: userService.formatDateForInput(updateData.dateOfBirth),
        faculty: updateData.faculty,
        major: updateData.major
      };
      
      setUser(updatedUser);
      authService.saveUser(updatedUser);
      
      setSuccessMessage('Thông tin đã được cập nhật thành công!');
      setIsEditing(false);
      
      // Tải lại dữ liệu sau khi lưu
      await loadUserData();
      
      setTimeout(() => {
        setSuccessMessage('');
      }, 3000);
      
    } catch (error) {
      console.error('Error saving profile:', error);
      setErrorMessage('Có lỗi xảy ra khi lưu thông tin: ' + error.message);
      setTimeout(() => setErrorMessage(''), 5000);
    }
  };

  const handleCancelEdit = () => {
    setEditForm({
      fullName: user.fullName || '',
      email: user.email || '',
      phone: user.phone || '',
      address: user.address || '',
      dateOfBirth: user.dateOfBirth || '',
      faculty: user.faculty || '',
      major: user.major || ''
    });
    setIsEditing(false);
  };

  // Avatar upload functions
  const handleAvatarClick = () => {
    if (fileInputRef.current) {
      fileInputRef.current.click();
    }
  };

  const handleAvatarUpload = async (event) => {
    const file = event.target.files[0];
    if (!file) return;
    
    const validTypes = ['image/jpeg', 'image/png', 'image/jpg', 'image/webp'];
    if (!validTypes.includes(file.type)) {
      setErrorMessage('Chỉ chấp nhận file ảnh (JPEG, PNG, JPG, WEBP)');
      setTimeout(() => setErrorMessage(''), 5000);
      return;
    }
    
    if (file.size > 5 * 1024 * 1024) {
      setErrorMessage('Kích thước file quá lớn (tối đa 5MB)');
      setTimeout(() => setErrorMessage(''), 5000);
      return;
    }
    
    try {
      setUploading(true);
      setErrorMessage('');
      
      const result = await userService.uploadAvatar(user.username, file);
      console.log('✅ New avatar uploaded:', result);
      
      const newAvatarUrl = await userService.getAvatarUrl(user.username);
      
      setUser(prev => ({
        ...prev,
        avatar: newAvatarUrl
      }));
      
      const storedUser = authService.getUser();
      if (storedUser) {
        authService.saveUser({
          ...storedUser,
          avatar: newAvatarUrl
        });
      }
      
      setSuccessMessage('Ảnh đại diện đã được cập nhật thành công!');
      setTimeout(() => setSuccessMessage(''), 3000);
      
    } catch (error) {
      console.error('❌ Error uploading avatar:', error);
      setErrorMessage('Lỗi upload ảnh: ' + error.message);
      setTimeout(() => setErrorMessage(''), 5000);
    } finally {
      setUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    }
  };

  // Helpers
  const formatDateOfBirth = (dateString) => {
    return userService.formatDateOfBirth(dateString);
  };

  const formatAcademicYear = (academicYear) => {
    if (!academicYear) return 'Chưa cập nhật';
    return academicYear.toString();
  };

  const getCurrentBorrowed = () => {
    if (borrowStats && borrowStats.currentBorrowed !== undefined) {
      return borrowStats.currentBorrowed;
    }
    return user?.currentBorrowed || 0;
  };

  const getMaxBorrowLimit = () => {
    return borrowStats?.maxBorrowLimit || 2;
  };

  if (loading) {
    return (
      <div className="profile-loading-screen">
        <div className="profile-loading-spinner"></div>
        <p>Đang tải thông tin hồ sơ...</p>
      </div>
    );
  }

  return (
    <div className={`profile-page ${darkMode ? 'profile-dark' : 'profile-light'}`}>
      <Navbar
        user={user}
        onLogout={handleLogout}
        darkMode={darkMode}
        toggleDarkMode={toggleDarkMode}
        toggleLanguage={toggleLanguage}
        language={language}
        onAvatarClick={handleAvatarClick}
      />

      <input
        type="file"
        ref={fileInputRef}
        onChange={handleAvatarUpload}
        accept="image/jpeg,image/png,image/jpg,image/webp"
        style={{ display: 'none' }}
      />

      <div className="profile-container">
        <div className="profile-header">
          <button 
            className="profile-back-button"
            onClick={() => navigate('/dashboard')}
          >
            <ArrowLeft size={20} />
            <span>Quay lại Dashboard</span>
          </button>
          
          <h1 className="profile-title">Hồ sơ cá nhân</h1>
          
          <div style={{ width: '130px' }}></div>
        </div>

        <div className="profile-content">
          {/* Left Column - Avatar & Basic Info */}
          <div className="profile-avatar-section">
            <div className="profile-avatar-wrapper">
              {uploading ? (
                <div className="avatar-uploading-overlay">
                  <Loader size={24} className="loading-spinner" />
                  <span>Đang cập nhật...</span>
                </div>
              ) : null}
              
              <img 
                src={user?.avatar} 
                alt={user?.fullName}
                className={`profile-avatar ${uploading ? 'avatar-uploading' : ''}`}
                onError={(e) => {
                  e.target.style.display = 'none';
                  const container = e.target.parentElement;
                  const initials = user?.fullName?.split(' ').map(n => n[0]).join('').toUpperCase() || 'U';
                  container.innerHTML = `<div class="profile-avatar-initials">${initials}</div>`;
                }}
              />
              
              <button 
                className="profile-avatar-edit-btn" 
                title="Đổi ảnh đại diện"
                onClick={handleAvatarClick}
                disabled={uploading}
              >
                <Camera size={18} />
              </button>
            </div>
            
            <h2 className="profile-name">{user?.fullName}</h2>
            <p className="profile-username">@{user?.username}</p>
            
            <div className="profile-badges">
              <span className="profile-badge profile-badge-primary">Sinh viên</span>
              {user?.academicYear && (
                <span className="profile-badge profile-badge-secondary">
                  {user.academicYear}
                </span>
              )}
            </div>
            
            <div className="profile-quick-stats">
              <div className="quick-stat">
                <Book size={20} />
                <div>
                  <span className="stat-value">{getCurrentBorrowed()}/{getMaxBorrowLimit()}</span>
                  <span className="stat-label">Sách đang mượn</span>
                  <small style={{ 
                    display: 'block', 
                    fontSize: '12px', 
                    color: borrowStats?.canBorrowMore ? '#10b981' : '#ef4444',
                    marginTop: '5px'
                  }}>
                    {borrowStats?.canBorrowMore ? '🟢 Có thể mượn thêm' : '🔴 Đã đạt giới hạn'}
                  </small>
                </div>
              </div>
            </div>
            
            <div className="avatar-upload-info">
              <small style={{ color: '#666', display: 'block', marginTop: '10px' }}>
                <Upload size={12} style={{ marginRight: '5px', verticalAlign: 'middle' }} />
                Nhấn vào biểu tượng camera để đổi ảnh đại diện
              </small>
              <small style={{ color: '#888', fontSize: '11px', display: 'block', marginTop: '5px' }}>
                Hỗ trợ: JPEG, PNG, JPG, WEBP (tối đa 5MB)
              </small>
            </div>
          </div>

          {/* Right Column - Detailed Info */}
          <div className="profile-info-section">
            <div className="profile-section-header">
              <h3>Thông tin cá nhân</h3>
              {!isEditing ? (
                <button 
                  className="profile-edit-btn"
                  onClick={() => setIsEditing(true)}
                  disabled={uploading}
                >
                  <Edit size={18} />
                  <span>Chỉnh sửa</span>
                </button>
              ) : (
                <button 
                  className="profile-edit-btn profile-edit-cancel"
                  onClick={handleCancelEdit}
                  disabled={uploading}
                >
                  <X size={18} />
                  <span>Hủy</span>
                </button>
              )}
            </div>

            {/* Messages */}
            {errorMessage && (
              <div className="profile-error-message">
                <AlertCircle size={18} />
                <span>{errorMessage}</span>
              </div>
            )}

            {successMessage && (
              <div className="profile-success-message">
                <CheckCircle size={18} />
                <span>{successMessage}</span>
              </div>
            )}

            {/* Personal Information */}
            <div className="profile-info-grid">
              {/* Full Name */}
              <div className="profile-info-item">
                <User size={20} />
                <div>
                  <label>Họ và tên</label>
                  {isEditing ? (
                    <input 
                      type="text" 
                      name="fullName"
                      value={editForm.fullName}
                      onChange={handleEditChange}
                      className="profile-edit-input" 
                      placeholder="Nhập họ và tên"
                      disabled={uploading}
                    />
                  ) : (
                    <p>{user?.fullName || 'Chưa cập nhật'}</p>
                  )}
                </div>
              </div>

              {/* Email */}
              <div className="profile-info-item">
                <Mail size={20} />
                <div>
                  <label>Email</label>
                  {isEditing ? (
                    <input 
                      type="email" 
                      name="email"
                      value={editForm.email}
                      onChange={handleEditChange}
                      className="profile-edit-input" 
                      placeholder="email@hutech.edu.vn"
                      disabled={uploading}
                    />
                  ) : (
                    <p>{user?.email || 'Chưa cập nhật'}</p>
                  )}
                </div>
              </div>

              {/* Phone */}
              <div className="profile-info-item">
                <Phone size={20} />
                <div>
                  <label>Số điện thoại</label>
                  {isEditing ? (
                    <input 
                      type="tel" 
                      name="phone"
                      value={editForm.phone}
                      onChange={handleEditChange}
                      className="profile-edit-input" 
                      placeholder="0123 456 789"
                      disabled={uploading}
                    />
                  ) : (
                    <p>{user?.phone || 'Chưa cập nhật'}</p>
                  )}
                </div>
              </div>

              {/* Address */}
              <div className="profile-info-item">
                <MapPin size={20} />
                <div>
                  <label>Địa chỉ</label>
                  {isEditing ? (
                    <input 
                      type="text" 
                      name="address"
                      value={editForm.address}
                      onChange={handleEditChange}
                      className="profile-edit-input" 
                      placeholder="Địa chỉ hiện tại"
                      disabled={uploading}
                    />
                  ) : (
                    <p>{user?.address || 'Chưa cập nhật'}</p>
                  )}
                </div>
              </div>

              {/* Date of Birth */}
              <div className="profile-info-item">
                <Calendar size={20} />
                <div>
                  <label>Ngày sinh</label>
                  {isEditing ? (
                    <input 
                      type="date" 
                      name="dateOfBirth"
                      value={editForm.dateOfBirth}
                      onChange={handleEditChange}
                      className="profile-edit-input" 
                      max={new Date().toISOString().split('T')[0]}
                      disabled={uploading}
                    />
                  ) : (
                    <p>{formatDateOfBirth(user?.dateOfBirth)}</p>
                  )}
                </div>
              </div>

              {/* Student ID */}
              <div className="profile-info-item">
                <Key size={20} />
                <div>
                  <label>Mã sinh viên</label>
                  <p className="profile-user-code">{user?.studentId || 'Chưa có'}</p>
                </div>
              </div>

              {/* Faculty */}
              <div className="profile-info-item">
                <Building size={20} />
                <div>
                  <label>Khoa/Viện</label>
                  {isEditing ? (
                    <input 
                      type="text" 
                      name="faculty"
                      value={editForm.faculty}
                      onChange={handleEditChange}
                      className="profile-edit-input" 
                      placeholder="Nhập tên khoa/viện"
                      disabled={uploading}
                    />
                  ) : (
                    <p>{user?.faculty || 'Chưa cập nhật'}</p>
                  )}
                </div>
              </div>

              {/* Major */}
              <div className="profile-info-item">
                <GraduationCap size={20} />
                <div>
                  <label>Chuyên ngành</label>
                  {isEditing ? (
                    <input 
                      type="text" 
                      name="major"
                      value={editForm.major}
                      onChange={handleEditChange}
                      className="profile-edit-input" 
                      placeholder="Nhập chuyên ngành"
                      disabled={uploading}
                    />
                  ) : (
                    <p>{user?.major || 'Chưa cập nhật'}</p>
                  )}
                </div>
              </div>

              {/* Academic Year */}
              <div className="profile-info-item">
                <Users size={20} />
                <div>
                  <label>Năm nhập học</label>
                  <p>{formatAcademicYear(user?.academicYear)}</p>
                </div>
              </div>

              {/* Current Borrowed */}
              <div className="profile-info-item">
                <Book size={20} />
                <div>
                  <label>Sách đang mượn</label>
                  <p className="profile-stat-number">{getCurrentBorrowed()}/{getMaxBorrowLimit()}</p>
                  {borrowStats && (
                    <small style={{ 
                      fontSize: '12px', 
                      color: borrowStats.canBorrowMore ? '#10b981' : '#ef4444' 
                    }}>
                      {borrowStats.canBorrowMore ? '🟢 Có thể mượn thêm' : '🔴 Đã đạt giới hạn'}
                    </small>
                  )}
                </div>
              </div>
            </div>

            {isEditing && (
              <div className="profile-save-actions">
                <button 
                  className="profile-cancel-btn"
                  onClick={handleCancelEdit}
                  disabled={uploading}
                >
                  <X size={18} />
                  <span>Hủy bỏ</span>
                </button>
                <button 
                  className="profile-save-btn"
                  onClick={handleSaveProfile}
                  disabled={uploading}
                >
                  <Save size={18} />
                  <span>Lưu thay đổi</span>
                </button>
              </div>
            )}

            {/* Logout Button */}
            <div className="profile-logout-container">
              <button 
                onClick={handleLogout}
                className="profile-logout-btn"
                disabled={uploading}
              >
                <ArrowLeft size={18} />
                <span>Đăng xuất</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <Footer />
    </div>
  );
};

export default Profile;
