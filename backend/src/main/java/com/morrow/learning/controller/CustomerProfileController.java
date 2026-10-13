package com.morrow.learning.controller;

import com.morrow.learning.dto.ChangePasswordRequest;
import com.morrow.learning.dto.UserProfileUpdateRequest;
import com.morrow.learning.dto.UserProfileView;
import com.morrow.learning.service.CustomerProfileService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/customer")
public class CustomerProfileController {
    private final CustomerProfileService customerProfileService;

    public CustomerProfileController(CustomerProfileService customerProfileService) {
        this.customerProfileService = customerProfileService;
    }

    private String requireEmail(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vui lòng đăng nhập để tiếp tục");
        }
        return principal.getName();
    }

    @GetMapping("/profile")
    public UserProfileView viewProfile(Principal principal) {
        return customerProfileService.getProfile(requireEmail(principal));
    }

    @PutMapping("/profile")
    public UserProfileView updateProfile(Principal principal, @Valid @RequestBody UserProfileUpdateRequest request) {
        return customerProfileService.updateProfile(requireEmail(principal), request);
    }

    @PutMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(Principal principal, @Valid @RequestBody ChangePasswordRequest request) {
        customerProfileService.changePassword(requireEmail(principal), request);
    }
}
