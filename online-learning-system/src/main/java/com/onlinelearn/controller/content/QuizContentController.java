// Thư mục: src/main/java/com/onlinelearn/controller/content/QuizContentController.java
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

    /**
     * 8. Danh sách bài thi / Quiz (Quizzes List)
     * URL: GET /content/quizzes
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
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("quizTypes", testTypeRepository.findAll());
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedQuizTypeId", quizTypeId);
        model.addAttribute("keyword", keyword);

        return "content/quiz-list";
    }

    /**
     * 9. Form thêm Quiz mới
     * URL: GET /content/quizzes/new
     */
    @GetMapping("/new")
    public String newQuizForm(@RequestParam(value = "subjectId", required = false) Long subjectId, Model model) {
        Quiz quiz = new Quiz();
        if (subjectId != null) {
            subjectRepository.findById(subjectId).ifPresent(quiz::setSubject);
        }

        populateQuizFormModel(model, quiz, subjectId);
        return "content/quiz-details";
    }

    /**
     * 9. Form chỉnh sửa Quiz (Quiz Details)
     * URL: GET /content/quizzes/{id}
     */
    @GetMapping("/{id}")
    public String editQuizForm(@PathVariable("id") Long id, Model model) {
        Quiz quiz = quizContentService.getQuizById(id);
        Long subjectId = quiz.getSubject() != null ? quiz.getSubject().getId() : null;

        populateQuizFormModel(model, quiz, subjectId);
        model.addAttribute("hasAttempts", quizContentService.hasAttempts(id));

        return "content/quiz-details";
    }

    /**
     * Lưu bài Quiz cùng danh sách câu hỏi gắn kèm
     * URL: POST /content/quizzes/save
     */
    @PostMapping("/save")
    public String saveQuiz(@ModelAttribute("quiz") Quiz quiz,
                           @RequestParam("subjectId") Long subjectId,
                           @RequestParam(value = "levelId", required = false) Long levelId,
                           @RequestParam(value = "quizTypeId", required = false) Long quizTypeId,
                           @RequestParam(value = "questionIds", required = false) List<Long> questionIds,
                           RedirectAttributes redirectAttributes) {
        try {
            quizContentService.saveQuiz(quiz, subjectId, levelId, quizTypeId, questionIds);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu bài Quiz thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (quiz.getId() != null) {
                return "redirect:/content/quizzes/" + quiz.getId();
            }
        }

        return "redirect:/content/quizzes";
    }

    /**
     * Xóa bài Quiz (Chặn nếu đã có QuizAttempt)
     * URL: POST /content/quizzes/{id}/delete
     */
    @PostMapping("/{id}/delete")
    public String deleteQuiz(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            quizContentService.deleteQuiz(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bài Quiz thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/content/quizzes";
    }

    private void populateQuizFormModel(Model model, Quiz quiz, Long subjectId) {
        model.addAttribute("quiz", quiz);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("levels", levelRepository.findAll());
        model.addAttribute("quizTypes", testTypeRepository.findAll());

        if (subjectId != null) {
            model.addAttribute("availableQuestions", questionRepository.findBySubjectId(subjectId));
        } else {
            model.addAttribute("availableQuestions", new ArrayList<>());
        }

        List<Long> selectedQuestionIds = quiz.getQuizQuestions() != null
                ? quiz.getQuizQuestions().stream().map(qq -> qq.getQuestion().getId()).toList()
                : new ArrayList<>();
        model.addAttribute("selectedQuestionIds", selectedQuestionIds);
    }
}
