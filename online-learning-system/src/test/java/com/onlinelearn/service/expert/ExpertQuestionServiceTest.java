package com.onlinelearn.service.expert;

import com.onlinelearn.dto.expert.QuestionFormDTO;
import com.onlinelearn.entity.Question;
import com.onlinelearn.entity.Role;
import com.onlinelearn.entity.Subject;
import com.onlinelearn.entity.User;
import com.onlinelearn.repository.*;
import com.onlinelearn.util.ExcelParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpertQuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AnswerOptionRepository answerOptionRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private SubjectDimensionRepository dimensionRepository;
    @Mock
    private QuestionLevelRepository levelRepository;
    @Mock
    private ExcelParser excelParser;

    @InjectMocks
    private ExpertQuestionService expertQuestionService;

    private User expertUser;
    private User otherExpert;
    private Subject ownedSubject;

    @BeforeEach
    void setUp() {
        Role expertRole = Role.builder().id(1L).code("EXPERT").name("Expert").build();
        expertUser = User.builder().id(10L).fullName("Expert One").role(expertRole).build();
        otherExpert = User.builder().id(20L).fullName("Expert Two").role(expertRole).build();

        ownedSubject = Subject.builder()
                .id(100L)
                .name("Java Core")
                .owner(expertUser)
                .build();
    }

    @Test
    void testSaveQuestion_Validation_AtLeastTwoOptionsRequired() {
        QuestionFormDTO dto = QuestionFormDTO.builder()
                .subjectId(100L)
                .content("Valid content?")
                .optionContents(List.of("Only one option"))
                .correctIndex(0)
                .build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));

        assertThrows(IllegalArgumentException.class, () ->
                expertQuestionService.saveQuestion(dto, expertUser)
        );
    }

    @Test
    void testSaveQuestion_Validation_CorrectAnswerIndexOutOfBounds() {
        QuestionFormDTO dto = QuestionFormDTO.builder()
                .subjectId(100L)
                .content("Valid content?")
                .optionContents(List.of("Option 1", "Option 2"))
                .correctIndex(5)
                .build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));

        assertThrows(IllegalArgumentException.class, () ->
                expertQuestionService.saveQuestion(dto, expertUser)
        );
    }

    @Test
    void testSaveQuestion_Ownership_BlockedIfDifferentExpert() {
        QuestionFormDTO dto = QuestionFormDTO.builder()
                .subjectId(100L)
                .content("Question?")
                .optionContents(List.of("A", "B"))
                .correctIndex(0)
                .build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));

        assertThrows(AccessDeniedException.class, () ->
                expertQuestionService.saveQuestion(dto, otherExpert)
        );
    }

    @Test
    void testSaveQuestion_Success() {
        QuestionFormDTO dto = QuestionFormDTO.builder()
                .subjectId(100L)
                .content("Question?")
                .optionContents(List.of("Option A", "Option B"))
                .correctIndex(0)
                .build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));
        when(questionRepository.save(any(Question.class))).thenAnswer(invocation -> {
            Question q = invocation.getArgument(0);
            q.setId(1L);
            return q;
        });

        Question saved = expertQuestionService.saveQuestion(dto, expertUser);

        assertNotNull(saved);
        assertEquals("Question?", saved.getContent());
        verify(questionRepository).save(any(Question.class));
        verify(answerOptionRepository).saveAll(any());
    }

    @Test
    void testExportQuestionsToExcel_Success() throws IOException {
        when(questionRepository.searchQuestions(eq(10L), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(new Question()));
        when(excelParser.exportQuestions(any())).thenReturn(new byte[]{1, 2, 3});

        byte[] result = expertQuestionService.exportQuestionsToExcel(expertUser, null, null, null, null, null, null);
        assertNotNull(result);
        assertEquals(3, result.length);
        verify(excelParser).exportQuestions(any());
    }
}
