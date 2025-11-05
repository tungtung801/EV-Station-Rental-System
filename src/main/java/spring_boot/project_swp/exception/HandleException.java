package spring_boot.project_swp.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import spring_boot.project_swp.exception.Print_Exception.UserNotVerifiedException;

@RestControllerAdvice
public class HandleException {

  private Map<String, Object> createBody(HttpStatus status, String message, String error) {
    Map<String, Object> body = new HashMap<>();
    body.put("timestamp", LocalDateTime.now());
    body.put("status", status.value());
    body.put("error", error);
    body.put("message", message);
    return body;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    for (var err : ex.getBindingResult().getFieldErrors()) {
      fieldErrors.put(err.getField(), err.getDefaultMessage());
    }
    Map<String, Object> body =
        createBody(HttpStatus.BAD_REQUEST, "Validation failed", "Bad Request");
    body.put("fields", fieldErrors);
    return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
    Map<String, Object> body = createBody(HttpStatus.NOT_FOUND, ex.getMessage(), "Not Found");
    return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
    Map<String, Object> body = createBody(HttpStatus.CONFLICT, ex.getMessage(), "Conflict");
    return new ResponseEntity<>(body, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
    ex.printStackTrace();
    Map<String, Object> body =
        createBody(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Đã có lỗi không mong muốn xảy ra. Vui lòng thử lại sau.",
            "Internal Server Error");
    return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(UserNotVerifiedException.class)
  public ResponseEntity<Object> handleUserNotVerifiedException(UserNotVerifiedException ex) {
    Map<String, Object> body = new HashMap<>();
    body.put("message", ex.getMessage());
    return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
  }
}
