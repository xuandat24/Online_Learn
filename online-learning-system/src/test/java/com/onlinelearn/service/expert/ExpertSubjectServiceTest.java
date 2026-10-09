package com.onlinelearn.service.expert;

import com.onlinelearn.dto.expert.SubjectDimensionFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.repository.*;
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
class ExpertSubjectServiceTest {

    @Mock
    private SubjectRepository subjectRepository;
    @Mock
    private SubjectCategoryRepository categoryRepository;
    @Mock
    private SubjectDimensionRepository dimensionRepository;
    @Mock
    private DimensionTypeRepository dimensionTypeRepository;
    @Mock
    private PricePackageRepository pricePackageRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ExpertSubjectService expertSubjectService;

    private User expertUser;
    private User otherExpert;
    private Subject ownedSubject;

    @BeforeEach
    void setUp() {
        Role expertRole = Role.builder().id(1L).code("EXPERT").name("Expert").build();
        expertUser = User.builder().id(10L).fullName("Expert").role(expertRole).build();
        otherExpert = User.builder().id(20L).fullName("Other Expert").role(expertRole).build();

        ownedSubject = Subject.builder()
                .id(100L)
                .name("Java Core")
                .owner(expertUser)
                .build();
    }

    @Test
    void testGetSubjectsForExpert_FiltersByOwnerId() {
        when(subjectRepository.searchSubjects(eq(10L), any(), any(), any()))
                .thenReturn(List.of(ownedSubject));

        List<Subject> result = expertSubjectService.getSubjectsForExpert(expertUser, null, null, null);

        assertEquals(1, result.size());
        verify(subjectRepository).searchSubjects(eq(10L), any(), any(), any());
    }

    @Test
    void testAddDimension_ExpertAllowedForOwnSubject() {
        SubjectDimensionFormDTO dto = SubjectDimensionFormDTO.builder()
                .typeId(1L)
                .name("OOP")
                .description("Desc")
                .build();

        DimensionType dimType = DimensionType.builder().id(1L).name("Domain").build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));
        when(dimensionTypeRepository.findById(1L)).thenReturn(Optional.of(dimType));
        when(dimensionRepository.save(any(SubjectDimension.class))).thenAnswer(i -> i.getArgument(0));

        SubjectDimension added = expertSubjectService.addDimension(100L, dto, expertUser);

        assertNotNull(added);
        assertEquals("OOP", added.getName());
        verify(dimensionRepository).save(any(SubjectDimension.class));
    }

    @Test
    void testAddDimension_BlockedForOtherExpert() {
        SubjectDimensionFormDTO dto = SubjectDimensionFormDTO.builder()
                .typeId(1L)
                .name("OOP")
                .description("Desc")
                .build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));

        assertThrows(AccessDeniedException.class, () ->
                expertSubjectService.addDimension(100L, dto, otherExpert)
        );
    }

    @Test
    void testPublishSubject_AdminAllowed() {
        Role adminRole = Role.builder().id(2L).code("ADMIN").name("Admin").build();
        User adminUser = User.builder().id(99L).role(adminRole).build();

        when(subjectRepository.findById(100L)).thenReturn(Optional.of(ownedSubject));
        when(subjectRepository.save(any(Subject.class))).thenAnswer(i -> i.getArgument(0));

        expertSubjectService.publishSubject(100L, adminUser);

        assertEquals(com.onlinelearn.entity.enums.SubjectStatus.PUBLISHED, ownedSubject.getStatus());
        verify(subjectRepository).save(ownedSubject);
    }

    @Test
    void testPublishSubject_ExpertBlocked() {
        assertThrows(AccessDeniedException.class, () ->
                expertSubjectService.publishSubject(100L, expertUser)
        );
    }
}
