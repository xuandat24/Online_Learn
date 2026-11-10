package vn.onlinelearn.service;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.dto.Requests.*;
import vn.onlinelearn.exception.ApiException;
@Service @Transactional
public class AdministrationService {
    @Autowired Data db; @Autowired Rules rules; @Autowired AuthService auth;
    public Object users(){rules.role("Admin");return db.users.findAll();}
    public Object invite(UserCreate r){rules.role("Admin");if(!Rules.ROLES.contains(r.roleCode()))throw ApiException.bad("Vai trò không hợp lệ.");if(db.users.existsByEmail(Rules.email(r.email())))throw ApiException.conflict("Email đã tồn tại.");var u=new User();u.email=Rules.email(r.email());u.fullName=r.fullName();u.roleCode=r.roleCode();db.users.saveAndFlush(u);auth.issueLink(u,"RESET_PASSWORD");return u;}
    public Object user(Long id,UserUpdate r){var actor=rules.role("Admin");if(!Rules.ROLES.contains(r.roleCode()))throw ApiException.bad("Vai trò không hợp lệ.");var u=db.users.lockById(id).orElseThrow(ApiException::missing);if(actor.id().equals(id)&&(!r.roleCode().equals("Admin")||!r.status().equals("Active")))throw ApiException.bad("Không thể tự vô hiệu hóa quyền quản trị.");if(!u.roleCode.equals(r.roleCode())&&(!db.access.findByUserId(id).isEmpty()||db.subjects.findAll().stream().anyMatch(s->s.ownerUserId.equals(id))))throw ApiException.conflict("Tài khoản còn khóa học hoặc quyền học đang liên kết.");u.roleCode=r.roleCode();u.status=r.status();u.tokenVersion++;return u;}
    public Object settings(){rules.role("Admin","Marketing");return db.settings.findAll();}
    public Object setting(Long id,SettingInput r){rules.role("Admin");if(r.type().equals("Role")&&!Rules.ROLES.contains(r.value()))throw ApiException.bad("Hệ thống chỉ có 6 vai trò tài khoản; Guest không có tài khoản.");var s=id==null?new Setting():db.settings.findById(id).orElseThrow(ApiException::missing);if(id!=null&&!s.type.equals(r.type()))throw ApiException.bad("Không thể thay đổi loại thiết lập.");s.type=r.type();s.value=r.value();s.sortOrder=r.sortOrder();s.description=r.description();s.status=r.status();return db.settings.save(s);}
    public Object posts(){rules.role("Admin","Marketing");return db.posts.findAll();}
    public Object post(Long id,PostInput r){var actor=rules.role("Admin","Marketing");rules.setting(r.categorySettingId(),"Post Category");var p=id==null?new Post():db.posts.findById(id).orElseThrow(ApiException::missing);if(id==null)p.authorUserId=actor.id();p.title=r.title();p.categorySettingId=r.categorySettingId();p.thumbnailUrl=Rules.url(r.thumbnailUrl());p.briefInfo=r.briefInfo();p.body=Rules.html(r.body());p.featured=r.featured();p.status=r.status();p.updatedAt=Rules.now();return db.posts.save(p);}
    public Object sliders(){rules.role("Admin","Marketing");return db.sliders.findAll();}
    public Object slider(Long id,SliderInput r){rules.role("Admin","Marketing");var s=id==null?new Slider():db.sliders.findById(id).orElseThrow(ApiException::missing);s.title=r.title();s.imageUrl=Rules.url(r.imageUrl());s.backlink=Rules.url(r.backlink());s.status=r.status();s.notes=r.notes();return db.sliders.save(s);}
    public Object dashboard(){var actor=rules.role("Admin","Sale","Marketing");var registrations=db.registrations.findAll();var result=Rules.map("courses",db.subjects.count(),"publishedCourses",db.subjects.findAll().stream().filter(s->s.publishStatus.equals("Published")).count(),"posts",db.posts.count());if(!actor.role().equals("Marketing")){result.put("registrations",registrations.size());result.put("paidRegistrations",registrations.stream().filter(r->r.status.equals("Paid")).count());result.put("revenue",registrations.stream().filter(r->r.status.equals("Paid")).map(r->r.totalCost).reduce(BigDecimal.ZERO,BigDecimal::add));}return result;}
}
