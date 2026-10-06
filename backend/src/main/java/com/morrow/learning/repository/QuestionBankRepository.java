package com.morrow.learning.repository;

import com.morrow.learning.domain.QuestionBankItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionBankRepository extends JpaRepository<QuestionBankItem, Long> {
    List<QuestionBankItem> findBySubject(String subject);
    List<QuestionBankItem> findByLevel(String level);
    List<QuestionBankItem> findBySubjectAndLevel(String subject, String level);
}
