// Thư mục: src/main/java/com/onlinelearn/controller/content/SubjectContentController.java
package com.onlinelearn.controller.content;

import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.SubjectStatus;
import com.onlinelearn.repository.DimensionTypeRepository;
import com.onlinelearn.repository.SubjectCategoryRepository;
import com.onlinelearn.repository.UserRepository;
import com.onlinelearn.security.CustomUserDetails;
import com.onlinelearn.service.SubjectContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/content/subjects")
@RequiredArgsConstructor
public class SubjectContentController {

    private final SubjectContentService subjectContentService;
    private final SubjectCategoryRepository categoryRepository;
    private final DimensionTypeRepository dimensionTypeRepository;
    private final UserRepository userRepository;

    /**
     * 1. Danh sách môn học (Subjects List)
     * - Expert: chỉ xem môn học của mình (owner = principal)
     * - Admin: xem toàn bộ môn học
     */
    @GetMapping
    public String listSubjects(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(value = "categoryId", required = false) Long categoryId,
                               @RequestParam(value = "status", required = false) SubjectStatus status,
                               @RequestParam(value = "keyword", required = false) String keyword,
                               Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<Subject> subjects = subjectContentService.getSubjectsForUser(currentUser, categoryId, status, keyword);

        model.addAttribute("subjects", subjects);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", currentUser != null && currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getCode()));

        return "content/subject-list";
    }

    /**
     * 2. Chi tiết môn học (Subject Details) với giao diện 3 Tab:
     * - Tab 1: Overview
     * - Tab 2: Dimension
     * - Tab 3: Price Package
     */
    @GetMapping("/{id}")
    public String subjectDetails(@PathVariable("id") Long id,
                                 @RequestParam(value = "tab", defaultValue = "overview") String activeTab,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        Subject subject = subjectContentService.getSubjectById(id);
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        boolean isAdmin = currentUser != null && currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getCode());

        model.addAttribute("subject", subject);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("dimensionTypes", dimensionTypeRepository.findAll());
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("isAdmin", isAdmin);

        return "content/subject-details";
    }

    /**
     * Cập nhật thông tin Tab Overview
     */
    @PostMapping("/{id}/overview")
    public String updateOverview(@PathVariable("id") Long id,
                                 @ModelAttribute("subject") Subject formSubject,
                                 @RequestParam(value = "categoryId", required = false) Long categoryId,
                                 RedirectAttributes redirectAttributes) {
        subjectContentService.updateSubjectOverview(id, formSubject, categoryId);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin khóa học thành công!");
        return "redirect:/content/subjects/" + id + "?tab=overview";
    }

    /**
     * Xuất bản môn học (Chỉ Admin)
     */
    @PostMapping("/{id}/publish")
    public String publishSubject(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        subjectContentService.publishSubject(id, userDetails != null ? userDetails.getUser() : null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xuất bản (PUBLISHED) khóa học thành công!");
        return "redirect:/content/subjects/" + id + "?tab=overview";
    }

    /**
     * Hủy xuất bản môn học (Chỉ Admin)
     */
    @PostMapping("/{id}/unpublish")
    public String unpublishSubject(@PathVariable("id") Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        subjectContentService.unpublishSubject(id, userDetails != null ? userDetails.getUser() : null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy xuất bản (UNPUBLISHED) khóa học!");
        return "redirect:/content/subjects/" + id + "?tab=overview";
    }

    /**
     * Thêm Dimension vào môn học (Cả Admin và Expert)
     */
    @PostMapping("/{id}/dimensions")
    public String addDimension(@PathVariable("id") Long id,
                               @ModelAttribute SubjectDimension dimension,
                               @RequestParam(value = "dimensionTypeId", required = false) Long dimensionTypeId,
                               RedirectAttributes redirectAttributes) {
        if (dimensionTypeId != null) {
            dimension.setType(dimensionTypeRepository.findById(dimensionTypeId).orElse(null));
        }
        subjectContentService.addDimension(id, dimension, dimensionTypeId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm Dimension thành công!");
        return "redirect:/content/subjects/" + id + "?tab=dimension";
    }

    /**
     * Xóa Dimension
     */
    @PostMapping("/{id}/dimensions/{dimId}/delete")
    public String deleteDimension(@PathVariable("id") Long id,
                                  @PathVariable("dimId") Long dimId,
                                  RedirectAttributes redirectAttributes) {
        subjectContentService.deleteDimension(dimId);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa Dimension!");
        return "redirect:/content/subjects/" + id + "?tab=dimension";
    }

    /**
     * Thêm Price Package (Chỉ Admin)
     */
    @PostMapping("/{id}/packages")
    public String addPricePackage(@PathVariable("id") Long id,
                                  @ModelAttribute PricePackage pricePackage,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        subjectContentService.addPricePackage(id, pricePackage, userDetails != null ? userDetails.getUser() : null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm Price Package thành công!");
        return "redirect:/content/subjects/" + id + "?tab=package";
    }

    /**
     * Xóa Price Package (Chỉ Admin)
     */
    @PostMapping("/{id}/packages/{pkgId}/delete")
    public String deletePricePackage(@PathVariable("id") Long id,
                                     @PathVariable("pkgId") Long pkgId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        subjectContentService.deletePricePackage(pkgId, userDetails != null ? userDetails.getUser() : null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa Price Package!");
        return "redirect:/content/subjects/" + id + "?tab=package";
    }

    /**
     * Form tạo môn học mới (Chỉ Admin - có trường chọn Owner Expert)
     */
    @GetMapping("/new")
    public String newSubjectForm(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        if (currentUser == null || currentUser.getRole() == null || !"ADMIN".equals(currentUser.getRole().getCode())) {
            return "redirect:/content/subjects";
        }

        model.addAttribute("subject", new Subject());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("experts", userRepository.findByRoleCode("EXPERT"));
        return "content/subject-new";
    }

    /**
     * Xử lý tạo môn học mới (Chỉ Admin)
     */
    @PostMapping("/new")
    public String createSubject(@ModelAttribute Subject subject,
                                @RequestParam(value = "categoryId", required = false) Long categoryId,
                                @RequestParam(value = "ownerId", required = false) Long ownerId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        if (currentUser == null || currentUser.getRole() == null || !"ADMIN".equals(currentUser.getRole().getCode())) {
            return "redirect:/content/subjects";
        }

        Subject created = subjectContentService.createSubject(subject, categoryId, ownerId);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo môn học mới thành công!");
        return "redirect:/content/subjects/" + created.getId();
    }
}
