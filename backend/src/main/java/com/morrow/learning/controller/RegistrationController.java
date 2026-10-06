package com.morrow.learning.controller;

import com.morrow.learning.dto.RegistrationEditRequest;
import com.morrow.learning.dto.RegistrationRequest;
import com.morrow.learning.dto.RegistrationView;
import com.morrow.learning.service.RegistrationService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegistrationView submit(Principal principal, @Valid @RequestBody RegistrationRequest request) {
        return registrationService.submit(principal == null ? null : principal.getName(), request);
    }

    @GetMapping("/me")
    public List<RegistrationView> mine(Principal principal) {
        return registrationService.mine(principal.getName());
    }

    @PutMapping("/{id}")
    public RegistrationView edit(Principal principal, @PathVariable Long id,
                                 @Valid @RequestBody RegistrationEditRequest request) {
        return registrationService.edit(principal.getName(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(Principal principal, @PathVariable Long id) {
        registrationService.cancel(principal.getName(), id);
    }
}