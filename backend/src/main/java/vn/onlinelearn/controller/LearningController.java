package vn.onlinelearn.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.onlinelearn.service.LearningService;
import vn.onlinelearn.dto.Requests.AnswerInput;
@RestController
public class LearningController {
    private final LearningService s;
    public LearningController(LearningService s){this.s=s;}
    @GetMapping("/api/my/courses") public Object courses(){return s.courses();}
    @GetMapping("/api/learning/courses/{id}/lessons") public Object lessons(@PathVariable Long id){return s.lessons(id);}
    @GetMapping("/api/learning/lessons/{id}") public Object lesson(@PathVariable Long id){return s.lesson(id);}
    @PostMapping("/api/learning/lessons/{id}/complete") public Object complete(@PathVariable Long id){return s.complete(id);}
    @PostMapping("/api/learning/quizzes/{id}/start") public Object start(@PathVariable Long id){return s.start(id);}
    @GetMapping("/api/learning/quizzes/{id}/history") public Object history(@PathVariable Long id){return s.history(id);}
    @GetMapping("/api/attempts/{id}") public Object attempt(@PathVariable Long id){return s.attempt(id);}
    @PutMapping("/api/attempts/{id}/answers") public Object answer(@PathVariable Long id,@Valid @RequestBody AnswerInput r){return s.answer(id,r);}
    @PostMapping("/api/attempts/{id}/submit") public Object submit(@PathVariable Long id){return s.submit(id);}
}
