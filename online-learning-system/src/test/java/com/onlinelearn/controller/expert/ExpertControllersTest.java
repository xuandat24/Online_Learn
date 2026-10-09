package com.onlinelearn.controller.expert;

import com.onlinelearn.dto.expert.PricePackageFormDTO;
import com.onlinelearn.dto.expert.QuestionFormDTO;
import com.onlinelearn.dto.expert.QuizFormDTO;
import com.onlinelearn.dto.expert.SubjectDimensionFormDTO;
import com.onlinelearn.dto.expert.SubjectOverviewFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.LessonStatus;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import com.onlinelearn.entity.enums.QuestionStatus;
import com.onlinelearn.entity.enums.SubjectStatus;
import com.onlinelearn.repository.*;
import com.onlinelearn.service.expert.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {
        ExpertSubjectController.class,
        ExpertLessonController.class,
        ExpertQuestionController.class,
        ExpertQuizController.class
})
@AutoConfigureMockMvc(addFilters = false)
class ExpertControllersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private org.springframework.data.jpa.mapping.JpaMetamodelMappingContext jpaMappingContext;

    @MockBean
    private ExpertSubjectService expertSubjectService;
    @MockBean
    private SubjectCategoryRepository categoryRepository;
    @MockBean
    private DimensionTypeRepository dimensionTypeRepository;
    @MockBean
    private SubjectRepository subjectRepository;
    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ExpertLessonService expertLessonService;
    @MockBean
    private LessonRepository lessonRepository;
    @MockBean
    private QuizRepository quizRepository;

    @MockBean
    private ExpertQuestionService expertQuestionService;
    @MockBean
    private ExpertQuestionImportService expertQuestionImportService;
    @MockBean
    private QuestionRepository questionRepository;
    @MockBean
    private QuestionLevelRepository questionLevelRepository;
    @MockBean
    private SubjectDimensionRepository dimensionRepository;

    @MockBean
    private ExpertQuizService expertQuizService;
    @MockBean
    private TestTypeRepository testTypeRepository;

    private Subject mockSubject;
    private Lesson mockLesson;
    private Question mockQuestion;
    private Quiz mockQuiz;

    @BeforeEach
    void setUp() {
        mockSubject = Subject.builder()
                .id(1L)
                .name("Java Core")
                .status(SubjectStatus.PUBLISHED)
                .dimensions(new ArrayList<>())
                .pricePackages(new ArrayList<>())
                .lessons(new ArrayList<>())
                .build();

        mockLesson = Lesson.builder()
                .id(10L)
                .name("Lesson 1")
                .subject(mockSubject)
                .type(LessonTypeEnum.LESSON)
                .status(LessonStatus.ACTIVE)
                .orderNum(1)
                .build();

        mockQuestion = Question.builder()
                .id(100L)
                .content("What is OOP?")
                .subject(mockSubject)
                .status(QuestionStatus.ACTIVE)
                .answers(new ArrayList<>())
                .dimensions(new java.util.HashSet<>())
                .build();

        mockQuiz = Quiz.builder()
                .id(200L)
                .name("Quiz 1")
                .subject(mockSubject)
                .quizQuestions(new ArrayList<>())
                .build();
    }

    // ─────────────────────────── SUBJECTS ───────────────────────────

    @Test
    void testListSubjects() throws Exception {
        when(expertSubjectService.getSubjectsForExpert(any(), any(), any(), any()))
                .thenReturn(List.of(mockSubject));

        mockMvc.perform(get("/content/subjects"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/subject-list"))
                .andExpect(model().attributeExists("subjects", "categories"));
    }

    @Test
    void testSubjectDetails() throws Exception {
        when(expertSubjectService.getSubjectById(eq(1L), any())).thenReturn(mockSubject);
        when(expertSubjectService.toOverviewFormDTO(mockSubject))
                .thenReturn(new SubjectOverviewFormDTO());

        mockMvc.perform(get("/content/subjects/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/subject-details"))
                .andExpect(model().attributeExists("subject", "overviewForm", "dimensionForm", "pricePackageForm"));
    }

    @Test
    void testUpdateOverview() throws Exception {
        mockMvc.perform(post("/content/subjects/1/overview")
                        .param("name", "Updated Java Core"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/subjects/1?tab=overview"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(expertSubjectService).updateSubjectOverview(eq(1L), any(), any());
    }

    @Test
    void testAddDimension() throws Exception {
        mockMvc.perform(post("/content/subjects/1/dimensions")
                        .param("name", "OOP Concept")
                        .param("typeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/subjects/1?tab=dimension"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(expertSubjectService).addDimension(eq(1L), any(SubjectDimensionFormDTO.class), any());
    }

    @Test
    void testEditDimension() throws Exception {
        mockMvc.perform(post("/content/subjects/1/dimensions/5/edit")
                        .param("name", "Updated OOP Concept")
                        .param("typeId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/subjects/1?tab=dimension"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(expertSubjectService).updateDimension(eq(5L), any(SubjectDimensionFormDTO.class), any());
    }

    @Test
    void testDeleteDimension() throws Exception {
        mockMvc.perform(post("/content/subjects/1/dimensions/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/subjects/1?tab=dimension"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(expertSubjectService).deleteDimension(eq(5L), any());
    }

    // ─────────────────────────── LESSONS ───────────────────────────

    @Test
    void testListSubjectLessons() throws Exception {
        when(subjectRepository.findById(1L)).thenReturn(java.util.Optional.of(mockSubject));
        when(expertLessonService.getLessonsBySubject(eq(1L), any(), any()))
                .thenReturn(List.of(mockLesson));

        mockMvc.perform(get("/content/subjects/1/lessons"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/lesson-list"))
                .andExpect(model().attributeExists("subject", "lessons"));
    }

    @Test
    void testNewLessonForm() throws Exception {
        when(subjectRepository.findById(1L)).thenReturn(java.util.Optional.of(mockSubject));

        mockMvc.perform(get("/content/lessons/new").param("subjectId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/lesson-details"))
                .andExpect(model().attributeExists("subject", "lessonForm"));
    }

    @Test
    void testEditLessonForm() throws Exception {
        when(expertLessonService.getLessonById(eq(10L), any())).thenReturn(mockLesson);
        when(expertLessonService.toFormDTO(mockLesson)).thenReturn(new com.onlinelearn.dto.expert.LessonFormDTO());

        mockMvc.perform(get("/content/lessons/10"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/lesson-details"))
                .andExpect(model().attributeExists("subject", "lessonForm"));
    }

    @Test
    void testSaveLesson() throws Exception {
        when(expertLessonService.saveLesson(any(), any())).thenReturn(mockLesson);

        mockMvc.perform(post("/content/lessons/save")
                        .param("subjectId", "1")
                        .param("name", "Lesson 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/subjects/1/lessons"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void testToggleLessonStatus() throws Exception {
        mockMvc.perform(post("/content/lessons/10/toggle-status")
                        .param("subjectId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/subjects/1/lessons"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(expertLessonService).toggleLessonStatus(eq(10L), any());
    }

    // ─────────────────────────── QUESTIONS ───────────────────────────

    @Test
    void testListQuestions() throws Exception {
        when(expertQuestionService.getQuestionsForExpert(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(mockQuestion));

        mockMvc.perform(get("/content/questions"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/question-list"))
                .andExpect(model().attributeExists("questions", "subjects", "levels"));
    }

    @Test
    void testNewQuestionForm() throws Exception {
        when(expertQuestionService.getSubjectsForExpert(any())).thenReturn(List.of(mockSubject));

        mockMvc.perform(get("/content/questions/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/question-details"))
                .andExpect(model().attributeExists("questionForm", "subjects", "levels"));
    }

    @Test
    void testEditQuestionForm() throws Exception {
        when(expertQuestionService.getQuestionById(eq(100L), any())).thenReturn(mockQuestion);
        when(expertQuestionService.toFormDTO(mockQuestion)).thenReturn(new QuestionFormDTO());

        mockMvc.perform(get("/content/questions/100"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/question-details"))
                .andExpect(model().attributeExists("questionForm"));
    }

    @Test
    void testViewQuestion() throws Exception {
        when(expertQuestionService.getQuestionById(eq(100L), any())).thenReturn(mockQuestion);

        mockMvc.perform(get("/content/questions/100/view"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/question-view"))
                .andExpect(model().attributeExists("question"));
    }

    @Test
    void testSaveQuestion() throws Exception {
        mockMvc.perform(post("/content/questions/save")
                        .param("subjectId", "1")
                        .param("content", "What is OOP?")
                        .param("optionContents", "Opt A", "Opt B")
                        .param("correctIndex", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/questions"));

        verify(expertQuestionService).saveQuestion(any(QuestionFormDTO.class), any());
    }

    @Test
    void testToggleQuestionStatus() throws Exception {
        mockMvc.perform(post("/content/questions/100/toggle-status"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/questions"));

        verify(expertQuestionService).toggleQuestionStatus(eq(100L), any());
    }

    @Test
    void testDownloadQuestionTemplate() throws Exception {
        when(expertQuestionImportService.generateSampleExcelTemplate()).thenReturn(new byte[]{1, 2, 3});

        mockMvc.perform(get("/content/questions/template"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"question_import_template.xlsx\""));
    }

    @Test
    void testImportQuestions() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", new byte[]{1, 2});
        when(expertQuestionImportService.importQuestionsFromExcel(any(), any()))
                .thenReturn(new ExpertQuestionImportService.ImportResult(1, 1, 0, List.of()));

        mockMvc.perform(multipart("/content/questions/import").file(file))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/questions"));
    }

    @Test
    void testExportQuestions() throws Exception {
        when(expertQuestionService.exportQuestionsToExcel(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new byte[]{4, 5, 6});

        mockMvc.perform(get("/content/questions/export"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"questions_export.xlsx\""));
    }

    // ─────────────────────────── QUIZZES ───────────────────────────

    @Test
    void testListQuizzes() throws Exception {
        when(expertQuizService.getQuizzesForExpert(any(), any(), any(), any()))
                .thenReturn(List.of(mockQuiz));

        mockMvc.perform(get("/content/quizzes"))
                .andExpect(status().isOk())
                .andExpect(view().name("content/quizzes/list"))
                .andExpect(model().attributeExists("quizzes"));
    }

    @Test
    void testDeleteQuiz() throws Exception {
        when(expertQuizService.deleteQuiz(eq(200L), any()))
                .thenReturn(new ExpertQuizService.DeleteQuizResult(List.of("Lesson 1")));

        mockMvc.perform(post("/content/quizzes/200/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/content/quizzes"));

        verify(expertQuizService).deleteQuiz(eq(200L), any());
    }
}
