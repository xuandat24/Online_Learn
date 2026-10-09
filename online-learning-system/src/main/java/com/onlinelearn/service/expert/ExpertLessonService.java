package com.onlinelearn.service.expert;

import com.onlinelearn.dto.expert.LessonFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.LessonStatus;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import com.onlinelearn.repository.LessonRepository;
import com.onlinelearn.repository.QuizRepository;
import com.onlinelearn.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpertLessonService {

    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;
    private final QuizRepository quizRepository;

    public List<Lesson> getLessonsBySubject(Long subjectId, String keyword, User currentUser) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + subjectId));
        validateSubjectOwnership(subject, currentUser);

        if (keyword != null && !keyword.trim().isEmpty()) {
            return lessonRepository.findBySubjectIdAndNameContainingIgnoreCaseOrderByOrderNumAsc(subjectId, keyword.trim());
        }
        return lessonRepository.findBySubjectIdOrderByOrderNumAsc(subjectId);
    }

    public Lesson getLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học ID: " + id));
    }

    public Lesson getLessonById(Long id, User currentUser) {
        Lesson lesson = getLessonById(id);
        validateSubjectOwnership(lesson.getSubject(), currentUser);
        return lesson;
    }

    public LessonFormDTO toFormDTO(Lesson lesson) {
        return LessonFormDTO.builder()
                .id(lesson.getId())
                .subjectId(lesson.getSubject() != null ? lesson.getSubject().getId() : null)
                .name(lesson.getName())
                .orderNum(lesson.getOrderNum())
                .parentId(lesson.getParentLesson() != null ? lesson.getParentLesson().getId() : null)
                .type(lesson.getType() != null ? lesson.getType() : LessonTypeEnum.LESSON)
                .status(lesson.getStatus() != null ? lesson.getStatus() : LessonStatus.ACTIVE)
                .videoLink(lesson.getVideoLink())
                .htmlContent(lesson.getHtmlContent())
                .quizId(lesson.getQuiz() != null ? lesson.getQuiz().getId() : null)
                .build();
    }

    /**
     * Quy tắc nghiệp vụ cho Lesson:
     * - SUBJECT_TOPIC -> null videoLink, htmlContent, quiz
     * - LESSON -> giữ videoLink & htmlContent, null quiz
     * - QUIZ -> null videoLink & htmlContent, gán selected Quiz
     */
    @Transactional
    public Lesson saveLesson(LessonFormDTO formDTO, User currentUser) {
        if (formDTO.getSubjectId() == null) {
            throw new IllegalArgumentException("Môn học không được để trống!");
        }

        Subject subject = subjectRepository.findById(formDTO.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại"));

        validateSubjectOwnership(subject, currentUser);

        Lesson lesson;
        if (formDTO.getId() != null) {
            lesson = getLessonById(formDTO.getId(), currentUser);
        } else {
            lesson = new Lesson();
        }

        lesson.setSubject(subject);
        lesson.setName(formDTO.getName());
        lesson.setOrderNum(formDTO.getOrderNum() != null ? formDTO.getOrderNum() : 1);
        lesson.setStatus(formDTO.getStatus() != null ? formDTO.getStatus() : LessonStatus.ACTIVE);

        LessonTypeEnum type = formDTO.getType() != null ? formDTO.getType() : LessonTypeEnum.LESSON;
        lesson.setType(type);

        if (formDTO.getParentId() != null) {
            Lesson parent = lessonRepository.findById(formDTO.getParentId()).orElse(null);
            lesson.setParentLesson(parent);
        } else {
            lesson.setParentLesson(null);
        }

        switch (type) {
            case SUBJECT_TOPIC -> {
                lesson.setVideoLink(null);
                lesson.setHtmlContent(null);
                lesson.setQuiz(null);
            }
            case LESSON -> {
                lesson.setVideoLink(formDTO.getVideoLink());
                lesson.setHtmlContent(formDTO.getHtmlContent());
                lesson.setQuiz(null);
            }
            case QUIZ -> {
                lesson.setVideoLink(null);
                lesson.setHtmlContent(null);
                if (formDTO.getQuizId() != null) {
                    Quiz quiz = quizRepository.findById(formDTO.getQuizId())
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Quiz được chọn!"));
                    lesson.setQuiz(quiz);
                } else {
                    lesson.setQuiz(null);
                }
            }
        }

        return lessonRepository.save(lesson);
    }

    @Transactional
    public void toggleLessonStatus(Long id, User currentUser) {
        Lesson lesson = getLessonById(id, currentUser);
        if (lesson.getStatus() == LessonStatus.ACTIVE) {
            lesson.setStatus(LessonStatus.INACTIVE);
        } else {
            lesson.setStatus(LessonStatus.ACTIVE);
        }
        lessonRepository.save(lesson);
    }

    public void validateSubjectOwnership(Subject subject, User currentUser) {
        if (currentUser != null && currentUser.getRole() != null && "EXPERT".equals(currentUser.getRole().getCode())) {
            if (subject == null || subject.getOwner() == null || !subject.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Bạn không có quyền quản trị bài học của môn này!");
            }
        }
    }
}
