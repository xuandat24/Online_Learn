package vn.onlinelearn.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import vn.onlinelearn.service.AdministrationService;
import vn.onlinelearn.dto.Requests.*;
@RestController
public class AdministrationController {
    private final AdministrationService s;public AdministrationController(AdministrationService s){this.s=s;}
    @GetMapping("/api/admin/users") public Object users(){return s.users();}
    @PostMapping("/api/admin/users") public Object invite(@Valid @RequestBody UserCreate r){return s.invite(r);}
    @PutMapping("/api/admin/users/{id}") public Object user(@PathVariable Long id,@Valid @RequestBody UserUpdate r){return s.user(id,r);}
    @GetMapping({"/api/admin/settings","/api/marketing/settings"}) public Object settings(){return s.settings();}
    @PostMapping("/api/admin/settings") public Object setting(@Valid @RequestBody SettingInput r){return s.setting(null,r);}
    @PutMapping("/api/admin/settings/{id}") public Object setting(@PathVariable Long id,@Valid @RequestBody SettingInput r){return s.setting(id,r);}
    @GetMapping("/api/marketing/posts") public Object posts(){return s.posts();}
    @PostMapping("/api/marketing/posts") public Object post(@Valid @RequestBody PostInput r){return s.post(null,r);}
    @PutMapping("/api/marketing/posts/{id}") public Object post(@PathVariable Long id,@Valid @RequestBody PostInput r){return s.post(id,r);}
    @GetMapping("/api/marketing/sliders") public Object sliders(){return s.sliders();}
    @PostMapping("/api/marketing/sliders") public Object slider(@Valid @RequestBody SliderInput r){return s.slider(null,r);}
    @PutMapping("/api/marketing/sliders/{id}") public Object slider(@PathVariable Long id,@Valid @RequestBody SliderInput r){return s.slider(id,r);}
    @GetMapping("/api/dashboard/summary") public Object dashboard(){return s.dashboard();}
}
