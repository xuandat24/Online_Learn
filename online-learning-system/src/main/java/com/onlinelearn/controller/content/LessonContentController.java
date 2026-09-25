// Thư mục: src/main/java/com/onlinelearn/controller/content/LessonContentController.java
package com.onlinelearn.controller.content;

import com.onlinelearn.entity.Lesson;
import com.onlinelearn.entity.Subject;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import com.onlinelearn.repository.QuizRepository;
import com.onlinelearn.repository.SubjectRepository;
import com.onlinelearn.service.LessonContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/content")
@RequiredArgsConstructor
public class LessonContentController {

    private final LessonContentService lessonContentService;
    private final SubjectRepository subjectRepository;
    private final QuizRepository quizRepository;

    /**
     * 3. Danh sách bài học của một môn học (Subject Lessons)
     * URL: GET /content/subjects/{subjectId}/lessons
     */
    @GetMapping("/subjects/{subjectId}/lessons")
    public String listSubjectLessons(@PathVariable("subjectId") Long subjectId,
                                     @RequestParam(value = "keyword", required = false) String keyword,
                                     Model model) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + subjectId));

        List<Lesson> lessons = lessonContentService.getLessonsBySubject(subjectId, keyword);

        model.addAttribute("subject", subject);
        model.addAttribute("lessons", lessons);
        model.addAttribute("keyword", keyword);

        return "content/lesson-list";
    }

    /**
     * 4. Form thêm bài học mới
     * URL: GET /content/subjects/{subjectId}/lessons/new
     */
    @GetMapping("/subjects/{subjectId}/lessons/new")
    public String newLessonForm(@PathVariable("subjectId") Long subjectId, Model model) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại"));

        Lesson lesson = new Lesson();
        lesson.setSubject(subject);
        lesson.setType(LessonTypeEnum.LESSON);

        populateLessonFormModel(model, subject, lesson);
        return "content/lesson-details";
    }

    /**
     * 4. Form chỉnh sửa bài học (Lesson Details)
     * URL: GET /content/lessons/{id}
     */
    @GetMapping("/lessons/{id}")
    public String editLessonForm(@PathVariable("id") Long id, Model model) {
        Lesson lesson = lessonContentService.getLessonById(id);
        populateLessonFormModel(model, lesson.getSubject(), lesson);
        return "content/lesson-details";
    }

    /**
     * Lưu bài học (Thêm mới hoặc Cập nhật)
     * URL: POST /content/lessons/save
     */
    @PostMapping("/lessons/save")
    public String saveLesson(@ModelAttribute("lesson") Lesson lesson,
                             @RequestParam("subjectId") Long subjectId,
                             @RequestParam(value = "parentId", required = false) Long parentId,
                             @RequestParam(value = "quizId", required = false) Long quizId,
                             RedirectAttributes redirectAttributes) {
        lessonContentService.saveLesson(lesson, subjectId, parentId, quizId);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu bài học thành công!");
        return "redirect:/content/subjects/" + subjectId + "/lessons";
    }

    /**
     * Kích hoạt / Hủy kích hoạt bài học
     * URL: POST /content/lessons/{id}/toggle-status
     */
    @PostMapping("/lessons/{id}/toggle-status")
    public String toggleLessonStatus(@PathVariable("id") Long id,
                                     @RequestParam("subjectId") Long subjectId,
                                     RedirectAttributes redirectAttributes) {
        lessonContentService.toggleLessonStatus(id);
        redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái bài học!");
        return "redirect:/content/subjects/" + subjectId + "/lessons";
    }

    private void populateLessonFormModel(Model model, Subject subject, Lesson lesson) {
        model.addAttribute("subject", subject);
        model.addAttribute("lesson", lesson);
        model.addAttribute("allSubjects", subjectRepository.findAll());
        // Lấy danh sách Topic làm parent
        model.addAttribute("parentTopics", lessonContentService.getLessonsBySubject(subject.getId(), null).stream()
                .filter(l -> l.getType() == LessonTypeEnum.SUBJECT_TOPIC)
                .toList());
        // Lấy danh sách Quiz của môn học để chọn khi type == QUIZ
        model.addAttribute("quizzes", quizRepository.findBySubjectId(subject.getId()));
        model.addAttribute("lessonTypes", LessonTypeEnum.values());
    }
}
