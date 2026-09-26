package com.university_digital_library.user_service.repository;

import com.university_digital_library.user_service.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    
    Optional<UserProfile> findByUsername(String username);

    // ĐÃ SỬA: email/studentId/lecturerId được mã hóa AES-GCM (IV ngẫu nhiên),
    // nên WHERE email = ? / WHERE student_id = ? không bao giờ khớp — Hibernate
    // mã hóa tham số truyền vào thành ciphertext MỚI, khác hoàn toàn ciphertext
    // đã lưu. Tra cứu và kiểm tra trùng giờ dùng cột blind-index (HMAC, không
    // random) thay vì cột đã mã hóa. Xem BlindIndexService để biết chi tiết.
    Optional<UserProfile> findByEmailHash(String emailHash);

    Optional<UserProfile> findByStudentIdHash(String studentIdHash);

    Optional<UserProfile> findByLecturerIdHash(String lecturerIdHash);

    boolean existsByUsername(String username);

    boolean existsByEmailHash(String emailHash);
    
    List<UserProfile> findByUserType(UserProfile.UserType userType);
    
    List<UserProfile> findByUserTypeAndIsActiveTrue(UserProfile.UserType userType);
    
    @Query("SELECT u FROM UserProfile u WHERE u.isActive = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.studentId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<UserProfile> searchUsers(@Param("keyword") String keyword);
    
    @Query("SELECT u FROM UserProfile u WHERE u.userType = 'STUDENT' AND u.isActive = true AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.studentId) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<UserProfile> searchStudents(@Param("keyword") String keyword);
}
