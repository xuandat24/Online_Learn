package com.morrow.learning.service;

import com.morrow.learning.domain.Role;
import com.morrow.learning.domain.User;
import com.morrow.learning.dto.AdminUserView;
import com.morrow.learning.repository.UserRepository;
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

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserView> list() {
        return userRepository.findAllByOrderByIdAsc().stream().map(AdminUserView::from).toList();
    }

    @Transactional
    public AdminUserView updateRole(Long id, Role role) {
        if (role == Role.GUEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registered accounts cannot use the GUEST role");
        }
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        user.updateRole(role);
        return AdminUserView.from(user);
    }

    @Transactional
    public AdminUserView createUser(String fullName, String email, String password, Role role, String phone, String gender) {
        if (role == Role.GUEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registered accounts cannot use the GUEST role");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        var user = new User(fullName, email, passwordEncoder.encode(password != null ? password : "password123"), role, phone, gender);
        return AdminUserView.from(userRepository.save(user));
    }

    @Transactional
    public AdminUserView updateUser(Long id, String fullName, String phone, String gender, Role role,
                                    String status, String password) {
        if (role == Role.GUEST) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registered accounts cannot use the GUEST role");
        }
        var user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (fullName != null && !fullName.isBlank()) user.setFullName(fullName);
        if (phone != null) user.setPhone(phone);
        if (gender != null) user.setGender(gender);
        if (role != null) user.updateRole(role);
        if (status != null) {
            if (!status.equals("ACTIVE") && !status.equals("LOCKED")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status must be ACTIVE or LOCKED");
            }
            user.setStatus(status);
        }
        if (password != null && !password.isBlank()) {
            if (password.length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must contain at least 8 characters");
            }
            user.setPasswordHash(passwordEncoder.encode(password));
        }
        return AdminUserView.from(userRepository.save(user));
    }
}