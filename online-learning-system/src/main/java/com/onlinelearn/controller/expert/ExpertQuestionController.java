package com.onlinelearn.controller.expert;

import com.onlinelearn.dto.expert.QuestionFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.QuestionStatus;
import com.onlinelearn.repository.*;
import com.onlinelearn.security.CustomUserDetails;
import com.onlinelearn.service.expert.ExpertQuestionImportService;
import com.onlinelearn.service.expert.ExpertQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/content/questions")
@RequiredArgsConstructor
public class ExpertQuestionController {

    private final ExpertQuestionService expertQuestionService;
    private final ExpertQuestionImportService expertQuestionImportService;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final QuestionLevelRepository levelRepository;
    private final SubjectDimensionRepository dimensionRepository;

    /**
     * 6.5 Danh sách câu hỏi (Questions List)
     * - Columns: ID, Content, Subject, Dimension, Lesson, Level, Status
     * - Filters: Subject, Lesson, Dimension, Level, Status. Search: Content.
     * - Actions: View, Edit, Hide/Show, Import, Export.
     */
    @GetMapping
    public String listQuestions(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(value = "subjectId", required = false) Long subjectId,
                                @RequestParam(value = "lessonId", required = false) Long lessonId,
                                @RequestParam(value = "dimensionId", required = false) Long dimensionId,
                                @RequestParam(value = "levelId", required = false) Long levelId,
                                @RequestParam(value = "status", required = false) QuestionStatus status,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<Question> questions = expertQuestionService.getQuestionsForExpert(
                currentUser, subjectId, lessonId, dimensionId, levelId, status, keyword);

        List<Subject> availableSubjects = expertQuestionService.getSubjectsForExpert(currentUser);

        model.addAttribute("questions", questions);
        model.addAttribute("subjects", availableSubjects);
        model.addAttribute("levels", levelRepository.findAll());
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedLessonId", lessonId);
        model.addAttribute("selectedDimensionId", dimensionId);
        model.addAttribute("selectedLevelId", levelId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        if (subjectId != null) {
            model.addAttribute("lessons", lessonRepository.findBySubjectIdOrderByOrderNumAsc(subjectId));
            model.addAttribute("dimensions", dimensionRepository.findBySubjectId(subjectId));
        } else {
            model.addAttribute("lessons", new ArrayList<>());
            model.addAttribute("dimensions", new ArrayList<>());
        }

        return "content/question-list";
    }

    /**
     * 6.6 Form thêm câu hỏi mới
     * URL: GET /content/questions/new
     */
    @GetMapping("/new")
    public String newQuestionForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @RequestParam(value = "subjectId", required = false) Long subjectId,
                                  Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<Subject> subjects = expertQuestionService.getSubjectsForExpert(currentUser);

        Long effectiveSubjectId = subjectId;
        if (effectiveSubjectId == null && !subjects.isEmpty()) {
            effectiveSubjectId = subjects.get(0).getId();
        }

        QuestionFormDTO formDTO = QuestionFormDTO.builder()
                .subjectId(effectiveSubjectId)
                .optionContents(List.of("", "", "", ""))
                .correctIndex(0)
                .build();

        populateQuestionFormModel(model, formDTO, effectiveSubjectId, currentUser);
        return "content/question-details";
    }

    /**
     * 6.6 Form chỉnh sửa câu hỏi (Question Details)
     * URL: GET /content/questions/{id}
     */
    @GetMapping("/{id}")
    public String editQuestionForm(@PathVariable("id") Long id,
                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                   Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Question question = expertQuestionService.getQuestionById(id, currentUser);
        QuestionFormDTO formDTO = expertQuestionService.toFormDTO(question);

        populateQuestionFormModel(model, formDTO, formDTO.getSubjectId(), currentUser);
        return "content/question-details";
    }

    /**
     * Xem chi tiết câu hỏi (View Action)
     * URL: GET /content/questions/{id}/view
     */
    @GetMapping("/{id}/view")
    public String viewQuestion(@PathVariable("id") Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Question question = expertQuestionService.getQuestionById(id, currentUser);
        model.addAttribute("question", question);
        return "content/question-view";
    }

    /**
     * 6.6 Lưu câu hỏi bằng QuestionFormDTO
     * URL: POST /content/questions/{id} hoặc /content/questions/save hoặc /content/questions/new
     */
    @PostMapping({"/save", "/new", "/{id}"})
    public String saveQuestion(@PathVariable(value = "id", required = false) Long pathId,
                               @ModelAttribute("questionForm") QuestionFormDTO formDTO,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        if (pathId != null && formDTO.getId() == null) {
            formDTO.setId(pathId);
        }

        User currentUser = userDetails != null ? userDetails.getUser() : null;
        try {
            expertQuestionService.saveQuestion(formDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu câu hỏi thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (formDTO.getId() != null) {
                return "redirect:/content/questions/" + formDTO.getId();
            }
            return "redirect:/content/questions/new?subjectId=" + formDTO.getSubjectId();
        }

        return "redirect:/content/questions";
    }

    /**
     * Ẩn / Hiện câu hỏi
     * URL: POST /content/questions/{id}/toggle-status
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        expertQuestionService.toggleQuestionStatus(id, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái câu hỏi!");
        return "redirect:/content/questions";
    }

    // ─────────────────────────── EXCEL IMPORT / EXPORT / TEMPLATE ───────────────────────────

    /**
     * Import câu hỏi hàng loạt từ file Excel
     * URL: POST /content/questions/import
     */
    @PostMapping("/import")
    public String handleImport(@RequestParam("file") MultipartFile file,
                               @RequestParam(value = "subjectId", required = false) Long subjectId,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một file Excel (.xlsx) để import!");
            return "redirect:/content/questions";
        }

        ExpertQuestionImportService.ImportResult result = expertQuestionImportService.importQuestionsFromExcel(file, subjectId);

        if (result.errorCount() == 0) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Import thành công " + result.successCount() + " câu hỏi từ file Excel!");
        } else {
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Import hoàn tất với " + result.successCount() + " câu hỏi thành công và "
                            + result.errorCount() + " lỗi. Chi tiết: " + String.join("; ", result.errorMessages()));
        }

        return "redirect:/content/questions";
    }

    /**
     * Tải file Excel mẫu câu hỏi
     * URL: GET /content/questions/template
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] fileBytes = expertQuestionImportService.generateSampleExcelTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"question_import_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileBytes);
    }

    /**
     * Xuất danh sách câu hỏi ra file Excel (Excel Export)
     * URL: GET /content/questions/export
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportQuestions(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam(value = "subjectId", required = false) Long subjectId,
                                                  @RequestParam(value = "lessonId", required = false) Long lessonId,
                                                  @RequestParam(value = "dimensionId", required = false) Long dimensionId,
                                                  @RequestParam(value = "levelId", required = false) Long levelId,
                                                  @RequestParam(value = "status", required = false) QuestionStatus status,
                                                  @RequestParam(value = "keyword", required = false) String keyword) throws IOException {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        byte[] fileBytes = expertQuestionService.exportQuestionsToExcel(
                currentUser, subjectId, lessonId, dimensionId, levelId, status, keyword);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"questions_export.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileBytes);
    }

    private void populateQuestionFormModel(Model model, QuestionFormDTO formDTO, Long subjectId, User currentUser) {
        model.addAttribute("questionForm", formDTO);
        model.addAttribute("subjects", expertQuestionService.getSubjectsForExpert(currentUser));
        model.addAttribute("levels", levelRepository.findAll());

        if (subjectId != null) {
            model.addAttribute("lessons", lessonRepository.findBySubjectIdOrderByOrderNumAsc(subjectId));
            model.addAttribute("dimensions", dimensionRepository.findBySubjectId(subjectId));
        } else {
            model.addAttribute("lessons", new ArrayList<>());
            model.addAttribute("dimensions", new ArrayList<>());
        }
    }
}
