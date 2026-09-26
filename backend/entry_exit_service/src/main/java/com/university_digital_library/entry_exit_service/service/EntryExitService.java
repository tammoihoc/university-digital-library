package com.university_digital_library.entry_exit_service.service;

import com.university_digital_library.entry_exit_service.dto.EntryExitRequest;
import com.university_digital_library.entry_exit_service.dto.EntryExitResponse;
import com.university_digital_library.entry_exit_service.feign.UserClient;
import com.university_digital_library.entry_exit_service.model.EntryExitRecord;
import com.university_digital_library.entry_exit_service.repository.EntryExitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntryExitService {

    private final EntryExitRepository repository;
    private final UserClient userClient;

    // CHECK-IN
    @Transactional
    public EntryExitResponse checkIn(String identifier, EntryExitRequest request, String authorization) {
        Map<String, Object> userProfile = getUserByIdentifier(identifier, authorization);
        if (userProfile == null || userProfile.isEmpty()) {
            throw new RuntimeException("Không tìm thấy người dùng với username hoặc studentId: " + identifier);
        }

        String userId = (String) userProfile.getOrDefault("username", identifier);

        // Chặn check-in trùng: nếu user đang có bản ghi INSIDE chưa checkout thì không cho
        // check-in thêm lần nữa (tránh sinh ra nhiều bản ghi INSIDE cùng lúc, khiến
        // checkOut() chỉ đóng được bản ghi mới nhất còn các bản ghi cũ bị "kẹt" mãi mãi
        // ở trạng thái INSIDE, làm sai lệch danh sách "đang có mặt trong thư viện").
        Optional<EntryExitRecord> existingInside = repository.findFirstByUserIdAndStatusOrderByEntryTimeDesc(userId, "INSIDE");
        if (existingInside.isPresent()) {
            throw new RuntimeException("Người dùng đã check-in trước đó (tại cơ sở " +
                    existingInside.get().getBranch() + "), vui lòng check-out trước khi check-in lại.");
        }
        String fullName = (String) userProfile.getOrDefault("fullName", userId);
        String studentId = (String) userProfile.getOrDefault("studentId", userId);
        String userType = (String) userProfile.getOrDefault("userType", "STUDENT");
        String faculty = (String) userProfile.getOrDefault("faculty", "Không xác định");
        String major = (String) userProfile.getOrDefault("major", "");

        String branch = request.getBranch() != null ? request.getBranch() : "B";

        EntryExitRecord record = EntryExitRecord.builder()
                .userId(userId)
                .entryTime(LocalDateTime.now())
                .branch(branch)
                .status("INSIDE")
                .userType(userType)
                .fullName(fullName)
                .studentId(studentId)
                .faculty(faculty)
                .major(major)
                .build();

        EntryExitRecord saved = repository.save(record);
        log.info("✅ User {} checked in at branch {} at {}", userId, saved.getBranch(), saved.getEntryTime());

        return convertToResponse(saved);
    }

    // Helper: tìm user profile theo identifier (username hoặc studentId)
    private Map<String, Object> getUserByIdentifier(String identifier, String authorization) {
        Map<String, Object> profile = null;
        try {
            profile = userClient.getUserProfile(identifier, authorization);
        } catch (Exception e) {
            log.debug("User not found by username: {}, trying studentId", identifier);
        }
        if (profile == null || profile.isEmpty() || "Không xác định".equals(profile.get("fullName"))) {
            try {
                profile = userClient.getUserByStudentId(identifier, authorization);
            } catch (Exception e) {
                log.debug("User not found by studentId: {}", identifier);
            }
        }
        return profile;
    }

public Optional<EntryExitRecord> getLatestEntry(String userId) {
    // Chỉ lấy bản ghi check-in trong ngày hôm nay (từ 00:00)
    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
    return repository.findFirstByUserIdAndStatusAndEntryTimeAfterOrderByEntryTimeDesc(
        userId, "INSIDE", todayStart
    );
}

    // CHECK-OUT
    @Transactional
    public EntryExitResponse checkOut(String userId) {
        EntryExitRecord record = repository.findFirstByUserIdAndStatusOrderByEntryTimeDesc(userId, "INSIDE")
                .orElseThrow(() -> new RuntimeException("User is not checked in"));

        record.setExitTime(LocalDateTime.now());
        record.setStatus("OUTSIDE");

        EntryExitRecord saved = repository.save(record);
        log.info("User {} checked out", userId);

        return convertToResponse(saved);
    }

    // QUERIES
    public List<EntryExitResponse> getCurrentEntries() {
        return repository.findByStatus("INSIDE").stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EntryExitResponse> getUserHistory(String userId) {
        return repository.findByUserId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EntryExitResponse> getHistory(LocalDateTime start, LocalDateTime end) {
        return repository.findByEntryTimeBetween(start, end).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public boolean isUserCheckedInToday(String userId) {
        return repository.existsByUserIdAndStatusAndEntryTimeAfter(
                userId, "INSIDE", LocalDateTime.now().minusDays(1));
    }

    public boolean isUserCheckedInToday(String userId, String branch) {
        return repository.existsByUserIdAndStatusAndEntryTimeAfterAndBranch(
                userId, "INSIDE", LocalDateTime.now().minusDays(1), branch);
    }

    public List<EntryExitRecord> getTodayEntries(String userId) {
        return repository.findByUserIdAndStatusAndEntryTimeAfter(
                userId, "INSIDE", LocalDateTime.now().minusDays(1));
    }

    // CONVERTER
    private EntryExitResponse convertToResponse(EntryExitRecord record) {
        return EntryExitResponse.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .entryTime(record.getEntryTime())
                .exitTime(record.getExitTime())
                .branch(record.getBranch())
                .status(record.getStatus())
                .fullName(record.getFullName())
                .studentId(record.getStudentId())
                .userType(record.getUserType())
                .faculty(record.getFaculty())
                .major(record.getMajor())
                .message(record.getStatus().equals("INSIDE") ? "Checked in" : "Checked out")
                .build();
    }
}
