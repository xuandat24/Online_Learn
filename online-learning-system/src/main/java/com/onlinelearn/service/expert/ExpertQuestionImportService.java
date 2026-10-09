package com.onlinelearn.service.expert;

import com.onlinelearn.dto.expert.QuestionImportDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.MediaType;
import com.onlinelearn.entity.enums.QuestionStatus;
import com.onlinelearn.repository.*;
import com.onlinelearn.util.ExcelParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpertQuestionImportService {

    private final ExcelParser excelParser;
    private final SubjectRepository subjectRepository;
    private final QuestionLevelRepository levelRepository;
    private final SubjectDimensionRepository dimensionRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;

    public record ImportResult(int totalRows, int successCount, int errorCount, List<String> errorMessages) {}

    @Transactional
    public ImportResult importQuestionsFromExcel(MultipartFile file, Long defaultSubjectId) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;

        List<QuestionImportDTO> dtos;
        try (InputStream is = file.getInputStream()) {
            dtos = excelParser.parseQuestions(is);
        } catch (Exception e) {
            log.error("Failed to parse Excel file", e);
            return new ImportResult(0, 0, 1, List.of("Lỗi khi đọc file Excel: " + e.getMessage()));
        }

        if (dtos.isEmpty()) {
            return new ImportResult(0, 0, 1, List.of("File Excel không có dữ liệu câu hỏi hợp lệ"));
        }

        Subject defaultSubject = defaultSubjectId != null
                ? subjectRepository.findById(defaultSubjectId).orElse(null)
                : null;

        QuestionLevel defaultLevel = levelRepository.findByCode("MEDIUM")
                .orElse(levelRepository.findAll().isEmpty() ? null : levelRepository.findAll().get(0));

        List<Question> questionsToSave = new ArrayList<>();
        List<List<AnswerOption>> optionsForQuestions = new ArrayList<>();

        for (QuestionImportDTO dto : dtos) {
            int row = dto.getRowNum();

            if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                errors.add("Dòng " + row + ": Nội dung câu hỏi không được để trống");
                continue;
            }
            if (dto.getOptA() == null || dto.getOptA().trim().isEmpty() ||
                dto.getOptB() == null || dto.getOptB().trim().isEmpty()) {
                errors.add("Dòng " + row + ": Bắt buộc phải có ít nhất 2 đáp án A và B");
                continue;
            }
            if (dto.getCorrectOpt() == null || dto.getCorrectOpt().trim().isEmpty()) {
                errors.add("Dòng " + row + ": Chưa chỉ định đáp án đúng (A/B/C/D)");
                continue;
            }

            String correct = dto.getCorrectOpt().trim().toUpperCase();
            if (!List.of("A", "B", "C", "D").contains(correct)) {
                errors.add("Dòng " + row + ": Đáp án đúng phải là một trong các giá trị A, B, C, D");
                continue;
            }

            Subject subject = defaultSubject;
            if (dto.getSubjectName() != null && !dto.getSubjectName().trim().isEmpty()) {
                subject = subjectRepository.findByName(dto.getSubjectName().trim()).orElse(defaultSubject);
            }
            if (subject == null) {
                errors.add("Dòng " + row + ": Không tìm thấy môn học '" + dto.getSubjectName() + "'");
                continue;
            }

            QuestionLevel level = defaultLevel;
            if (dto.getLevelCode() != null && !dto.getLevelCode().trim().isEmpty()) {
                level = levelRepository.findByCode(dto.getLevelCode().trim().toUpperCase()).orElse(defaultLevel);
            }

            Lesson lesson = null;
            if (dto.getLessonName() != null && !dto.getLessonName().trim().isEmpty()) {
                List<Lesson> lessons = lessonRepository.findBySubjectIdAndNameContainingIgnoreCaseOrderByOrderNumAsc(
                        subject.getId(), dto.getLessonName().trim());
                if (!lessons.isEmpty()) {
                    lesson = lessons.get(0);
                }
            }

            Set<SubjectDimension> dimensions = new HashSet<>();
            if (dto.getDimensionName() != null && !dto.getDimensionName().trim().isEmpty()) {
                List<SubjectDimension> dims = dimensionRepository.findBySubjectId(subject.getId());
                for (SubjectDimension sd : dims) {
                    if (sd.getName().equalsIgnoreCase(dto.getDimensionName().trim())) {
                        dimensions.add(sd);
                        break;
                    }
                }
            }

            Question question = Question.builder()
                    .subject(subject)
                    .lesson(lesson)
                    .dimensions(dimensions)
                    .level(level)
                    .content(dto.getContent().trim())
                    .explanation(dto.getExplanation() != null ? dto.getExplanation().trim() : null)
                    .mediaType(MediaType.NONE)
                    .status(QuestionStatus.ACTIVE)
                    .build();

            List<AnswerOption> options = new ArrayList<>();
            options.add(AnswerOption.builder().question(question).content(dto.getOptA().trim()).isCorrect("A".equals(correct)).build());
            options.add(AnswerOption.builder().question(question).content(dto.getOptB().trim()).isCorrect("B".equals(correct)).build());
            if (dto.getOptC() != null && !dto.getOptC().trim().isEmpty()) {
                options.add(AnswerOption.builder().question(question).content(dto.getOptC().trim()).isCorrect("C".equals(correct)).build());
            }
            if (dto.getOptD() != null && !dto.getOptD().trim().isEmpty()) {
                options.add(AnswerOption.builder().question(question).content(dto.getOptD().trim()).isCorrect("D".equals(correct)).build());
            }

            questionsToSave.add(question);
            optionsForQuestions.add(options);
        }

        if (!questionsToSave.isEmpty()) {
            List<Question> savedQuestions = questionRepository.saveAll(questionsToSave);
            List<AnswerOption> allOptions = new ArrayList<>();

            for (int i = 0; i < savedQuestions.size(); i++) {
                Question savedQ = savedQuestions.get(i);
                List<AnswerOption> opts = optionsForQuestions.get(i);
                for (AnswerOption opt : opts) {
                    opt.setQuestion(savedQ);
                    allOptions.add(opt);
                }
            }

            answerOptionRepository.saveAll(allOptions);
            successCount = savedQuestions.size();
        }

        return new ImportResult(dtos.size(), successCount, errors.size(), errors);
    }

    public byte[] generateSampleExcelTemplate() throws IOException {
        return excelParser.generateSampleTemplate();
    }

    public byte[] exportQuestions(List<Question> questions) throws IOException {
        return excelParser.exportQuestions(questions);
    }
}
