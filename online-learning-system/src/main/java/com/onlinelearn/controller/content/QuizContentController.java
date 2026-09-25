package com.onlinelearn.controller.content;

import com.onlinelearn.entity.*;
import com.onlinelearn.repository.*;
import com.onlinelearn.security.CustomUserDetails;
import com.onlinelearn.service.QuizContentService;
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
public class QuizContentController {

    private final QuizContentService quizContentService;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final QuestionLevelRepository levelRepository;
    private final TestTypeRepository testTypeRepository;
    private final SubjectDimensionRepository dimensionRepository;

    // ─────────────────────────── LIST ───────────────────────────

    /**
     * GET /content/quizzes — Danh sách Quiz với filter/search
     */
    @GetMapping
    public String listQuizzes(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(value = "subjectId", required = false) Long subjectId,
                              @RequestParam(value = "quizTypeId", required = false) Long quizTypeId,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<Quiz> quizzes = quizContentService.getQuizzesForUser(currentUser, subjectId, quizTypeId, keyword);

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("subjects", quizContentService.getSubjectsForUser(currentUser));
        model.addAttribute("quizTypes", testTypeRepository.findAll());
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedQuizTypeId", quizTypeId);
        model.addAttribute("keyword", keyword);

        return "content/quizzes/list";
    }

    // ─────────────────────────── CREATE ───────────────────────────

    /**
     * GET /content/quizzes/new — Form tạo Quiz mới
     */
    @GetMapping("/new")
    public String newQuizForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                              @RequestParam(value = "subjectId", required = false) Long subjectId,
                              Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Quiz quiz = new Quiz();
        if (subjectId != null) {
            subjectRepository.findById(subjectId).ifPresent(quiz::setSubject);
        }
        populateFormModel(model, quiz, subjectId, currentUser);
        return "content/quizzes/form";
    }

    // ─────────────────────────── EDIT ───────────────────────────

    /**
     * GET /content/quizzes/{id}/edit — Form chỉnh sửa Quiz (luôn cho phép, kể cả đã có attempt)
     */
    @GetMapping("/{id}/edit")
    public String editQuizForm(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @PathVariable("id") Long id,
                               Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Quiz quiz = quizContentService.getQuizById(id);
        Long subjectId = quiz.getSubject() != null ? quiz.getSubject().getId() : null;
        populateFormModel(model, quiz, subjectId, currentUser);
        return "content/quizzes/form";
    }

    // ─────────────────────────── SAVE ───────────────────────────

    /**
     * POST /content/quizzes/save — Lưu Quiz (Create hoặc Update)
     */
    @PostMapping("/save")
    public String saveQuiz(@AuthenticationPrincipal CustomUserDetails userDetails,
                           @ModelAttribute("quiz") Quiz quiz,
                           @RequestParam("subjectId") Long subjectId,
                           @RequestParam(value = "levelId", required = false) Long levelId,
                           @RequestParam(value = "quizTypeId", required = false) Long quizTypeId,
                           @RequestParam(value = "questionIds", required = false) List<Long> questionIds,
                           RedirectAttributes redirectAttributes) {
        try {
            quizContentService.saveQuiz(quiz, subjectId, levelId, quizTypeId, questionIds);
            redirectAttributes.addFlashAttribute("successMessage", "✅ Lưu bài Quiz thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi: " + e.getMessage());
            if (quiz.getId() != null) {
                return "redirect:/content/quizzes/" + quiz.getId() + "/edit";
            }
            return "redirect:/content/quizzes/new?subjectId=" + subjectId;
        }
        return "redirect:/content/quizzes";
    }

    // ─────────────────────────── DELETE ───────────────────────────

    /**
     * POST /content/quizzes/{id}/delete — Xóa Quiz kèm toàn bộ dữ liệu liên quan
     */
    @PostMapping("/{id}/delete")
    public String deleteQuiz(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            QuizContentService.DeleteQuizResult result = quizContentService.deleteQuiz(id);
            if (result.hasDetachedLessons()) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "✅ Xóa Quiz thành công! Đã gỡ khỏi " + result.detachedLessonNames().size()
                                + " Lesson: " + String.join(", ", result.detachedLessonNames()));
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "✅ Xóa bài Quiz thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "❌ Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/content/quizzes";
    }

    // ─────────────────────────── HELPER ───────────────────────────

    private void populateFormModel(Model model, Quiz quiz, Long subjectId, User currentUser) {
        model.addAttribute("quiz", quiz);
        model.addAttribute("subjects", quizContentService.getSubjectsForUser(currentUser));
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

        List<Long> selectedQuestionIds = (quiz.getQuizQuestions() != null)
                ? quiz.getQuizQuestions().stream().map(qq -> qq.getQuestion().getId()).toList()
                : new ArrayList<>();
        model.addAttribute("selectedQuestionIds", selectedQuestionIds);
    }
}
