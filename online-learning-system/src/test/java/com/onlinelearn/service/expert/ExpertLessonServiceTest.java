package com.onlinelearn.service.expert;

import com.onlinelearn.dto.expert.LessonFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import com.onlinelearn.repository.LessonRepository;
import com.onlinelearn.repository.QuizRepository;
import com.onlinelearn.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpertLessonServiceTest {

    @Mock
    private LessonRepository lessonRepository;
    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private QuizRepository quizRepository;

    @InjectMocks
    private ExpertLessonService expertLessonService;

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
    void testGetLessonsBySubject_BlockedIfDifferentExpert() {
        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));

        assertThrows(AccessDeniedException.class, () ->
                expertLessonService.getLessonsBySubject(100L, null, otherExpert)
        );
    }

    @Test
    void testSaveLesson_TopicType_ClearsVideoAndHtmlAndQuiz() {
        LessonFormDTO dto = LessonFormDTO.builder()
                .subjectId(100L)
                .name("Chương 1: Mở đầu")
                .type(LessonTypeEnum.SUBJECT_TOPIC)
                .videoLink("https://youtube.com/invalid")
                .htmlContent("<p>Invalid</p>")
                .quizId(999L)
                .build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));

        Lesson saved = expertLessonService.saveLesson(dto, expertUser);

        assertNull(saved.getVideoLink());
        assertNull(saved.getHtmlContent());
        assertNull(saved.getQuiz());
        assertEquals(LessonTypeEnum.SUBJECT_TOPIC, saved.getType());
    }

    @Test
    void testSaveLesson_LessonType_KeepsVideoAndHtml_ClearsQuiz() {
        LessonFormDTO dto = LessonFormDTO.builder()
                .subjectId(100L)
                .name("Bài 1: Cài đặt JDK")
                .type(LessonTypeEnum.LESSON)
                .videoLink("https://youtube.com/watch?v=123")
                .htmlContent("<p>Nội dung</p>")
                .quizId(999L)
                .build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(i -> i.getArgument(0));

        Lesson saved = expertLessonService.saveLesson(dto, expertUser);

        assertEquals("https://youtube.com/watch?v=123", saved.getVideoLink());
        assertEquals("<p>Nội dung</p>", saved.getHtmlContent());
        assertNull(saved.getQuiz());
    }
}
