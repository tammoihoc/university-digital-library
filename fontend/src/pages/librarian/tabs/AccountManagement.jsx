import React, { useState, useEffect } from 'react';
import { Users, Search, Lock, Unlock, RefreshCw, Plus, Edit, X, Save } from 'lucide-react';

const fallbackUsers = [
  { id: 1, username: 'admin', fullName: 'Quản trị viên', email: 'admin@hutech.edu.vn', role: 'ADMIN', isLocked: false },
  { id: 2, username: 'librarian', fullName: 'Thủ thư chính', email: 'librarian@hutech.edu.vn', role: 'LIBRARIAN', isLocked: false },
  { id: 3, username: 'student1', fullName: 'Nguyễn Văn A', email: 'student1@hutech.edu.vn', role: 'STUDENT', isLocked: false },
];

const AccountManagement = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [error, setError] = useState('');

  const [showModal, setShowModal] = useState(false);
  const [isEditMode, setIsEditMode] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const [formData, setFormData] = useState({
    username: '',
    fullName: '',
    firstName: '',
    lastName: '',
    email: '',
    phone: '',
    address: '',
    dateOfBirth: '',
    userType: 'STUDENT',
    studentId: '',
    faculty: '',
    major: '',
    academicYear: new Date().getFullYear(),
    password: '',
  });
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    setLoading(true);
    setError('');
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('http://localhost:8080/api/users/all', {
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        const data = await response.json();
        setUsers(data);
      } else {
        console.warn('⚠️ API users/all not available, using fallback');
        setUsers(fallbackUsers);
      }
    } catch (error) {
      console.error('Error loading users:', error);
      setError('Không thể tải danh sách tài khoản, hiển thị dữ liệu mẫu');
      setUsers(fallbackUsers);
    } finally {
      setLoading(false);
    }
  };

  const handleLockUser = async (username) => {
    if (!window.confirm(`Bạn có chắc muốn khóa tài khoản ${username}?`)) return;
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/users/${username}/lock`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
        body: JSON.stringify({ days: 30, reason: 'Vi phạm quy định' }),
      });
      if (response.ok) {
        alert(`✅ Đã khóa tài khoản ${username}`);
        loadUsers();
      } else {
        alert('❌ Khóa tài khoản thất bại');
      }
    } catch (error) {
      console.error('Error locking user:', error);
      alert('❌ Khóa tài khoản thất bại');
    }
  };

  const handleUnlockUser = async (username) => {
    if (!window.confirm(`Bạn có chắc muốn mở khóa tài khoản ${username}?`)) return;
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`http://localhost:8080/api/users/${username}/unlock`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` },
      });
      if (response.ok) {
        alert(`✅ Đã mở khóa tài khoản ${username}`);
        loadUsers();
      } else {
        alert('❌ Mở khóa tài khoản thất bại');
      }
    } catch (error) {
      console.error('Error unlocking user:', error);
      alert('❌ Mở khóa tài khoản thất bại');
    }
  };

  const handleOpenAddModal = () => {
    setIsEditMode(false);
    setEditingUser(null);
    setFormData({
      username: '',
      fullName: '',
      firstName: '',
      lastName: '',
      email: '',
      phone: '',
      address: '',
      dateOfBirth: '',
      userType: 'STUDENT',
      studentId: '',
      faculty: '',
      major: '',
      academicYear: new Date().getFullYear(),
      password: '',
    });
    setShowModal(true);
  };

  const handleOpenEditModal = (user) => {
    setIsEditMode(true);
    setEditingUser(user);
    let firstName = user.firstName || '';
    let lastName = user.lastName || '';
    if (!firstName && !lastName && user.fullName) {
      const parts = user.fullName.trim().split(' ');
      lastName = parts.slice(0, -1).join(' ') || parts[0];
      firstName = parts[parts.length - 1] || '';
    }
    let dateOfBirth = '';
    if (user.dateOfBirth) {
      try {
        const d = new Date(user.dateOfBirth);
        if (!isNaN(d.getTime())) dateOfBirth = d.toISOString().split('T')[0];
      } catch (e) { /* ignore */ }
    }
    setFormData({
      username: user.username || '',
      fullName: user.fullName || '',
      firstName: firstName,
      lastName: lastName,
      email: user.email || '',
      phone: user.phone || '',
      address: user.address || '',
      dateOfBirth: dateOfBirth,
      userType: user.userType || user.role || 'STUDENT',
      studentId: user.studentId || '',
      faculty: user.faculty || '',
      major: user.major || '',
      academicYear: user.academicYear || new Date().getFullYear(),
      password: '',
    });
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setEditingUser(null);
  };

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    if (name === 'fullName') {
      const parts = value.trim().split(' ');
      const lastName = parts.slice(0, -1).join(' ') || '';
      const firstName = parts[parts.length - 1] || '';
      setFormData(prev => ({
        ...prev,
        fullName: value,
        firstName,
        lastName,
      }));
    } else {
      setFormData(prev => ({ ...prev, [name]: value }));
    }
  };

  const handleSubmitForm = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const token = localStorage.getItem('token');
      if (isEditMode) {
        // Cập nhật profile và userType
        const updateData = {
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          phone: formData.phone,
          address: formData.address,
          dateOfBirth: formData.dateOfBirth || null,
          studentId: formData.studentId,
          faculty: formData.faculty,
          major: formData.major,
          academicYear: parseInt(formData.academicYear),
          userType: formData.userType,
        };
        const response = await fetch(`http://localhost:8080/api/users/profile/${formData.username}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
          body: JSON.stringify(updateData),
        });
        if (response.ok) {
          alert('✅ Cập nhật tài khoản thành công!');
          handleCloseModal();
          loadUsers();
        } else {
          const errText = await response.text();
          alert('❌ Cập nhật thất bại: ' + errText);
        }
      } else {
        // 1. Tạo auth user
        const registerData = {
          username: formData.username,
          password: formData.password || '123456',
          roles: [formData.userType],
        };
        const authResponse = await fetch('http://localhost:8080/api/auth/register', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(registerData),
        });
        if (!authResponse.ok) {
          const errText = await authResponse.text();
          if (errText.includes('already exists')) {
            alert('❌ Username đã tồn tại, vui lòng chọn tên khác');
          } else {
            alert('❌ Tạo user auth thất bại: ' + errText);
          }
          throw new Error('Tạo user auth thất bại: ' + errText);
        }

        // 2. Tạo profile
        const profileData = {
          username: formData.username,
          firstName: formData.firstName,
          lastName: formData.lastName,
          email: formData.email,
          phone: formData.phone,
          address: formData.address,
          dateOfBirth: formData.dateOfBirth || null,
          userType: formData.userType,
          studentId: formData.studentId,
          faculty: formData.faculty,
          major: formData.major,
          academicYear: parseInt(formData.academicYear),
        };
        const profileResponse = await fetch('http://localhost:8080/api/users/profile', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
          body: JSON.stringify(profileData),
        });
        if (profileResponse.ok) {
          alert('✅ Thêm tài khoản thành công!');
          handleCloseModal();
          loadUsers();
        } else {
          const errText = await profileResponse.text();
          alert('❌ Tạo profile thất bại: ' + errText);
        }
      }
    } catch (error) {
      console.error('Error saving user:', error);
      // Không alert thêm vì đã alert ở trên
    } finally {
      setSubmitting(false);
    }
  };

  const filteredUsers = users.filter(
    (u) =>
      u.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      u.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      u.fullName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  if (loading) {
    return (
      <div className="ld-loading-screen">
        <div className="ld-loading-spinner"></div>
        <p>Đang tải danh sách...</p>
      </div>
    );
  }

  return (
    <div>
      <div className="ld-toolbar">
        <div className="ld-search-box">
          <Search size={18} className="ld-search-icon" />
          <input
            type="text"
            placeholder="Tìm kiếm tài khoản..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div style={{ display: 'flex', gap: '8px' }}>
          <button className="ld-btn-primary" onClick={handleOpenAddModal}>
            <Plus size={16} /> Thêm tài khoản
          </button>
          <button className="ld-btn-secondary" onClick={loadUsers}>
            <RefreshCw size={16} /> Làm mới
          </button>
        </div>
      </div>

      {error && <div className="ld-error-message">{error}</div>}

      <div className="ld-recent-section">
        <div className="ld-section-header">
          <h3><Users size={18} /> Quản lý tài khoản</h3>
          <span className="ld-badge-count">{filteredUsers.length} tài khoản</span>
        </div>
        <div className="ld-table-wrapper">
          <table className="ld-data-table">
            <thead>
              <tr>
                <th>Tên đăng nhập</th>
                <th>Họ tên</th>
                <th>Email</th>
                <th>Vai trò</th>
                <th>Trạng thái</th>
                <th>Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.map((u) => (
                <tr key={u.id || u.username}>
                  <td><span className="ld-user-code">{u.username}</span></td>
                  <td>{u.fullName || u.firstName || 'N/A'}</td>
                  <td>{u.email || 'N/A'}</td>
                  <td><span className="ld-status-badge status-active">{u.role || u.userType || 'STUDENT'}</span></td>
                  <td>
                    <span className={`ld-status-badge ${u.isLocked ? 'status-overdue' : 'status-returned'}`}>
                      {u.isLocked ? '🔒 Đã khóa' : '✅ Hoạt động'}
                    </span>
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                      <button className="ld-action-btn edit" onClick={() => handleOpenEditModal(u)}>
                        <Edit size={14} /> Sửa
                      </button>
                      {u.isLocked ? (
                        <button className="ld-action-btn edit" onClick={() => handleUnlockUser(u.username)}>
                          <Unlock size={14} /> Mở khóa
                        </button>
                      ) : (
                        <button className="ld-action-btn delete" onClick={() => handleLockUser(u.username)}>
                          <Lock size={14} /> Khóa
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {filteredUsers.length === 0 && <tr><td colSpan="6" className="text-center">Không tìm thấy tài khoản</td></tr>}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <div className="ld-modal-overlay" onClick={handleCloseModal}>
          <div className="ld-modal ld-modal-large" onClick={(e) => e.stopPropagation()}>
            <div className="ld-modal-header">
              <h3>{isEditMode ? '✏️ Sửa tài khoản' : '➕ Thêm tài khoản mới'}</h3>
              <button className="ld-modal-close" onClick={handleCloseModal}>×</button>
            </div>
            <div className="ld-modal-body">
              <form onSubmit={handleSubmitForm}>
                <div className="ld-form-row">
                  <div className="ld-form-group">
                    <label>Tên đăng nhập <span className="required">*</span></label>
                    <input
                      type="text"
                      name="username"
                      value={formData.username}
                      onChange={handleFormChange}
                      disabled={isEditMode}
                      placeholder="Nhập username"
                      required={!isEditMode}
                    />
                  </div>
                  <div className="ld-form-group">
                    <label>Họ và tên</label>
                    <input
                      type="text"
                      name="fullName"
                      value={formData.fullName}
                      onChange={handleFormChange}
                      placeholder="Nhập họ và tên"
                    />
                  </div>
                </div>
                <div className="ld-form-row">
                  <div className="ld-form-group">
                    <label>Email</label>
                    <input type="email" name="email" value={formData.email} onChange={handleFormChange} placeholder="email@hutech.edu.vn" />
                  </div>
                  <div className="ld-form-group">
                    <label>Số điện thoại</label>
                    <input type="text" name="phone" value={formData.phone} onChange={handleFormChange} placeholder="Số điện thoại" />
                  </div>
                </div>
                <div className="ld-form-row">
                  <div className="ld-form-group">
                    <label>Địa chỉ</label>
                    <input type="text" name="address" value={formData.address} onChange={handleFormChange} placeholder="Địa chỉ" />
                  </div>
                  <div className="ld-form-group">
                    <label>Ngày sinh</label>
                    <input type="date" name="dateOfBirth" value={formData.dateOfBirth} onChange={handleFormChange} />
                  </div>
                </div>
                <div className="ld-form-row">
                  <div className="ld-form-group">
                    <label>Vai trò</label>
                    <select
                      name="userType"
                      value={formData.userType}
                      onChange={handleFormChange}
                    >
                      <option value="ADMIN">ADMIN</option>
                      <option value="LIBRARIAN">LIBRARIAN</option>
                      <option value="STUDENT">STUDENT</option>
                      <option value="LECTURER">LECTURER</option>
                    </select>
                  </div>
                  <div className="ld-form-group">
                    <label>MSSV (nếu là sinh viên)</label>
                    <input type="text" name="studentId" value={formData.studentId} onChange={handleFormChange} placeholder="Mã số sinh viên" />
                  </div>
                </div>
                <div className="ld-form-row">
                  <div className="ld-form-group">
                    <label>Khoa/Viện</label>
                    <input type="text" name="faculty" value={formData.faculty} onChange={handleFormChange} placeholder="Khoa/Viện" />
                  </div>
                  <div className="ld-form-group">
                    <label>Chuyên ngành</label>
                    <input type="text" name="major" value={formData.major} onChange={handleFormChange} placeholder="Chuyên ngành" />
                  </div>
                </div>
                <div className="ld-form-row">
                  <div className="ld-form-group">
                    <label>Năm học</label>
                    <input type="number" name="academicYear" value={formData.academicYear} onChange={handleFormChange} placeholder="2025" />
                  </div>
                  {!isEditMode && (
                    <div className="ld-form-group">
                      <label>Mật khẩu <span className="required">*</span></label>
                      <input type="password" name="password" value={formData.password} onChange={handleFormChange} placeholder="Nhập mật khẩu (mặc định: 123456)" required={!isEditMode} />
                      <small style={{ color: '#888' }}>Mật khẩu mặc định là 123456 nếu để trống</small>
                    </div>
                  )}
                </div>
                <div className="ld-modal-footer" style={{ marginTop: '16px' }}>
                  <button type="button" className="ld-btn-secondary" onClick={handleCloseModal}><X size={16} /> Hủy</button>
                  <button type="submit" className="ld-btn-primary" disabled={submitting}>
                    {submitting ? 'Đang lưu...' : <Save size={16} />}
                    {isEditMode ? ' Cập nhật' : ' Thêm tài khoản'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AccountManagement;
