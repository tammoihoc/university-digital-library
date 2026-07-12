// src/pages/auth/Login.jsx
import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../../services/authService.jsx';
import userService from '../../services/userService.jsx';
import './Login.css';

// Import ảnh
import logo from '../../assets/logo.png';
import library1 from '../../assets/library1.jpg';
import library2 from '../../assets/library.jpg';
import library3 from '../../assets/library2.jpg';
import library4 from '../../assets/library3.jpg';

const Login = () => {
  const [formData, setFormData] = useState({
    username: '',
    password: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [currentSlide, setCurrentSlide] = useState(0);
  const [floatingShapes, setFloatingShapes] = useState([]);
  const [transitionDirection, setTransitionDirection] = useState('next');
  const navigate = useNavigate();

  // Slides data
  const slides = [
    {
      id: 1,
      image: library1,
      title: "Thư Viện Hiện Đại",
      description: "Không gian học tập lý tưởng với hàng ngàn đầu sách và tài liệu điện tử"
    },
    {
      id: 2,
      image: library2,
      title: "Phòng Lab Công Nghệ",
      description: "Trang thiết bị hiện đại phục vụ nghiên cứu và thực hành"
    },
    {
      id: 3,
      image: library3,
      title: "Đội Ngũ Giảng Viên",
      description: "Chuyên gia giàu kinh nghiệm, tận tâm với sự nghiệp đào tạo"
    },
    {
      id: 4,
      image: library4,
      title: "Cơ Sở Vật Chất",
      description: "Khuôn viên khang trang với đầy đủ tiện nghi hiện đại"
    }
  ];

  useEffect(() => {
    // Tạo floating shapes
    const shapes = Array.from({ length: 15 }, (_, i) => ({
      id: i,
      type: Math.random() > 0.5 ? 'circle' : 'square',
      size: Math.random() * 30 + 10,
      left: Math.random() * 100,
      delay: Math.random() * 20,
      duration: Math.random() * 30 + 20
    }));
    setFloatingShapes(shapes);

    // Tự động chuyển slide
    const slideInterval = setInterval(() => {
      goToSlide((currentSlide + 1) % slides.length, 'next');
    }, 5000);

    return () => clearInterval(slideInterval);
  }, [currentSlide, slides.length]);

  const goToSlide = (index, direction = 'next') => {
    setTransitionDirection(direction);
    setCurrentSlide(index);
  };

  const handleSlideClick = (e) => {
    const slideWidth = e.currentTarget.offsetWidth;
    const clickX = e.nativeEvent.offsetX;
    const isLeftClick = clickX < slideWidth / 2;
    
    if (isLeftClick) {
      const prevSlide = currentSlide === 0 ? slides.length - 1 : currentSlide - 1;
      goToSlide(prevSlide, 'prev');
    } else {
      const nextSlide = (currentSlide + 1) % slides.length;
      goToSlide(nextSlide, 'next');
    }
  };

  const nextSlide = () => {
    const nextSlide = (currentSlide + 1) % slides.length;
    goToSlide(nextSlide, 'next');
  };

  const prevSlide = () => {
    const prevSlide = currentSlide === 0 ? slides.length - 1 : currentSlide - 1;
    goToSlide(prevSlide, 'prev');
  };

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

    setLoading(true);
    setError('');

    try {
      const response = await authService.login({
        username: formData.username,
        password: formData.password
      });
      
      if (response && response.token) {
        // Lấy user từ storage
        const user = authService.getUser();
        const userRole = authService.getUserRole();
        
        console.log('User role:', userRole);
        
        setError('Đăng nhập thành công! Đang chuyển hướng...');
        
        setTimeout(() => {
          if (userRole === 'LIBRARIAN' || userRole === 'ADMIN') {
            navigate('/librarian');
          } else {
            navigate('/dashboard');
          }
        }, 1000);
      } else {
        throw new Error('Invalid response from server');
      }
      
    } catch (err) {
      console.error('Login error:', err);
      setError(err.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại tên đăng nhập và mật khẩu.');
      setLoading(false);
    }
  };

  const handleImageError = (e) => {
    e.target.style.display = 'none';
  };

  return (
    <div className="login-container">
      <div className="animated-background">
        <div className="background-image"></div>
        <div className="gradient-overlay"></div>
        
        {floatingShapes.map(shape => (
          <div
            key={shape.id}
            className={`floating-shape ${shape.type}`}
            style={{
              left: `${shape.left}%`,
              width: `${shape.size}px`,
              height: `${shape.size}px`,
              animationDelay: `${shape.delay}s`,
              animationDuration: `${shape.duration}s`
            }}
          />
        ))}
      </div>

      <div className="login-main-container">
        <div className="login-gallery">
          <div className="gallery-container">
            <div 
              className="slideshow-container" 
              onClick={handleSlideClick}
            >
              {slides.map((slide, index) => (
                <div
                  key={slide.id}
                  className={`slide ${
                    index === currentSlide ? 'active' : ''
                  } ${
                    transitionDirection === 'next' ? 'slide-next' : 'slide-prev'
                  }`}
                >
                  <img 
                    src={slide.image} 
                    alt={slide.title}
                    className="slide-image"
                    onError={handleImageError}
                  />
                  <div className="slide-content">
                    <h2 className="slide-title">{slide.title}</h2>
                    <p className="slide-description">{slide.description}</p>
                  </div>
                </div>
              ))}
              
              <div className="slide-nav-left" onClick={(e) => { e.stopPropagation(); prevSlide(); }}>
                <div className="nav-arrow">
                  <span className="arrow-icon">‹</span>
                </div>
              </div>
              <div className="slide-nav-right" onClick={(e) => { e.stopPropagation(); nextSlide(); }}>
                <div className="nav-arrow">
                  <span className="arrow-icon">›</span>
                </div>
              </div>
            </div>
            
            <div className="slideshow-dots">
              {slides.map((_, index) => (
                <div
                  key={index}
                  className={`dot ${index === currentSlide ? 'active' : ''}`}
                  onClick={() => goToSlide(index, index > currentSlide ? 'next' : 'prev')}
                />
              ))}
            </div>
          </div>
        </div>

        <div className="login-center-container">
          <div className="login-card">
            <div className="login-header">
              <div className="school-logo-container">
                <img 
                  src={logo} 
                  alt="Logo HUTECH" 
                  className="school-logo"
                  onError={(e) => {
                    e.target.style.display = 'none';
                  }}
                />
              </div>
              
              <h3 className="login-title">Đăng Nhập</h3>
            </div>

            <form onSubmit={handleSubmit} className="login-form">
              {error && (
                <div className={`error-message ${error.includes('thành công') ? 'success-message' : ''}`}>
                  <div className="error-icon">
                    {error.includes('thành công') ? '✅' : '⚠️'}
                  </div>
                  {error}
                </div>
              )}

              <div className="form-group">
                <label htmlFor="username" className="input-label">
                  Tên đăng nhập
                </label>
                <div className="input-container">
                  <input
                    type="text"
                    id="username"
                    name="username"
                    value={formData.username}
                    onChange={handleChange}
                    required
                    className="form-input"
                    placeholder="Nhập tên đăng nhập"
                    disabled={loading}
                  />
                </div>
              </div>

              <div className="form-group">
                <label htmlFor="password" className="input-label">
                  Mật khẩu
                </label>
                <div className="input-container">
                  <input
                    type="password"
                    id="password"
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    required
                    className="form-input"
                    placeholder="Nhập mật khẩu"
                    disabled={loading}
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading || !formData.username || !formData.password}
                className={`login-button ${loading ? 'loading' : ''}`}
              >
                {loading ? (
                  <div className="button-loading">
                    <div className="button-spinner"></div>
                    <span>Đang đăng nhập...</span>
                  </div>
                ) : (
                  <span className="button-text">Đăng nhập</span>
                )}
              </button>
            </form>

<div className="login-links">
  <Link to="/forgot-password" className="link">
    Quên mật khẩu?
  </Link>
  <span className="separator">|</span>
  <Link to="/register" className="link">
    Đăng ký tài khoản
  </Link>
</div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
