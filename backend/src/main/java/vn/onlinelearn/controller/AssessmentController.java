package vn.onlinelearn.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.onlinelearn.service.AssessmentService;
import vn.onlinelearn.dto.Requests.*;
@RestController @RequestMapping("/api/manage/courses/{subject}")
public class AssessmentController {
    private final AssessmentService service; public AssessmentController(AssessmentService service){this.service=service;}
    @GetMapping("/questions") public Object questions(@PathVariable Long subject){return service.questions(subject);}
    @GetMapping("/questions/import-template") public Object template(@PathVariable Long subject){return service.importTemplate(subject);}
    @PostMapping("/questions/import") public Object importQuestions(@PathVariable Long subject,@Valid @RequestBody QuestionImport r){return service.importQuestions(subject,r);}
    @PostMapping("/questions") public Object question(@PathVariable Long subject,@Valid @RequestBody QuestionInput r){return service.question(subject,null,r);}
    @PutMapping("/questions/{id}") public Object question(@PathVariable Long subject,@PathVariable Long id,@Valid @RequestBody QuestionInput r){return service.question(subject,id,r);}
    @PostMapping("/quizzes") public Object quiz(@PathVariable Long subject,@Valid @RequestBody QuizInput r){return service.quiz(subject,null,r);}
    @PutMapping("/quizzes/{id}") public Object quiz(@PathVariable Long subject,@PathVariable Long id,@Valid @RequestBody QuizInput r){return service.quiz(subject,id,r);}
}
