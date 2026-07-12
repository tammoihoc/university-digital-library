// book-service/src/main/java/.../constant/LocationConstants.java
package com.university_digital_library.book_service.constant;

import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum LocationConstants {
    
    // KHU 100 - TRIẾT HỌC & TÂM LÝ HỌC
    ZONE_100(100, "Triết học & Tâm lý học"),
    
    // KHU 200 - TÔN GIÁO
    ZONE_200(200, "Tôn giáo"),
    
    // KHU 300 - KHOA HỌC XÃ HỘI
    ZONE_300(300, "Khoa học xã hội"),
    
    // KHU 400 - NGÔN NGỮ (Theo yêu cầu của bạn)
    ZONE_400(400, "Ngôn ngữ"),
    
    // KHU 500 - KHOA HỌC TỰ NHIÊN
    ZONE_500(500, "Khoa học tự nhiên"),
    
    // KHU 600 - CÔNG NGHỆ
    ZONE_600(600, "Công nghệ"),
    
    // KHU 700 - NGHỆ THUẬT
    ZONE_700(700, "Nghệ thuật"),
    
    // KHU 800 - VĂN HỌC
    ZONE_800(800, "Văn học"),
    
    // KHU 900 - LỊCH SỬ & ĐỊA LÝ
    ZONE_900(900, "Lịch sử & Địa lý");
    
    private final int code;
    private final String name;
    
    LocationConstants(int code, String name) {
        this.code = code;
        this.name = name;
    }
    
    public static String getZoneName(int code) {
        for (LocationConstants zone : values()) {
            if (zone.code == code) {
                return zone.name;
            }
        }
        return "Khác";
    }
    
    // KỆ trong khu 400 (Ngôn ngữ)
    public static final Map<Integer, String> SHELF_400 = new HashMap<>();
    static {
        SHELF_400.put(1, "Tổng quan ngôn ngữ");
        SHELF_400.put(2, "Tiếng Việt");
        SHELF_400.put(3, "Tiếng Anh");
        SHELF_400.put(4, "Tiếng Trung");
        SHELF_400.put(5, "Tiếng Nhật");
        SHELF_400.put(6, "Tiếng Hàn");
        SHELF_400.put(7, "Tiếng Pháp");
        SHELF_400.put(8, "Tiếng Đức");
        SHELF_400.put(9, "Ngôn ngữ khác");
    }
    
    // CỘT trong kệ 3 (Tiếng Anh)
    public static final Map<Integer, String> COLUMN_3 = new HashMap<>();
    static {
        COLUMN_3.put(1, "Ngữ pháp");
        COLUMN_3.put(2, "Từ vựng");
        COLUMN_3.put(3, "Phát âm");
        COLUMN_3.put(4, "Giao tiếp");
        COLUMN_3.put(5, "Viết");
        COLUMN_3.put(6, "Đọc hiểu");
        COLUMN_3.put(7, "Nghe");
        COLUMN_3.put(8, "Luyện thi");
        COLUMN_3.put(9, "Khác");
    }
    
    // HÀNG trong cột 3 (Phát âm)
    public static final Map<Integer, String> ROW_3 = new HashMap<>();
    static {
        ROW_3.put(1, "Lỗi phát âm thường gặp");
        ROW_3.put(2, "Cách cải thiện phát âm");
        ROW_3.put(3, "Phát âm theo vùng miền");
        ROW_3.put(4, "IPA - Bảng phiên âm");
        ROW_3.put(5, "Trọng âm & Ngữ điệu");
        ROW_3.put(6, "Phát âm nâng cao");
        ROW_3.put(7, "Bài tập phát âm");
        ROW_3.put(8, "Phát âm chuyên sâu");
        ROW_3.put(9, "Tài liệu tham khảo");
    }
}
