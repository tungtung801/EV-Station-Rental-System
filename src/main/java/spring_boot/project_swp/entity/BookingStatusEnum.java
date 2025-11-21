package spring_boot.project_swp.entity;

public enum BookingStatusEnum {
  PENDING, // Offline: Chờ khách đến. Online: Chờ thanh toán.
  CONFIRMED, // Đã chốt (Tiền nong xong xuôi/Hoặc Staff đã duyệt)
  CANCELLED, // Hủy (Quá giờ, khách hủy)
  IN_PROGRESS, // Đang đi
  COMPLETED // Đã trả xe
}
