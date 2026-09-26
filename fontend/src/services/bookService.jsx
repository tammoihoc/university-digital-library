const GATEWAY_URL = 'http://localhost:8080';

const bookService = {
  async getAllBooks(page = 0, size = 20, sortBy = 'createdAt', direction = 'desc') {
    try {
      console.log('📚 Fetching books...');
      
      const token = localStorage.getItem('token');
      const url = `${GATEWAY_URL}/api/books?page=${page}&size=${size}&sortBy=${sortBy}&direction=${direction}`;
      console.log('URL:', url);
      
      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      console.log('Response status:', response.status);
      
      if (!response.ok) {
        const errorText = await response.text();
        console.error('Error response:', errorText);
        throw new Error(`HTTP ${response.status}`);
      }
      
      const data = await response.json();
      console.log('Books data:', data);
      
      if (data && data.content) {
        return {
          content: data.content,
          totalElements: data.totalElements || 0,
          totalPages: data.totalPages || 1,
          size: data.size || size,
          number: data.number || page,
          first: data.first || true,
          last: data.last || true,
          empty: data.empty || false
        };
      }
      
      if (Array.isArray(data)) {
        return {
          content: data,
          totalElements: data.length,
          totalPages: 1,
          size: data.length,
          number: 0,
          first: true,
          last: true,
          empty: data.length === 0
        };
      }
      
      console.warn('No data, using fallback');
      return this.getFallbackBooksResponse();
      
    } catch (error) {
      console.error('Error fetching books:', error.message);
      return this.getFallbackBooksResponse();
    }
  },
async uploadPdf(bookId, file) {
    const formData = new FormData();
    formData.append('file', file);
    const token = localStorage.getItem('token');
    const response = await fetch(`${GATEWAY_URL}/api/books/${bookId}/upload-pdf`, {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
        body: formData
    });
    if (!response.ok) throw new Error('Upload PDF failed');
    return await response.json();
},

async getPdfInfo(bookId) {
    const token = localStorage.getItem('token');
    const response = await fetch(`${GATEWAY_URL}/api/books/${bookId}/pdf-info`, {
        headers: { 'Authorization': `Bearer ${token}` }
    });
    if (!response.ok) throw new Error('Failed to fetch PDF info');
    return await response.json();
},
  async searchBooks(keyword, page = 0, size = 20) {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`${GATEWAY_URL}/api/books/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      
      const data = await response.json();
      
      if (data && data.content) {
        return {
          content: data.content,
          totalElements: data.totalElements || 0,
          totalPages: data.totalPages || 1,
          size: data.size || size,
          number: data.number || page
        };
      }
      
      return this.getFallbackBooksResponse();
    } catch (error) {
      console.error('Error searching books:', error);
      return this.getFallbackBooksResponse();
    }
  },

  async getBookById(id) {
    try {
      console.log(`📖 Fetching book with ID: ${id}`);
      
      const token = localStorage.getItem('token');
      const response = await fetch(`${GATEWAY_URL}/api/books/${id}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { 'Authorization': `Bearer ${token}` })
        },
        credentials: 'include'
      });
      
      if (!response.ok) throw new Error(`HTTP ${response.status}`);
      
      const book = await response.json();
      console.log('Book data received:', book);
      
      if (book) {
        if (book.mainCoverImageUrl || book.coverImageUrl) {
          book.cover = this.processImageUrl(book.mainCoverImageUrl || book.coverImageUrl);
        }
        
        if (book.pdfFileUrl) {
          book.fullPdfUrl = this.processPdfUrl(book.pdfFileUrl, id);
          book.pdfViewerUrl = `${book.fullPdfUrl}#toolbar=0&navpanes=0&scrollbar=1`;
        }
        
        book.availableOnline = book.canReadOnline || false;
        book.availablePhysical = book.canBorrowPhysical || false;
        book.physicalCopies = book.totalPhysicalCopies || 0;
        book.borrowedCopies = (book.totalPhysicalCopies || 0) - (book.availablePhysicalCopies || 0);
        book.reads = book.viewCount || 0;
        book.rating = book.averageRating || 4.5;
        book.category = book.categories?.[0] || book.department || 'Chung';
      }
      
      return book;
      
    } catch (error) {
      console.error('Error fetching book:', error);
      const fallbackBooks = this.getFallbackBooks();
      return fallbackBooks.find(b => b.id === parseInt(id)) || fallbackBooks[0];
    }
  },

  async getPdfBlob(bookId) {
    try {
      const token = localStorage.getItem('token');
      const pdfUrl = `${GATEWAY_URL}/api/books/${bookId}/pdf`;
      
      console.log('Fetching PDF from:', pdfUrl);
      
      const headers = {
        'Accept': 'application/pdf'
      };
      
      if (token) {
        headers['Authorization'] = `Bearer ${token}`;
      }
      
      const response = await fetch(pdfUrl, {
        method: 'GET',
        credentials: 'include',
        headers: headers
      });
      
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }
      
      const blob = await response.blob();
      console.log('PDF blob received, size:', blob.size);
      
      return blob;
      
    } catch (error) {
      console.error('Error fetching PDF blob:', error);
      throw error;
    }
  },

  async getPdfBlobUrl(bookId) {
    try {
      const blob = await this.getPdfBlob(bookId);
      const blobUrl = URL.createObjectURL(blob);
      return blobUrl;
    } catch (error) {
      console.error('Error creating blob URL:', error);
      return `${GATEWAY_URL}/api/books/${bookId}/pdf`;
    }
  },
async getBranches() {
    try {
        const token = localStorage.getItem('token');
        const response = await fetch(`${GATEWAY_URL}/api/books/branches`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (!response.ok) throw new Error('Failed to fetch branches');
        return await response.json();
    } catch (error) {
        console.error('Error fetching branches:', error);
        return [];
    }
},
  processImageUrl(imageUrl) {
    if (!imageUrl) {
      const randomId = Math.floor(Math.random() * 1000);
      return `https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&h=500&fit=crop&random=${randomId}`;
    }
    
    if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
      return imageUrl;
    }
    
    if (imageUrl.startsWith('/uploads/')) {
      return `${GATEWAY_URL}${imageUrl}`;
    }
    
    if (imageUrl.startsWith('uploads/')) {
      return `${GATEWAY_URL}/${imageUrl}`;
    }
    
    return `${GATEWAY_URL}/uploads/${imageUrl}`;
  },

  processPdfUrl(pdfUrl, bookId) {
    if (!pdfUrl) return null;
    
    if (bookId) {
      return `${GATEWAY_URL}/api/books/${bookId}/pdf`;
    }
    
    if (pdfUrl.startsWith('http://') || pdfUrl.startsWith('https://')) {
      return pdfUrl;
    }
    
    let filename = pdfUrl;
    if (pdfUrl.includes('/')) {
      filename = pdfUrl.substring(pdfUrl.lastIndexOf('/') + 1);
    }
    
    return `${GATEWAY_URL}/uploads/${filename}`;
  },

  getFallbackBooksResponse() {
    return {
      content: this.getFallbackBooks(),
      totalElements: 4,
      totalPages: 1,
      size: 20,
      number: 0,
      first: true,
      last: true,
      empty: false
    };
  },

  getFallbackBooks() {
    return [
      {
        id: 1,
        title: "Clean Code: A Handbook of Agile Software Craftsmanship",
        author: "Robert C. Martin",
        category: "Lập trình",
        rating: 4.8,
        reads: 1245,
        cover: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&h=500&fit=crop",
        availableOnline: true,
        availablePhysical: true,
        physicalCopies: 5,
        borrowedCopies: 2,
        pdfFileUrl: "/uploads/clean_code.pdf",
        description: "Cuốn sách kinh điển về viết code sạch và bảo trì được",
        canReadOnline: true,
        canBorrowPhysical: true,
        viewCount: 1245,
        averageRating: 4.8,
        publicationYear: 2008,
        pages: 464,
        publisher: "Prentice Hall"
      },
      {
        id: 2,
        title: "Introduction to Algorithms",
        author: "Thomas H. Cormen",
        category: "Khoa học máy tính",
        rating: 4.6,
        reads: 2345,
        cover: "https://images.unsplash.com/photo-1535905557558-afc4877a26fc?w=400&h=500&fit=crop",
        availableOnline: true,
        availablePhysical: true,
        physicalCopies: 8,
        borrowedCopies: 2,
        pdfFileUrl: "/uploads/algorithms.pdf",
        description: "Sách giáo trình thuật toán kinh điển của MIT",
        canReadOnline: true,
        canBorrowPhysical: true,
        viewCount: 2345,
        averageRating: 4.6,
        publicationYear: 2009,
        pages: 1312,
        publisher: "MIT Press"
      },
      {
        id: 3,
        title: "The Pragmatic Programmer",
        author: "Andrew Hunt, David Thomas",
        category: "Lập trình",
        rating: 4.9,
        reads: 1890,
        cover: "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400&h=500&fit=crop",
        availableOnline: true,
        availablePhysical: false,
        physicalCopies: 0,
        borrowedCopies: 0,
        pdfFileUrl: "/uploads/pragmatic_programmer.pdf",
        description: "Hướng dẫn trở thành lập trình viên chuyên nghiệp",
        canReadOnline: true,
        canBorrowPhysical: false,
        viewCount: 1890,
        averageRating: 4.9,
        publicationYear: 1999,
        pages: 352,
        publisher: "Addison-Wesley"
      },
      {
        id: 4,
        title: "Design Patterns",
        author: "Erich Gamma",
        category: "Lập trình",
        rating: 4.7,
        reads: 1567,
        cover: "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&h=500&fit=crop",
        availableOnline: false,
        availablePhysical: true,
        physicalCopies: 3,
        borrowedCopies: 3,
        pdfFileUrl: null,
        description: "Các mẫu thiết kế phần mềm hướng đối tượng",
        canReadOnline: false,
        canBorrowPhysical: true,
        viewCount: 1567,
        averageRating: 4.7,
        publicationYear: 1994,
        pages: 416,
        publisher: "Addison-Wesley"
      }
    ];
  }
};

export default bookService;
