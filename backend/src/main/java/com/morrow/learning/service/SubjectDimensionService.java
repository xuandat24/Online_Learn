package com.morrow.learning.service;

import com.morrow.learning.domain.SubjectDimension;
import com.morrow.learning.dto.SubjectDimensionView;
import com.morrow.learning.dto.SubjectDimensionWriteRequest;
import com.morrow.learning.repository.SubjectDimensionRepository;
import com.morrow.learning.repository.SubjectRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubjectDimensionService {
    private final SubjectRepository subjectRepository;
    private final SubjectDimensionRepository dimensionRepository;

    public SubjectDimensionService(SubjectRepository subjectRepository,
                                   SubjectDimensionRepository dimensionRepository) {
        this.subjectRepository = subjectRepository;
        this.dimensionRepository = dimensionRepository;
    }

    @Transactional(readOnly = true)
    public List<SubjectDimensionView> list(Long subjectId) {
        requireSubject(subjectId);
        return dimensionRepository.findAllBySubjectIdOrderByDisplayOrderAscIdAsc(subjectId).stream()
                .map(SubjectDimensionView::from).toList();
    }

    @Transactional
    public SubjectDimensionView create(Long subjectId, SubjectDimensionWriteRequest request) {
        var subject = requireSubject(subjectId);
        return SubjectDimensionView.from(dimensionRepository.save(new SubjectDimension(subject, request.name().trim(),
                request.description() == null ? "" : request.description().trim(),
                request.displayOrder(), request.active())));
    }

    @Transactional
    public SubjectDimensionView update(Long subjectId, Long dimensionId, SubjectDimensionWriteRequest request) {
        SubjectDimension dimension = findDimension(subjectId, dimensionId);
        dimension.update(request.name().trim(), request.description() == null ? "" : request.description().trim(),
                request.displayOrder(), request.active());
        return SubjectDimensionView.from(dimension);
    }

    @Transactional
    public void delete(Long subjectId, Long dimensionId) {
        dimensionRepository.delete(findDimension(subjectId, dimensionId));
    }

    private com.morrow.learning.domain.Subject requireSubject(Long subjectId) {
        return subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
    }

    private SubjectDimension findDimension(Long subjectId, Long dimensionId) {
        requireSubject(subjectId);
        return dimensionRepository.findByIdAndSubjectId(dimensionId, subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dimension not found"));
    }
}
