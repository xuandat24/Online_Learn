package vn.onlinelearn;

import java.util.*;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.fasterxml.jackson.databind.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.*;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import vn.onlinelearn.entity.*;
import vn.onlinelearn.repository.Data;
import vn.onlinelearn.security.JwtService;
import vn.onlinelearn.service.Rules;

/** Isolated test fixtures and signed test callbacks; no real gateway transaction. */
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:business_test;MODE=MSSQLServer;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.flyway.enabled=false","spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true","app.jwt-secret=isolated-business-test-secret-at-least-32-bytes","app.mail-mode=disabled","app.sepay.merchant-id=TESTONLY","app.sepay.secret-key=isolated-test-checkout-key","app.sepay.ipn-secret-key=isolated-test-ipn-key"})
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class BusinessFlowIntegrationTest {
    @Autowired MockMvc mvc; @Autowired Data db; @Autowired JwtService jwt; @Autowired ObjectMapper json;
    User user(String role,String email){var u=new User();u.email=email;u.fullName="TEST ONLY "+role;u.roleCode=role;u.emailVerifiedAt=Rules.now();return db.users.saveAndFlush(u);}
    Setting setting(String type,String value){var s=new Setting();s.type=type;s.value=value;return db.settings.saveAndFlush(s);}
    JsonNode call(String method,String path,String token,Object body,int status) throws Exception {
        var request=switch(method){case "POST"->post(path);case "PUT"->put(path);default->get(path);};
        if(token!=null)request.header("Authorization","Bearer "+token);
        if(body!=null)request.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
        var result=mvc.perform(request).andExpect(status().is(status)).andReturn();
        return result.getResponse().getContentAsString().isBlank()?json.nullNode():json.readTree(result.getResponse().getContentAsString());
    }
    void sepay(Object body,int expected) throws Exception {
        mvc.perform(post("/api/payments/sepay/ipn").header("X-Secret-Key","isolated-test-ipn-key").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body))).andExpect(status().is(expected));
    }
    @Test void fullBusinessFlowAndAuthorization() throws Exception {
        var admin=user("Admin","admin@integration.test");var author=user("Author","author@integration.test");var outsider=user("Author","outsider@integration.test");
        var customer=user("Customer","customer@integration.test");var staff=user("Staff","staff@integration.test");
        String adminJwt=jwt.issue(admin),authorJwt=jwt.issue(author),customerJwt=jwt.issue(customer);
        var category=setting("Subject Category","Integration course category");var level=setting("Question Level","Integration level");var type=setting("Test Type","Integration quiz");
        var courseInput=Rules.map("name","TEST ONLY: End-to-end course","ownerUserId",author.userId,"categorySettingId",category.settingId,"tagline","Integration fixture","description","<p>Learning fixture</p>","featured",true,"publishStatus","Published");
        long course=call("POST","/api/manage/courses",adminJwt,courseInput,200).get("subjectId").asLong();
        call("GET","/api/manage/courses/"+course+"/questions/import-template",authorJwt,null,200);
        long questionsBefore=db.questions.count();
        var validImport=Rules.map("content","Valid batch entry","levelSettingId",level.settingId,"status","Active","options",List.of(Rules.map("content","A","correct",true),Rules.map("content","B","correct",false)));
        var invalidImport=Rules.map("content","Invalid batch entry","levelSettingId",level.settingId,"status","Active","options",List.of(Rules.map("content","A","correct",false),Rules.map("content","B","correct",false)));
        call("POST","/api/manage/courses/"+course+"/questions/import",authorJwt,Rules.map("questions",List.of(validImport,invalidImport)),400);
        assertEquals(questionsBefore,db.questions.count(),"A failed batch must roll back earlier questions");
        call("GET","/api/manage/courses/"+course,jwt.issue(outsider),null,403);
        call("GET","/api/admin/users",customerJwt,null,403);call("GET","/api/admin/users",jwt.issue(staff),null,403);
        var packageInput=Rules.map("name","Integration 3-month package","durationMonths",3,"listPrice",150000,"salePrice",100000,"status","Active");
        call("POST","/api/manage/courses/"+course+"/packages",authorJwt,packageInput,403);
        long pkg=call("POST","/api/manage/courses/"+course+"/packages",adminJwt,packageInput,200).get("packageId").asLong();
        var lessonInput=Rules.map("name","Integration lesson","lessonType","Lesson","sortOrder",1,"htmlContent","<p>Protected lesson content</p>","status","Active");
        long lesson=call("POST","/api/manage/courses/"+course+"/lessons",authorJwt,lessonInput,200).get("lessonId").asLong();
        long question=call("POST","/api/manage/courses/"+course+"/questions",authorJwt,Rules.map("content","2 + 2 = ?","levelSettingId",level.settingId,"status","Active","options",List.of(Rules.map("content","4","correct",true),Rules.map("content","5","correct",false))),200).get("questionId").asLong();
        long quiz=call("POST","/api/manage/courses/"+course+"/quizzes",authorJwt,Rules.map("name","Integration quiz","quizTypeSettingId",type.settingId,"durationMinutes",5,"passRate",70,"questionIds",List.of(question)),200).get("quizId").asLong();
        call("POST","/api/manage/courses/"+course+"/lessons",authorJwt,Rules.map("name","Quiz lesson","lessonType","Quiz","sortOrder",2,"quizId",quiz,"status","Active"),200);
        var publicCourse=call("GET","/api/public/courses/"+course,null,null,200);
        assertFalse(publicCourse.toString().contains("Protected lesson content"));
        call("GET","/api/learning/lessons/"+lesson,customerJwt,null,403);
        var registration=call("POST","/api/registrations",null,Rules.map("packageId",pkg,"fullName","TEST ONLY Guest","email",customer.email,"mobile","0900000000"),200);
        long registrationId=registration.path("registration").path("registrationId").asLong();String registrationToken=registration.path("token").asText();
        assertEquals("Submitted",registration.path("registration").path("status").asText());
        mvc.perform(post("/api/registrations/"+registrationId+"/payments").header("X-Registration-Token",registrationToken)).andExpect(status().isOk()).andExpect(jsonPath("$.checkoutUrl").isString());
        var payment=db.payments.findByRegistrationIdOrderByCreatedAtDesc(registrationId).get(0);
        mvc.perform(post("/api/registrations/"+registrationId+"/payments").header("X-Registration-Token",registrationToken).contentType(MediaType.APPLICATION_JSON).content("{\"amount\":1}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.fields.order_amount").value("100000")).andExpect(jsonPath("$.fields.payment_method").value("BANK_TRANSFER"));
        assertEquals(1,db.payments.findByRegistrationIdOrderByCreatedAtDesc(registrationId).size());
        call("GET","/api/learning/lessons/"+lesson,customerJwt,null,403);
        var order=Rules.map("order_invoice_number",payment.orderCode,"order_id","SEPAY-TEST","order_amount","100000.00","order_currency","VND","order_status","CAPTURED");
        var transaction=Rules.map("id","test-payment-001","transaction_id","provider-reference-001","transaction_amount","100000","transaction_currency","VND","transaction_status","APPROVED","transaction_type","PAYMENT","payment_method","BANK_TRANSFER");
        var ipn=Rules.map("timestamp",java.time.Instant.now().getEpochSecond(),"notification_type","ORDER_PAID","order",order,"transaction",transaction);
        String ipnPath="/api/payments/sepay/ipn";
        mvc.perform(post(ipnPath).contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(ipn))).andExpect(status().isUnauthorized());
        mvc.perform(post(ipnPath).header("X-Secret-Key","wrong").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(ipn))).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/payments/sepay/success").param("status","Paid")).andExpect(status().isSeeOther());
        assertEquals("Submitted",db.registrations.findById(registrationId).orElseThrow().status);
        order.put("order_amount","1");sepay(ipn,400);order.put("order_amount","100000.00");
        order.put("order_invoice_number","unknown");sepay(ipn,404);order.put("order_invoice_number",payment.orderCode);
        transaction.put("transaction_currency","USD");sepay(ipn,400);transaction.put("transaction_currency","VND");
        transaction.put("transaction_status","PENDING");sepay(ipn,400);transaction.put("transaction_status","APPROVED");
        ipn.put("timestamp",1);sepay(ipn,400);ipn.put("timestamp",java.time.Instant.now().getEpochSecond());
        assertEquals(0,db.access.findByUserId(customer.userId).size());
        Runnable deliver=()->{try{sepay(ipn,200);}catch(Exception e){throw new RuntimeException(e);}};
        java.util.concurrent.CompletableFuture.allOf(java.util.concurrent.CompletableFuture.runAsync(deliver),java.util.concurrent.CompletableFuture.runAsync(deliver)).join();
        sepay(ipn,200);
        transaction.put("id","different-payment");sepay(ipn,409);transaction.put("id","test-payment-001");
        assertEquals(1,db.access.findByUserId(customer.userId).size());assertEquals("Paid",db.registrations.findById(registrationId).orElseThrow().status);
        var second=call("POST","/api/registrations",null,Rules.map("packageId",pkg,"fullName","TEST ONLY duplicate reference","email",customer.email,"mobile","0900000000"),200);
        long secondId=second.path("registration").path("registrationId").asLong();
        mvc.perform(post("/api/registrations/"+secondId+"/payments").header("X-Registration-Token",second.path("token").asText())).andExpect(status().isOk());
        order.put("order_invoice_number",db.payments.findByRegistrationIdOrderByCreatedAtDesc(secondId).get(0).orderCode);sepay(ipn,409);
        assertEquals("Submitted",db.registrations.findById(secondId).orElseThrow().status);assertEquals(1,db.access.findByUserId(customer.userId).size());
        call("GET","/api/learning/lessons/"+lesson,customerJwt,null,200);
        call("POST","/api/learning/lessons/"+lesson+"/complete",customerJwt,null,200);
        var attempt=call("POST","/api/learning/quizzes/"+quiz+"/start",customerJwt,null,200);long attemptId=attempt.path("attempt").path("attemptId").asLong();
        assertFalse(attempt.toString().contains("isCorrect"));assertFalse(attempt.toString().contains("\"correct\""));
        long correct=db.options.findByQuestionIdOrderBySortOrderAsc(question).stream().filter(o->o.isCorrect).findFirst().orElseThrow().optionId;
        call("PUT","/api/attempts/"+attemptId+"/answers",customerJwt,Rules.map("questionId",question,"optionId",correct,"markedForReview",true),200);
        var result=call("POST","/api/attempts/"+attemptId+"/submit",customerJwt,null,200);assertEquals(100,result.path("attempt").path("score").asInt());
        call("PUT","/api/manage/courses/"+course+"/quizzes/"+quiz,authorJwt,Rules.map("name","Cannot edit attempted quiz","quizTypeSettingId",type.settingId,"durationMinutes",10,"passRate",70,"questionIds",List.of(question)),409);
        call("PUT","/api/attempts/"+attemptId+"/answers",customerJwt,Rules.map("questionId",question,"optionId",correct,"markedForReview",false),409);
        var other=user("Customer","other@integration.test");call("GET","/api/attempts/"+attemptId,jwt.issue(other),null,403);
        var access=db.access.findByUserId(customer.userId).get(0);access.validTo=Rules.now().minusSeconds(1);db.access.saveAndFlush(access);
        call("GET","/api/learning/lessons/"+lesson,customerJwt,null,403);
    }
}
