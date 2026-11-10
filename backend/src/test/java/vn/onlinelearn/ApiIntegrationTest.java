package vn.onlinelearn;
import org.junit.jupiter.api.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:onlinelearn_test;MODE=MSSQLServer;NON_KEYWORDS=VALUE;DB_CLOSE_DELAY=-1","spring.datasource.driver-class-name=org.h2.Driver","spring.datasource.username=sa","spring.datasource.password=","spring.flyway.enabled=false","spring.jpa.hibernate.ddl-auto=create-drop","spring.jpa.properties.hibernate.hbm2ddl.halt_on_error=true","app.jwt-secret=isolated-test-secret-at-least-32-bytes-long","app.mail-mode=disabled"})
@AutoConfigureMockMvc
@org.springframework.test.context.ActiveProfiles("test")
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired vn.onlinelearn.repository.MailOutboxRepository outbox;
    @Autowired com.fasterxml.jackson.databind.ObjectMapper json;
    @Test void publicCatalogAccessible() throws Exception {mvc.perform(get("/api/public/courses")).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));}
    @Test void managementRequiresAuthentication() throws Exception {mvc.perform(get("/api/admin/users")).andExpect(status().isUnauthorized());mvc.perform(get("/api/my/courses")).andExpect(status().isUnauthorized());}
    @Test void registrationCannotChooseAdminAndMustVerifyEmail() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"fullName\":\"Integration test\",\"email\":\"integration@example.test\",\"password\":\"TestPassword123!\",\"roleCode\":\"Admin\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"integration@example.test\",\"password\":\"TestPassword123!\"}")).andExpect(status().isForbidden());
    }
    @Test void malformedRegistrationRejected() throws Exception {mvc.perform(post("/api/registrations").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());}
    @Test void unsignedCallbackNeverAccepted() throws Exception {mvc.perform(get("/api/payments/vnpay/ipn").param("vnp_ResponseCode","00")).andExpect(status().isOk()).andExpect(jsonPath("$.RspCode").value("97"));}
    private String latestToken(String email){
        var mail=outbox.findAll().stream().filter(m->m.recipient.equals(email)).max(java.util.Comparator.comparing(m->m.mailId)).orElseThrow();
        return mail.body.split("\\?token=",2)[1].split("\\s",2)[0];
    }
    @Test void verificationResetAndLogoutTokensWorkEndToEnd() throws Exception {
        String email="auth-flow@example.test";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("fullName","AUTH TEST ONLY","email",email,"password","TestPassword123!")))).andExpect(status().isOk());
        String verification=latestToken(email);
        mvc.perform(post("/api/auth/verify").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("token",verification)))).andExpect(status().isOk());
        mvc.perform(post("/api/auth/verify").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("token",verification)))).andExpect(status().isBadRequest());
        var login=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("email",email,"password","TestPassword123!")))).andExpect(status().isOk()).andExpect(jsonPath("$.user.roleCode").value("Customer")).andReturn();
        String token=json.readTree(login.getResponse().getContentAsString()).get("token").asText();
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").header("Authorization","Bearer "+token)).andExpect(status().isOk());
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/forgot").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("email",email)))).andExpect(status().isOk());
        String reset=latestToken(email);String payload=json.writeValueAsString(java.util.Map.of("token",reset,"password","ResetPassword456!","confirmPassword","ResetPassword456!"));
        mvc.perform(post("/api/auth/reset").contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/reset").contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(java.util.Map.of("email",email,"password","ResetPassword456!")))).andExpect(status().isOk());
    }
}
