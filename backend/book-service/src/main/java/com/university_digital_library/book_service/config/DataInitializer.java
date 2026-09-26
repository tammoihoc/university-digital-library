// book-service/src/main/java/.../config/DataInitializer.java
package com.university_digital_library.book_service.config;

import com.university_digital_library.book_service.model.LibraryBranch;
import com.university_digital_library.book_service.repository.LibraryBranchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final LibraryBranchRepository branchRepository;
    
    @Bean
    public CommandLineRunner initLibraryBranches() {
        return args -> {
            if (branchRepository.count() == 0) {
                // Thư viện chính - Cơ sở 1
                branchRepository.save(LibraryBranch.builder()
                    .name("Thư viện chính HUTECH")
                    .address("Tầng 03 tòa nhà B, 475A Điện Biên Phủ, P.25, Q.Bình Thạnh, TP.HCM")
                    .openingHours("""
                        📅 Thứ 2: 9h00 - 19h00
                        📅 Thứ 3 - Thứ 6: 8h00 - 19h00
                        📅 Thứ 7: 8h00 - 11h30
                        📅 Chủ nhật: Đóng cửa
                        """)
                    .phone("(028) 1234 5678")
                    .email("thuvien@hutech.edu.vn")
                    .mapUrl("https://maps.app.goo.gl/...")
                    .isActive(true)
                    .build());
                
                // Thư viện Quận 9 - Cơ sở 2
                branchRepository.save(LibraryBranch.builder()
                    .name("Thư viện HUTECH - Cơ sở Quận 9")
                    .address("Tòa nhà E3 (Phòng E3.02.01), Khu Công nghệ cao, Thủ Đức, TP.HCM")
                    .openingHours("""
                        📅 Thứ 2 - Thứ 6: 8h00 - 16h15
                        📅 Thứ 7: 8h00 - 11h15
                        📅 Chủ nhật: Đóng cửa
                        """)
                    .phone("(028) 1234 8765")
                    .email("thuvien9@hutech.edu.vn")
                    .mapUrl("https://maps.app.goo.gl/...")
                    .isActive(true)
                    .build());
                
                log.info("✅ Initialized 2 library branches!");
            }
        };
    }
}
