package com.onlinelearn.controller.expert;

import com.onlinelearn.dto.expert.LessonFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.LessonStatus;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import com.onlinelearn.repository.QuizRepository;
import com.onlinelearn.repository.SubjectRepository;
import com.onlinelearn.security.CustomUserDetails;
import com.onlinelearn.service.expert.ExpertLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/content")
@RequiredArgsConstructor
public class ExpertLessonController {

    private final ExpertLessonService expertLessonService;
    private final SubjectRepository subjectRepository;
    private final QuizRepository quizRepository;

    /**
     * 6.3 Danh sách bài học của một môn học (Subject Lessons)
     * URL: GET /content/subjects/{subjectId}/lessons
     */
    @GetMapping("/subjects/{subjectId}/lessons")
    public String listSubjectLessons(@PathVariable("subjectId") Long subjectId,
                                     @RequestParam(value = "keyword", required = false) String keyword,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + subjectId));

        List<Lesson> lessons = expertLessonService.getLessonsBySubject(subjectId, keyword, currentUser);

        model.addAttribute("subject", subject);
        model.addAttribute("lessons", lessons);
        model.addAttribute("keyword", keyword);

        return "content/lesson-list";
    }

    /**
     * 6.4 Form thêm bài học mới
     * URL: GET /content/subjects/{subjectId}/lessons/new OR /content/lessons/new?subjectId=...
     */
    @GetMapping({"/subjects/{subjectId}/lessons/new", "/lessons/new"})
    public String newLessonForm(@PathVariable(value = "subjectId", required = false) Long pathSubjectId,
                                @RequestParam(value = "subjectId", required = false) Long paramSubjectId,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        Long subjectId = pathSubjectId != null ? pathSubjectId : paramSubjectId;
        if (subjectId == null) {
            return "redirect:/content/subjects";
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại"));

        LessonFormDTO formDTO = LessonFormDTO.builder()
                .subjectId(subjectId)
                .type(LessonTypeEnum.LESSON)
                .status(LessonStatus.ACTIVE)
                .orderNum(1)
                .build();

        populateLessonFormModel(model, subject, formDTO, userDetails != null ? userDetails.getUser() : null);
        return "content/lesson-details";
    }

    /**
     * 6.4 Form chỉnh sửa bài học (Lesson Details)
     * URL: GET /content/lessons/{id}
     */
    @GetMapping("/lessons/{id}")
    public String editLessonForm(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal CustomUserDetails userDetails,
                                 Model model) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Lesson lesson = expertLessonService.getLessonById(id, currentUser);
        LessonFormDTO formDTO = expertLessonService.toFormDTO(lesson);

        populateLessonFormModel(model, lesson.getSubject(), formDTO, currentUser);
        return "content/lesson-details";
    }

    /**
     * 6.4 Lưu bài học (POST /content/lessons/{id} hoặc /content/lessons/save hoặc /content/lessons/new)
     */
    @PostMapping({"/lessons/{id}", "/lessons/save", "/lessons/new"})
    public String saveLesson(@PathVariable(value = "id", required = false) Long pathId,
                             @ModelAttribute("lessonForm") LessonFormDTO formDTO,
                             @AuthenticationPrincipal CustomUserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (pathId != null && formDTO.getId() == null) {
            formDTO.setId(pathId);
        }

        User currentUser = userDetails != null ? userDetails.getUser() : null;
        Lesson saved = expertLessonService.saveLesson(formDTO, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu bài học thành công!");
        return "redirect:/content/subjects/" + saved.getSubject().getId() + "/lessons";
    }

    /**
     * Kích hoạt / Hủy kích hoạt bài học
     * URL: POST /content/lessons/{id}/toggle-status
     */
    @PostMapping("/lessons/{id}/toggle-status")
    public String toggleLessonStatus(@PathVariable("id") Long id,
                                     @RequestParam("subjectId") Long subjectId,
                                     @AuthenticationPrincipal CustomUserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = userDetails != null ? userDetails.getUser() : null;
        expertLessonService.toggleLessonStatus(id, currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái bài học!");
        return "redirect:/content/subjects/" + subjectId + "/lessons";
    }

    private void populateLessonFormModel(Model model, Subject subject, LessonFormDTO formDTO, User currentUser) {
        model.addAttribute("subject", subject);
        model.addAttribute("lessonForm", formDTO);
        model.addAttribute("allSubjects", subjectRepository.findAll());
        model.addAttribute("parentTopics", expertLessonService.getLessonsBySubject(subject.getId(), null, currentUser).stream()
                .filter(l -> l.getType() == LessonTypeEnum.SUBJECT_TOPIC && (formDTO.getId() == null || !l.getId().equals(formDTO.getId())))
                .toList());
        model.addAttribute("quizzes", quizRepository.findBySubjectId(subject.getId()));
        model.addAttribute("lessonTypes", LessonTypeEnum.values());
    }
}
