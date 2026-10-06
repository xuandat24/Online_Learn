package com.morrow.learning.controller;

import com.morrow.learning.domain.Role;
import com.morrow.learning.dto.AdminUserView;
import com.morrow.learning.dto.RoleUpdateRequest;
import com.morrow.learning.service.AdminUserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserView> list() {
        return adminUserService.list();
    }

    public record CreateUserRequest(String fullName, String email, String password, Role role, String phone, String gender) {}
    public record UpdateUserRequest(String fullName, String phone, String gender, Role role, String status) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserView create(@RequestBody CreateUserRequest request) {
        return adminUserService.createUser(
                request.fullName(),
                request.email(),
                request.password(),
                request.role() != null ? request.role() : Role.CUSTOMER,
                request.phone(),
                request.gender()
        );
    }

    @PutMapping("/{id}")
    public AdminUserView update(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return adminUserService.updateUser(
                id,
                request.fullName(),
                request.phone(),
                request.gender(),
                request.role(),
                request.status()
        );
    }

    @PatchMapping("/{id}/role")
    public AdminUserView updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return adminUserService.updateRole(id, request.role());
    }
}