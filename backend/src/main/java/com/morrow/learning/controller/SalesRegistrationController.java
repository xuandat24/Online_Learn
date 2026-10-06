package com.morrow.learning.controller;

import com.morrow.learning.dto.RegistrationView;
import com.morrow.learning.service.SalesRegistrationService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales/registrations")
@PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
public class SalesRegistrationController {
    private final SalesRegistrationService salesRegistrationService;

    public SalesRegistrationController(SalesRegistrationService salesRegistrationService) {
        this.salesRegistrationService = salesRegistrationService;
    }

    @GetMapping
    public List<RegistrationView> submitted() {
        return salesRegistrationService.submitted();
    }

    @PatchMapping("/{id}/paid")
    public RegistrationView markPaid(@PathVariable Long id) {
        return salesRegistrationService.markPaid(id);
    }
}