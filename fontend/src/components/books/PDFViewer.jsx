// src/components/books/PDFViewer.jsx
import React, { useState, useEffect } from 'react';
import { 
  Maximize2, Minimize2, ZoomIn, ZoomOut, 
  Download, ExternalLink, FileText, X 
} from 'lucide-react';
import './PDFViewer.css';

const PDFViewer = ({ book, pdfUrl, onClose }) => {
  const [zoomLevel, setZoomLevel] = useState(100);
  const [isFullscreen, setIsFullscreen] = useState(false);

  const handleZoomIn = () => {
    setZoomLevel(prev => Math.min(prev + 25, 200));
  };

  const handleZoomOut = () => {
    setZoomLevel(prev => Math.max(prev - 25, 50));
  };

  const handleResetZoom = () => {
    setZoomLevel(100);
  };

  const toggleFullscreen = () => {
    const element = document.getElementById('pdf-embed-container');
    if (!element) return;
    
    if (!isFullscreen) {
      if (element.requestFullscreen) {
        element.requestFullscreen();
      } else if (element.webkitRequestFullscreen) {
        element.webkitRequestFullscreen();
      } else if (element.msRequestFullscreen) {
        element.msRequestFullscreen();
      }
    } else {
      if (document.exitFullscreen) {
        document.exitFullscreen();
      } else if (document.webkitExitFullscreen) {
        document.webkitExitFullscreen();
      } else if (document.msExitFullscreen) {
        document.msExitFullscreen();
      }
    }
  };

  useEffect(() => {
    const handleFullscreenChange = () => {
      setIsFullscreen(!!document.fullscreenElement);
    };
    
    document.addEventListener('fullscreenchange', handleFullscreenChange);
    document.addEventListener('webkitfullscreenchange', handleFullscreenChange);
    document.addEventListener('msfullscreenchange', handleFullscreenChange);
    
    return () => {
      document.removeEventListener('fullscreenchange', handleFullscreenChange);
      document.removeEventListener('webkitfullscreenchange', handleFullscreenChange);
      document.removeEventListener('msfullscreenchange', handleFullscreenChange);
    };
  }, []);

const handleDownload = async () => {
  try {
    // Sử dụng service để tải blob
    const blob = await bookService.getPdfBlob(book.id);
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${book.title.replace(/[^a-z0-9]/gi, '_')}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('Download error:', error);
    alert('Lỗi khi tải PDF: ' + error.message);
  }
};

  return (
    <div id="pdf-viewer-section" className="pdf-viewer-section">
      <div className="pdf-viewer-header">
        <FileText size={24} />
        <h3>Đang xem: {book.title}</h3>
        <button 
          className="close-pdf-btn"
          onClick={onClose}
        >
          <X size={16} /> Đóng
        </button>
      </div>
      
      <div id="pdf-embed-container" className="pdf-embed-container">
        <div className="pdf-controls-toolbar">
          <div className="pdf-controls-left">
            <button onClick={handleZoomOut} title="Thu nhỏ">
              <ZoomOut size={18} />
            </button>
            <span className="zoom-level">{zoomLevel}%</span>
            <button onClick={handleZoomIn} title="Phóng to">
              <ZoomIn size={18} />
            </button>
            <button onClick={handleResetZoom} title="Đặt lại zoom">
              100%
            </button>
          </div>
          
          <div className="pdf-controls-center">
            <span>PDF Viewer</span>
          </div>
          
          <div className="pdf-controls-right">
            <button onClick={toggleFullscreen} title="Toàn màn hình">
              {isFullscreen ? <Minimize2 size={18} /> : <Maximize2 size={18} />}
            </button>
            <button onClick={() => window.open(pdfUrl, '_blank')} title="Mở tab mới">
              <ExternalLink size={18} />
            </button>
            <button onClick={handleDownload} title="Tải xuống">
              <Download size={18} />
            </button>
          </div>
        </div>
        
        <div className="pdf-embed-wrapper" style={{ height: '600px' }}>
          <iframe
  src={`${pdfUrl}#toolbar=0&navpanes=0&scrollbar=1`}
  className="pdf-embed"
  title={`PDF của sách: ${book.title}`}
  style={{
    width: `${zoomLevel}%`,
    height: '100%',
    transformOrigin: 'top left'
  }}
/>
        </div>
        
        <div className="pdf-embed-info">
          <p><strong>📄 Tên file:</strong> {book.title}.pdf</p>
          <p><strong>💡 Mẹo:</strong> Dùng Ctrl + Scroll để zoom nhanh trong PDF viewer</p>
        </div>
      </div>
    </div>
  );
};

export default PDFViewer;
