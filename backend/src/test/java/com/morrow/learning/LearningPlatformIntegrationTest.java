package com.morrow.learning;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:morrow;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "management.health.mail.enabled=false",
        "app.admin.email=admin@example.com",
        "app.admin.password=adminPassword123",
        "app.admin.full-name=Test Admin"
})
@AutoConfigureMockMvc
@Transactional
class LearningPlatformIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

        @MockitoBean
        private JavaMailSender mailSender;

    @Test
        void customerCannotBypassRegistrationPaymentToCreateCourseAccess() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(6)));

        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Taylor Learner","email":"taylor@example.com","password":"learning123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andReturn();

        JsonNode body = objectMapper.readTree(registration.getResponse().getContentAsString());
        String token = body.get("token").asText();

        mockMvc.perform(post("/api/enrollments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/enrollments/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));

        mockMvc.perform(get("/api/learning/courses/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousVisitorCannotEnroll() throws Exception {
        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void guestCanSubmitRegistrationWithoutCreatingAnAccount() throws Exception {
        MvcResult catalog = mockMvc.perform(get("/api/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].pricePackages", org.hamcrest.Matchers.hasSize(1)))
                .andReturn();
        JsonNode course = objectMapper.readTree(catalog.getResponse().getContentAsString()).get(0);
        long courseId = course.path("id").asLong();
        long packageId = course.path("pricePackages").get(0).path("id").asLong();

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":%d,"pricePackageId":%d,"fullName":"Guest Learner","email":"guest-registration@example.com","phone":"+1 415 555 0100"}
                                """.formatted(courseId, packageId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.email").value("guest-registration@example.com"));

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":%d,"pricePackageId":%d,"fullName":"Invalid","email":"not-an-email","phone":"bad"}
                                """.formatted(courseId, packageId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Existing Customer","email":"existing-registration@example.com","password":"learning123"}
                                """))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":%d,"pricePackageId":%d,"fullName":"Existing Customer","email":"existing-registration@example.com","phone":"+1 415 555 0100"}
                                """.formatted(courseId, packageId)))
                .andExpect(status().isConflict());
    }

    @Test
    void customerCanEditAndCancelOnlyOwnSubmittedRegistration() throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Customer One","email":"registration-owner@example.com","password":"learning123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode auth = objectMapper.readTree(registration.getResponse().getContentAsString());
        String token = auth.path("token").asText();
        JsonNode course = objectMapper.readTree(mockMvc.perform(get("/api/courses"))
                .andReturn().getResponse().getContentAsString()).get(0);
        long courseId = course.path("id").asLong();
        long packageId = course.path("pricePackages").get(0).path("id").asLong();

        MvcResult created = mockMvc.perform(post("/api/registrations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":%d,"pricePackageId":%d,"fullName":"Customer One","email":"registration-owner@example.com","phone":"+1 415 555 0100"}
                                """.formatted(courseId, packageId)))
                .andExpect(status().isCreated())
                .andReturn();
        long registrationId = objectMapper.readTree(created.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(put("/api/registrations/{id}", registrationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pricePackageId":%d,"fullName":"Customer One","email":"another-account@example.com","phone":"+1 415 555 0100"}
                                """.formatted(packageId)))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/registrations/{id}", registrationId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pricePackageId":%d,"fullName":"Customer Updated","email":"registration-owner@example.com","phone":"+1 415 555 0101"}
                                """.formatted(packageId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Customer Updated"));

        mockMvc.perform(delete("/api/registrations/{id}", registrationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/registrations/{id}", registrationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void saleApprovalCreatesAccountSendsTemporaryLoginAndGrantsAccess() throws Exception {
        JsonNode course = objectMapper.readTree(mockMvc.perform(get("/api/courses"))
                .andReturn().getResponse().getContentAsString()).get(0);
        long courseId = course.path("id").asLong();
        long packageId = course.path("pricePackages").get(0).path("id").asLong();
        MvcResult guestRegistration = mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":%d,"pricePackageId":%d,"fullName":"New Paid Learner","email":"new-paid-learner@example.com","phone":"+1 415 555 0102"}
                                """.formatted(courseId, packageId)))
                .andExpect(status().isCreated())
                .andReturn();
        long registrationId = objectMapper.readTree(guestRegistration.getResponse().getContentAsString())
                .path("id").asLong();

        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.com","password":"adminPassword123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString())
                .path("token").asText();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                "/api/sales/registrations/{id}/paid", registrationId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.loginEmailSent").value(true));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        org.mockito.Mockito.verify(mailSender).send(messageCaptor.capture());
        String body = messageCaptor.getValue().getText();
        java.util.regex.Matcher passwordMatch = java.util.regex.Pattern
                .compile("Temporary password: ([A-Za-z0-9]+)").matcher(body);
        org.junit.jupiter.api.Assertions.assertTrue(passwordMatch.find());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new-paid-learner@example.com","password":"%s"}
                                """.formatted(passwordMatch.group(1))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"));
    }

    @Test
    void onlyAdminCanAssignAnInternalRole() throws Exception {
        MvcResult registration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Role Test","email":"role-test@example.com","password":"learning123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode customer = objectMapper.readTree(registration.getResponse().getContentAsString());
        Long customerId = customer.path("user").path("id").asLong();
        String customerToken = customer.path("token").asText();

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.com","password":"adminPassword123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(login.getResponse().getContentAsString()).path("token").asText();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                "/api/admin/users/{id}/role", customerId)
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"EXPERT\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                "/api/admin/users/{id}/role", customerId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"EXPERT\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("EXPERT"));
    }

    @Test
    void expertSubjectAssignmentAndPaidLearnerQuizProgressAreEnforced() throws Exception {
        MvcResult expertRegistration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Assigned Expert","email":"assigned-expert@example.com","password":"learning123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode expertAuth = objectMapper.readTree(expertRegistration.getResponse().getContentAsString());
        long expertId = expertAuth.path("user").path("id").asLong();
        String expertToken = expertAuth.path("token").asText();

        MvcResult adminLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin@example.com","password":"adminPassword123"}
                                """))
                .andExpect(status().isOk())
                .andReturn();
        String adminToken = objectMapper.readTree(adminLogin.getResponse().getContentAsString())
                .path("token").asText();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                "/api/admin/users/{id}/role", expertId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"EXPERT\"}"))
                .andExpect(status().isOk());
        JsonNode subject = objectMapper.readTree(mockMvc.perform(get("/api/admin/subjects")
                        .header("Authorization", "Bearer " + adminToken))
                .andReturn().getResponse().getContentAsString()).get(0);
        long subjectId = subject.path("id").asLong();

        String coursePayload = """
                {"title":"Expert subject course","description":"A course managed by an assigned expert.","category":"Design","instructor":"Assigned Expert","subjectId":%d,"level":"Beginner","duration":"2h","price":25,"image":"","accent":"sage","published":true,"pricePackages":[{"name":"Thirty day access","price":25,"currency":"USD","accessDays":30,"published":true}]}
                """.formatted(subjectId);
        mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + expertToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(coursePayload))
                .andExpect(status().isForbidden());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                                "/api/admin/subjects/{subjectId}/experts/{expertId}", subjectId, expertId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated());

        MvcResult courseResponse = mockMvc.perform(post("/api/admin/courses")
                        .header("Authorization", "Bearer " + expertToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(coursePayload))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode createdCourse = objectMapper.readTree(courseResponse.getResponse().getContentAsString());
        long courseId = createdCourse.path("id").asLong();
        long packageId = createdCourse.path("pricePackages").get(0).path("id").asLong();

        MvcResult outsiderRegistration = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"No Access","email":"no-access@example.com","password":"learning123"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        String outsiderToken = objectMapper.readTree(outsiderRegistration.getResponse().getContentAsString())
                .path("token").asText();
        mockMvc.perform(get("/api/learning/courses/{courseId}", courseId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());

        MvcResult guestRegistration = mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"courseId":%d,"pricePackageId":%d,"fullName":"New Learner","email":"new-course-learner@example.com","phone":"+1 415 555 0110"}
                                """.formatted(courseId, packageId)))
                .andExpect(status().isCreated())
                .andReturn();
        long registrationId = objectMapper.readTree(guestRegistration.getResponse().getContentAsString())
                .path("id").asLong();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                "/api/sales/registrations/{id}/paid", registrationId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
        ArgumentCaptor<SimpleMailMessage> credentials = ArgumentCaptor.forClass(SimpleMailMessage.class);
        org.mockito.Mockito.verify(mailSender).send(credentials.capture());
        java.util.regex.Matcher password = java.util.regex.Pattern.compile("Temporary password: ([A-Za-z0-9]+)")
                .matcher(credentials.getValue().getText());
        org.junit.jupiter.api.Assertions.assertTrue(password.find());
        MvcResult learnerLogin = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new-course-learner@example.com","password":"%s"}
                                """.formatted(password.group(1))))
                .andExpect(status().isOk())
                .andReturn();
        String learnerToken = objectMapper.readTree(learnerLogin.getResponse().getContentAsString())
                .path("token").asText();
        mockMvc.perform(put("/api/registrations/{id}", registrationId)
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pricePackageId":%d,"fullName":"New Paid Learner","email":"new-course-learner@example.com","phone":"+1 415 555 0110"}
                                """.formatted(packageId)))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/api/registrations/{id}", registrationId)
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isConflict());

        MvcResult lessonResponse = mockMvc.perform(post("/api/admin/courses/{courseId}/lessons", courseId)
                        .header("Authorization", "Bearer " + expertToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Lesson one","summary":"A useful first lesson.","content":"Read and practice.","videoUrl":"https://example.com/lesson.mp4","displayOrder":1,"published":true}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long lessonId = objectMapper.readTree(lessonResponse.getResponse().getContentAsString()).path("id").asLong();

        MvcResult quizResponse = mockMvc.perform(post("/api/admin/courses/{courseId}/quizzes", courseId)
                        .header("Authorization", "Bearer " + expertToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"First check","description":"Check the key idea.","passingScore":50,"published":true,"questions":[{"prompt":"Which answer is right?","options":["First","Second"],"correctOptionIndex":1}]}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode quiz = objectMapper.readTree(quizResponse.getResponse().getContentAsString());
        long quizId = quiz.path("id").asLong();
        long questionId = quiz.path("questions").get(0).path("id").asLong();

        mockMvc.perform(get("/api/learning/courses/{courseId}", courseId)
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessons[0].completed").value(false));
        mockMvc.perform(get("/api/learning/quizzes/{quizId}", quizId)
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].correctOptionIndex").doesNotExist());

        MvcResult attemptResponse = mockMvc.perform(post("/api/learning/quizzes/{quizId}/attempts", quizId)
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andReturn();
        long attemptId = objectMapper.readTree(attemptResponse.getResponse().getContentAsString()).path("id").asLong();
        mockMvc.perform(post("/api/learning/attempts/{attemptId}/submit", attemptId)
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isConflict());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(
                                "/api/learning/attempts/{attemptId}/answers/{questionId}", attemptId, questionId)
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"selectedOptionIndex\":1}"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/learning/attempts/{attemptId}", attemptId)
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answers[0].selectedOptionIndex").value(1));
        mockMvc.perform(post("/api/learning/quizzes/{quizId}/attempts", quizId)
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attemptId));

        mockMvc.perform(put("/api/admin/courses/{courseId}/quizzes/{quizId}", courseId, quizId)
                        .header("Authorization", "Bearer " + expertToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Edited quiz","description":"Should be locked.","passingScore":50,"published":true,"questions":[{"prompt":"Changed?","options":["No","Yes"],"correctOptionIndex":0}]}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/learning/lessons/{lessonId}/progress", lessonId)
                        .header("Authorization", "Bearer " + learnerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"completed\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
        mockMvc.perform(get("/api/learning/courses/{courseId}", courseId)
                        .header("Authorization", "Bearer " + learnerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lessons[0].completed").value(true));
    }
}