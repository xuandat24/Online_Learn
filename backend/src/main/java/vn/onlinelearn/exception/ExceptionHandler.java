package vn.onlinelearn.exception;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

@RestControllerAdvice
public class ExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(ApiException.class)
    ResponseEntity<?> business(ApiException e) { return ResponseEntity.status(e.status).body(Map.of("message", e.getMessage())); }
    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException e) {
        var errors = e.getBindingResult().getFieldErrors().stream().map(x -> x.getField() + ": " + x.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(Map.of("message", "Dữ liệu không hợp lệ.", "errors", errors));
    }
    @org.springframework.web.bind.annotation.ExceptionHandler({IllegalArgumentException.class, org.springframework.http.converter.HttpMessageNotReadableException.class})
    ResponseEntity<?> invalid(Exception e) { return ResponseEntity.badRequest().body(Map.of("message", "Yêu cầu không hợp lệ. Vui lòng kiểm tra dữ liệu.")); }
    @org.springframework.web.bind.annotation.ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<?> denied(Exception e) { return ResponseEntity.status(403).body(Map.of("message", "Bạn không có quyền thực hiện thao tác này.")); }
    @org.springframework.web.bind.annotation.ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> integrity(Exception e) { return ResponseEntity.status(409).body(Map.of("message", "Dữ liệu bị trùng hoặc đang được sử dụng. Vui lòng tải lại.")); }
}
