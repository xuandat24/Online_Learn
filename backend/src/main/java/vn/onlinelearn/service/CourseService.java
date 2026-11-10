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
public class CourseService {
    @Autowired Data db; @Autowired Rules rules;
    public Object list(String q,String status,Long category,int page,int size) {
        var a=rules.role("Admin","Author");
        return Rules.page(db.subjects.findAll().stream().filter(s->a.role().equals("Admin")||s.ownerUserId.equals(a.id()))
            .filter(s->q==null||s.name.toLowerCase().contains(q.toLowerCase())).filter(s->status==null||s.publishStatus.equals(status))
            .filter(s->category==null||s.categorySettingId.equals(category)).sorted(Comparator.comparing((Subject x)->x.updatedAt).reversed()).toList(),page,size);
    }
    public Object detail(Long id) { var s=rules.assigned(id); return Rules.map("course",s,"packages",db.packages.findBySubjectId(id),"lessons",db.lessons.findBySubjectIdOrderBySortOrderAsc(id),"dimensions",db.dimensions.findBySubjectId(id),"quizzes",db.quizzes.findBySubjectId(id)); }
    public Subject save(Long id,Course r) {
        var a=rules.role("Admin","Author"); Subject s;
        if(id==null) { rules.role("Admin"); s=new Subject(); } else s=rules.assigned(id);
        if(a.role().equals("Author") && (!s.ownerUserId.equals(r.ownerUserId()) || !s.publishStatus.equals(r.publishStatus()) || s.featured!=r.featured())) throw ApiException.forbidden();
        var owner=db.users.findById(r.ownerUserId()).filter(u->u.roleCode.equals("Author")&&u.status.equals("Active")).orElseThrow(()->ApiException.bad("Hãy chọn Author đang hoạt động."));
        rules.setting(r.categorySettingId(),"Subject Category");
        if(db.subjects.findAll().stream().anyMatch(x->x.name.equalsIgnoreCase(r.name().strip())&&!Objects.equals(x.subjectId,id))) throw ApiException.conflict("Tên khóa học đã tồn tại.");
        s.name=r.name().strip(); s.ownerUserId=owner.userId; s.categorySettingId=r.categorySettingId(); s.tagline=r.tagline(); s.thumbnailUrl=Rules.url(r.thumbnailUrl()); s.description=Rules.html(r.description()); s.featured=r.featured(); s.publishStatus=r.publishStatus(); s.updatedAt=Rules.now();
        return db.subjects.save(s);
    }
    public PricePackage savePackage(Long subjectId,Long id,PackageInput r) {
        rules.role("Admin"); rules.assigned(subjectId);
        var p=id==null?new PricePackage():db.packages.findById(id).orElseThrow(ApiException::missing);
        if(id!=null&&!p.subjectId.equals(subjectId)) throw ApiException.forbidden();
        if(r.salePrice().compareTo(r.listPrice())>0 || r.salePrice().stripTrailingZeros().scale()>0 || r.listPrice().stripTrailingZeros().scale()>0) throw ApiException.bad("Giá VND phải là số nguyên; giá bán không vượt giá niêm yết.");
        p.subjectId=subjectId; p.name=r.name(); p.durationMonths=r.durationMonths(); p.listPrice=r.listPrice(); p.salePrice=r.salePrice(); p.description=r.description(); p.status=r.status(); return db.packages.save(p);
    }
    public Lesson saveLesson(Long subjectId,Long id,LessonInput r) {
        rules.assigned(subjectId);
        var l=id==null?new Lesson():db.lessons.findById(id).orElseThrow(ApiException::missing);
        if(id!=null&&!l.subjectId.equals(subjectId)) throw ApiException.forbidden();
        if(r.packageId()!=null && !db.packages.findById(r.packageId()).orElseThrow(ApiException::missing).subjectId.equals(subjectId)) throw ApiException.bad("Gói không thuộc khóa học.");
        if(r.parentTopicId()!=null) {
            var parent=db.lessons.findById(r.parentTopicId()).orElseThrow(ApiException::missing);
            if(!parent.subjectId.equals(subjectId)||!parent.lessonType.equals("Subject Topic")||Objects.equals(id,parent.lessonId)) throw ApiException.bad("Chương học không hợp lệ.");
        }
        if(r.lessonType().equals("Quiz")) {
            if(r.quizId()==null||!db.quizzes.findById(r.quizId()).orElseThrow(ApiException::missing).subjectId.equals(subjectId)) throw ApiException.bad("Bài kiểm tra không thuộc khóa học.");
        } else if(r.quizId()!=null) throw ApiException.bad("Chỉ bài học loại Quiz được liên kết bài kiểm tra.");
        if(r.lessonType().equals("Lesson") && (r.videoUrl()==null||r.videoUrl().isBlank()) && (r.htmlContent()==null||r.htmlContent().isBlank())) throw ApiException.bad("Bài học cần video hoặc nội dung.");
        if(id!=null && db.lessons.findBySubjectIdOrderBySortOrderAsc(subjectId).stream().anyMatch(x->Objects.equals(x.parentTopicId,id)) && !r.lessonType().equals("Subject Topic")) throw ApiException.bad("Chương đang chứa bài học nên không thể đổi loại.");
        l.subjectId=subjectId; l.name=r.name(); l.lessonType=r.lessonType(); l.sortOrder=r.sortOrder(); l.packageId=r.packageId(); l.parentTopicId=r.parentTopicId(); l.quizId=r.quizId();
        l.videoUrl=r.lessonType().equals("Lesson")?Rules.url(r.videoUrl()):null; l.htmlContent=r.lessonType().equals("Lesson")?Rules.html(r.htmlContent()):null; l.status=r.status(); return db.lessons.save(l);
    }
    public SubjectDimension saveDimension(Long subjectId,Long id,DimensionInput r) {
        rules.assigned(subjectId); rules.setting(r.typeSettingId(),"Subject Dimension");
        var d=id==null?new SubjectDimension():db.dimensions.findById(id).orElseThrow(ApiException::missing);
        if(id!=null&&!d.subjectId.equals(subjectId)) throw ApiException.forbidden();
        d.subjectId=subjectId; d.typeSettingId=r.typeSettingId(); d.name=r.name(); d.description=r.description(); return db.dimensions.save(d);
    }
    public Object metadata() {
        rules.role("Admin","Author"); return Rules.map("settings",db.settings.findAll(),"authors",db.users.findAll().stream().filter(u->u.roleCode.equals("Author")&&u.status.equals("Active")).map(u->Rules.map("userId",u.userId,"fullName",u.fullName)).toList());
    }
}
