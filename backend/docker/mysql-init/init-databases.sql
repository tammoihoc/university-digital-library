-- ============================================================
-- Script tách database cho university-digital-library
-- Chạy 1 lần trên MySQL server (dưới quyền root) trước khi
-- start các service. Mỗi service có database + user riêng,
-- không được cấp quyền chéo sang database khác.
-- ============================================================

-- auth-service
CREATE DATABASE IF NOT EXISTS auth_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'auth_user'@'%' IDENTIFIED BY 'auth_pass';
GRANT ALL PRIVILEGES ON auth_db.* TO 'auth_user'@'%';

-- user-service
CREATE DATABASE IF NOT EXISTS user_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'user_user'@'%' IDENTIFIED BY 'user_pass';
GRANT ALL PRIVILEGES ON user_db.* TO 'user_user'@'%';

-- book-service
CREATE DATABASE IF NOT EXISTS book_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'book_user'@'%' IDENTIFIED BY 'book_pass';
GRANT ALL PRIVILEGES ON book_db.* TO 'book_user'@'%';

-- borrow-service
CREATE DATABASE IF NOT EXISTS borrow_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'borrow_user'@'%' IDENTIFIED BY 'borrow_pass';
GRANT ALL PRIVILEGES ON borrow_db.* TO 'borrow_user'@'%';

-- fine_service
CREATE DATABASE IF NOT EXISTS fine_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'fine_user'@'%' IDENTIFIED BY 'fine_pass';
GRANT ALL PRIVILEGES ON fine_db.* TO 'fine_user'@'%';

-- entry_exit_service
CREATE DATABASE IF NOT EXISTS entry_exit_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'entry_exit_user'@'%' IDENTIFIED BY 'entry_exit_pass';
GRANT ALL PRIVILEGES ON entry_exit_db.* TO 'entry_exit_user'@'%';

FLUSH PRIVILEGES;

-- ============================================================
-- LƯU Ý DI TRÚ DỮ LIỆU CŨ:
-- Nếu bạn đã có data trong database "university_digital_library"
-- cũ, cần export riêng từng nhóm bảng rồi import vào DB mới
-- tương ứng, ví dụ:
--
--   mysqldump university_digital_library users user_roles \
--     > auth_backup.sql
--   mysql auth_db < auth_backup.sql
--
--   mysqldump university_digital_library books book_locations \
--     library_branches book_access_rules book_categories \
--     > book_backup.sql
--   mysql book_db < book_backup.sql
--
--   mysqldump university_digital_library borrow_records reservations \
--     > borrow_backup.sql
--   mysql borrow_db < borrow_backup.sql
--
--   mysqldump university_digital_library fine_records penalties \
--     > fine_backup.sql
--   mysql fine_db < fine_backup.sql
--
--   mysqldump university_digital_library entry_exit_records \
--     > entry_exit_backup.sql
--   mysql entry_exit_db < entry_exit_backup.sql
--
--   mysqldump university_digital_library user_profiles \
--     > user_backup.sql
--   mysql user_db < user_backup.sql
--
-- CHÚ Ý: bảng "book_locations" trong DB cũ có thể đang chứa
-- data lẫn lộn từ 2 schema khác nhau (book-service tạo cột
-- colNum/rowNum, borrow-service từng tạo cột column/row do
-- entity trùng tên đã bị xoá). Kiểm tra kỹ cấu trúc bảng
-- này trước khi import sang book_db — chỉ book-service còn
-- sở hữu bảng này sau khi tách.
-- ============================================================
