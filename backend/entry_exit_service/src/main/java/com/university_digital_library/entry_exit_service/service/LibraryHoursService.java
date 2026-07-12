// entry-exit-service/src/main/java/.../service/LibraryHoursService.java
package com.university_digital_library.entry_exit_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class LibraryHoursService {
    
    // Giờ mở cửa theo từng cơ sở và ngày
    private static final Map<String, Map<DayOfWeek, TimeRange>> BRANCH_HOURS = new HashMap<>();
    
    static {
        // Cơ sở B - Thư viện chính (475A Điện Biên Phủ)
        Map<DayOfWeek, TimeRange> branchBHours = new HashMap<>();
        branchBHours.put(DayOfWeek.MONDAY, new TimeRange(LocalTime.of(9, 0), LocalTime.of(19, 0)));
        branchBHours.put(DayOfWeek.TUESDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(19, 0)));
        branchBHours.put(DayOfWeek.WEDNESDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(19, 0)));
        branchBHours.put(DayOfWeek.THURSDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(19, 0)));
        branchBHours.put(DayOfWeek.FRIDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(19, 0)));
        branchBHours.put(DayOfWeek.SATURDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(11, 30)));
        // Chủ nhật đóng cửa - không có trong map
        BRANCH_HOURS.put("B", branchBHours);
        
        // Cơ sở E - Thư viện Quận 9
        Map<DayOfWeek, TimeRange> branchEHours = new HashMap<>();
        branchEHours.put(DayOfWeek.MONDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(16, 15)));
        branchEHours.put(DayOfWeek.TUESDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(16, 15)));
        branchEHours.put(DayOfWeek.WEDNESDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(16, 15)));
        branchEHours.put(DayOfWeek.THURSDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(16, 15)));
        branchEHours.put(DayOfWeek.FRIDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(16, 15)));
        branchEHours.put(DayOfWeek.SATURDAY, new TimeRange(LocalTime.of(8, 0), LocalTime.of(11, 15)));
        // Chủ nhật đóng cửa - không có trong map
        BRANCH_HOURS.put("E", branchEHours);
    }
    
    /**
     * Kiểm tra thư viện có đang mở cửa không
     */
    public boolean isLibraryOpen(String branch, LocalDateTime checkTime) {
        Map<DayOfWeek, TimeRange> branchHours = BRANCH_HOURS.get(branch.toUpperCase());
        if (branchHours == null) {
            log.warn("Unknown branch: {}", branch);
            return false;
        }
        
        DayOfWeek dayOfWeek = checkTime.getDayOfWeek();
        TimeRange range = branchHours.get(dayOfWeek);
        
        if (range == null) {
            log.info("Library {} is closed on {}", branch, dayOfWeek);
            return false;
        }
        
        LocalTime checkTimeOnly = checkTime.toLocalTime();
        boolean isOpen = !checkTimeOnly.isBefore(range.getStart()) && !checkTimeOnly.isAfter(range.getEnd());
        
        log.info("Library {} open status at {}: {}", branch, checkTimeOnly, isOpen);
        return isOpen;
    }
    
    /**
     * Lấy thông tin giờ mở cửa
     */
    public String getOpeningHours(String branch) {
        Map<DayOfWeek, TimeRange> branchHours = BRANCH_HOURS.get(branch.toUpperCase());
        if (branchHours == null) {
            return "Không có thông tin";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("📚 Thư viện ").append(branch.equals("B") ? "chính (475A Điện Biên Phủ)" : "cơ sở Quận 9 (Khu Công nghệ cao)");
        sb.append("\n");
        
        for (Map.Entry<DayOfWeek, TimeRange> entry : branchHours.entrySet()) {
            String day = getVietnameseDay(entry.getKey());
            TimeRange range = entry.getValue();
            sb.append(String.format("  • %s: %s - %s\n", day, range.getStart(), range.getEnd()));
        }
        sb.append("  • Chủ nhật: Đóng cửa");
        
        return sb.toString();
    }
    
    private String getVietnameseDay(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Thứ 2";
            case TUESDAY: return "Thứ 3";
            case WEDNESDAY: return "Thứ 4";
            case THURSDAY: return "Thứ 5";
            case FRIDAY: return "Thứ 6";
            case SATURDAY: return "Thứ 7";
            default: return day.toString();
        }
    }
    
    // Inner class cho khoảng thời gian
    static class TimeRange {
        private final LocalTime start;
        private final LocalTime end;
        
        public TimeRange(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }
        
        public LocalTime getStart() { return start; }
        public LocalTime getEnd() { return end; }
        
        @Override
        public String toString() {
            return String.format("%02d:%02d - %02d:%02d", start.getHour(), start.getMinute(), 
                                 end.getHour(), end.getMinute());
        }
    }
}
