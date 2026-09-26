// auth-service/src/main/java/com/university_digital_library/auth_service/repository/UserRepository.java
package com.university_digital_library.auth_service.repository;

import com.university_digital_library.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional<User> findByUsernameAndIsActiveTrue(String username);
}
