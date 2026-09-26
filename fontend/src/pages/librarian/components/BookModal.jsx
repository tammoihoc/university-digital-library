import React, { useState, useEffect } from 'react';
import bookService from '../../../services/bookService';

const BookModal = ({
  editingBook,
  bookFormData = {},
  setBookFormData,
  coverPreview,
  coverFile,
  pdfFile,
  handleCoverChange,
  handlePdfChange,        // prop từ cha – chỉ set pdfFile state
  handleSaveBook,
  savingBook,
  onClose
}) => {
  const [imageError, setImageError] = useState(false);
  const [branches, setBranches] = useState([]);
  const [loadingBranches, setLoadingBranches] = useState(true);

  // Lấy danh sách chi nhánh
  useEffect(() => {
    const fetchBranches = async () => {
      setLoadingBranches(true);
      const data = await bookService.getBranches();
      setBranches(data);
      setLoadingBranches(false);
    };
    fetchBranches();
  }, []);

  if (!bookFormData || typeof bookFormData !== 'object') {
    return (
      <div className="ld-modal-overlay" onClick={onClose}>
        <div className="ld-modal" onClick={(e) => e.stopPropagation()}>
          <div className="ld-modal-body" style={{ textAlign: 'center', padding: '40px' }}>
            <p>Đang tải dữ liệu...</p>
          </div>
        </div>
      </div>
    );
  }

  // Lấy tên file PDF hiện tại nếu có
  const currentPdfName = editingBook?.pdfFileUrl
    ? editingBook.pdfFileUrl.split('/').pop()
    : null;

  const coverImageUrl = coverPreview || null;

  // --- HANDLERS ---
  const handleInputChange = (e) => {
    const { name, value, type } = e.target;
    setBookFormData({
      ...bookFormData,
      [name]: type === 'number' ? (value === '' ? '' : Number(value)) : value
    });
  };

  const handleBranchChange = (e) => {
    setBookFormData({
      ...bookFormData,
      libraryBranchId: parseInt(e.target.value) || 1
    });
  };

  const handleSelectChange = (e) => {
    const { name, value } = e.target;
    setBookFormData({
      ...bookFormData,
      [name]: value
    });
  };

  // ----- XỬ LÝ PDF: upload và lấy số trang ngay khi chọn file -----
  const handleLocalPdfChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (handlePdfChange) handlePdfChange(e);

    if (editingBook && editingBook.id) {
        try {
            await bookService.uploadPdf(editingBook.id, file);
            console.log('✅ Upload PDF thành công, đang lấy thông tin...');
            await new Promise(resolve => setTimeout(resolve, 500)); // tăng delay lên 500ms
            const pdfInfo = await bookService.getPdfInfo(editingBook.id);
            console.log('📄 PDF Info:', pdfInfo);
            if (pdfInfo) {
                let newPages = pdfInfo.pages;
                if (!newPages || newPages === 0) {
                    newPages = pdfInfo.pageCountFromFile || 0;
                }
                if (newPages && newPages > 0) {
                    setBookFormData(prev => ({
                        ...prev,
                        pages: newPages
                    }));
                    console.log(`✅ Đã cập nhật số trang: ${newPages}`);
                } else {
                    console.warn('⚠️ Không lấy được số trang từ PDF');
                }
            }
        } catch (error) {
            console.error('❌ Lỗi xử lý PDF:', error);
            alert('Lỗi upload hoặc lấy số trang PDF: ' + error.message);
        }
    }
};
  // ---------- RENDER ----------
  return (
    <div className="ld-modal-overlay" onClick={onClose}>
      <div className="ld-modal ld-modal-large" onClick={(e) => e.stopPropagation()}>
        <div className="ld-modal-header">
          <h3>{editingBook ? '✏️ Sửa sách' : '📚 Thêm sách mới'}</h3>
          <button className="ld-modal-close" onClick={onClose}>×</button>
        </div>

        <div className="ld-modal-body">
          {/* --- Form fields --- */}
          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Tên sách <span className="required">*</span></label>
              <input
                type="text"
                name="title"
                value={bookFormData.title || ''}
                onChange={handleInputChange}
                placeholder="Nhập tên sách"
              />
            </div>
            <div className="ld-form-group">
              <label>Tác giả <span className="required">*</span></label>
              <input
                type="text"
                name="author"
                value={bookFormData.author || ''}
                onChange={handleInputChange}
                placeholder="Nhập tên tác giả"
              />
            </div>
          </div>

          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>ISBN</label>
              <input
                type="text"
                name="isbn"
                value={bookFormData.isbn || ''}
                onChange={handleInputChange}
                placeholder="Mã ISBN"
              />
            </div>
            <div className="ld-form-group">
              <label>Nhà xuất bản</label>
              <input
                type="text"
                name="publisher"
                value={bookFormData.publisher || ''}
                onChange={handleInputChange}
                placeholder="Nhà xuất bản"
              />
            </div>
          </div>

          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Năm xuất bản</label>
              <input
                type="number"
                name="publicationYear"
                value={bookFormData.publicationYear || ''}
                onChange={handleInputChange}
              />
            </div>
          <div className="ld-form-group">
            <label>Số trang</label>
            <input
              type="number"
              name="pages"
              value={bookFormData.pages || ''}
              onChange={handleInputChange}
              placeholder="Sẽ tự động cập nhật từ PDF"
            />
            <small style={{ color: '#64748b', fontSize: '11px' }}>
              {editingBook?.pdfFileUrl
                ? '📄 Đã có PDF (chọn file mới để cập nhật số trang)'
                : 'Tải PDF để tự động lấy số trang'}
            </small>
          </div>
          </div>

          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Số lượng bản</label>
              <input
                type="number"
                name="totalPhysicalCopies"
                value={bookFormData.totalPhysicalCopies || ''}
                onChange={handleInputChange}
              />
            </div>
            <div className="ld-form-group">
              <label>Giá (VNĐ)</label>
              <input
                type="number"
                name="price"
                value={bookFormData.price || ''}
                onChange={handleInputChange}
              />
            </div>
          </div>

          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Loại sách</label>
              <select
                name="type"
                value={bookFormData.type || 'NON_FICTION'}
                onChange={handleSelectChange}
              >
                <option value="NON_FICTION">Non-Fiction</option>
                <option value="FICTION">Fiction</option>
                <option value="TEXTBOOK">Textbook</option>
                <option value="REFERENCE">Reference</option>
                <option value="THESIS">Thesis</option>
                <option value="RESEARCH_PAPER">Research Paper</option>
                <option value="MAGAZINE">Magazine</option>
              </select>
            </div>
            <div className="ld-form-group">
              <label>Hình thức truy cập</label>
              <select
                name="accessType"
                value={bookFormData.accessType || 'HYBRID'}
                onChange={handleSelectChange}
              >
                <option value="PHYSICAL_ONLY">Chỉ mượn vật lý</option>
                <option value="FULL_DIGITAL">Chỉ đọc online</option>
                <option value="HYBRID">Cả hai</option>
                <option value="PREVIEW_ONLY">Xem trước</option>
              </select>
            </div>
          </div>

          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Ngôn ngữ</label>
              <select
                name="language"
                value={bookFormData.language || 'vi'}
                onChange={handleSelectChange}
              >
                <option value="vi">Tiếng Việt</option>
                <option value="en">Tiếng Anh</option>
                <option value="zh">Tiếng Trung</option>
                <option value="ja">Tiếng Nhật</option>
                <option value="ko">Tiếng Hàn</option>
                <option value="fr">Tiếng Pháp</option>
                <option value="de">Tiếng Đức</option>
              </select>
            </div>
            <div className="ld-form-group">
              <label>Chi nhánh</label>
              <select
                name="libraryBranchId"
                value={bookFormData.libraryBranchId || 1}
                onChange={handleBranchChange}
                disabled={loadingBranches}
              >
                {loadingBranches ? (
                  <option>Đang tải...</option>
                ) : (
                  branches.map(branch => (
                    <option key={branch.id} value={branch.id}>
                      🏢 {branch.name} - {branch.address}
                    </option>
                  ))
                )}
              </select>
            </div>
          </div>

          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Khoa/Viện</label>
              <input
                type="text"
                name="department"
                value={bookFormData.department || ''}
                onChange={handleInputChange}
                placeholder="VD: Công nghệ thông tin"
              />
            </div>
            <div className="ld-form-group">
              <label>Mã học phần</label>
              <input
                type="text"
                name="courseCode"
                value={bookFormData.courseCode || ''}
                onChange={handleInputChange}
              />
            </div>
          </div>

          <div className="ld-form-group">
            <label>Thể loại (cách nhau bằng dấu phẩy)</label>
            <input
              type="text"
              name="categories"
              value={bookFormData.categories || ''}
              onChange={handleInputChange}
              placeholder="Lập trình, Khoa học máy tính, AI"
            />
          </div>

          <div className="ld-form-group">
            <label>Mô tả</label>
            <textarea
              rows="3"
              name="description"
              value={bookFormData.description || ''}
              onChange={handleInputChange}
              placeholder="Nhập mô tả sách..."
            />
          </div>

          {/* --- ẢNH BÌA & PDF --- */}
          <div className="ld-form-row">
            <div className="ld-form-group">
              <label>Ảnh bìa</label>
              {coverImageUrl && (
                <div style={{ marginBottom: '8px' }}>
                  <img
                    src={coverImageUrl}
                    alt="Cover"
                    className="ld-preview-img"
                    style={{ maxWidth: '150px', maxHeight: '150px', objectFit: 'cover', borderRadius: '8px' }}
                    onError={() => setImageError(true)}
                  />
                  {!imageError && (
                    <div style={{ fontSize: '12px', color: '#64748b', marginTop: '4px' }}>
                      ✅ Ảnh hiện tại
                    </div>
                  )}
                  {imageError && (
                    <div style={{ fontSize: '12px', color: '#ef4444', marginTop: '4px' }}>
                      ❌ Không thể tải ảnh
                    </div>
                  )}
                </div>
              )}
              {coverFile && (
                <div style={{ marginBottom: '8px' }}>
                  <img
                    src={URL.createObjectURL(coverFile)}
                    alt="New cover"
                    className="ld-preview-img"
                    style={{ maxWidth: '150px', maxHeight: '150px', objectFit: 'cover', borderRadius: '8px' }}
                  />
                  <div style={{ fontSize: '12px', color: '#64748b', marginTop: '4px' }}>
                    📸 Ảnh mới (chưa lưu)
                  </div>
                </div>
              )}
              <input type="file" accept="image/*" onChange={handleCoverChange} />
              <small style={{ color: '#64748b', fontSize: '11px' }}>
                {coverImageUrl ? 'Chọn file mới để thay đổi ảnh' : 'Chưa có ảnh bìa'}
              </small>
            </div>

            <div className="ld-form-group">
              <label>File PDF</label>
              {editingBook?.pdfFileUrl && !pdfFile && (
                <div style={{ marginBottom: '8px', fontSize: '13px', color: '#0f1b33' }}>
                  📄 <strong>PDF hiện tại:</strong> {currentPdfName}
                </div>
              )}
              {pdfFile && (
                <div style={{ marginBottom: '8px', fontSize: '13px', color: '#0f1b33' }}>
                  📄 <strong>Đã chọn:</strong> {pdfFile.name}
                </div>
              )}
              {/* GỌI HÀM handleLocalPdfChange thay vì handlePdfChange */}
              <input type="file" accept=".pdf" onChange={handleLocalPdfChange} />
              <small style={{ color: '#64748b', fontSize: '11px' }}>
                {editingBook?.pdfFileUrl
                  ? '✅ Đã có PDF (chọn file mới để thay thế và cập nhật số trang)'
                  : 'Chưa có PDF'}
              </small>
            </div>
          </div>
        </div>

        <div className="ld-modal-footer">
          <button className="ld-btn-secondary" onClick={onClose}>Hủy</button>
          <button className="ld-btn-primary" onClick={handleSaveBook} disabled={savingBook}>
            {savingBook ? 'Đang lưu...' : (editingBook ? 'Cập nhật' : 'Thêm sách')}
          </button>
        </div>
      </div>
    </div>
  );
};

export default BookModal;
