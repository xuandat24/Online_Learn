package vn.onlinelearn.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.onlinelearn.dto.Requests.*;
import vn.onlinelearn.service.*;

@RestController
public class CourseController {
    private final CatalogService catalog; private final CourseService course;
    public CourseController(CatalogService catalog,CourseService course) { this.catalog=catalog; this.course=course; }
    @GetMapping("/api/public/home") public Object home() { return catalog.home(); }
    @GetMapping("/api/public/settings") public Object settings(@RequestParam(required=false) String type) { return catalog.settings(type); }
    @GetMapping("/api/public/courses") public Object courses(@RequestParam(required=false) String q,@RequestParam(required=false) Long category,@RequestParam(defaultValue="false") boolean featured,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="9") int size,@RequestParam(defaultValue="newest") String sort) { return catalog.courses(q,category,featured,page,size,sort); }
    @GetMapping("/api/public/courses/{id}") public Object course(@PathVariable Long id) { return catalog.course(id); }
    @GetMapping("/api/public/posts") public Object posts(@RequestParam(required=false) String q,@RequestParam(required=false) Long category,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="9") int size) { return catalog.posts(q,category,page,size); }
    @GetMapping("/api/public/posts/{id}") public Object post(@PathVariable Long id) { return catalog.post(id); }
    @GetMapping("/api/manage/metadata") public Object metadata() { return course.metadata(); }
    @GetMapping("/api/manage/courses") public Object list(@RequestParam(required=false) String q,@RequestParam(required=false) String status,@RequestParam(required=false) Long category,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return course.list(q,status,category,page,size); }
    @GetMapping("/api/manage/courses/{id}") public Object detail(@PathVariable Long id) { return course.detail(id); }
    @PostMapping("/api/manage/courses") public Object create(@Valid @RequestBody Course r) { return course.save(null,r); }
    @PutMapping("/api/manage/courses/{id}") public Object update(@PathVariable Long id,@Valid @RequestBody Course r) { return course.save(id,r); }
    @PostMapping("/api/manage/courses/{id}/packages") public Object addPackage(@PathVariable Long id,@Valid @RequestBody PackageInput r) { return course.savePackage(id,null,r); }
    @PutMapping("/api/manage/courses/{id}/packages/{pid}") public Object packageUpdate(@PathVariable Long id,@PathVariable Long pid,@Valid @RequestBody PackageInput r) { return course.savePackage(id,pid,r); }
    @PostMapping("/api/manage/courses/{id}/lessons") public Object addLesson(@PathVariable Long id,@Valid @RequestBody LessonInput r) { return course.saveLesson(id,null,r); }
    @PutMapping("/api/manage/courses/{id}/lessons/{lid}") public Object lessonUpdate(@PathVariable Long id,@PathVariable Long lid,@Valid @RequestBody LessonInput r) { return course.saveLesson(id,lid,r); }
    @PostMapping("/api/manage/courses/{id}/dimensions") public Object dimension(@PathVariable Long id,@Valid @RequestBody DimensionInput r) { return course.saveDimension(id,null,r); }
    @PutMapping("/api/manage/courses/{id}/dimensions/{did}") public Object dimensionUpdate(@PathVariable Long id,@PathVariable Long did,@Valid @RequestBody DimensionInput r) { return course.saveDimension(id,did,r); }
}
