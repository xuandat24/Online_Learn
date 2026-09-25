// Thư mục: src/main/java/com/onlinelearn/controller/content/QuestionImportController.java
package com.onlinelearn.controller.content;

import com.onlinelearn.service.QuestionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/content/questions")
@RequiredArgsConstructor
public class QuestionImportController {

    private final QuestionImportService questionImportService;

    /**
     * 7. Nhận file upload Excel để import câu hỏi hàng loạt
     * URL: POST /content/questions/import
     */
    @PostMapping("/import")
    public String handleImport(@RequestParam("file") MultipartFile file,
                               @RequestParam(value = "subjectId", required = false) Long subjectId,
                               RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng chọn một file Excel (.xlsx) để import!");
            return "redirect:/content/questions";
        }

        QuestionImportService.ImportResult result = questionImportService.importQuestionsFromExcel(file, subjectId);

        if (result.errorCount() == 0) {
            redirectAttributes.addFlashAttribute("successMessage",
                    "Import thành công " + result.successCount() + " câu hỏi từ file Excel!");
        } else {
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Import hoàn tất với " + result.successCount() + " câu hỏi thành công và "
                            + result.errorCount() + " lỗi. Chi tiết: " + String.join("; ", result.errorMessages()));
        }

        return "redirect:/content/questions";
    }

    /**
     * 7. Tải file Excel mẫu câu hỏi
     * URL: GET /content/questions/template
     */
    @GetMapping("/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] fileBytes = questionImportService.generateSampleExcelTemplate();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"question_import_template.xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(fileBytes);
    }
}
