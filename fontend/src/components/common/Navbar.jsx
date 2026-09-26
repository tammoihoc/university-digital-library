// src/components/common/Navbar.jsx
import React, { useState, useEffect, useRef } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { 
  Sun, Moon, Globe, Bell, Menu, X, 
  BookOpen, Library, Star, Clock, LogOut,
  LayoutDashboard, RefreshCw, BarChart3, Users, BookMarked, 
  MapPin, DollarSign, TrendingUp, User, Camera, Key, ChevronDown
} from 'lucide-react';
import authService from '../../services/authService.jsx';
import userService from '../../services/userService.jsx';
import logoImage from '../../assets/logo.png';
import './Navbar.css';

const Navbar = ({ 
  user, 
  onLogout, 
  darkMode, 
  toggleDarkMode, 
  toggleLanguage, 
  language,
  onAvatarClick 
}) => {
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [avatarUrl, setAvatarUrl] = useState(null);
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef(null);

  const isLibrarian = authService.isLibrarian();
  const isStudent = authService.isStudent();
  const isAdmin = authService.isAdmin();

  useEffect(() => {
    if (user?.username) {
      loadAvatar();
    }
  }, [user]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const loadAvatar = async () => {
    if (!user?.username) return;
    try {
      const url = await userService.getAvatarUrl(user.username);
      setAvatarUrl(url);
    } catch (error) {
      console.error('Error loading avatar:', error);
      setAvatarUrl(`https://api.dicebear.com/7.x/avataaars/svg?seed=${user.username}`);
    }
  };

  const handleLogoClick = () => {
    if (isLibrarian) {
      navigate('/librarian');
    } else {
      navigate('/dashboard');
    }
  };

  const handleUserProfileClick = () => {
    setShowDropdown(false);
    if (onAvatarClick) {
      onAvatarClick();
    } else {
      navigate('/profile');
    }
  };

  const handleLogoutClick = () => {
    setShowDropdown(false);
    if (onLogout) {
      onLogout();
    }
  };

  const handleImageError = (e) => {
    console.error('❌ Logo failed to load, using fallback');
    e.target.style.display = 'none';
    const container = e.target.parentElement;
    container.innerHTML = '<div class="brand-logo-fallback">HUTECH</div>';
  };

  const handleDropdownToggle = () => {
    setShowDropdown(!showDropdown);
  };

  // Hàm xử lý đổi avatar (mở file input)
  const handleChangeAvatar = () => {
    setShowDropdown(false);
    // Gửi sự kiện để component cha xử lý
    if (onAvatarClick) {
      onAvatarClick();
    } else {
      // Nếu không có hàm từ cha, mở file input mặc định
      const fileInput = document.createElement('input');
      fileInput.type = 'file';
      fileInput.accept = 'image/jpeg,image/png,image/jpg,image/webp';
      fileInput.onchange = async (e) => {
        const file = e.target.files[0];
        if (!file) return;
        try {
          await userService.uploadAvatar(user.username, file);
          const newAvatar = await userService.getAvatarUrl(user.username);
          setAvatarUrl(newAvatar);
          // Cập nhật localStorage
          const storedUser = authService.getUser();
          if (storedUser) {
            authService.saveUser({ ...storedUser, avatar: newAvatar });
          }
          alert('✅ Ảnh đại diện đã được cập nhật!');
          window.location.reload();
        } catch (error) {
          alert('❌ Upload avatar thất bại: ' + error.message);
        }
      };
      fileInput.click();
    }
  };

  return (
    <nav className="navbar">
      <div className="nav-container">
        <div className="nav-brand">
          <img 
            src={logoImage} 
            alt="HUTECH Logo" 
            className="brand-logo"
            onClick={handleLogoClick}
            onError={handleImageError}
          />
        </div>

        <button 
          className="mobile-menu-btn"
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
        >
          {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>

        <div className={`nav-links ${mobileMenuOpen ? 'open' : ''}`}>
          {isStudent && (
            <>
              <Link to="/dashboard" className="nav-link">
                <BookOpen size={18} />
                <span>Trang chủ</span>
              </Link>
              <Link to="/dashboard" className="nav-link">
                <Library size={18} />
                <span>Danh mục</span>
              </Link>
              <Link to="/dashboard" className="nav-link">
                <Star size={18} />
                <span>Yêu thích</span>
              </Link>
              <Link to="/dashboard" className="nav-link">
                <Clock size={18} />
                <span>Lịch sử</span>
              </Link>
            </>
          )}
          
          {(isLibrarian || isAdmin) && (
            <>
              <Link to="/librarian" className="nav-link">
                <LayoutDashboard size={18} />
                <span>Tổng quan</span>
              </Link>
              <Link to="/librarian/books" className="nav-link">
                <BookMarked size={18} />
                <span>Quản lý sách</span>
              </Link>
              <Link to="/librarian/shelves" className="nav-link">
                <MapPin size={18} />
                <span>Vị trí sách</span>
              </Link>
              <Link to="/librarian/borrow-return" className="nav-link">
                <RefreshCw size={18} />
                <span>Mượn/Trả</span>
              </Link>
              <Link to="/librarian/fines" className="nav-link">
                <DollarSign size={18} />
                <span>Quản lý phạt</span>
              </Link>
              <Link to="/librarian/entry-exit" className="nav-link">
                <Users size={18} />
                <span>Ra vào</span>
              </Link>
              <Link to="/librarian/reports" className="nav-link">
                <TrendingUp size={18} />
                <span>Báo cáo</span>
              </Link>
            </>
          )}
        </div>

        <div className="nav-actions">
          <button className="action-btn" onClick={toggleDarkMode}>
            {darkMode ? <Sun size={20} /> : <Moon size={20} />}
          </button>
          
          <button className="action-btn" onClick={toggleLanguage}>
            <Globe size={20} />
            <span className="language-badge">{language === 'vi' ? 'VI' : 'EN'}</span>
          </button>
          
          <button className="action-btn notification-btn">
            <Bell size={20} />
            <span className="notification-badge">3</span>
          </button>
          
          <div className="user-profile-container" ref={dropdownRef}>
            <div className="user-profile" onClick={handleDropdownToggle}>
              <div className="avatar-container">
                {avatarUrl ? (
                  <img 
                    src={avatarUrl} 
                    alt={user?.name} 
                    className="user-avatar"
                  />
                ) : (
                  <div className="avatar-initials">
                    {user?.name?.split(' ').map(n => n[0]).join('').toUpperCase() || 'U'}
                  </div>
                )}
              </div>
                
              <div className="user-info">
                <span className="user-name" title={user?.name || "Van A Nguyen"}>
                  {user?.name || "Van A Nguyen"}
                </span>
                <span className="user-id" title={user?.studentId || user?.librarianId || "ID"}>
                  {user?.studentId || user?.librarianId || (isLibrarian ? 'LIB001' : 'ST2024001')}
                </span>
              </div>
              
              <ChevronDown size={16} className={`dropdown-arrow ${showDropdown ? 'rotate' : ''}`} />
            </div>
            
            {showDropdown && (
              <div className="user-dropdown">
                <button className="dropdown-item" onClick={handleUserProfileClick}>
                  <User size={16} /> Hồ sơ cá nhân
                </button>
                <button className="dropdown-item" onClick={handleChangeAvatar}>
                  <Camera size={16} /> Đổi ảnh đại diện
                </button>
                <button className="dropdown-item" onClick={() => navigate('/change-password')}>
                  <Key size={16} /> Đổi mật khẩu
                </button>
                <hr className="dropdown-divider" />
                <button className="dropdown-item logout" onClick={handleLogoutClick}>
                  <LogOut size={16} /> Đăng xuất
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
