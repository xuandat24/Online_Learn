// Thư mục: src/main/java/com/onlinelearn/service/QuestionContentService.java
package com.onlinelearn.service;

import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.QuestionStatus;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class QuestionContentService {

    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final SubjectDimensionRepository dimensionRepository;
    private final QuestionLevelRepository levelRepository;

    public List<Question> getQuestionsForUser(User currentUser, Long subjectId, Long lessonId, Long levelId,
                                              QuestionStatus status, String keyword) {
        Long ownerId = null;
        if (currentUser != null && currentUser.getRole() != null && "EXPERT".equals(currentUser.getRole().getCode())) {
            ownerId = currentUser.getId();
        }
        return questionRepository.searchQuestions(ownerId, subjectId, lessonId, levelId, status, keyword);
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi ID: " + id));
    }

    /**
     * Lưu câu hỏi cùng danh sách đáp án.
     * Quy tắc nghiệp vụ bắt buộc: tối thiểu 2 đáp án, ít nhất 1 đáp án đúng.
     */
    @Transactional
    public Question saveQuestion(Question formQuestion,
                                 Long subjectId,
                                 Long lessonId,
                                 Long levelId,
                                 List<Long> dimensionIds,
                                 List<String> optionContents,
                                 Integer correctIndex) {

        // 1. Kiểm tra validation nghiệp vụ
        if (optionContents == null || optionContents.size() < 2) {
            throw new IllegalArgumentException("Câu hỏi phải có tối thiểu 2 đáp án lựa chọn!");
        }
        if (correctIndex == null || correctIndex < 0 || correctIndex >= optionContents.size()) {
            throw new IllegalArgumentException("Bạn phải chỉ định ít nhất 1 đáp án đúng!");
        }

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Môn học không hợp lệ"));

        Question question;
        if (formQuestion.getId() != null) {
            question = getQuestionById(formQuestion.getId());
        } else {
            question = new Question();
        }

        question.setSubject(subject);
        question.setContent(formQuestion.getContent());
        question.setExplanation(formQuestion.getExplanation());
        question.setMediaUrl(formQuestion.getMediaUrl());
        question.setMediaType(formQuestion.getMediaType() != null ? formQuestion.getMediaType() : com.onlinelearn.entity.enums.MediaType.NONE);
        question.setStatus(formQuestion.getStatus() != null ? formQuestion.getStatus() : QuestionStatus.ACTIVE);

        if (lessonId != null) {
            Lesson lesson = lessonRepository.findById(lessonId).orElse(null);
            question.setLesson(lesson);
        } else {
            question.setLesson(null);
        }

        if (levelId != null) {
            QuestionLevel level = levelRepository.findById(levelId).orElse(null);
            question.setLevel(level);
        }

        if (dimensionIds != null && !dimensionIds.isEmpty()) {
            Set<SubjectDimension> dims = new HashSet<>(dimensionRepository.findAllById(dimensionIds));
            question.setDimensions(dims);
        }

        question = questionRepository.save(question);

        // 2. Cập nhật các đáp án (AnswerOptions)
        // Xóa đáp án cũ nếu đang sửa
        if (formQuestion.getId() != null) {
            List<AnswerOption> oldOptions = answerOptionRepository.findByQuestionId(question.getId());
            answerOptionRepository.deleteAll(oldOptions);
        }

        List<AnswerOption> options = new ArrayList<>();
        for (int i = 0; i < optionContents.size(); i++) {
            String content = optionContents.get(i);
            if (content != null && !content.trim().isEmpty()) {
                boolean isCorrect = (i == correctIndex);
                options.add(AnswerOption.builder()
                        .question(question)
                        .content(content.trim())
                        .isCorrect(isCorrect)
                        .build());
            }
        }
        answerOptionRepository.saveAll(options);

        return question;
    }

    @Transactional
    public void toggleQuestionStatus(Long id) {
        Question question = getQuestionById(id);
        if (question.getStatus() == QuestionStatus.ACTIVE) {
            question.setStatus(QuestionStatus.INACTIVE);
        } else {
            question.setStatus(QuestionStatus.ACTIVE);
        }
        questionRepository.save(question);
    }
}
