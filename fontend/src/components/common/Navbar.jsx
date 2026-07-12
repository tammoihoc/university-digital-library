// src/components/common/Navbar.jsx
import React, { useState, useEffect } from 'react';  // ✅ THÊM useEffect
import { useNavigate, Link } from 'react-router-dom';
import { 
  Sun, Moon, Globe, Bell, Menu, X, 
  BookOpen, Library, Star, Clock, LogOut,
  LayoutDashboard, RefreshCw, BarChart3, Users, BookMarked, 
  MapPin, DollarSign, TrendingUp
} from 'lucide-react';
import authService from '../../services/authService.jsx';
import userService from '../../services/userService.jsx';

// Import ảnh logo
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

  const isLibrarian = authService.isLibrarian();
  const isStudent = authService.isStudent();
  const isAdmin = authService.isAdmin();

  // ✅ THÊM useEffect để load avatar
  useEffect(() => {
    if (user?.username) {
      loadAvatar();
    }
  }, [user]);

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
    window.location.reload();
  };

  const handleUserProfileClick = () => {
    if (onAvatarClick) {
      onAvatarClick();
    } else {
      navigate('/profile');
    }
  };

  const handleLogoutClick = (e) => {
    e.stopPropagation();
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
          
          <div 
            className="user-profile-container" 
            onClick={handleUserProfileClick}
            style={{ cursor: 'pointer' }}
          >
            <div className="user-profile">
              <div className="avatar-container" title="Xem hồ sơ">
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
              
              <button 
                className="logout-btn" 
                onClick={handleLogoutClick}
                title="Đăng xuất"
              >
                <LogOut size={16} />
              </button>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
