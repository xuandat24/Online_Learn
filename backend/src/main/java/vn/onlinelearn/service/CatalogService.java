package vn.onlinelearn.service;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.exception.ApiException;

@Service @Transactional(readOnly=true)
public class CatalogService {
    @Autowired Data db;
    public Map<String,Object> courseCard(Subject s) {
        var price=db.packages.findBySubjectId(s.subjectId).stream().filter(p->p.status.equals("Active")).map(p->p.salePrice).min(BigDecimal::compareTo).orElse(null);
        return Rules.map("subjectId",s.subjectId,"name",s.name,"tagline",s.tagline,"thumbnailUrl",s.thumbnailUrl,"featured",s.featured,
            "category",db.settings.findById(s.categorySettingId).map(x->x.value).orElse(""),"categorySettingId",s.categorySettingId,
            "author",db.users.findById(s.ownerUserId).map(x->x.fullName).orElse(""),"price",price,
            "lessonCount",db.lessons.findBySubjectIdOrderBySortOrderAsc(s.subjectId).stream().filter(x->x.status.equals("Active")&&!x.lessonType.equals("Subject Topic")).count());
    }
    public Map<String,Object> courses(String q,Long category,boolean featured,int page,int size,String sort) {
        var items=db.subjects.findAll().stream().filter(s->s.publishStatus.equals("Published"))
            .filter(s->q==null || s.name.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)))
            .filter(s->category==null || category.equals(s.categorySettingId)).filter(s->!featured || s.featured)
            .sorted(Comparator.comparing((Subject s)->s.updatedAt).reversed()).map(this::courseCard).toList();
        if("price".equals(sort)) items=items.stream().sorted(Comparator.comparing(x->(BigDecimal)x.get("price"),Comparator.nullsLast(Comparator.naturalOrder()))).toList();
        return Rules.page(items,page,size);
    }
    public Map<String,Object> course(Long id) {
        var s=db.subjects.findById(id).filter(x->x.publishStatus.equals("Published")).orElseThrow(ApiException::missing);
        var result=new LinkedHashMap<>(courseCard(s)); result.put("description",s.description);
        result.put("packages",db.packages.findBySubjectId(id).stream().filter(x->x.status.equals("Active")).toList());
        result.put("lessons",db.lessons.findBySubjectIdOrderBySortOrderAsc(id).stream().filter(x->x.status.equals("Active"))
            .map(x->Rules.map("lessonId",x.lessonId,"name",x.name,"lessonType",x.lessonType,"sortOrder",x.sortOrder,"parentTopicId",x.parentTopicId,"packageId",x.packageId)).toList());
        return result;
    }
    public Object settings(String type) { return db.settings.findAll().stream().filter(x->x.status.equals("Active") && Set.of("Subject Category","Post Category").contains(x.type)).filter(x->type==null||x.type.equals(type)).sorted(Comparator.comparing(x->x.sortOrder==null?0:x.sortOrder)).toList(); }
    public Map<String,Object> postCard(Post p,boolean body) {
        var m=Rules.map("postId",p.postId,"title",p.title,"thumbnailUrl",p.thumbnailUrl,"briefInfo",p.briefInfo,"featured",p.featured,"updatedAt",p.updatedAt,
            "author",db.users.findById(p.authorUserId).map(x->x.fullName).orElse(""),"category",db.settings.findById(p.categorySettingId).map(x->x.value).orElse(""));
        if(body)m.put("body",p.body); return m;
    }
    public Object posts(String q,Long category,int page,int size) {
        return Rules.page(db.posts.findAll().stream().filter(x->x.status.equals("Visible"))
            .filter(x->q==null||x.title.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT)))
            .filter(x->category==null||category.equals(x.categorySettingId))
            .sorted(Comparator.comparing((Post x)->x.updatedAt).reversed()).map(x->postCard(x,false)).toList(),page,size);
    }
    public Object post(Long id) { return postCard(db.posts.findById(id).filter(x->x.status.equals("Visible")).orElseThrow(ApiException::missing),true); }
    public Object home() { return Rules.map("courses",courses(null,null,true,0,6,"newest").get("content"),"posts",((Map<?,?>)posts(null,null,0,3)).get("content"),
        "categories",settings("Subject Category"),"sliders",db.sliders.findAll().stream().filter(x->x.status.equals("Visible")).toList()); }
}
