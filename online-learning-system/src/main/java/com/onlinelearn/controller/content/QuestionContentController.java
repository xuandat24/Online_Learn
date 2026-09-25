// Thư mục: src/main/java/com/onlinelearn/controller/content/QuestionContentController.java
package com.onlinelearn.controller.content;

import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.QuestionStatus;
import com.onlinelearn.repository.*;
import com.onlinelearn.security.CustomUserDetails;
import com.onlinelearn.service.QuestionContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/content/questions")
@RequiredArgsConstructor
public class QuestionContentController {

    private final QuestionContentService questionContentService;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final QuestionLevelRepository levelRepository;
    private final SubjectDimensionRepository dimensionRepository;

    /**
     * 5. Danh sách câu hỏi (Questions List)
     * URL: GET /content/questions
     */
    @GetMapping
    public String listQuestions(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestParam(value = "subjectId", required = false) Long subjectId,
                                @RequestParam(value = "lessonId", required = false) Long lessonId,
                                @RequestParam(value = "levelId", required = false) Long levelId,
                                @RequestParam(value = "status", required = false) QuestionStatus status,
                                @RequestParam(value = "keyword", required = false) String keyword,
                                Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        List<Question> questions = questionContentService.getQuestionsForUser(currentUser, subjectId, lessonId, levelId, status, keyword);

        model.addAttribute("questions", questions);
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("levels", levelRepository.findAll());
        model.addAttribute("selectedSubjectId", subjectId);
        model.addAttribute("selectedLessonId", lessonId);
        model.addAttribute("selectedLevelId", levelId);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);

        if (subjectId != null) {
            model.addAttribute("lessons", lessonRepository.findBySubjectIdOrderByOrderNumAsc(subjectId));
        }

        return "content/question-list";
    }

    /**
     * 6. Form thêm câu hỏi mới
     * URL: GET /content/questions/new
     */
    @GetMapping("/new")
    public String newQuestionForm(@RequestParam(value = "subjectId", required = false) Long subjectId, Model model) {
        Question question = new Question();
        if (subjectId != null) {
            subjectRepository.findById(subjectId).ifPresent(question::setSubject);
        }

        populateQuestionFormModel(model, question, subjectId);
        return "content/question-details";
    }

    /**
     * 6. Form chỉnh sửa câu hỏi (Question Details)
     * URL: GET /content/questions/{id}
     */
    @GetMapping("/{id}")
    public String editQuestionForm(@PathVariable("id") Long id, Model model) {
        Question question = questionContentService.getQuestionById(id);
        Long subjectId = question.getSubject() != null ? question.getSubject().getId() : null;

        populateQuestionFormModel(model, question, subjectId);
        return "content/question-details";
    }

    /**
     * Lưu câu hỏi (Thêm mới hoặc Cập nhật)
     * URL: POST /content/questions/save
     */
    @PostMapping("/save")
    public String saveQuestion(@ModelAttribute("question") Question question,
                               @RequestParam("subjectId") Long subjectId,
                               @RequestParam(value = "lessonId", required = false) Long lessonId,
                               @RequestParam(value = "levelId", required = false) Long levelId,
                               @RequestParam(value = "dimensionIds", required = false) List<Long> dimensionIds,
                               @RequestParam("optionContent") List<String> optionContents,
                               @RequestParam("correctIndex") Integer correctIndex,
                               RedirectAttributes redirectAttributes) {
        try {
            questionContentService.saveQuestion(question, subjectId, lessonId, levelId, dimensionIds, optionContents, correctIndex);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu câu hỏi thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            if (question.getId() != null) {
                return "redirect:/content/questions/" + question.getId();
            }
            return "redirect:/content/questions/new?subjectId=" + subjectId;
        }

        return "redirect:/content/questions";
    }

    /**
     * Ẩn / Hiện câu hỏi
     * URL: POST /content/questions/{id}/toggle-status
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        questionContentService.toggleQuestionStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái câu hỏi!");
        return "redirect:/content/questions";
    }

    private void populateQuestionFormModel(Model model, Question question, Long subjectId) {
        model.addAttribute("question", question);
        model.addAttribute("subjects", subjectRepository.findAll());
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
