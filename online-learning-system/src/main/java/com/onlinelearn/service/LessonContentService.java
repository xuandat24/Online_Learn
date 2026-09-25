// Thư mục: src/main/java/com/onlinelearn/service/LessonContentService.java
package com.onlinelearn.service;

import com.onlinelearn.entity.Lesson;
import com.onlinelearn.entity.Quiz;
import com.onlinelearn.entity.Subject;
import com.onlinelearn.entity.enums.LessonStatus;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import com.onlinelearn.repository.LessonRepository;
import com.onlinelearn.repository.QuizRepository;
import com.onlinelearn.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LessonContentService {

    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;
    private final QuizRepository quizRepository;

    public List<Lesson> getLessonsBySubject(Long subjectId, String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return lessonRepository.findBySubjectIdAndNameContainingIgnoreCaseOrderByOrderNumAsc(subjectId, keyword.trim());
        }
        return lessonRepository.findBySubjectIdOrderByOrderNumAsc(subjectId);
    }

    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học ID: " + id));
    }

    /**
     * Lưu bài học với xử lý logic tương ứng với từng loại:
     * - SUBJECT_TOPIC: không có videoLink, htmlContent, quiz
     * - LESSON: có videoLink và/hoặc htmlContent
     * - QUIZ: gán bài Quiz được chọn
     */
    @Transactional
    public Lesson saveLesson(Lesson formLesson, Long subjectId, Long parentId, Long quizId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại"));

        Lesson lesson;
        if (formLesson.getId() != null) {
            lesson = getLessonById(formLesson.getId());
        } else {
            lesson = new Lesson();
            lesson.setSubject(subject);
        }

        lesson.setName(formLesson.getName());
        lesson.setOrderNum(formLesson.getOrderNum() != null ? formLesson.getOrderNum() : 1);
        lesson.setType(formLesson.getType());
        lesson.setStatus(formLesson.getStatus() != null ? formLesson.getStatus() : LessonStatus.ACTIVE);

        if (parentId != null) {
            Lesson parent = lessonRepository.findById(parentId).orElse(null);
            lesson.setParentLesson(parent);
        } else {
            lesson.setParentLesson(null);
        }

        if (formLesson.getType() == LessonTypeEnum.SUBJECT_TOPIC) {
            lesson.setVideoLink(null);
            lesson.setHtmlContent(null);
            lesson.setQuiz(null);
        } else if (formLesson.getType() == LessonTypeEnum.LESSON) {
            lesson.setVideoLink(formLesson.getVideoLink());
            lesson.setHtmlContent(formLesson.getHtmlContent());
            lesson.setQuiz(null);
        } else if (formLesson.getType() == LessonTypeEnum.QUIZ) {
            lesson.setVideoLink(null);
            lesson.setHtmlContent(null);
            if (quizId != null) {
                Quiz quiz = quizRepository.findById(quizId).orElse(null);
                lesson.setQuiz(quiz);
            }
        }

        return lessonRepository.save(lesson);
    }

    @Transactional
    public void toggleLessonStatus(Long id) {
        Lesson lesson = getLessonById(id);
        if (lesson.getStatus() == LessonStatus.ACTIVE) {
            lesson.setStatus(LessonStatus.INACTIVE);
        } else {
            lesson.setStatus(LessonStatus.ACTIVE);
        }
        lessonRepository.save(lesson);
    }
}
