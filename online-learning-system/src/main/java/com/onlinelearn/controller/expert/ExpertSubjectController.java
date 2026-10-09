package com.onlinelearn.controller.expert;

import com.onlinelearn.dto.expert.PricePackageFormDTO;
import com.onlinelearn.dto.expert.SubjectDimensionFormDTO;
import com.onlinelearn.dto.expert.SubjectOverviewFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.SubjectStatus;
import com.onlinelearn.repository.DimensionTypeRepository;
import com.onlinelearn.repository.SubjectCategoryRepository;
import com.onlinelearn.repository.UserRepository;
import com.onlinelearn.security.CustomUserDetails;
import com.onlinelearn.service.expert.ExpertSubjectService;
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
public class ExpertSubjectController {

    private final ExpertSubjectService expertSubjectService;
    private final SubjectCategoryRepository categoryRepository;
    private final DimensionTypeRepository dimensionTypeRepository;
    private final UserRepository userRepository;

    /**
     * 6.1 Danh sách môn học (Subjects List)
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
        boolean isAdmin = currentUser != null && currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getCode());

        List<Subject> subjects = expertSubjectService.getSubjectsForUser(currentUser, categoryId, status, keyword);

        model.addAttribute("subjects", subjects);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isAdmin", isAdmin);

        return "content/subject-list";
    }

    /**
     * 6.2 Chi tiết môn học (Subject Details) với giao diện 3 Tab: Overview / Dimension / Price Package
     */
    @GetMapping("/{id}")
    public String subjectDetails(@PathVariable("id") Long id,
                                 @RequestParam(value = "tab", defaultValue = "overview") String activeTab,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        boolean isAdmin = currentUser != null && currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().getCode());

        Subject subject = expertSubjectService.getSubjectById(id, currentUser);
        SubjectOverviewFormDTO overviewForm = expertSubjectService.toOverviewFormDTO(subject);

        model.addAttribute("subject", subject);
        model.addAttribute("overviewForm", overviewForm);
        model.addAttribute("dimensionForm", new SubjectDimensionFormDTO());
        model.addAttribute("pricePackageForm", new PricePackageFormDTO());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("dimensionTypes", dimensionTypeRepository.findAll());
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("isAdmin", isAdmin);

        return "content/subject-details";
    }

    /**
     * Cập nhật thông tin Tab Overview bằng SubjectOverviewFormDTO
     */
    @PostMapping("/{id}/overview")
    public String updateOverview(@PathVariable("id") Long id,
                                 @ModelAttribute("overviewForm") SubjectOverviewFormDTO formDTO,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        expertSubjectService.updateSubjectOverview(id, formDTO, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin khóa học thành công!");
        return "redirect:/content/subjects/" + id + "?tab=overview";
    }

    /**
     * Thêm Dimension vào môn học (Cả Admin và Expert)
     */
    @PostMapping("/{id}/dimensions")
    public String addDimension(@PathVariable("id") Long id,
                               @ModelAttribute("dimensionForm") SubjectDimensionFormDTO formDTO,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        expertSubjectService.addDimension(id, formDTO, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm Dimension thành công!");
        return "redirect:/content/subjects/" + id + "?tab=dimension";
    }

    /**
     * Sửa Dimension (Cả Admin và Expert)
     */
    @PostMapping("/{id}/dimensions/{dimId}/edit")
    public String editDimension(@PathVariable("id") Long id,
                                @PathVariable("dimId") Long dimId,
                                @ModelAttribute("dimensionForm") SubjectDimensionFormDTO formDTO,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        expertSubjectService.updateDimension(dimId, formDTO, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật Dimension thành công!");
        return "redirect:/content/subjects/" + id + "?tab=dimension";
    }

    /**
     * Xóa Dimension (Cả Admin và Expert)
     */
    @PostMapping("/{id}/dimensions/{dimId}/delete")
    public String deleteDimension(@PathVariable("id") Long id,
                                  @PathVariable("dimId") Long dimId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        expertSubjectService.deleteDimension(dimId, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa Dimension!");
        return "redirect:/content/subjects/" + id + "?tab=dimension";
    }

    // ─────────────────────────── ADMIN-ONLY ACTIONS ───────────────────────────

    @PostMapping("/{id}/publish")
    public String publishSubject(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        expertSubjectService.publishSubject(id, userDetails != null ? userDetails.getUser() : null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xuất bản (PUBLISHED) khóa học thành công!");
        return "redirect:/content/subjects/" + id + "?tab=overview";
    }

    @PostMapping("/{id}/unpublish")
    public String unpublishSubject(@PathVariable("id") Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        expertSubjectService.unpublishSubject(id, userDetails != null ? userDetails.getUser() : null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã hủy xuất bản (UNPUBLISHED) khóa học!");
        return "redirect:/content/subjects/" + id + "?tab=overview";
    }

    @PostMapping("/{id}/packages")
    public String addPricePackage(@PathVariable("id") Long id,
                                  @ModelAttribute("pricePackageForm") PricePackageFormDTO formDTO,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  RedirectAttributes redirectAttributes) {
        expertSubjectService.addPricePackage(id, formDTO, userDetails != null ? userDetails.getUser() : null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thêm Price Package thành công!");
        return "redirect:/content/subjects/" + id + "?tab=package";
    }

    @PostMapping("/{id}/packages/{pkgId}/edit")
    public String editPricePackage(@PathVariable("id") Long id,
                                   @PathVariable("pkgId") Long pkgId,
                                   @ModelAttribute("pricePackageForm") PricePackageFormDTO formDTO,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   RedirectAttributes redirectAttributes) {
        expertSubjectService.updatePricePackage(id, pkgId, formDTO, userDetails != null ? userDetails.getUser() : null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật Price Package thành công!");
        return "redirect:/content/subjects/" + id + "?tab=package";
    }

    @PostMapping("/{id}/packages/{pkgId}/delete")
    public String deletePricePackage(@PathVariable("id") Long id,
                                     @PathVariable("pkgId") Long pkgId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        expertSubjectService.deletePricePackage(pkgId, userDetails != null ? userDetails.getUser() : null);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa Price Package!");
        return "redirect:/content/subjects/" + id + "?tab=package";
    }

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

    @PostMapping("/new")
    public String createSubject(@ModelAttribute Subject subject,
                                @RequestParam(value = "categoryId", required = false) Long categoryId,
                                @RequestParam(value = "ownerId", required = false) Long ownerId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Subject created = expertSubjectService.createSubject(subject, categoryId, ownerId, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo môn học mới thành công!");
        return "redirect:/content/subjects/" + created.getId();
    }
}
