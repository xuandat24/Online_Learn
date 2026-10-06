package com.morrow.learning.service;

import com.morrow.learning.domain.ExpertSubjectAssignment;
import com.morrow.learning.domain.Role;
import com.morrow.learning.domain.Subject;
import com.morrow.learning.domain.User;
import com.morrow.learning.dto.ExpertSubjectView;
import com.morrow.learning.dto.SubjectView;
import com.morrow.learning.dto.SubjectWriteRequest;
import com.morrow.learning.repository.ExpertSubjectAssignmentRepository;
import com.morrow.learning.repository.SubjectRepository;
import com.morrow.learning.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SubjectService {
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final ExpertSubjectAssignmentRepository assignmentRepository;

    public SubjectService(SubjectRepository subjectRepository, UserRepository userRepository,
                          ExpertSubjectAssignmentRepository assignmentRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.assignmentRepository = assignmentRepository;
    }

    @Transactional(readOnly = true)
    public List<SubjectView> list() {
        return subjectRepository.findAllByOrderByNameAsc().stream().map(SubjectView::from).toList();
    }

    @Transactional
    public SubjectView create(SubjectWriteRequest request) {
        if (subjectRepository.findByNameIgnoreCase(request.name().trim()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A subject with this name already exists");
        }
        return SubjectView.from(subjectRepository.save(new Subject(request.name().trim(),
                request.description().trim(), request.active())));
    }

    @Transactional
    public SubjectView update(Long subjectId, SubjectWriteRequest request) {
        Subject subject = findSubject(subjectId);
        subjectRepository.findByNameIgnoreCase(request.name().trim())
                .filter(existing -> !existing.getId().equals(subjectId))
                .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "A subject with this name already exists"); });
        subject.update(request.name().trim(), request.description().trim(), request.active());
        return SubjectView.from(subject);
    }

    @Transactional(readOnly = true)
    public List<ExpertSubjectView> assignments(Long subjectId) {
        findSubject(subjectId);
        return assignmentRepository.findAll().stream()
                .filter(assignment -> assignment.getSubject().getId().equals(subjectId))
                .map(ExpertSubjectView::from).toList();
    }

    @Transactional
    public ExpertSubjectView assign(Long subjectId, Long expertId) {
        Subject subject = findSubject(subjectId);
        if (!subject.isActive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Inactive subjects cannot be assigned");
        }
        User expert = userRepository.findById(expertId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expert account not found"));
        if (expert.getRole() != Role.EXPERT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only an Expert account can be assigned to a subject");
        }
        if (assignmentRepository.existsByExpertIdAndSubjectId(expertId, subjectId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This expert is already assigned to the subject");
        }
        return ExpertSubjectView.from(assignmentRepository.save(new ExpertSubjectAssignment(expert, subject)));
    }

    @Transactional
    public void unassign(Long subjectId, Long expertId) {
        ExpertSubjectAssignment assignment = assignmentRepository.findByExpertIdAndSubjectId(expertId, subjectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject assignment not found"));
        assignmentRepository.delete(assignment);
    }

    private Subject findSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subject not found"));
    }
}