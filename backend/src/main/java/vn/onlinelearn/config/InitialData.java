package vn.onlinelearn.config;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.service.Rules;

/** Explicit opt-in local fixtures. No payment/access is fabricated. */
@Component
public class InitialData implements ApplicationRunner {
    @Autowired Data db; @Autowired PasswordEncoder encoder;
    @Value("${app.demo-seed}") boolean demo;
    @Value("${app.demo-password}") String demoPassword;
    @Value("${BOOTSTRAP_ADMIN_EMAIL:}") String adminEmail;
    @Value("${BOOTSTRAP_ADMIN_PASSWORD:}") String adminPassword;
    private User account(String name,String email,String role,String password){
        return db.users.findByEmail(email).orElseGet(()->{var u=new User();u.fullName=name;u.email=email;u.roleCode=role;u.passwordHash=encoder.encode(password);u.emailVerifiedAt=Rules.now();return db.users.saveAndFlush(u);});
    }
    private Setting setting(String type,String value){return db.settings.findByTypeAndValue(type,value).orElseGet(()->{var s=new Setting();s.type=type;s.value=value;s.sortOrder=0;return db.settings.saveAndFlush(s);});}
    @Override @Transactional public void run(ApplicationArguments args){
        if(db.users.count()==0&&!adminEmail.isBlank()&&!adminPassword.isBlank()){
            Rules.password(adminPassword,adminPassword);account("OnlineLearn Administrator",Rules.email(adminEmail),"Admin",adminPassword);
        }
        if(!demo)return;
        Rules.password(demoPassword,demoPassword);
        for(String role:Rules.ROLES)account("[DEMO] "+role,role.toLowerCase(Locale.ROOT)+"@onlinelearn.test",role,demoPassword);
        var author=db.users.findByEmail("author@onlinelearn.test").orElseThrow();
        var category=setting("Subject Category","Lập trình");setting("Subject Category","Thiết kế");setting("Subject Category","Kỹ năng nghề nghiệp");
        var level=setting("Question Level","Cơ bản");var quizType=setting("Test Type","Kiểm tra bài học");setting("Subject Dimension","Nhóm kiến thức");var postCategory=setting("Post Category","Phương pháp học");
        if(db.subjects.existsByNameIgnoreCase("[DEMO] Java Spring Boot từ nền tảng"))return;
        var s=new Subject();s.name="[DEMO] Java Spring Boot từ nền tảng";s.ownerUserId=author.userId;s.categorySettingId=category.settingId;s.tagline="Dữ liệu trình diễn — xây dựng API đầu tiên theo kiến trúc MVC.";s.description="<h2>Bắt đầu hành trình Backend</h2><p>Khóa học minh họa phục vụ kiểm thử. Bạn sẽ tìm hiểu Controller, Service và Repository, sau đó tự đánh giá bằng bài kiểm tra.</p>";s.featured=true;s.publishStatus="Published";db.subjects.saveAndFlush(s);
        var p=new PricePackage();p.subjectId=s.subjectId;p.name="[DEMO] Gói 3 tháng";p.durationMonths=3;p.listPrice=new BigDecimal("299000");p.salePrice=new BigDecimal("199000");db.packages.save(p);
        var l=new Lesson();l.subjectId=s.subjectId;l.name="[DEMO] Kiến trúc MVC trong Spring Boot";l.lessonType="Lesson";l.sortOrder=1;l.htmlContent="<h2>Luồng xử lý một request</h2><p>Controller tiếp nhận yêu cầu HTTP và xác thực đầu vào. Service xử lý quy tắc nghiệp vụ. Repository truy vấn dữ liệu qua JPA.</p><p>Ví dụ: CourseController → CourseService → SubjectRepository → SQL Server.</p>";db.lessons.save(l);
        var q=new Question();q.subjectId=s.subjectId;q.levelSettingId=level.settingId;q.content="Lớp nào chịu trách nhiệm xử lý quy tắc nghiệp vụ?";q.explanation="Service điều phối và thực thi quy tắc nghiệp vụ, còn Repository làm việc với dữ liệu.";db.questions.saveAndFlush(q);
        for(int i=0;i<3;i++){var o=new QuestionOption();o.questionId=q.questionId;o.content=List.of("Controller","Service","Repository").get(i);o.isCorrect=i==1;o.sortOrder=i;db.options.save(o);}
        var quiz=new Quiz();quiz.subjectId=s.subjectId;quiz.name="[DEMO] Hiểu về MVC";quiz.quizTypeSettingId=quizType.settingId;quiz.levelSettingId=level.settingId;quiz.durationMinutes=5;quiz.passRate=new BigDecimal("70");quiz.questionIds=new ArrayList<>(List.of(q.questionId));db.quizzes.saveAndFlush(quiz);
        var test=new Lesson();test.subjectId=s.subjectId;test.name="[DEMO] Kiểm tra kiến thức MVC";test.lessonType="Quiz";test.sortOrder=2;test.quizId=quiz.quizId;db.lessons.save(test);
        var post=new Post();post.authorUserId=author.userId;post.categorySettingId=postCategory.settingId;post.title="[DEMO] Học một kỹ năng mới theo từng bước nhỏ";post.briefInfo="Nội dung mẫu dùng để kiểm tra trang bài viết.";post.body="<h2>Chọn một mục tiêu cụ thể</h2><p>Dành thời gian đều đặn cho một nội dung nhỏ, áp dụng vào bài tập và tự kiểm tra kiến thức sau mỗi chương.</p>";post.status="Visible";db.posts.save(post);
    }
}
