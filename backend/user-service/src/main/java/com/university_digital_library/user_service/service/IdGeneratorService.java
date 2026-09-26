// user-service/src/main/java/.../service/IdGeneratorService.java
package com.university_digital_library.user_service.service;

import com.university_digital_library.user_service.model.UserProfile;
import com.university_digital_library.user_service.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdGeneratorService {
    
    private final UserProfileRepository repository;
    
    public String generateId(UserProfile.UserType userType) {
        String prefix;
        String fieldName;
        
        switch (userType) {
            case STUDENT:
                prefix = "SV";
                fieldName = "studentId";
                break;
            case LECTURER:
                prefix = "GV";
                fieldName = "lecturerId";
                break;
            case LIBRARIAN:
                prefix = "TT";
                fieldName = "librarianId";
                break;
            default:
                throw new IllegalArgumentException("Cannot generate ID for type: " + userType);
        }
        
        int maxNumber = findMaxNumber(prefix, fieldName);
        int newNumber = maxNumber + 1;
        
        return String.format("%s-%03d", prefix, newNumber);
    }
    
    private int findMaxNumber(String prefix, String fieldName) {
        List<UserProfile> users = repository.findAll();
        int max = 0;
        
        for (UserProfile user : users) {
            String id = null;
            if (fieldName.equals("studentId")) {
                id = user.getStudentId();
            } else if (fieldName.equals("lecturerId")) {
                id = user.getLecturerId();
            } else if (fieldName.equals("librarianId")) {
                id = user.getLibrarianId();
            }
            
            if (id != null && id.startsWith(prefix)) {
                try {
                    String numberPart = id.substring(id.lastIndexOf("-") + 1);
                    int num = Integer.parseInt(numberPart);
                    if (num > max) {
                        max = num;
                    }
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
        
        return max;
    }
}
