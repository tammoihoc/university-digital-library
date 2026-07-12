// user-service/src/main/java/.../service/IdGeneratorService.java
package com.university_digital_library.user_service.service;

import com.university_digital_library.user_service.model.UserProfile;
import com.university_digital_library.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdGeneratorService {
    
    private final UserProfileRepository userProfileRepository;
    
    /**
     * Tạo mã số tự động dựa vào loại user
     * @param userType STUDENT, LECTURER, LIBRARIAN
     * @return mã số tự động (SV-001, GV-001, TT-001)
     */
    public String generateId(UserProfile.UserType userType) {
        String prefix;
        String fieldToCheck;
        
        switch (userType) {
            case STUDENT:
                prefix = "SV";
                fieldToCheck = "studentId";
                break;
            case LECTURER:
                prefix = "GV";
                fieldToCheck = "lecturerId";
                break;
            case LIBRARIAN:
                prefix = "TT";
                fieldToCheck = "librarianId";
                break;
            default:
                throw new IllegalArgumentException("Cannot generate ID for user type: " + userType);
        }
        
        // Tìm số lớn nhất hiện có
        int maxNumber = findMaxNumber(prefix, fieldToCheck);
        int newNumber = maxNumber + 1;
        
        String newId = String.format("%s-%03d", prefix, newNumber);
        log.info("Generated new ID: {} for type: {}", newId, userType);
        
        return newId;
    }
    
    /**
     * Tìm số lớn nhất trong các ID hiện có
     */
    private int findMaxNumber(String prefix, String fieldToCheck) {
        return userProfileRepository.findAll().stream()
                .filter(profile -> {
                    if (fieldToCheck.equals("studentId")) {
                        return profile.getStudentId() != null && profile.getStudentId().startsWith(prefix);
                    } else if (fieldToCheck.equals("lecturerId")) {
                        return profile.getLecturerId() != null && profile.getLecturerId().startsWith(prefix);
                    } else if (fieldToCheck.equals("librarianId")) {
                        return profile.getLibrarianId() != null && profile.getLibrarianId().startsWith(prefix);
                    }
                    return false;
                })
                .mapToInt(profile -> {
                    String id;
                    if (fieldToCheck.equals("studentId")) {
                        id = profile.getStudentId();
                    } else if (fieldToCheck.equals("lecturerId")) {
                        id = profile.getLecturerId();
                    } else {
                        id = profile.getLibrarianId();
                    }
                    
                    try {
                        // Lấy phần số sau dấu gạch ngang (SV-001 -> 1)
                        String numberPart = id.substring(id.lastIndexOf("-") + 1);
                        return Integer.parseInt(numberPart);
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
    }
}
