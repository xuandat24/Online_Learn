package vn.onlinelearn.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import vn.onlinelearn.service.PaymentService;

@RestController @RequestMapping("/api/payments/sepay")
public class PaymentController {
    private final PaymentService service;
    @Value("${app.frontend-url}") String frontend;
    public PaymentController(PaymentService service){this.service=service;}
    @PostMapping("/ipn") public Object ipn(@RequestHeader(value="X-Secret-Key",required=false) String key,@RequestBody JsonNode body){return service.ipn(key,body);}
    // No browser parameter is trusted to update payment state.
    @RequestMapping(value={"/success","/error","/cancel"},method={RequestMethod.GET,RequestMethod.POST})
    public ResponseEntity<Void> callback(){return ResponseEntity.status(303).header("Location",frontend.replaceAll("/+$","")+"/payment/return").build();}
}
