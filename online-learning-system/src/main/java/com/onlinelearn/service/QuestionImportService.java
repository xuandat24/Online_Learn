// Thư mục: src/main/java/com/onlinelearn/service/QuestionImportService.java
package com.onlinelearn.service;

import com.onlinelearn.entity.AnswerOption;
import com.onlinelearn.entity.Question;
import com.onlinelearn.entity.QuestionLevel;
import com.onlinelearn.entity.Subject;
import com.onlinelearn.entity.enums.QuestionStatus;
import com.onlinelearn.repository.AnswerOptionRepository;
import com.onlinelearn.repository.QuestionLevelRepository;
import com.onlinelearn.repository.QuestionRepository;
import com.onlinelearn.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionImportService {

    private final SubjectRepository subjectRepository;
    private final QuestionLevelRepository levelRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;

    public record ImportResult(int totalRows, int successCount, int errorCount, List<String> errorMessages) {}

    /**
     * Đọc file Excel câu hỏi, kiểm tra tính hợp lệ và lưu vào Database.
     */
    @Transactional
    public ImportResult importQuestionsFromExcel(MultipartFile file, Long defaultSubjectId) {
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int totalRows = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            totalRows = sheet.getLastRowNum();

            QuestionLevel defaultLevel = levelRepository.findByCode("MEDIUM")
                    .orElse(levelRepository.findAll().isEmpty() ? null : levelRepository.findAll().get(0));

            Subject defaultSubject = defaultSubjectId != null
                    ? subjectRepository.findById(defaultSubjectId).orElse(null)
                    : null;

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                try {
                    String subjectName = getCellValue(row.getCell(0));
                    String content = getCellValue(row.getCell(1));
                    String levelCode = getCellValue(row.getCell(2));
                    String optA = getCellValue(row.getCell(3));
                    String optB = getCellValue(row.getCell(4));
                    String optC = getCellValue(row.getCell(5));
                    String optD = getCellValue(row.getCell(6));
                    String correctOpt = getCellValue(row.getCell(7)).toUpperCase().trim();
                    String explanation = getCellValue(row.getCell(8));

                    if (content.isEmpty() || optA.isEmpty() || optB.isEmpty() || correctOpt.isEmpty()) {
                        errors.add("Dòng " + (r + 1) + ": Thiếu nội dung câu hỏi hoặc đáp án A, B hoặc đáp án đúng");
                        continue;
                    }

                    Subject subject = defaultSubject;
                    if (!subjectName.isEmpty()) {
                        subject = subjectRepository.findByName(subjectName).orElse(defaultSubject);
                    }
                    if (subject == null) {
                        errors.add("Dòng " + (r + 1) + ": Không xác định được môn học '" + subjectName + "'");
                        continue;
                    }

                    QuestionLevel level = levelRepository.findByCode(levelCode).orElse(defaultLevel);

                    Question q = Question.builder()
                            .subject(subject)
                            .content(content)
                            .level(level)
                            .explanation(explanation)
                            .status(QuestionStatus.ACTIVE)
                            .build();
                    q = questionRepository.save(q);

                    // Add options
                    List<AnswerOption> options = new ArrayList<>();
                    options.add(AnswerOption.builder().question(q).content(optA).isCorrect("A".equals(correctOpt)).build());
                    options.add(AnswerOption.builder().question(q).content(optB).isCorrect("B".equals(correctOpt)).build());
                    if (!optC.isEmpty()) {
                        options.add(AnswerOption.builder().question(q).content(optC).isCorrect("C".equals(correctOpt)).build());
                    }
                    if (!optD.isEmpty()) {
                        options.add(AnswerOption.builder().question(q).content(optD).isCorrect("D".equals(correctOpt)).build());
                    }

                    answerOptionRepository.saveAll(options);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Dòng " + (r + 1) + ": Lỗi định dạng - " + e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Lỗi khi đọc file Excel import câu hỏi: {}", e.getMessage());
            errors.add("File Excel không đúng định dạng hoặc bị lỗi: " + e.getMessage());
        }

        return new ImportResult(totalRows, successCount, errors.size(), errors);
    }

    /**
     * Tạo file Excel mẫu Question Import Template.
     */
    public byte[] generateSampleExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Questions");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Môn học", "Nội dung câu hỏi", "Mức độ (EASY/MEDIUM/HARD)",
                    "Đáp án A", "Đáp án B", "Đáp án C", "Đáp án D",
                    "Đáp án đúng (A/B/C/D)", "Giải thích chi tiết"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Dòng dữ liệu mẫu 1
            Row sample1 = sheet.createRow(1);
            sample1.createCell(0).setCellValue("Java Core cơ bản");
            sample1.createCell(1).setCellValue("Từ khóa nào được sử dụng để định nghĩa hằng số trong Java?");
            sample1.createCell(2).setCellValue("EASY");
            sample1.createCell(3).setCellValue("static");
            sample1.createCell(4).setCellValue("final");
            sample1.createCell(5).setCellValue("const");
            sample1.createCell(6).setCellValue("immutable");
            sample1.createCell(7).setCellValue("B");
            sample1.createCell(8).setCellValue("Từ khóa 'final' trong Java dùng để khai báo biến không thể thay đổi giá trị.");

            // Dòng dữ liệu mẫu 2
            Row sample2 = sheet.createRow(2);
            sample2.createCell(0).setCellValue("Spring Boot từ cơ bản đến nâng cao");
            sample2.createCell(1).setCellValue("Port mặc định của máy chủ Tomcat tích hợp trong Spring Boot là gì?");
            sample2.createCell(2).setCellValue("EASY");
            sample2.createCell(3).setCellValue("80");
            sample2.createCell(4).setCellValue("8080");
            sample2.createCell(5).setCellValue("3306");
            sample2.createCell(6).setCellValue("5000");
            sample2.createCell(7).setCellValue("B");
            sample2.createCell(8).setCellValue("Cổng mặc định là 8080 nếu không cấu hình lại qua server.port.");

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
