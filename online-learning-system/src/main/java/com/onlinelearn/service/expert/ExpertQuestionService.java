package com.onlinelearn.service.expert;

import com.onlinelearn.dto.expert.QuestionFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.MediaType;
import com.onlinelearn.entity.enums.QuestionStatus;
import com.onlinelearn.repository.*;
import com.onlinelearn.util.ExcelParser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ExpertQuestionService {

    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final SubjectRepository subjectRepository;
    private final LessonRepository lessonRepository;
    private final SubjectDimensionRepository dimensionRepository;
    private final QuestionLevelRepository levelRepository;
    private final ExcelParser excelParser;

    /**
     * Lấy danh sách câu hỏi theo quyền của Expert (chỉ thấy câu hỏi thuộc Subject do mình sở hữu).
     */
    public List<Question> getQuestionsForExpert(User currentUser, Long subjectId, Long lessonId, Long dimensionId,
                                                Long levelId, QuestionStatus status, String keyword) {
        Long ownerId = null;
        if (currentUser != null) {
            ownerId = currentUser.getId();
        }
        return questionRepository.searchQuestions(ownerId, subjectId, lessonId, dimensionId, levelId, status, keyword);
    }

    public List<Subject> getSubjectsForExpert(User currentUser) {
        if (currentUser != null) {
            return subjectRepository.findByOwnerId(currentUser.getId());
        }
        return subjectRepository.findAll();
    }

    public Question getQuestionById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi ID: " + id));
    }

    public Question getQuestionById(Long id, User currentUser) {
        Question question = getQuestionById(id);
        validateOwnership(question.getSubject(), currentUser);
        return question;
    }

    public QuestionFormDTO toFormDTO(Question question) {
        List<AnswerOption> answers = question.getAnswers() != null ? question.getAnswers() : new ArrayList<>();
        List<String> optionContents = new ArrayList<>();
        Integer correctIndex = null;

        for (int i = 0; i < answers.size(); i++) {
            AnswerOption opt = answers.get(i);
            optionContents.add(opt.getContent());
            if (Boolean.TRUE.equals(opt.getIsCorrect()) && correctIndex == null) {
                correctIndex = i;
            }
        }

        List<Long> dimIds = question.getDimensions() != null
                ? question.getDimensions().stream().map(SubjectDimension::getId).toList()
                : new ArrayList<>();

        return QuestionFormDTO.builder()
                .id(question.getId())
                .subjectId(question.getSubject() != null ? question.getSubject().getId() : null)
                .lessonId(question.getLesson() != null ? question.getLesson().getId() : null)
                .levelId(question.getLevel() != null ? question.getLevel().getId() : null)
                .dimensionIds(dimIds)
                .content(question.getContent())
                .mediaType(question.getMediaType() != null ? question.getMediaType() : MediaType.NONE)
                .mediaUrl(question.getMediaUrl())
                .explanation(question.getExplanation())
                .status(question.getStatus() != null ? question.getStatus() : QuestionStatus.ACTIVE)
                .optionContents(optionContents)
                .correctIndex(correctIndex != null ? correctIndex : 0)
                .build();
    }

    /**
     * Lưu câu hỏi trắc nghiệm kèm AnswerOptions.
     * Quy tắc nghiệp vụ (Section 6 SKILLANDRULES):
     * - Mỗi câu hỏi phải có ít nhất 2 đáp án không rỗng.
     * - Bắt buộc có ít nhất 1 đáp án được đánh dấu đúng.
     */
    @Transactional
    public Question saveQuestion(QuestionFormDTO formDTO, User currentUser) {
        if (formDTO.getSubjectId() == null) {
            throw new IllegalArgumentException("Môn học không được để trống!");
        }

        Subject subject = subjectRepository.findById(formDTO.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Môn học không tồn tại"));

        validateOwnership(subject, currentUser);

        // Kiểm tra ít nhất 2 đáp án hợp lệ
        List<String> optionContents = formDTO.getOptionContents();
        if (optionContents == null || optionContents.stream().filter(s -> s != null && !s.trim().isEmpty()).count() < 2) {
            throw new IllegalArgumentException("Câu hỏi phải có ít nhất 2 phương án trả lời không rỗng!");
        }

        int correctIdx = formDTO.getCorrectIndex() != null ? formDTO.getCorrectIndex() : 0;
        if (correctIdx < 0 || correctIdx >= optionContents.size() ||
                optionContents.get(correctIdx) == null || optionContents.get(correctIdx).trim().isEmpty()) {
            throw new IllegalArgumentException("Đáp án đúng được chọn không hợp lệ!");
        }

        Question question;
        if (formDTO.getId() != null) {
            question = getQuestionById(formDTO.getId(), currentUser);
        } else {
            question = new Question();
        }

        question.setSubject(subject);
        question.setContent(formDTO.getContent());
        question.setMediaType(formDTO.getMediaType() != null ? formDTO.getMediaType() : MediaType.NONE);
        question.setMediaUrl(formDTO.getMediaUrl());
        question.setExplanation(formDTO.getExplanation());
        question.setStatus(formDTO.getStatus() != null ? formDTO.getStatus() : QuestionStatus.ACTIVE);

        if (formDTO.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(formDTO.getLessonId()).orElse(null);
            question.setLesson(lesson);
        } else {
            question.setLesson(null);
        }

        if (formDTO.getLevelId() != null) {
            QuestionLevel level = levelRepository.findById(formDTO.getLevelId()).orElse(null);
            question.setLevel(level);
        } else {
            question.setLevel(null);
        }

        if (formDTO.getDimensionIds() != null && !formDTO.getDimensionIds().isEmpty()) {
            List<SubjectDimension> dims = dimensionRepository.findAllById(formDTO.getDimensionIds());
            question.setDimensions(new HashSet<>(dims));
        } else {
            question.setDimensions(new HashSet<>());
        }

        Question saved = questionRepository.save(question);

        // Lưu danh sách câu trả lời
        if (formDTO.getId() != null) {
            List<AnswerOption> oldOptions = answerOptionRepository.findByQuestionId(saved.getId());
            answerOptionRepository.deleteAll(oldOptions);
        }

        List<AnswerOption> optionsToSave = new ArrayList<>();
        for (int i = 0; i < optionContents.size(); i++) {
            String optContent = optionContents.get(i);
            if (optContent != null && !optContent.trim().isEmpty()) {
                optionsToSave.add(AnswerOption.builder()
                        .question(saved)
                        .content(optContent.trim())
                        .isCorrect(i == correctIdx)
                        .build());
            }
        }
        answerOptionRepository.saveAll(optionsToSave);

        return saved;
    }

    @Transactional
    public void toggleQuestionStatus(Long id, User currentUser) {
        Question question = getQuestionById(id, currentUser);
        if (question.getStatus() == QuestionStatus.ACTIVE) {
            question.setStatus(QuestionStatus.INACTIVE);
        } else {
            question.setStatus(QuestionStatus.ACTIVE);
        }
        questionRepository.save(question);
    }

    /**
     * Xuất danh sách câu hỏi ra file Excel (.xlsx).
     */
    public byte[] exportQuestionsToExcel(User currentUser, Long subjectId, Long lessonId, Long dimensionId,
                                         Long levelId, QuestionStatus status, String keyword) throws IOException {
        List<Question> questions = getQuestionsForExpert(currentUser, subjectId, lessonId, dimensionId, levelId, status, keyword);
        return excelParser.exportQuestions(questions);
    }

    public void validateOwnership(Subject subject, User currentUser) {
        if (currentUser != null && currentUser.getRole() != null && "EXPERT".equals(currentUser.getRole().getCode())) {
            if (subject == null || subject.getOwner() == null || !subject.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Bạn không có quyền quản trị câu hỏi của môn học này!");
            }
        }
    }
}
