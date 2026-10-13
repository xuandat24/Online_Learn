package com.morrow.learning.service;

import com.morrow.learning.domain.Role;
import com.morrow.learning.domain.User;
import com.morrow.learning.dto.AdminUserView;
import com.morrow.learning.repository.UserRepository;
import com.morrow.learning.repository.RegistrationRepository;
import com.morrow.learning.repository.EnrollmentRepository;
import com.morrow.learning.repository.QuizAttemptRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationRepository registrationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                            RegistrationRepository registrationRepository,
                            EnrollmentRepository enrollmentRepository,
                            QuizAttemptRepository quizAttemptRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.registrationRepository = registrationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserView> list(Role role, String search) {
        List<User> list = userRepository.findAllByOrderByIdAsc();
        return list.stream()
                .filter(u -> role == null || u.getRole() == role)
                .filter(u -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    return (u.getFullName() != null && u.getFullName().toLowerCase().contains(q))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(q))
                            || (u.getPhone() != null && u.getPhone().toLowerCase().contains(q));
                })
                .map(AdminUserView::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminUserView getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với ID: " + id));
        return AdminUserView.from(user);
    }

    @Transactional
    public AdminUserView updateRole(Long id, Role role) {
        if (role == Role.GUEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản đăng ký không thể gán vai trò GUEST");
        }
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        user.updateRole(role);
        return AdminUserView.from(user);
    }

    @Transactional
    public AdminUserView createUser(String fullName, String email, String password, Role role, String phone, String gender) {
        if (role == Role.GUEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản đăng ký không thể gán vai trò GUEST");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Địa chỉ email đã được sử dụng");
        }
        var user = new User(fullName, email, passwordEncoder.encode(password != null && !password.isBlank() ? password : "password123"),
                role != null ? role : Role.CUSTOMER, phone, gender);
        return AdminUserView.from(userRepository.save(user));
    }

    @Transactional
    public AdminUserView updateUser(Long id, String fullName, String phone, String gender, Role role,
                                    String status, String password) {
        if (role == Role.GUEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tài khoản đăng ký không thể gán vai trò GUEST");
        }
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        if (fullName != null && !fullName.isBlank()) user.setFullName(fullName.trim());
        if (phone != null) user.setPhone(phone.trim());
        if (gender != null) user.setGender(gender.trim());
        if (role != null) user.updateRole(role);
        if (status != null) {
            if (!status.equals("ACTIVE") && !status.equals("LOCKED")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái chỉ có thể là ACTIVE hoặc LOCKED");
            }
            user.setStatus(status);
        }
        if (password != null && !password.isBlank()) {
            if (password.length() < 6) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mật khẩu phải chứa ít nhất 6 ký tự");
            }
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        return AdminUserView.from(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng với ID: " + id));
        boolean hasRelatedData = registrationRepository.existsByCustomerId(id)
                || enrollmentRepository.existsByUserId(id)
                || quizAttemptRepository.existsByUserId(id);
        if (hasRelatedData) {
            // Nếu đã có dữ liệu liên kết học tập / đăng ký / bài thi, chuyển trạng thái sang LOCKED (soft-delete)
            user.setStatus("LOCKED");
            userRepository.save(user);
        } else {
            userRepository.delete(user);
        }
    }
}