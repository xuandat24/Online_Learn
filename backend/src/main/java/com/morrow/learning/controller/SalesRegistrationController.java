package com.morrow.learning.controller;

import com.morrow.learning.dto.RegistrationView;
import com.morrow.learning.service.SalesRegistrationService;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.morrow.learning.domain.RegistrationStatus;

@RestController
@RequestMapping("/api/sales/registrations")
@PreAuthorize("hasAnyRole('ADMIN', 'SALE')")
public class SalesRegistrationController {
    private final SalesRegistrationService salesRegistrationService;

    public SalesRegistrationController(SalesRegistrationService salesRegistrationService) {
        this.salesRegistrationService = salesRegistrationService;
    }

    @GetMapping("/submitted")
    public List<RegistrationView> submitted() {
        return salesRegistrationService.submitted();
    }

    @GetMapping
    public List<RegistrationView> getAllRegistrations() {
        return salesRegistrationService.getAllRegistrations();
    }

    @GetMapping("/{id}")
    public RegistrationView getRegistrationById(@PathVariable Long id) {
        return salesRegistrationService.getRegistrationById(id);
    }

    @GetMapping("/search")
    public List<RegistrationView> searchRegistrations(@RequestParam String keyword) {
        return salesRegistrationService.searchRegistrations(keyword);
    }

    @GetMapping("/filter")
    public List<RegistrationView> filterByStatus(@RequestParam RegistrationStatus status) {
        return salesRegistrationService.filterByStatus(status);
    }

    @PostMapping("/{id}/cancel")
    public void cancelRegistration(@PathVariable Long id, @RequestParam String reason) {
        salesRegistrationService.cancelRegistration(id, reason);
    }

    @PatchMapping("/{id}/paid")
    public RegistrationView markPaid(@PathVariable Long id) {
        return salesRegistrationService.markPaid(id);
    }
}