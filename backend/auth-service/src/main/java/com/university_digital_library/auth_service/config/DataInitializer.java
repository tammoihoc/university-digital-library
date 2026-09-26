package com.university_digital_library.auth_service.config;

import com.university_digital_library.auth_service.model.User;
import com.university_digital_library.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .roles(Set.of("ADMIN"))
                        .build();

                User student = User.builder()
                        .username("student")
                        .password(passwordEncoder.encode("123456"))
                        .roles(Set.of("STUDENT"))
                        .build();
                        
                User librarian = User.builder()
                        .username("librarian")
                        .password(passwordEncoder.encode("lib123"))
                        .roles(Set.of("LIBRARIAN"))
                        .build();
                        
                User lecturer = User.builder()
                        .username("lecturer")
                        .password(passwordEncoder.encode("lec123"))
                        .roles(Set.of("LECTURER"))
                        .build();

                userRepository.save(admin);
                userRepository.save(student);
                userRepository.save(librarian);
                userRepository.save(lecturer);

                log.info("Default users created!");
                log.info("  - admin / admin123 (ADMIN)");
                log.info("  - student / 123456 (STUDENT)");
                log.info("  - librarian / lib123 (LIBRARIAN)");
                log.info("  - lecturer / lec123 (LECTURER)");
            }
        };
    }
}
