package com.university_digital_library.user_service.repository;

import com.university_digital_library.user_service.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);
    Optional<UserProfile> findByStudentId(String studentId);
    Optional<UserProfile> findByLecturerId(String lecturerId);
    Optional<UserProfile> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByStudentId(String studentId);
    boolean existsByLecturerId(String lecturerId);
}
