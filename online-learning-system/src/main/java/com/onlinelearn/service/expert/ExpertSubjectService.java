package com.onlinelearn.service.expert;

import com.onlinelearn.dto.expert.PricePackageFormDTO;
import com.onlinelearn.dto.expert.SubjectDimensionFormDTO;
import com.onlinelearn.dto.expert.SubjectOverviewFormDTO;
import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.PackageStatus;
import com.onlinelearn.entity.enums.SubjectStatus;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpertSubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectCategoryRepository categoryRepository;
    private final SubjectDimensionRepository dimensionRepository;
    private final DimensionTypeRepository dimensionTypeRepository;
    private final PricePackageRepository pricePackageRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách Subject cho Expert.
     * Quy tắc nghiệp vụ cố định: Expert chỉ thấy Subject do chính mình sở hữu (ownerId = currentUser.id).
     */
    public List<Subject> getSubjectsForExpert(User currentUser, Long categoryId, SubjectStatus status, String keyword) {
        Long ownerFilterId = null;
        if (currentUser != null) {
            ownerFilterId = currentUser.getId();
        }
        return subjectRepository.searchSubjects(ownerFilterId, categoryId, status, keyword);
    }

    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + id));
    }

    public Subject getSubjectById(Long id, User currentUser) {
        Subject subject = getSubjectById(id);
        validateSubjectOwnership(subject, currentUser);
        return subject;
    }

    public SubjectOverviewFormDTO toOverviewFormDTO(Subject subject) {
        return SubjectOverviewFormDTO.builder()
                .id(subject.getId())
                .name(subject.getName())
                .categoryId(subject.getCategory() != null ? subject.getCategory().getId() : null)
                .thumbnail(subject.getThumbnail())
                .featured(subject.getFeatured())
                .briefInfo(subject.getBriefInfo())
                .description(subject.getDescription())
                .status(subject.getStatus())
                .build();
    }

    @Transactional
    public Subject updateSubjectOverview(Long subjectId, SubjectOverviewFormDTO formDTO, User currentUser) {
        Subject subject = getSubjectById(subjectId, currentUser);
        subject.setName(formDTO.getName());
        subject.setBriefInfo(formDTO.getBriefInfo());
        subject.setDescription(formDTO.getDescription());
        subject.setThumbnail(formDTO.getThumbnail());
        subject.setFeatured(Boolean.TRUE.equals(formDTO.getFeatured()));

        if (formDTO.getCategoryId() != null) {
            SubjectCategory category = categoryRepository.findById(formDTO.getCategoryId()).orElse(null);
            subject.setCategory(category);
        }

        return subjectRepository.save(subject);
    }

    @Transactional
    public SubjectDimension addDimension(Long subjectId, SubjectDimensionFormDTO dto, User currentUser) {
        Subject subject = getSubjectById(subjectId, currentUser);

        DimensionType type = dimensionTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Loại chuẩn không tồn tại: " + dto.getTypeId()));

        SubjectDimension dimension = SubjectDimension.builder()
                .subject(subject)
                .type(type)
                .name(dto.getName())
                .description(dto.getDescription())
                .build();

        return dimensionRepository.save(dimension);
    }

    @Transactional
    public SubjectDimension updateDimension(Long dimensionId, SubjectDimensionFormDTO dto, User currentUser) {
        SubjectDimension dimension = dimensionRepository.findById(dimensionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Dimension ID: " + dimensionId));

        validateSubjectOwnership(dimension.getSubject(), currentUser);

        DimensionType type = dimensionTypeRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new IllegalArgumentException("Loại chuẩn không tồn tại: " + dto.getTypeId()));

        dimension.setType(type);
        dimension.setName(dto.getName());
        dimension.setDescription(dto.getDescription());

        return dimensionRepository.save(dimension);
    }

    @Transactional
    public void deleteDimension(Long dimensionId, User currentUser) {
        SubjectDimension dimension = dimensionRepository.findById(dimensionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Dimension ID: " + dimensionId));

        validateSubjectOwnership(dimension.getSubject(), currentUser);
        dimensionRepository.delete(dimension);
    }

    public void validateSubjectOwnership(Subject subject, User currentUser) {
        if (currentUser != null && currentUser.getRole() != null && "EXPERT".equals(currentUser.getRole().getCode())) {
            if (subject == null || subject.getOwner() == null || !subject.getOwner().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Bạn không có quyền quản trị môn học này!");
            }
        }
    }

    public List<Subject> getSubjectsForUser(User currentUser, Long categoryId, SubjectStatus status, String keyword) {
        Long ownerFilterId = null;
        if (currentUser != null && currentUser.getRole() != null && "EXPERT".equals(currentUser.getRole().getCode())) {
            ownerFilterId = currentUser.getId();
        }
        return subjectRepository.searchSubjects(ownerFilterId, categoryId, status, keyword);
    }

    @Transactional
    public Subject createSubject(Subject subject, Long categoryId, Long ownerId, User currentUser) {
        validateAdmin(currentUser);
        if (categoryId != null) {
            SubjectCategory category = categoryRepository.findById(categoryId).orElse(null);
            subject.setCategory(category);
        }
        if (ownerId != null) {
            User owner = userRepository.findById(ownerId).orElse(null);
            subject.setOwner(owner);
        }
        subject.setStatus(SubjectStatus.UNPUBLISHED);
        return subjectRepository.save(subject);
    }

    @Transactional
    public void publishSubject(Long id, User currentUser) {
        validateAdmin(currentUser);
        Subject subject = getSubjectById(id);
        subject.setStatus(SubjectStatus.PUBLISHED);
        subjectRepository.save(subject);
    }

    @Transactional
    public void unpublishSubject(Long id, User currentUser) {
        validateAdmin(currentUser);
        Subject subject = getSubjectById(id);
        subject.setStatus(SubjectStatus.UNPUBLISHED);
        subjectRepository.save(subject);
    }

    @Transactional
    public PricePackage addPricePackage(Long subjectId, PricePackageFormDTO dto, User currentUser) {
        validateAdmin(currentUser);
        Subject subject = getSubjectById(subjectId);

        PricePackage pkg = PricePackage.builder()
                .subject(subject)
                .packageName(dto.getPackageName())
                .accessDuration(dto.getAccessDuration() != null ? dto.getAccessDuration() : 180)
                .listPrice(dto.getListPrice())
                .salePrice(dto.getSalePrice())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : PackageStatus.ACTIVE)
                .build();

        return pricePackageRepository.save(pkg);
    }

    @Transactional
    public PricePackage updatePricePackage(Long subjectId, Long packageId, PricePackageFormDTO dto, User currentUser) {
        validateAdmin(currentUser);
        PricePackage pkg = pricePackageRepository.findById(packageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Price Package ID: " + packageId));

        pkg.setPackageName(dto.getPackageName());
        pkg.setAccessDuration(dto.getAccessDuration());
        pkg.setListPrice(dto.getListPrice());
        pkg.setSalePrice(dto.getSalePrice());
        pkg.setDescription(dto.getDescription());
        if (dto.getStatus() != null) {
            pkg.setStatus(dto.getStatus());
        }

        return pricePackageRepository.save(pkg);
    }

    @Transactional
    public void deletePricePackage(Long packageId, User currentUser) {
        validateAdmin(currentUser);
        pricePackageRepository.deleteById(packageId);
    }

    private void validateAdmin(User user) {
        if (user == null || user.getRole() == null || !"ADMIN".equals(user.getRole().getCode())) {
            throw new AccessDeniedException("Hành động này chỉ dành riêng cho Quản trị viên (Admin)!");
        }
    }
}
