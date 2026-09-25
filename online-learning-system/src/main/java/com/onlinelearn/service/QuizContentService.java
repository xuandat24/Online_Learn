package com.onlinelearn.service;

import com.onlinelearn.entity.*;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizContentService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final LessonRepository lessonRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final QuestionLevelRepository levelRepository;
    private final TestTypeRepository testTypeRepository;

    // ─────────────────────────── READ ───────────────────────────

    public List<Quiz> getQuizzesForUser(User currentUser, Long subjectId, Long quizTypeId, String keyword) {
        Long ownerId = null;
        if (currentUser != null && currentUser.getRole() != null
                && "EXPERT".equals(currentUser.getRole().getCode())) {
            ownerId = currentUser.getId();
        }
        return quizRepository.searchQuizzes(ownerId, subjectId, quizTypeId, keyword);
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài Quiz ID: " + id));
    }

    /**
     * Lấy danh sách Subject mà user có thể tạo Quiz.
     * EXPERT chỉ thấy Subject mình sở hữu; ADMIN thấy tất cả.
     */
    public List<Subject> getSubjectsForUser(User currentUser) {
        if (currentUser != null && currentUser.getRole() != null
                && "EXPERT".equals(currentUser.getRole().getCode())) {
            return subjectRepository.findByOwnerId(currentUser.getId());
        }
        return subjectRepository.findAll();
    }

    // ─────────────────────────── CREATE / UPDATE ───────────────────────────

    /**
     * Tạo mới hoặc cập nhật Quiz kèm danh sách câu hỏi.
     * KHÔNG kiểm tra QuizAttempt — cho phép sửa tự do kể cả khi đã có lượt thi.
     */
    @Transactional
    public Quiz saveQuiz(Quiz formQuiz, Long subjectId, Long levelId, Long typeId, List<Long> questionIds) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại"));

        Quiz quiz;
        if (formQuiz.getId() != null) {
            quiz = getQuizById(formQuiz.getId());
        } else {
            quiz = new Quiz();
        }

        quiz.setSubject(subject);
        quiz.setName(formQuiz.getName());
        quiz.setDuration(formQuiz.getDuration() != null ? formQuiz.getDuration() : 15);
        quiz.setPassRate(formQuiz.getPassRate() != null ? formQuiz.getPassRate() : 50.0);
        quiz.setDescription(formQuiz.getDescription());

        if (levelId != null) {
            quiz.setLevel(levelRepository.findById(levelId).orElse(null));
        } else {
            quiz.setLevel(null);
        }

        if (typeId != null) {
            quiz.setQuizType(testTypeRepository.findById(typeId).orElse(null));
        } else {
            quiz.setQuizType(null);
        }

        quiz = quizRepository.save(quiz);

        // Đồng bộ QuizQuestion: xóa hết rồi insert lại theo danh sách mới
        quizQuestionRepository.deleteByQuizId(quiz.getId());

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

    // ─────────────────────────── DELETE ───────────────────────────

    /**
     * Xóa Quiz theo thứ tự cascade an toàn:
     *   1. Xóa QuizAnswer (theo các QuizAttempt của quiz này)
     *   2. Xóa QuizAttempt
     *   3. Xóa QuizQuestion
     *   4. Set Lesson.quiz = null cho Lesson đang trỏ tới quiz này
     *   5. Xóa Quiz
     *
     * KHÔNG chặn xóa khi đã có QuizAttempt.
     */
    @Transactional
    public DeleteQuizResult deleteQuiz(Long quizId) {
        Quiz quiz = getQuizById(quizId);

        // 1. Xóa QuizAnswer theo attemptIds
        List<QuizAttempt> attempts = quizAttemptRepository.findByQuizId(quizId);
        if (!attempts.isEmpty()) {
            List<Long> attemptIds = attempts.stream()
                    .map(QuizAttempt::getId)
                    .collect(Collectors.toList());
            quizAnswerRepository.deleteByAttemptIdIn(attemptIds);
        }

        // 2. Xóa QuizAttempt
        quizAttemptRepository.deleteAll(attempts);

        // 3. Xóa QuizQuestion
        quizQuestionRepository.deleteByQuizId(quizId);

        // 4. Set Lesson.quiz = null cho những Lesson đang gắn quiz này
        List<Lesson> linkedLessons = lessonRepository.findByQuizId(quizId);
        for (Lesson lesson : linkedLessons) {
            lesson.setQuiz(null);
        }
        lessonRepository.saveAll(linkedLessons);

        // 5. Xóa Quiz
        quizRepository.delete(quiz);

        return new DeleteQuizResult(linkedLessons.stream().map(Lesson::getName).collect(Collectors.toList()));
    }

    // ─────────────────────────── INNER RESULT CLASS ───────────────────────────

    /**
     * Trả về thông tin lesson bị gỡ quiz (để hiển thị thông báo).
     */
    public record DeleteQuizResult(List<String> detachedLessonNames) {
        public boolean hasDetachedLessons() {
            return detachedLessonNames != null && !detachedLessonNames.isEmpty();
        }
    }
}
