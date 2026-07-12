// BookService/src/main/java/.../service/CustomUserDetailsService.java
package com.university_digital_library.book_service.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Book service không cần load user từ DB
        // JWT đã xác thực rồi, chỉ cần trả về User object với username
        return User.withUsername(username)
                .password("")
                .authorities("ROLE_USER")
                .build();
    }
}
