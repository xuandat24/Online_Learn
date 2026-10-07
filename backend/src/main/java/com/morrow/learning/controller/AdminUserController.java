package com.morrow.learning.controller;

import com.morrow.learning.domain.Role;
import com.morrow.learning.dto.AdminUserView;
import com.morrow.learning.dto.RoleUpdateRequest;
import com.morrow.learning.service.AdminUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
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
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserView> list() {
        return adminUserService.list();
    }

    public record CreateUserRequest(
            @NotBlank @Size(max = 120) String fullName,
            @NotBlank @Email @Size(max = 190) String email,
            @NotBlank @Size(min = 8) String password,
            Role role,
            @Size(max = 30) String phone,
            @Size(max = 20) String gender) {}
    public record UpdateUserRequest(
            @NotBlank @Size(max = 120) String fullName,
            @Size(max = 30) String phone,
            @Size(max = 20) String gender,
            Role role,
            String status,
            String password) {}

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminUserView create(@Valid @RequestBody CreateUserRequest request) {
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
    public AdminUserView update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        return adminUserService.updateUser(
                id,
                request.fullName(),
                request.phone(),
                request.gender(),
                request.role(),
                request.status(),
                request.password()
        );
    }

    @PatchMapping("/{id}/role")
    public AdminUserView updateRole(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return adminUserService.updateRole(id, request.role());
    }
}