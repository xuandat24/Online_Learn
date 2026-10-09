package com.onlinelearn.util;

import com.onlinelearn.dto.expert.QuestionImportDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExcelParser {

    /**
     * Parses an Excel input stream containing question rows into a list of
     * QuestionImportDTO.
     */
    public List<QuestionImportDTO> parseQuestions(InputStream inputStream) throws IOException {
        List<QuestionImportDTO> dtoList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getLastRowNum() < 1) {
                return dtoList;
            }

            // Inspect header row
            Row headerRow = sheet.getRow(0);
            Map<String, Integer> colMap = buildColumnMap(headerRow);

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null)
                    continue;

                QuestionImportDTO dto = extractRow(row, colMap, r + 1);
                // Only consider non-empty rows
                if (dto != null && (isNotBlank(dto.getContent()) || isNotBlank(dto.getSubjectName()))) {
                    dtoList.add(dto);
                }
            }
        }

        return dtoList;
    }

    private Map<String, Integer> buildColumnMap(Row headerRow) {
        Map<String, Integer> map = new HashMap<>();
        if (headerRow == null)
            return map;

        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            String val = getCellValueAsString(cell).trim().toLowerCase();

            if (val.contains("content") || val.contains("nội dung") || val.contains("câu hỏi")) {
                map.put("content", c);
            } else if (val.contains("subject") || val.contains("môn học") || val.contains("khóa học")) {
                map.put("subject", c);
            } else if (val.contains("dimension") || val.contains("chuẩn đầu ra")) {
                map.put("dimension", c);
            } else if (val.contains("lesson") || val.contains("bài học")) {
                map.put("lesson", c);
            } else if (val.contains("level") || val.contains("mức độ") || val.contains("độ khó")) {
                map.put("level", c);
            } else if (val.contains("answer a") || val.contains("đáp án a") || val.equals("a")) {
                map.put("optA", c);
            } else if (val.contains("answer b") || val.contains("đáp án b") || val.equals("b")) {
                map.put("optB", c);
            } else if (val.contains("answer c") || val.contains("đáp án c") || val.equals("c")) {
                map.put("optC", c);
            } else if (val.contains("answer d") || val.contains("đáp án d") || val.equals("d")) {
                map.put("optD", c);
            } else if (val.contains("correct") || val.contains("đáp án đúng")) {
                map.put("correctOpt", c);
            } else if (val.contains("explanation") || val.contains("giải thích")) {
                map.put("explanation", c);
            }
        }
        return map;
    }

    private QuestionImportDTO extractRow(Row row, Map<String, Integer> colMap, int rowNum) {
        // Fallback default column order if header map doesn't match:
        // Col 0: Content, Col 1: Subject, Col 2: Dimension, Col 3: Lesson, Col 4:
        // Level,
        // Col 5: OptA, Col 6: OptB, Col 7: OptC, Col 8: OptD, Col 9: Correct, Col 10:
        // Explanation
        int contentIdx = colMap.getOrDefault("content", 0);
        int subjectIdx = colMap.getOrDefault("subject", 1);
        int dimIdx = colMap.getOrDefault("dimension", 2);
        int lessonIdx = colMap.getOrDefault("lesson", 3);
        int levelIdx = colMap.getOrDefault("level", 4);
        int optAIdx = colMap.getOrDefault("optA", 5);
        int optBIdx = colMap.getOrDefault("optB", 6);
        int optCIdx = colMap.getOrDefault("optC", 7);
        int optDIdx = colMap.getOrDefault("optD", 8);
        int correctIdx = colMap.getOrDefault("correctOpt", 9);
        int expIdx = colMap.getOrDefault("explanation", 10);

        return QuestionImportDTO.builder()
                .rowNum(rowNum)
                .content(getCell(row, contentIdx))
                .subjectName(getCell(row, subjectIdx))
                .dimensionName(getCell(row, dimIdx))
                .lessonName(getCell(row, lessonIdx))
                .levelCode(getCell(row, levelIdx))
                .optA(getCell(row, optAIdx))
                .optB(getCell(row, optBIdx))
                .optC(getCell(row, optCIdx))
                .optD(getCell(row, optDIdx))
                .correctOpt(getCell(row, correctIdx).toUpperCase().trim())
                .explanation(getCell(row, expIdx))
                .build();
    }

    private String getCell(Row row, int colIndex) {
        if (colIndex < 0 || colIndex >= row.getLastCellNum())
            return "";
        Cell cell = row.getCell(colIndex);
        return getCellValueAsString(cell).trim();
    }

    public String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    /**
     * Generates a sample template Excel sheet matching prompt specifications:
     * Content, Subject, Dimension, Lesson, Level, Answer A, Answer B, Answer C,
     * Answer D, Correct Answer, Explanation
     */
    public byte[] generateSampleTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Questions Template");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            String[] headers = {
                    "Content",
                    "Subject",
                    "Dimension",
                    "Lesson",
                    "Level",
                    "Answer A",
                    "Answer B",
                    "Answer C",
                    "Answer D",
                    "Correct Answer",
                    "Explanation"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 6500);
            }

            // Sample Row 1
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Từ khóa nào được sử dụng để định nghĩa hằng số trong Java?");
            r1.createCell(1).setCellValue("Java Core cơ bản");
            r1.createCell(2).setCellValue("Kiến thức nền tảng Java");
            r1.createCell(3).setCellValue("Bài 2: Cú pháp cơ bản & Viết chương trình Hello World");
            r1.createCell(4).setCellValue("EASY");
            r1.createCell(5).setCellValue("static");
            r1.createCell(6).setCellValue("final");
            r1.createCell(7).setCellValue("const");
            r1.createCell(8).setCellValue("immutable");
            r1.createCell(9).setCellValue("B");
            r1.createCell(10)
                    .setCellValue("Từ khóa 'final' trong Java dùng để khai báo biến không thể thay đổi giá trị.");

            // Sample Row 2
            Row r2 = sheet.createRow(2);
            r2.createCell(0).setCellValue("Port mặc định của máy chủ Tomcat tích hợp trong Spring Boot là gì?");
            r2.createCell(1).setCellValue("Spring Boot từ cơ bản đến nâng cao");
            r2.createCell(2).setCellValue("Kiến trúc Spring Framework");
            r2.createCell(3).setCellValue("Bài 1: Khởi tạo dự án với Spring Initializr");
            r2.createCell(4).setCellValue("EASY");
            r2.createCell(5).setCellValue("80");
            r2.createCell(6).setCellValue("8080");
            r2.createCell(7).setCellValue("3306");
            r2.createCell(8).setCellValue("5000");
            r2.createCell(9).setCellValue("B");
            r2.createCell(10).setCellValue("Tomcat tích hợp chạy mặc định trên cổng 8080.");

            // Sample Row 3
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue("Annotation nào đánh dấu một Spring component là Service Layer?");
            r3.createCell(1).setCellValue("Spring Boot từ cơ bản đến nâng cao");
            r3.createCell(2).setCellValue("Lập trình REST API & Security");
            r3.createCell(3).setCellValue("Bài 2: Tạo Controller và REST API đầu tiên");
            r3.createCell(4).setCellValue("MEDIUM");
            r3.createCell(5).setCellValue("@Repository");
            r3.createCell(6).setCellValue("@Component");
            r3.createCell(7).setCellValue("@Service");
            r3.createCell(8).setCellValue("@Controller");
            r3.createCell(9).setCellValue("C");
            r3.createCell(10).setCellValue("@Service được sử dụng cho business logic trong tầng Service.");

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Exports a list of Question entities into an Excel (.xlsx) file as bytes.
     */
    public byte[] exportQuestions(List<com.onlinelearn.entity.Question> questions) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Exported Questions");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setWrapText(true);

            String[] headers = {
                    "ID",
                    "Content",
                    "Subject",
                    "Dimension",
                    "Lesson",
                    "Level",
                    "Answer A",
                    "Answer B",
                    "Answer C",
                    "Answer D",
                    "Correct Answer",
                    "Explanation",
                    "Status"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, i == 1 ? 12000 : 6500);
            }

            int rowIndex = 1;
            for (com.onlinelearn.entity.Question q : questions) {
                Row row = sheet.createRow(rowIndex++);

                row.createCell(0).setCellValue(q.getId() != null ? String.valueOf(q.getId()) : "");
                row.createCell(1).setCellValue(q.getContent() != null ? q.getContent() : "");
                row.createCell(2).setCellValue(q.getSubject() != null ? q.getSubject().getName() : "");

                String dimensionStr = "";
                if (q.getDimensions() != null && !q.getDimensions().isEmpty()) {
                    List<String> dimNames = new ArrayList<>();
                    for (com.onlinelearn.entity.SubjectDimension d : q.getDimensions()) {
                        dimNames.add(d.getName());
                    }
                    dimensionStr = String.join(", ", dimNames);
                }
                row.createCell(3).setCellValue(dimensionStr);
                row.createCell(4).setCellValue(q.getLesson() != null ? q.getLesson().getName() : "");
                row.createCell(5).setCellValue(q.getLevel() != null ? q.getLevel().getName() : "");

                // Answers
                List<com.onlinelearn.entity.AnswerOption> answers = q.getAnswers() != null ? q.getAnswers() : List.of();
                String optA = answers.size() > 0 ? answers.get(0).getContent() : "";
                String optB = answers.size() > 1 ? answers.get(1).getContent() : "";
                String optC = answers.size() > 2 ? answers.get(2).getContent() : "";
                String optD = answers.size() > 3 ? answers.get(3).getContent() : "";

                row.createCell(6).setCellValue(optA != null ? optA : "");
                row.createCell(7).setCellValue(optB != null ? optB : "");
                row.createCell(8).setCellValue(optC != null ? optC : "");
                row.createCell(9).setCellValue(optD != null ? optD : "");

                String correctLetter = "";
                for (int i = 0; i < answers.size(); i++) {
                    if (Boolean.TRUE.equals(answers.get(i).getIsCorrect())) {
                        correctLetter = switch (i) {
                            case 0 -> "A";
                            case 1 -> "B";
                            case 2 -> "C";
                            case 3 -> "D";
                            default -> String.valueOf(i + 1);
                        };
                        break;
                    }
                }
                row.createCell(10).setCellValue(correctLetter);
                row.createCell(11).setCellValue(q.getExplanation() != null ? q.getExplanation() : "");
                row.createCell(12).setCellValue(q.getStatus() != null ? q.getStatus().name() : "");
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
