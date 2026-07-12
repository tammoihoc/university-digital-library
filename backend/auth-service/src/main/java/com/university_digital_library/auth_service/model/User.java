package com.university_digital_library.auth_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;

    // Thêm method validate roles
    public void validateRoles() {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Roles cannot be empty");
        }
        
        for (String role : roles) {
            if (!isValidRole(role)) {
                throw new IllegalArgumentException("Invalid role: " + role + ". Valid roles are: ADMIN, LIBRARIAN, LECTURER, STUDENT");
            }
        }
    }
    
    private boolean isValidRole(String role) {
        return Set.of("ADMIN", "LIBRARIAN", "LECTURER", "STUDENT").contains(role);
    }
}
