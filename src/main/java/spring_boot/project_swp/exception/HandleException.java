package spring_boot.project_swp.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class HandleException {

    // Helper method để tạo cấu trúc lỗi thống nhất
    private Map<String, Object> createBody(HttpStatus status, String message, String error) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        return body;
    }

    // 1. Lỗi Validation (@Valid, @NotNull...) -> 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (var err : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(err.getField(), err.getDefaultMessage());
        }
        Map<String, Object> body = createBody(HttpStatus.BAD_REQUEST, "Validation failed", "Bad Request");
        body.put("fields", fieldErrors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 2. Lỗi Không tìm thấy (NotFoundException) -> 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        Map<String, Object> body = createBody(HttpStatus.NOT_FOUND, ex.getMessage(), "Not Found");
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // 3. Lỗi Xung đột (ConflictException - Trùng tên, Trùng lịch) -> 409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        Map<String, Object> body = createBody(HttpStatus.CONFLICT, ex.getMessage(), "Conflict");
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // 4. Lỗi Yêu cầu sai (BadRequestException - Logic sai) -> 400 <--- MỚI THÊM
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        Map<String, Object> body = createBody(HttpStatus.BAD_REQUEST, ex.getMessage(), "Bad Request");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 5. Lỗi Chưa xác minh (UserNotVerifiedException) -> 403 Forbidden
    @ExceptionHandler(UserNotVerifiedException.class)
    public ResponseEntity<Object> handleUserNotVerifiedException(UserNotVerifiedException ex) {
        // Sửa lại để dùng chung format createBody cho đồng bộ
        Map<String, Object> body = createBody(HttpStatus.FORBIDDEN, ex.getMessage(), "Forbidden");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // 6. Lỗi Hệ thống (Exception chung) -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        ex.printStackTrace(); // Log lỗi ra console để debug
        Map<String, Object> body = createBody(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Đã có lỗi không mong muốn xảy ra. Vui lòng thử lại sau.",
                "Internal Server Error");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}