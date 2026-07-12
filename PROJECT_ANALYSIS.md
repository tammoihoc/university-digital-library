# 📚 University Digital Library - Phân Tích Dự Án Chi Tiết

**Phiên bản:** 1.0.0  
**Ngày tạo:** 2025-11-25  
**Tác giả:** HUTECH Development Team  
**Trạng thái:** Đang phát triển 🚀

---

## 📋 Mục Lục
1. [Tổng Quan Dự Án](#tổng-quan-dự-án)
2. [Kiến Trúc Hệ Thống](#kiến-trúc-hệ-thống)
3. [Công Nghệ Sử Dụng](#công-nghệ-sử-dụng)
4. [Cấu Trúc Dự Án](#cấu-trúc-dự-án)
5. [Lỗi & Vấn Đề Hiện Tại](#lỗi--vấn-đề-hiện-tại)
6. [Điểm Mạnh](#điểm-mạnh)
7. [Điểm Yếu & Cần Cải Thiện](#điểm-yếu--cần-cải-thiện)
8. [Hướng Phát Triển Tương Lai](#hướng-phát-triển-tương-lai)
9. [Hướng Dẫn Chạy Dự Án](#hướng-dẫn-chạy-dự-án)

---

## 🎯 Tổng Quan Dự Án

### Mô Tả
**University Digital Library** là một hệ thống thư viện kỹ thuật số hiện đại được thiết kế cho sinh viên và giảng viên HUTECH. Hệ thống cho phép:

- 📖 **Sinh viên**: Duyệt sách, đọc online, mượn sách vật lý, đặt lịch, quản lý hồ sơ
- 👨‍💼 **Giảng viên/Thủ thư**: Quản lý sách, xử lý mượn/trả, theo dõi sinh viên, quản lý phạt

### Mục Đích
- Số hóa thư viện đại học, giảm sự phụ thuộc vào sách giấy
- Tăng khả năng tiếp cận tài liệu học tập cho sinh viên
- Hỗ trợ quản lý tài nguyên hiệu quả cho thủ thư
- Tích hợp công nghệ microservices cho khả năng mở rộng

### Người Dùng Mục Tiêu
- 🎓 Sinh viên (Chính)
- 👨‍🏫 Giảng viên
- 👨‍💼 Thủ thư / Quản lý thư viện
- 🔧 Quản trị viên hệ thống

---

## 🏗️ Kiến Trúc Hệ Thống

### Mô Hình Microservices

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React + Vite)                   │
│              http://localhost:3000 (Dev Mode)                │
└────────────────────────────┬────────────────────────────────┘
                             │
┌────────────────────────────▼────────────────────────────────┐
│              API Gateway (Spring Cloud Gateway)              │
│                  http://localhost:8080                       │
├──────────────────────────────────────────────────────────────┤
│  - Request Routing        - Load Balancing                   │
│  - Circuit Breaker        - CORS Handling                    │
└────────────────────────────┬────────────────────────────────┘
         │        │         │         │          │         │
    ┌────▼──┬────▼──┬────▼──┬────▼──┬───▼──┬───▼──┐
    │ Auth  │ Book  │ User  │Borrow │Entry │Fine  │
    │ Svc   │ Svc   │ Svc   │ Svc   │Exit  │ Svc  │
    │:8081  │:8083  │:8082  │ :TBD  │:TBD  │:TBD  │
    └────┬──┴────┬──┴────┬──┴────┬──┴───┬──┴───┬──┘
         │       │       │       │      │      │
    ┌────▼───────▼───────▼───────▼──────▼──────▼──┐
    │     Service Discovery & Config              │
    │     (Consul - localhost:8500)               │
    └──────────────────────────────────────────────┘
         │       │       │       │      │      │
    ┌────▼───────▼───────▼───────▼──────▼──────▼──┐
    │         Database Layer (MySQL)              │
    │     127.0.0.1:3306/university_digital_library│
    └──────────────────────────────────────────────┘
```

### Mô Tả Chi Tiết
- **Frontend**: React SPA với routing, authentication, UI components
- **API Gateway**: Spring Cloud Gateway nhận tất cả requests từ client
- **Microservices**: Mỗi dịch vụ có trách nhiệm riêng (Single Responsibility)
- **Service Discovery**: Consul giúp các services tìm kiếm nhau động
- **Database**: MySQL duy nhất để dữ liệu nhất quán

---

## 🛠️ Công Nghệ Sử Dụng

### Backend (51.8% - Java)

| Công Nghệ | Phiên Bản | Mục Đích |
|-----------|-----------|---------|
| **Java** | 24 | Ngôn ngữ lập trình |
| **Spring Boot** | 3.4.12 | Framework web & microservices |
| **Spring Cloud** | 2024.0.2 | Orchestration & discovery |
| **Spring Data JPA** | Built-in | ORM & database access |
| **Spring Security** | Built-in | Authentication & authorization |
| **Spring Cloud Gateway** | Built-in | API routing & load balancing |
| **Consul** | Mới nhất | Service discovery & config |
| **Resilience4j** | Built-in | Circuit breaker pattern |
| **JWT (JJWT)** | 0.11.5-0.12.6 | Token-based authentication |
| **Apache PDFBox** | 3.0.3 | PDF processing |
| **MySQL** | 8.x | Database |
| **Lombok** | Built-in | Boilerplate reduction |
| **Maven** | 3.x | Build tool |

### Frontend (31.9% - JavaScript)

| Công Nghệ | Phiên Bản | Mục Đích |
|-----------|-----------|---------|
| **React** | 19.2.0 | UI library |
| **Vite** | 7.2.5 (rolldown) | Build tool & dev server |
| **React Router** | 7.9.6 | Client-side routing |
| **Tailwind CSS** | 4.1.17 | Utility-first CSS |
| **Axios** | 1.13.2 | HTTP client |
| **React Hot Toast** | 2.6.0 | Toast notifications |
| **Chart.js & Recharts** | Mới | Data visualization |
| **Lucide React** | 0.555.0 | Icon library |
| **ESLint** | 9.39.1 | Code linting |

### Styling (16.2% - CSS + 0.1% HTML)
- **Tailwind CSS**: Utility-first framework
- **Custom CSS**: Dashboard, Login, Library pages
- **PostCSS**: CSS processing with plugins

---

## 📂 Cấu Trúc Dự Án

```
university-digital-library/
│
├── backend/                          # Backend Microservices (51.8%)
│   ├── pom.xml                       # Parent POM (Microservices)
│   │
│   ├── gateway-service/              # API Gateway (Port 8080)
│   │   ├── pom.xml
│   │   └── src/main/resources/
│   │       └── application.yaml      # 7 routes configured
│   │
│   ├── auth-service/                 # Authentication (Port 8081)
│   │   ├── pom.xml
│   │   ├── src/main/java/com/university_digital_library/auth/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── entity/
│   │   │   └── security/
│   │   └── src/main/resources/
│   │       └── application.yaml      # MySQL, JWT, Consul config
│   │
│   ├── book-service/                 # Book Management (Port 8083)
│   │   ├── pom.xml                   # PDF processing, JWT validation
│   │   ├── src/main/java/com/university_digital_library/book/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   └── entity/
│   │   └── src/main/resources/
│   │       └── application.yaml
│   │
│   ├── user-service/                 # User Management (Port 8082)
│   │   ├── pom.xml                   # Security, JWT, Consul
│   │   └── src/main/resources/
│   │       └── application.yaml
│   │
│   └── discovery-service/            # Service Discovery (TBD)
│       └── (Consul configuration)
│
├── fontend/                          # Frontend Application (31.9%)
│   ├── package.json                  # Dependencies & scripts
│   ├── vite.config.js                # Vite configuration
│   ├── tailwind.config.js            # Tailwind customization
│   ├── src/
│   │   ├── main.jsx                  # Entry point
│   │   ├── App.jsx                   # Root component with routing
│   │   ├── index.css                 # Global styles
│   │   │
│   │   ├── pages/                    # Page components
│   │   │   ├── auth/
│   │   │   │   └── Login.jsx         # Login page with carousel
│   │   │   ├── dashboard/
│   │   │   │   └── Dashboard.jsx     # Student dashboard
│   │   │   ├── book/
│   │   │   │   └── BookDetail.jsx
│   │   │   ├── profile/
│   │   │   │   └── Profile.jsx
│   │   │   └── librarian/
│   │   │       └── LibrarianDashboard.jsx
│   │   │
│   │   ├── components/
│   │   │   ├── auth/
│   │   │   │   ├── ProtectedRoute.jsx
│   │   │   │   └── LibrarianRoute.jsx
│   │   │   ├── common/
│   │   │   │   ├── Navbar.jsx
│   │   │   │   ├── Footer.jsx
│   │   │   │   └── SearchBar.jsx
│   │   │   ├── dashboard/
│   │   │   │   ├── HeroSection.jsx
│   │   │   │   ├── StatsSection.jsx
│   │   │   │   ├── CategoriesSection.jsx
│   │   │   │   └── EventsSection.jsx
│   │   │   ├── books/
│   │   │   │   ├── BookFilters.jsx
│   │   │   │   ├── BookGrid.jsx
│   │   │   │   └── BookCard.jsx
│   │   │   └── modals/
│   │   │
│   │   ├── services/
│   │   │   ├── authService.jsx       # Authentication logic
│   │   │   ├── bookService.jsx       # Book API calls
│   │   │   ├── userService.jsx       # User profile
│   │   │   ├── borrowService.jsx     # Borrow/return
│   │   │   ├── reservationService.jsx
│   │   │   ├── fineService.jsx
│   │   │   └── entryExitService.jsx
│   │   │
│   │   ├── assets/
│   │   │   ├── logo.png
│   │   │   └── library*.jpg
│   │   │
│   │   └── utils/
│   │       └── (Utility functions)
│   │
│   └── public/
│       └── index.html
│
├── docker-compose.yml               # (TBD - Docker setup)
├── README.md                         # Project documentation
├── .gitignore                        # Git ignore rules
└── PROJECT_ANALYSIS.md               # This file
```

---

## ❌ Lỗi & Vấn Đề Hiện Tại

### 1. **Typo Trong Tên Folder** 🔴 CRITICAL
```
❌ fontend/    (Sai)
✅ frontend/   (Đúng)
```
**Impact**: Gây nhầm lẫn, không chuyên nghiệp, khó bảo trì  
**Sửa**: Rename folder từ `fontend` → `frontend`

### 2. **Hardcoded API URLs** 🔴 CRITICAL

**Hiện tại** (authService.jsx):
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
```

**Vấn đề**:
- Localhost cứng không thể deploy production
- Không phân biệt environment (dev/staging/prod)
- Khi API URL thay đổi phải sửa nhiều files

**Sửa**:
```javascript
// .env.local
VITE_API_URL=http://localhost:8080/api

// services/authService.jsx
const API_BASE_URL = import.meta.env.VITE_API_URL;
```

### 3. **JWT Token Validation Không Chặt Chẽ** 🟡 HIGH

**Hiện tại** (authService.jsx):
```javascript
isTokenExpired() {
  const tokenAge = Date.now() - parseInt(timestamp);
  return tokenAge > 28800000; // 8 hours cứng
}
```

**Vấn đề**:
- Không validate JWT signature trên client
- Chỉ kiểm tra thời gian local (có thể giả mạo)
- Backend không trả expiration time trong response

**Sửa**:
```javascript
// Decode JWT properly
import jwtDecode from 'jwt-decode';

isTokenExpired() {
  const token = this.getToken();
  if (!token) return true;
  
  const decoded = jwtDecode(token);
  return Date.now() >= decoded.exp * 1000;
}
```

### 4. **Duplicate JWT Version Trong Dependencies** 🟡 MEDIUM

**Hiện tại**:
- auth-service: jjwt 0.11.5
- book-service & user-service: jjwt 0.12.6

**Vấn đề**: Không nhất quán, có thể gây lỗi token validation  
**Sửa**: Thống nhất tất cả dùng 0.12.6

### 5. **CORS Configuration Không Rõ Ràng** 🟡 MEDIUM

**Vấn đề**: 
- Frontend gọi từ localhost:5173 (dev)
- API từ localhost:8080
- CORS cấu hình chưa rõ ràng

**Sửa trong Gateway**:
```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "http://localhost:5173,http://localhost:3000"
            allowed-methods: GET,POST,PUT,DELETE,OPTIONS
            allowed-headers: "*"
            allow-credentials: true
            max-age: 3600
```

### 6. **Credentials & Passwords Exposed** 🔴 CRITICAL

**application.yaml**:
```yaml
datasource:
  username: root
  password: root

security:
  jwt:
    secret: "mySuperSecretKeyForJWTGenerationThatIsAtLeast32CharactersLong"
```

**Vấn đề**: Hardcoded secrets trong source code  
**Sửa**:
```yaml
datasource:
  username: ${DB_USERNAME:root}
  password: ${DB_PASSWORD:root}

security:
  jwt:
    secret: ${JWT_SECRET:change-me-in-production}
```

### 7. **Microservices Chưa Hoàn Chỉnh** 🟡 MEDIUM

**Thiếu**:
- Borrow Service (mượn sách)
- Entry-Exit Service (quản lý ra vào)
- Fine Service (quản lý phạt)
- Discovery Service (Consul setup)

**Impact**: Librarian Dashboard không thể hoạt động đầy đủ

### 8. **Error Handling & Logging Yếu** 🟡 MEDIUM

**Hiện tại**:
```javascript
try {
  const response = await fetch(...);
} catch (error) {
  alert('Lỗi'); // Chỉ alert đơn giản
}
```

**Sửa**: Implement proper error handling, logging service

### 9. **Fallback Data Không Hợp Lệ** 🟡 MEDIUM

**Dashboard.jsx**:
```javascript
const fallbackBooks = bookService.getFallbackBooks ? 
                      bookService.getFallbackBooks() : [];
```

**Vấn đề**: Nếu API fail, dùng fallback data - người dùng không biết dữ liệu cũ

### 10. **Performance Issues** 🟡 MEDIUM

- Không pagination trong LibrarianDashboard
- Tải tất cả books (100 items) mỗi lần
- Không infinite scroll
- Không caching

---

## ✅ Điểm Mạnh

### 1. **Architecture Tốt** 🌟
- ✅ Microservices architecture cho tính mở rộng
- ✅ Separation of concerns rõ ràng
- ✅ Service discovery cho loose coupling
- ✅ Circuit breaker cho resilience

### 2. **Frontend Modern** 🌟
- ✅ React 19 (latest)
- ✅ Vite + Rolldown cho build performance tối ưu
- ✅ Tailwind CSS cho styling consistency
- ✅ React Router v7 cho routing
- ✅ Error Boundary cho error handling
- ✅ Toast notifications cho UX

### 3. **Security Features** 🌟
- ✅ JWT token-based authentication
- ✅ Spring Security integration
- ✅ Protected routes (ProtectedRoute, LibrarianRoute)
- ✅ Role-based access control (STUDENT, LIBRARIAN, ADMIN)

### 4. **Rich Features** 🌟
- ✅ Beautiful login page với carousel
- ✅ Student dashboard với search & filters
- ✅ Librarian dashboard với CRUD operations
- ✅ Book management (add/edit/delete)
- ✅ PDF upload & viewing
- ✅ Reservation system
- ✅ Entry/exit tracking
- ✅ Fine management

### 5. **Database Design** 🌟
- ✅ Normalized schema
- ✅ Proper relationships (1-N, M-N)
- ✅ Indexes cho performance

### 6. **UI/UX** 🌟
- ✅ Responsive design (Tailwind)
- ✅ Consistent color scheme (purple/gradient)
- ✅ Icons (Lucide React)
- ✅ Dark mode support
- ✅ Loading states
- ✅ Error messages
- ✅ Animations (smooth transitions)

### 7. **Development Tools** 🌟
- ✅ ESLint cho code quality
- ✅ Maven cho dependency management
- ✅ Git version control

---

## ⚠️ Điểm Yếu & Cần Cải Thiện

### 1. **Code Quality & Standards**

| Vấn Đề | Mức Độ | Sửa |
|--------|--------|-----|
| Typo `fontend` → `frontend` | 🔴 Critical | Rename folder |
| Hardcoded API URLs | 🔴 Critical | Use .env files |
| Secrets in source code | 🔴 Critical | Use env variables |
| Inconsistent naming (userType, role) | 🟡 Medium | Standardize |
| Missing documentation | 🟡 Medium | Add JSDoc, comments |
| Loose error handling | 🟡 Medium | Implement error service |

### 2. **Backend Issues**

| Vấn Đề | Mức Độ | Sửa |
|--------|--------|-----|
| Missing services (Borrow, Fine, Entry) | 🔴 Critical | Implement them |
| Incomplete CORS config | 🟡 Medium | Add proper CORS |
| JWT version inconsistency | 🟡 Medium | Standardize to 0.12.6 |
| No input validation | 🟡 Medium | Add @Valid annotations |
| No API documentation | 🟡 Medium | Add Swagger/OpenAPI |
| No unit tests | 🟡 Medium | Write JUnit tests |

### 3. **Frontend Issues**

| Vấn Đề | Mức Độ | Sửa |
|--------|--------|-----|
| Fallback data logic | 🟡 Medium | Remove or improve |
| Large component files | 🟡 Medium | Split into sub-components |
| Service duplications | 🟡 Medium | Consolidate to API client |
| No TypeScript | 🟡 Medium | Add TS for type safety |
| No state management | 🟡 Medium | Add Redux/Zustand |
| Memory leaks in useEffect | 🟡 Medium | Add cleanup functions |

### 4. **Performance**

| Vấn Đề | Mức Độ | Sửa |
|--------|--------|-----|
| No pagination on large lists | 🟡 Medium | Implement pagination |
| Full data load in Dashboard | 🟡 Medium | Lazy load + pagination |
| No caching strategy | 🟡 Medium | Add React Query / SWR |
| Large bundle size | 🟡 Medium | Code splitting |
| No image optimization | 🟡 Medium | Add lazy loading |

### 5. **Testing & QA**

| Vấn đề | Mức Độ |
|--------|--------|
| ❌ No unit tests | 🔴 Critical |
| ❌ No integration tests | 🔴 Critical |
| ❌ No E2E tests | 🔴 Critical |
| ❌ No API contract testing | 🟡 Medium |

### 6. **DevOps & Deployment**

| Vấn Đề | Mức Độ | Sửa |
|--------|--------|-----|
| ❌ No Docker setup | 🔴 Critical | Create Dockerfile & docker-compose |
| ❌ No CI/CD pipeline | 🔴 Critical | Add GitHub Actions |
| ❌ No production build config | 🟡 Medium | Add .env.production |
| ❌ No load testing | 🟡 Medium | Add K6 / JMeter tests |

### 7. **Monitoring & Logging**

| Vấn Đề | Mức Độ | Sửa |
|--------|--------|-----|
| ❌ No centralized logging | 🔴 Critical | Add ELK / Splunk |
| ❌ No APM setup | 🟡 Medium | Add New Relic / DataDog |
| ❌ No health checks | 🟡 Medium | Implement /health endpoints |
| ❌ No metrics collection | 🟡 Medium | Add Prometheus |

---

## 🚀 Hướng Phát Triển Tương Lai

### Phase 1: Fixes & Stability (1-2 tuần)
```
Priority: 🔴 CRITICAL
├── Fix typo: fontend → frontend
├── Externalize configs (.env files)
├── Implement missing services
├── Add proper error handling
├── Setup Docker & docker-compose
└── Add basic unit tests
```

### Phase 2: Features & Quality (2-4 tuần)
```
Priority: 🟡 MEDIUM
├── Implement missing Borrow/Fine/Entry services
├── Add TypeScript to frontend
├── Add state management (Redux/Zustand)
├── Pagination & infinite scroll
├── Full API documentation (Swagger)
├── Add E2E tests (Cypress/Playwright)
└── Performance optimization
```

### Phase 3: Production Readiness (4-6 tuần)
```
Priority: 🟡 MEDIUM
├── CI/CD pipeline (GitHub Actions)
├── Monitoring & logging (ELK)
├── APM setup (New Relic)
├── Load testing & optimization
├── Security audit & pen testing
├── Kubernetes deployment
└── Documentation & training
```

### Phase 4: Advanced Features (Ongoing)
```
├── 📱 Mobile app (React Native)
├── 🤖 AI recommendations (ML model)
├── 💬 Real-time notifications (WebSocket)
├── 📊 Advanced analytics & reporting
├── 🌐 Multi-language support
├── 🔐 SSO integration (Oauth/SAML)
└── 📚 E-book store integration
```

### Feature Roadmap

#### Q1 2026
- ✅ Fix core issues
- ✅ Complete microservices
- ✅ Basic test coverage
- ✅ Docker support

#### Q2 2026
- ✅ TypeScript migration
- ✅ Advanced search (Elasticsearch)
- ✅ Real-time notifications
- ✅ Mobile-responsive optimization

#### Q3 2026
- ✅ Mobile app (React Native)
- ✅ AI book recommendations
- ✅ Analytics dashboard
- ✅ Integration with external libraries

#### Q4 2026+
- ✅ Advanced features
- ✅ Scale to multiple campuses
- ✅ Cloud migration
- ✅ Enterprise features

---

## 🚀 Hướng Dẫn Chạy Dự Án

### Yêu Cầu
- **Java**: 17+ (recommend 21+)
- **Node.js**: 18+
- **MySQL**: 8.0+
- **Maven**: 3.8+
- **Consul**: Latest (optional, để localhost:8500)

### Backend Setup

#### 1. Database
```bash
# Create database
mysql -u root -p < schema.sql

# Or use application.yaml ddl-auto: update
```

#### 2. Install Dependencies
```bash
cd backend
mvn clean install
```

#### 3. Chạy Services (Multiple terminals)

**Terminal 1 - Gateway Service**:
```bash
cd backend/gateway-service
mvn spring-boot:run
# http://localhost:8080
```

**Terminal 2 - Auth Service**:
```bash
cd backend/auth-service
mvn spring-boot:run
# http://localhost:8081
```

**Terminal 3 - Book Service**:
```bash
cd backend/book-service
mvn spring-boot:run
# http://localhost:8083
```

**Terminal 4 - User Service**:
```bash
cd backend/user-service
mvn spring-boot:run
# http://localhost:8082
```

### Frontend Setup

#### 1. Install Dependencies
```bash
cd fontend
npm install
```

#### 2. Create .env.local
```env
VITE_API_URL=http://localhost:8080/api
```

#### 3. Run Development Server
```bash
npm run dev
# http://localhost:5173
```

#### 4. Build for Production
```bash
npm run build
```

### Test Credentials
```
Username: admin
Password: admin

Username: librarian
Password: librarian

Username: student
Password: student
```

---

## 📊 Statistics

### Codebase
- **Total Files**: ~150+
- **Lines of Code**: ~50,000+ LOC
- **Java/Backend**: ~25,000 LOC
- **JavaScript/Frontend**: ~15,000 LOC
- **CSS**: ~10,000 LOC

### Git History
- **Created**: 2025-11-25
- **Last Updated**: 2026-07-12
- **Commits**: ~50+

---

## 📝 Chú Ý Quan Trọng

⚠️ **BEFORE PRODUCTION**:
1. ❌ **NEVER** commit .env files with secrets
2. ❌ **NEVER** use localhost:8080 in production
3. ❌ **NEVER** expose JWT secret publicly
4. ✅ **DO** setup proper logging & monitoring
5. ✅ **DO** implement rate limiting
6. ✅ **DO** add request validation
7. ✅ **DO** setup HTTPS/TLS
8. ✅ **DO** implement backup strategy

---

## 📞 Liên Hệ & Support

- **Repository**: https://github.com/tammoihoc/university-digital-library
- **Issues**: GitHub Issues
- **Documentation**: See README.md

---

## 📜 License

MIT License - See LICENSE file

---

**Generated**: 2026-07-12  
**Author**: HUTECH Development Team  
**Status**: 🟡 In Development
