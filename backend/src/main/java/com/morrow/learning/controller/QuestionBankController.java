package com.morrow.learning.controller;

import com.morrow.learning.domain.QuestionBankItem;
import com.morrow.learning.repository.QuestionBankRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/questions")
public class QuestionBankController {
    private final QuestionBankRepository questionBankRepository;

    public QuestionBankController(QuestionBankRepository questionBankRepository) {
        this.questionBankRepository = questionBankRepository;
    }

    @GetMapping
    public List<QuestionBankItem> list(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return questionBankRepository.findAll().stream()
                .filter(item -> subject == null || subject.isBlank() || item.getSubject().equalsIgnoreCase(subject))
                .filter(item -> level == null || level.isBlank() || item.getLevel().equalsIgnoreCase(level))
                .filter(item -> status == null || status.isBlank() || item.getStatus().equalsIgnoreCase(status))
                .filter(item -> search == null || search.isBlank()
                        || item.getPrompt().toLowerCase().contains(search.trim().toLowerCase())
                        || item.getLessonTitle() != null
                        && item.getLessonTitle().toLowerCase().contains(search.trim().toLowerCase()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuestionBankItem create(@RequestBody QuestionBankItem item) {
        return questionBankRepository.save(item);
    }

    @PostMapping("/import")
    @ResponseStatus(HttpStatus.CREATED)
    public List<QuestionBankItem> importQuestions(@RequestBody List<QuestionBankItem> items) {
        return questionBankRepository.saveAll(items);
    }

    @PutMapping("/{id}")
    public QuestionBankItem update(@PathVariable Long id, @RequestBody QuestionBankItem updated) {
        var item = questionBankRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        item.setSubject(updated.getSubject());
        item.setLessonTitle(updated.getLessonTitle());
        item.setLevel(updated.getLevel());
        item.setPrompt(updated.getPrompt());
        item.setOptionsJson(updated.getOptionsJson());
        item.setCorrectOptionIndex(updated.getCorrectOptionIndex());
        item.setExplanation(updated.getExplanation());
        item.setStatus(updated.getStatus());
        return questionBankRepository.save(item);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!questionBankRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }
        questionBankRepository.deleteById(id);
    }
}
