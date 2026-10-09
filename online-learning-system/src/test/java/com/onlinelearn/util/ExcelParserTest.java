package com.onlinelearn.util;

import com.onlinelearn.dto.expert.QuestionImportDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExcelParserTest {

    private ExcelParser excelParser;

    @BeforeEach
    void setUp() {
        excelParser = new ExcelParser();
    }

    @Test
    void testGenerateSampleTemplateAndParse() throws IOException {
        byte[] templateBytes = excelParser.generateSampleTemplate();
        assertNotNull(templateBytes);
        assertTrue(templateBytes.length > 0);

        List<QuestionImportDTO> dtos = excelParser.parseQuestions(new ByteArrayInputStream(templateBytes));
        assertNotNull(dtos);
        assertFalse(dtos.isEmpty());
        assertEquals(3, dtos.size());

        QuestionImportDTO row1 = dtos.get(0);
        assertEquals("Từ khóa nào được sử dụng để định nghĩa hằng số trong Java?", row1.getContent());
        assertEquals("Java Core cơ bản", row1.getSubjectName());
        assertEquals("EASY", row1.getLevelCode());
        assertEquals("B", row1.getCorrectOpt());
        assertEquals("static", row1.getOptA());
        assertEquals("final", row1.getOptB());
    }
}
