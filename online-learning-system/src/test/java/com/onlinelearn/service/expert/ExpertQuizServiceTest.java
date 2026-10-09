package com.onlinelearn.service.expert;

import com.onlinelearn.dto.expert.QuizFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpertQuizServiceTest {

    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuizQuestionRepository quizQuestionRepository;
    @Mock
    private QuizAttemptRepository quizAttemptRepository;
    @Mock
    private QuizAnswerRepository quizAnswerRepository;
    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private QuestionLevelRepository levelRepository;
    @Mock
    private TestTypeRepository testTypeRepository;

    @InjectMocks
    private ExpertQuizService expertQuizService;

    private User expertUser;
    private Subject ownedSubject;
    private Quiz mockQuiz;

    @BeforeEach
    void setUp() {
        Role expertRole = Role.builder().id(1L).code("EXPERT").name("Expert").build();
        expertUser = User.builder().id(10L).fullName("Expert One").role(expertRole).build();

        ownedSubject = Subject.builder()
                .id(100L)
                .name("Java Core")
                .owner(expertUser)
                .build();

        mockQuiz = Quiz.builder()
                .id(200L)
                .name("Quiz 1")
                .subject(ownedSubject)
                .quizQuestions(new ArrayList<>())
                .build();
    }

    @Test
    void testSaveQuiz_Success() {
        QuizFormDTO dto = QuizFormDTO.builder()
                .subjectId(100L)
                .name("Java Basics Quiz")
                .duration(20)
                .passRate(70.0)
                .build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));
        when(quizRepository.save(any(Quiz.class))).thenAnswer(i -> i.getArgument(0));

        Quiz saved = expertQuizService.saveQuiz(dto, expertUser);

        assertNotNull(saved);
        assertEquals("Java Basics Quiz", saved.getName());
        assertEquals(20, saved.getDuration());
        assertEquals(70.0, saved.getPassRate());
    }

    @Test
    void testDeleteQuiz_CascadeDeletesAttemptsAndQuestions_UnlinksLessons() {
        when(quizRepository.findById(200L)).thenReturn(Optional.of(mockQuiz));

        QuizAttempt attempt = QuizAttempt.builder().id(1L).quiz(mockQuiz).build();
        when(quizAttemptRepository.findByQuizId(200L)).thenReturn(List.of(attempt));

        Lesson linkedLesson = Lesson.builder().id(10L).name("Lesson 1").quiz(mockQuiz).build();
        when(lessonRepository.findByQuizId(200L)).thenReturn(List.of(linkedLesson));

        ExpertQuizService.DeleteQuizResult result = expertQuizService.deleteQuiz(200L, expertUser);

        assertNotNull(result);
        assertTrue(result.hasDetachedLessons());
        assertEquals(List.of("Lesson 1"), result.detachedLessonNames());

        verify(quizAnswerRepository).deleteByAttemptIdIn(List.of(1L));
        verify(quizAttemptRepository).deleteAll(List.of(attempt));
        verify(quizQuestionRepository).deleteByQuizId(200L);
        verify(lessonRepository).saveAll(any());
        verify(quizRepository).delete(mockQuiz);
    }
}
