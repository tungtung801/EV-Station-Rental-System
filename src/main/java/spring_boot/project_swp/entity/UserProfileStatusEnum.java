package spring_boot.project_swp.entity;

public enum UserProfileStatusEnum {
    UNVERIFIED, // 1. Mới tạo acc, chưa up bằng lái (Khách chưa được phép thuê xe)
    PENDING,    // 2. Khách ĐÃ UP ẢNH, đang chờ Admin duyệt (Hiện trong danh sách Admin)
    VERIFIED,   // 3. Admin đã bấm DUYỆT (Khách được phép thuê xe)
    REJECTED    // 4. Admin bấm TỪ CHỐI (Yêu cầu up lại)
}
