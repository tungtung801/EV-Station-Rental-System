package spring_boot.project_swp.entity;

public enum RentalStatusEnum {
    PENDING_PICKUP, // Chờ nhận xe
    ACTIVE,         // <-- THÊM DÒNG NÀY (Đang thuê)
    COMPLETED,      // Đã trả xe
    CANCELLED       // Đã hủy
}