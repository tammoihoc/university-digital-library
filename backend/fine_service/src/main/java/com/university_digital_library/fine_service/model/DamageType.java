// fine-service/src/main/java/.../model/DamageType.java
package com.university_digital_library.fine_service.model;

public enum DamageType {
    LOST("Mất sách", 500000.0),           // Phạt 500k
    DAMAGED_HEAVY("Hư hỏng nặng", 300000.0), // Phạt 300k
    DAMAGED_MEDIUM("Hư hỏng vừa", 150000.0), // Phạt 150k
    DAMAGED_LIGHT("Hư hỏng nhẹ", 50000.0),   // Phạt 50k
    WATER_DAMAGE("Thấm nước", 200000.0),
    TORN_PAGES("Rách trang", 100000.0),
    WRITING_INSIDE("Viết/vẽ vào sách", 80000.0);
    
    private final String description;
    private final Double defaultFine;
    
    DamageType(String description, Double defaultFine) {
        this.description = description;
        this.defaultFine = defaultFine;
    }
    
    public String getDescription() { return description; }
    public Double getDefaultFine() { return defaultFine; }
}
