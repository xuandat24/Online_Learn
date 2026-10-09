package com.onlinelearn.controller.expert;

import com.onlinelearn.dto.expert.QuizFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.repository.*;
import com.onlinelearn.security.CustomUserDetails;
import com.onlinelearn.service.expert.ExpertQuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/content/quizzes")
@RequiredArgsConstructor
public class ExpertQuizController {

    private final ExpertQuizService expertQuizService;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final QuestionLevelRepository levelRepository;
    private final TestTypeRepository testTypeRepository;
    private final SubjectDimensionRepository dimensionRepository;

    /**
     * 6.8 GET /content/quizzes — Danh sách Quiz với filter/search
     */
    @GetMapping
    public String listQuizzes(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(value = "subjectId", required = false) Long subjectId,
                              @RequestParam(value = "quizTypeId", required = false) Long quizTypeId,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<Quiz> quizzes = expertQuizService.getQuizzesForExpert(currentUser, subjectId, quizTypeId, keyword);

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("subjects", expertQuizService.getSubjectsForExpert(currentUser));
        model.addAttribute("quizTypes", testTypeRepository.findAll());
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedQuizTypeId", quizTypeId);
        model.addAttribute("keyword", keyword);

        return "content/quizzes/list";
    }

    /**
     * 6.9 GET /content/quizzes/new — Form tạo Quiz mới
     */
    @GetMapping("/new")
    public String newQuizForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(value = "subjectId", required = false) Long subjectId,
                              Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<Subject> availableSubjects = expertQuizService.getSubjectsForExpert(currentUser);

        Long effectiveSubjectId = subjectId;
        if (effectiveSubjectId == null && !availableSubjects.isEmpty()) {
            effectiveSubjectId = availableSubjects.get(0).getId();
        }

        QuizFormDTO formDTO = QuizFormDTO.builder()
                .subjectId(effectiveSubjectId)
                .duration(15)
                .passRate(60.0)
                .build();

        populateFormModel(model, formDTO, effectiveSubjectId, currentUser);
        return "content/quizzes/form";
    }

    /**
     * 6.9 GET /content/quizzes/{id}/edit — Form chỉnh sửa Quiz (luôn cho phép, kể cả đã có attempt)
     */
    @GetMapping("/{id}/edit")
    public String editQuizForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable("id") Long id,
                               Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Quiz quiz = expertQuizService.getQuizById(id, currentUser);
        QuizFormDTO formDTO = expertQuizService.toFormDTO(quiz);

        populateFormModel(model, formDTO, formDTO.getSubjectId(), currentUser);
        return "content/quizzes/form";
    }

    /**
     * 6.9 POST /content/quizzes/new, /{id}/edit, /save — Lưu Quiz bằng QuizFormDTO
     */
    @PostMapping({"/save", "/new", "/{id}/edit"})
    public String saveQuiz(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @PathVariable(value = "id", required = false) Long pathId,
                           @ModelAttribute("quizForm") QuizFormDTO formDTO,
                           RedirectAttributes redirectAttributes) {
        if (pathId != null && formDTO.getId() == null) {
            formDTO.setId(pathId);
        }

        User currentUser = userDetails != null ? userDetails.getUser() : null;
        try {
            expertQuizService.saveQuiz(formDTO, currentUser);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Lưu bài Quiz thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            if (formDTO.getId() != null) {
                return "redirect:/content/quizzes/" + formDTO.getId() + "/edit";
            }
            return "redirect:/content/quizzes/new?subjectId=" + formDTO.getSubjectId();
        }
        return "redirect:/content/quizzes";
    }

    /**
     * 6.10 POST /content/quizzes/{id}/delete — Xóa Quiz kèm cascade an toàn
     */
    @PostMapping("/{id}/delete")
    public String deleteQuiz(@PathVariable("id") Long id,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        try {
            ExpertQuizService.DeleteQuizResult result = expertQuizService.deleteQuiz(id, currentUser);
            if (result.hasDetachedLessons()) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "✅ Xóa Quiz thành công! Đã tự động gỡ liên kết khỏi " + result.detachedLessonNames().size()
                                + " bài học: " + String.join(", ", result.detachedLessonNames()));
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "✅ Xóa bài Quiz thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/content/quizzes";
    }

    private void populateFormModel(Model model, QuizFormDTO formDTO, Long subjectId, User currentUser) {
        model.addAttribute("quizForm", formDTO);
        model.addAttribute("subjects", expertQuizService.getSubjectsForExpert(currentUser));
        model.addAttribute("levels", levelRepository.findAll());
        model.addAttribute("quizTypes", testTypeRepository.findAll());

        List<Question> availableQuestions = new ArrayList<>();
        List<SubjectDimension> dimensions = new ArrayList<>();

        if (subjectId != null) {
            availableQuestions = questionRepository.findBySubjectId(subjectId);
            dimensions = dimensionRepository.findBySubjectId(subjectId);
        }

        model.addAttribute("availableQuestions", availableQuestions);
        model.addAttribute("dimensions", dimensions);
        model.addAttribute("selectedQuestionIds", formDTO.getQuestionIds() != null ? formDTO.getQuestionIds() : new ArrayList<>());
    }
}
