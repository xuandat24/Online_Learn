// Thư mục: src/main/java/com/onlinelearn/service/QuizContentService.java
package com.onlinelearn.service;

import com.onlinelearn.entity.*;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizContentService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final QuestionLevelRepository levelRepository;
    private final TestTypeRepository testTypeRepository;

    public List<Quiz> getQuizzesForUser(User currentUser, Long subjectId, Long quizTypeId, String keyword) {
        Long ownerId = null;
        if (currentUser != null && currentUser.getRole() != null && "EXPERT".equals(currentUser.getRole().getCode())) {
            ownerId = currentUser.getId();
        }
        return quizRepository.searchQuizzes(ownerId, subjectId, quizTypeId, keyword);
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài Quiz ID: " + id));
    }

    public boolean hasAttempts(Long quizId) {
        return quizAttemptRepository.existsByQuizId(quizId);
    }

    /**
     * Lưu bài Quiz và danh sách câu hỏi đính kèm.
     * Quy tắc nghiệp vụ bắt buộc: Không cho Edit nếu Quiz đã có QuizAttempt.
     */
    @Transactional
    public Quiz saveQuiz(Quiz formQuiz, Long subjectId, Long levelId, Long typeId, List<Long> questionIds) {
        if (formQuiz.getId() != null && hasAttempts(formQuiz.getId())) {
            throw new IllegalStateException("Quiz này đã có học viên làm bài thi, không thể chỉnh sửa nội dung!");
        }

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
            QuestionLevel level = levelRepository.findById(levelId).orElse(null);
            quiz.setLevel(level);
        }

        if (typeId != null) {
            TestType testType = testTypeRepository.findById(typeId).orElse(null);
            quiz.setQuizType(testType);
        }

        quiz = quizRepository.save(quiz);

        // Cập nhật QuizQuestions
        if (formQuiz.getId() != null) {
            quizQuestionRepository.deleteByQuizId(quiz.getId());
        }

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
     * Xóa Quiz.
     * Quy tắc nghiệp vụ bắt buộc: Không cho Delete nếu Quiz đã có QuizAttempt.
     */
    @Transactional
    public void deleteQuiz(Long quizId) {
        if (hasAttempts(quizId)) {
            throw new IllegalStateException("Quiz này đã có học viên làm bài, không thể xóa!");
        }
        quizQuestionRepository.deleteByQuizId(quizId);
        quizRepository.deleteById(quizId);
    }
}
