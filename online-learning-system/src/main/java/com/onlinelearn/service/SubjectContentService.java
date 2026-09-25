// Thư mục: src/main/java/com/onlinelearn/service/SubjectContentService.java
package com.onlinelearn.service;

import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.PackageStatus;
import com.onlinelearn.entity.enums.SubjectStatus;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectContentService {

    private final SubjectRepository subjectRepository;
    private final SubjectCategoryRepository categoryRepository;
    private final SubjectDimensionRepository dimensionRepository;
    private final PricePackageRepository pricePackageRepository;
    private final UserRepository userRepository;

    /**
     * Lấy danh sách Subject dựa trên vai trò:
     * - Expert: chỉ lấy môn học mình sở hữu (owner_id = expert.id)
     * - Admin: lấy toàn bộ môn học
     */
    public List<Subject> getSubjectsForUser(User currentUser, Long categoryId, SubjectStatus status, String keyword) {
        Long ownerFilterId = null;
        if (currentUser != null && currentUser.getRole() != null && "EXPERT".equals(currentUser.getRole().getCode())) {
            ownerFilterId = currentUser.getId();
        }
        return subjectRepository.searchSubjects(ownerFilterId, categoryId, status, keyword);
    }

    public Subject getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy môn học ID: " + id));
    }

    /**
     * Cập nhật thông tin Overview của Subject (Name, Category, Thumbnail, Featured, Description).
     */
    @Transactional
    public Subject updateSubjectOverview(Long subjectId, Subject formSubject, Long categoryId) {
        Subject subject = getSubjectById(subjectId);
        subject.setName(formSubject.getName());
        subject.setBriefInfo(formSubject.getBriefInfo());
        subject.setDescription(formSubject.getDescription());
        subject.setThumbnail(formSubject.getThumbnail());
        subject.setFeatured(formSubject.getFeatured() != null && formSubject.getFeatured());

        if (categoryId != null) {
            SubjectCategory category = categoryRepository.findById(categoryId).orElse(null);
            subject.setCategory(category);
        }

        return subjectRepository.save(subject);
    }

    /**
     * Tạo Subject mới (chỉ Admin được phép tạo và gán Owner là Expert).
     */
    @Transactional
    public Subject createSubject(Subject subject, Long categoryId, Long ownerId) {
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

    /**
     * Xuất bản môn học (chỉ Admin).
     */
    @Transactional
    public void publishSubject(Long id, User currentUser) {
        validateAdmin(currentUser);
        Subject subject = getSubjectById(id);
        subject.setStatus(SubjectStatus.PUBLISHED);
        subjectRepository.save(subject);
    }

    /**
     * Hủy xuất bản môn học (chỉ Admin).
     */
    @Transactional
    public void unpublishSubject(Long id, User currentUser) {
        validateAdmin(currentUser);
        Subject subject = getSubjectById(id);
        subject.setStatus(SubjectStatus.UNPUBLISHED);
        subjectRepository.save(subject);
    }

    /**
     * Thêm Dimension (cả Expert và Admin đều được phép).
     */
    @Transactional
    public SubjectDimension addDimension(Long subjectId, SubjectDimension dimension, Long dimensionTypeId) {
        Subject subject = getSubjectById(subjectId);
        dimension.setSubject(subject);
        return dimensionRepository.save(dimension);
    }

    /**
     * Xóa Dimension.
     */
    @Transactional
    public void deleteDimension(Long dimensionId) {
        dimensionRepository.deleteById(dimensionId);
    }

    /**
     * Thêm Price Package (chỉ Admin được phép thêm).
     */
    @Transactional
    public PricePackage addPricePackage(Long subjectId, PricePackage pricePackage, User currentUser) {
        validateAdmin(currentUser);
        Subject subject = getSubjectById(subjectId);
        pricePackage.setSubject(subject);
        if (pricePackage.getStatus() == null) {
            pricePackage.setStatus(PackageStatus.ACTIVE);
        }
        return pricePackageRepository.save(pricePackage);
    }

    /**
     * Xóa Price Package (chỉ Admin).
     */
    @Transactional
    public void deletePricePackage(Long packageId, User currentUser) {
        validateAdmin(currentUser);
        pricePackageRepository.deleteById(packageId);
    }

    private void validateAdmin(User user) {
        if (user == null || user.getRole() == null || !"ADMIN".equals(user.getRole().getCode())) {
            throw new org.springframework.security.access.AccessDeniedException("Hành động này chỉ dành riêng cho Quản trị viên (Admin)!");
        }
    }
}
