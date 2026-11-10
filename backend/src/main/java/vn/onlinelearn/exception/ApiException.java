package vn.onlinelearn.exception;

public class ApiException extends RuntimeException {
    public final int status;
    public ApiException(int status, String message) { super(message); this.status = status; }
    public static ApiException bad(String message) { return new ApiException(400, message); }
    public static ApiException forbidden() { return new ApiException(403, "Bạn không có quyền thực hiện thao tác này."); }
    public static ApiException missing() { return new ApiException(404, "Không tìm thấy dữ liệu."); }
    public static ApiException conflict(String message) { return new ApiException(409, message); }
}
