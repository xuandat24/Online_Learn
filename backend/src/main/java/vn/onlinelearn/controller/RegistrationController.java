package vn.onlinelearn.controller;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import vn.onlinelearn.service.RegistrationService;
import vn.onlinelearn.dto.Requests.RegistrationInput;
import vn.onlinelearn.dto.Requests.RegistrationEdit;
@RestController
public class RegistrationController {
    private final RegistrationService service;
    public RegistrationController(RegistrationService service) { this.service=service; }
    @PostMapping("/api/registrations") public Object create(@Valid @RequestBody RegistrationInput input) { return service.create(input); }
    @GetMapping("/api/registrations/{id}") public Object detail(@PathVariable Long id,@RequestHeader(value="X-Registration-Token",required=false) String token) { return service.detail(id,token); }
    @PostMapping("/api/registrations/{id}/payments") public Object pay(@PathVariable Long id,@RequestHeader(value="X-Registration-Token",required=false) String token,HttpServletRequest request) { return service.payment(id,token,request.getRemoteAddr()); }
    @PostMapping("/api/registrations/{id}/cancel") public Object cancel(@PathVariable Long id,@RequestHeader(value="X-Registration-Token",required=false) String token) { return service.cancel(id,token); }
    @GetMapping("/api/my/registrations") public Object mine() { return service.mine(); }
    @GetMapping("/api/sales/registrations") public Object sales(@RequestParam(required=false) String q,@RequestParam(required=false) String status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) { return service.sales(q,status,page,size); }
    @GetMapping("/api/payments/vnpay/ipn") public Object callback(@RequestParam Map<String,String> values) { return service.callback(values); }
    @PostMapping("/api/sales/registrations/{id}/recheck") public Object recheck(@PathVariable Long id,HttpServletRequest request){return service.recheck(id,request.getRemoteAddr());}
    @PutMapping("/api/registrations/{id}") public Object edit(@PathVariable Long id,@Valid @RequestBody RegistrationEdit input){return service.edit(id,input);}
}
