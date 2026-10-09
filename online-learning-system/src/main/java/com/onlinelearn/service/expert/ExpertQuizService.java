package com.onlinelearn.service.expert;

import com.onlinelearn.dto.expert.QuizFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpertQuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final QuestionLevelRepository levelRepository;
    private final TestTypeRepository testTypeRepository;

    public List<Quiz> getQuizzesForExpert(User currentUser, Long subjectId, Long quizTypeId, String keyword) {
        Long ownerId = null;
        if (currentUser != null) {
            ownerId = currentUser.getId();
        }
        return quizRepository.searchQuizzes(ownerId, subjectId, quizTypeId, keyword);
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài Quiz ID: " + id));
    }

    public Quiz getQuizById(Long id, User currentUser) {
        Quiz quiz = getQuizById(id);
        validateQuizOwnership(quiz, currentUser);
        return quiz;
    }

    public QuizFormDTO toFormDTO(Quiz quiz) {
        List<Long> qIds = quiz.getQuizQuestions() != null
                ? quiz.getQuizQuestions().stream().map(qq -> qq.getQuestion().getId()).toList()
                : new ArrayList<>();

        return QuizFormDTO.builder()
                .id(quiz.getId())
                .subjectId(quiz.getSubject() != null ? quiz.getSubject().getId() : null)
                .name(quiz.getName())
                .levelId(quiz.getLevel() != null ? quiz.getLevel().getId() : null)
                .quizTypeId(quiz.getQuizType() != null ? quiz.getQuizType().getId() : null)
                .duration(quiz.getDuration() != null ? quiz.getDuration() : 15)
                .passRate(quiz.getPassRate() != null ? quiz.getPassRate() : 50.0)
                .description(quiz.getDescription())
                .questionIds(qIds)
                .build();
    }

    public List<Subject> getSubjectsForExpert(User currentUser) {
        if (currentUser != null) {
            return subjectRepository.findByOwnerId(currentUser.getId());
        }
        return subjectRepository.findAll();
    }

    /**
     * Tạo mới hoặc cập nhật Quiz kèm danh sách câu hỏi.
     * Quy tắc Section 6 SKILLANDRULES:
     * Expert có full CRUD trên Quiz — không chặn sửa/xóa kể cả khi đã có QuizAttempt.
     */
    @Transactional
    public Quiz saveQuiz(QuizFormDTO formDTO, User currentUser) {
        if (formDTO.getSubjectId() == null) {
            throw new IllegalArgumentException("Môn học không được để trống!");
        }

        Subject subject = subjectRepository.findById(formDTO.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại"));

        validateSubjectOwnership(subject, currentUser);

        Quiz quiz;
        if (formDTO.getId() != null) {
            quiz = getQuizById(formDTO.getId(), currentUser);
        } else {
            quiz = new Quiz();
        }

        quiz.setSubject(subject);
        quiz.setName(formDTO.getName());
        quiz.setDuration(formDTO.getDuration() != null ? formDTO.getDuration() : 15);
        quiz.setPassRate(formDTO.getPassRate() != null ? formDTO.getPassRate() : 50.0);
        quiz.setDescription(formDTO.getDescription());

        if (formDTO.getLevelId() != null) {
            quiz.setLevel(levelRepository.findById(formDTO.getLevelId()).orElse(null));
        } else {
            quiz.setLevel(null);
        }

        if (formDTO.getQuizTypeId() != null) {
            quiz.setQuizType(testTypeRepository.findById(formDTO.getQuizTypeId()).orElse(null));
        } else {
            quiz.setQuizType(null);
        }

        quiz = quizRepository.save(quiz);

        // Đồng bộ QuizQuestion
        quizQuestionRepository.deleteByQuizId(quiz.getId());

        List<Long> questionIds = formDTO.getQuestionIds();
        if (questionIds != null && !questionIds.isEmpty()) {
            List<QuizQuestion> quizQuestions = new ArrayList<>();
            int order = 1;
            for (Long qId : questionIds) {
                Question question = questionRepository.findById(qId).orElse(null);
                if (question != null) {
                    quizQuestions.add(QuizQuestion.builder()
                            .quiz(quiz)
                            .question(question)
                            .orderNum(order++)
                            .build());
                }
            }
            quizQuestionRepository.saveAll(quizQuestions);
        }

        return quiz;
    }

    /**
     * Xóa Quiz an toàn theo thứ tự:
     * 1. Xóa QuizAnswer theo attemptIds
     * 2. Xóa QuizAttempt
     * 3. Xóa QuizQuestion
     * 4. Gỡ liên kết Lesson.quiz = null
     * 5. Xóa Quiz
     */
    @Transactional
    public DeleteQuizResult deleteQuiz(Long quizId, User currentUser) {
        Quiz quiz = getQuizById(quizId, currentUser);

        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        if (!attempts.isEmpty()) {
            List<Long> attemptIds = attempts.stream()
                    .map(QuizAttempt::getId)
                    .collect(Collectors.toList());
            quizAnswerRepository.deleteByAttemptIdIn(attemptIds);
        }

        quizAttemptRepository.deleteAll(attempts);
        quizQuestionRepository.deleteByQuizId(quizId);

        List<Lesson> linkedLessons = lessonRepository.findByQuizId(quizId);
        for (Lesson lesson : linkedLessons) {
            lesson.setQuiz(null);
        }
        lessonRepository.saveAll(linkedLessons);

        quizRepository.delete(quiz);

        return new DeleteQuizResult(linkedLessons.stream().map(Lesson::getName).collect(Collectors.toList()));
    }

    private void validateQuizOwnership(Quiz quiz, User currentUser) {
        validateSubjectOwnership(quiz.getSubject(), currentUser);
    }

    private void validateSubjectOwnership(Subject subject, User currentUser) {
        if (currentUser != null && currentUser.getRole() != null && "EXPERT".equals(currentUser.getRole().getCode())) {
            if (subject == null || subject.getOwner() == null || !subject.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Bạn không có quyền quản trị bài Quiz này!");
            }
        }
    }

    public record DeleteQuizResult(List<String> detachedLessonNames) {
        public boolean hasDetachedLessons() {
            return detachedLessonNames != null && !detachedLessonNames.isEmpty();
        }
    }
}
