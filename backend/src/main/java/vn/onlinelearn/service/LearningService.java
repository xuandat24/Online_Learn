package vn.onlinelearn.service;
import java.util.*;
import java.math.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.dto.Requests.*;
import vn.onlinelearn.exception.ApiException;

@Service @Transactional
public class LearningService {
    @Autowired Data db; @Autowired Rules rules; @Autowired CatalogService catalog;
    public Object courses() { var a=rules.role("Customer"); return db.access.findByUserId(a.id()).stream().map(x->Rules.map("access",x,"course",catalog.courseCard(db.subjects.findById(x.subjectId).orElseThrow()))).toList(); }
    public Object lessons(Long subject) {
        rules.learning(subject,null); var uid=rules.actor().id(); var progress=db.progress.findByUserId(uid);
        return db.lessons.findBySubjectIdOrderBySortOrderAsc(subject).stream().filter(x->x.status.equals("Active")).map(x->{
            boolean allowed=x.packageId==null||rules.accesses(uid,subject).stream().anyMatch(a->db.registrations.findById(a.registrationId).orElseThrow().packageId.equals(x.packageId));
            return Rules.map("lessonId",x.lessonId,"name",x.name,"lessonType",x.lessonType,"allowed",allowed,"completed",progress.stream().anyMatch(p->p.lessonId.equals(x.lessonId)&&p.isCompleted));
        }).toList();
    }
    public Lesson lesson(Long id) { var l=db.lessons.findById(id).filter(x->x.status.equals("Active")).orElseThrow(ApiException::missing); rules.learning(l.subjectId,l.packageId); return l; }
    public Object complete(Long id) {
        var l=lesson(id); var uid=rules.actor().id(); db.users.lockById(uid).orElseThrow();
        if(l.lessonType.equals("Quiz")&&!db.attempts.existsByUserIdAndQuizIdAndStatus(uid,l.quizId,"Submitted")) throw ApiException.bad("Hãy hoàn thành bài kiểm tra trước.");
        var p=db.progress.findByUserIdAndLessonId(uid,id).orElseGet(LessonProgress::new); p.userId=uid; p.lessonId=id; p.isCompleted=true; p.updatedAt=Rules.now(); return db.progress.save(p);
    }
    private Quiz quizAccess(Long quizId) {
        var subjectId=db.quizzes.findById(quizId).orElseThrow(ApiException::missing).subjectId;
        db.subjects.lockById(subjectId).orElseThrow(ApiException::missing);
        var quiz=db.quizzes.lockById(quizId).orElseThrow(ApiException::missing); rules.learning(quiz.subjectId,null);
        boolean allowed=db.lessons.findBySubjectIdOrderBySortOrderAsc(quiz.subjectId).stream().filter(l->l.status.equals("Active")&&Objects.equals(l.quizId,quizId)).anyMatch(l->{try{rules.learning(l.subjectId,l.packageId);return true;}catch(ApiException e){return false;}});
        if(!allowed) throw ApiException.forbidden(); return quiz;
    }
    public Object start(Long quizId) {
        var uid=rules.role("Customer").id(); db.users.lockById(uid).orElseThrow();
        var previous=db.attempts.findByUserIdAndQuizIdOrderByStartedAtDesc(uid,quizId).stream().filter(x->x.status.equals("In Progress")).findFirst();
        if(previous.isPresent()) return attempt(previous.get().attemptId);
        var q=quizAccess(quizId);
        if(q.questionIds.isEmpty()) throw ApiException.bad("Bài kiểm tra chưa có câu hỏi.");
        var a=new QuizAttempt(); a.quizId=quizId; a.userId=uid; a.expiresAt=Rules.now().plusMinutes(q.durationMinutes); db.attempts.save(a); return attempt(a.attemptId);
    }
    private QuizAttempt own(Long id) { var a=db.attempts.lockById(id).orElseThrow(ApiException::missing); if(!a.userId.equals(rules.role("Customer").id())) throw ApiException.forbidden(); quizAccess(a.quizId); return a; }
    private void grade(QuizAttempt a) {
        if(!a.status.equals("In Progress")) return;
        var q=db.quizzes.findById(a.quizId).orElseThrow(); var answers=db.answers.findByAttemptId(a.attemptId);
        long correct=answers.stream().filter(x->x.optionId!=null&&db.options.findById(x.optionId).map(o->o.isCorrect&&o.questionId.equals(x.questionId)).orElse(false)).count();
        a.score=BigDecimal.valueOf(correct*100).divide(BigDecimal.valueOf(q.questionIds.size()),2,RoundingMode.HALF_UP); a.status="Submitted"; a.submittedAt=Rules.now();
    }
    public Object attempt(Long id) {
        var a=own(id); if(!Rules.now().isBefore(a.expiresAt)) grade(a); var q=db.quizzes.findById(a.quizId).orElseThrow(); boolean review=a.status.equals("Submitted");
        return Rules.map("attempt",a,"name",q.name,"passRate",q.passRate,"serverTime",Rules.now(),"answers",db.answers.findByAttemptId(id),"questions",q.questionIds.stream().map(qid->{
            var question=db.questions.findById(qid).orElseThrow(); var m=Rules.map("questionId",qid,"content",question.content,"mediaUrl",question.mediaUrl,"mediaType",question.mediaType,
                "options",db.options.findByQuestionIdOrderBySortOrderAsc(qid).stream().map(o->{ var option=Rules.map("optionId",o.optionId,"content",o.content); if(review)option.put("correct",o.isCorrect); return option; }).toList());
            if(review)m.put("explanation",question.explanation); return m;
        }).toList());
    }
    public Object answer(Long id,AnswerInput input) {
        var a=own(id); if(!a.status.equals("In Progress")||!Rules.now().isBefore(a.expiresAt)) throw ApiException.conflict("Bài kiểm tra đã kết thúc.");
        var q=db.quizzes.findById(a.quizId).orElseThrow(); if(!q.questionIds.contains(input.questionId())) throw ApiException.bad("Câu hỏi không thuộc bài kiểm tra.");
        if(input.optionId()!=null&&!db.options.findById(input.optionId()).orElseThrow(ApiException::missing).questionId.equals(input.questionId())) throw ApiException.bad("Đáp án không thuộc câu hỏi.");
        var answer=db.answers.findByAttemptIdAndQuestionId(id,input.questionId()).orElseGet(QuizAnswer::new); answer.attemptId=id; answer.questionId=input.questionId(); answer.optionId=input.optionId(); answer.markedForReview=input.markedForReview(); answer.updatedAt=Rules.now(); return db.answers.save(answer);
    }
    public Object submit(Long id) { var a=own(id); grade(a); return attempt(id); }
    public Object history(Long quizId) { quizAccess(quizId); return db.attempts.findByUserIdAndQuizIdOrderByStartedAtDesc(rules.actor().id(),quizId); }
}
