package vn.onlinelearn.service;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.dto.Requests.*;
import vn.onlinelearn.exception.ApiException;
@Service @Transactional
public class AssessmentService {
    @Autowired Data db; @Autowired Rules rules;
    public Object importTemplate(Long subject){
        rules.assigned(subject);
        var level=db.settings.findAll().stream().filter(s->s.type.equals("Question Level")&&s.status.equals("Active")).findFirst().orElseThrow(()->ApiException.bad("Hãy tạo Question Level trước khi tải mẫu."));
        return Rules.map("questions",List.of(Rules.map("content","Thay bằng nội dung câu hỏi của bạn","levelSettingId",level.settingId,"status","Active","explanation","Giải thích đáp án","options",List.of(Rules.map("content","Đáp án đúng","correct",true),Rules.map("content","Đáp án khác","correct",false)))));
    }
    public Object importQuestions(Long subject,QuestionImport input){
        rules.assigned(subject);var saved=new ArrayList<Long>();int index=0;
        for(var item:input.questions()){
            index++;try{saved.add(question(subject,null,item).questionId);}catch(ApiException e){throw new ApiException(e.status,"Câu "+index+": "+e.getMessage());}
        }
        return Rules.map("imported",saved.size(),"questionIds",saved);
    }
    public Object questions(Long subject) { rules.assigned(subject); return db.questions.findBySubjectId(subject).stream().map(q->Rules.map("question",q,"options",db.options.findByQuestionIdOrderBySortOrderAsc(q.questionId))).toList(); }
    public Question question(Long subject,Long id,QuestionInput input) {
        rules.assigned(subject); rules.setting(input.levelSettingId(),"Question Level");
        var q=id==null?new Question():db.questions.lockById(id).orElseThrow(ApiException::missing);
        if(id!=null&&!q.subjectId.equals(subject)) throw ApiException.forbidden();
        if(id!=null&&db.quizzes.findBySubjectId(subject).stream().anyMatch(x->x.questionIds.contains(id)&&db.attempts.existsByQuizId(x.quizId))) throw ApiException.conflict("Câu hỏi đã có bài làm; hãy tạo câu hỏi mới.");
        if(input.options().stream().filter(OptionInput::correct).count()!=1) throw ApiException.bad("Mỗi câu hỏi cần đúng một đáp án đúng.");
        if(input.lessonId()!=null&&!db.lessons.findById(input.lessonId()).orElseThrow(ApiException::missing).subjectId.equals(subject)) throw ApiException.bad("Bài học không thuộc khóa.");
        var dimensions=input.dimensionIds()==null?Set.<Long>of():input.dimensionIds();
        for(var d:dimensions) if(!db.dimensions.findById(d).orElseThrow(ApiException::missing).subjectId.equals(subject)) throw ApiException.bad("Dimension không thuộc khóa.");
        q.subjectId=subject;q.content=input.content();q.explanation=input.explanation();q.levelSettingId=input.levelSettingId();q.lessonId=input.lessonId();q.mediaUrl=Rules.url(input.mediaUrl());q.mediaType=input.mediaType();q.status=input.status();q.dimensionIds=new HashSet<>(dimensions);db.questions.saveAndFlush(q);
        if(id!=null){db.options.deleteByQuestionId(id);db.options.flush();}
        int index=0;for(var option:input.options()){var o=new QuestionOption();o.questionId=q.questionId;o.content=option.content();o.isCorrect=option.correct();o.sortOrder=index++;db.options.save(o);}return q;
    }
    public Quiz quiz(Long subject,Long id,QuizInput input) {
        rules.assigned(subject); rules.setting(input.quizTypeSettingId(),"Test Type"); if(input.levelSettingId()!=null)rules.setting(input.levelSettingId(),"Question Level");
        var q=id==null?new Quiz():db.quizzes.lockById(id).orElseThrow(ApiException::missing);
        if(id!=null&&!q.subjectId.equals(subject))throw ApiException.forbidden();
        if(id!=null&&db.attempts.existsByQuizId(id))throw ApiException.conflict("Bài kiểm tra đã có lượt làm, không thể chỉnh sửa.");
        if(new HashSet<>(input.questionIds()).size()!=input.questionIds().size())throw ApiException.bad("Câu hỏi bị trùng.");
        for(var qid:input.questionIds()){var question=db.questions.lockById(qid).orElseThrow(ApiException::missing);if(!question.subjectId.equals(subject)||!question.status.equals("Active"))throw ApiException.bad("Câu hỏi không hợp lệ.");}
        q.subjectId=subject;q.name=input.name();q.quizTypeSettingId=input.quizTypeSettingId();q.levelSettingId=input.levelSettingId();q.durationMinutes=input.durationMinutes();q.passRate=input.passRate();q.description=input.description();q.questionIds=new ArrayList<>(input.questionIds());return db.quizzes.save(q);
    }
}
