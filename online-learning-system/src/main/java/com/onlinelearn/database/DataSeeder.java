package com.onlinelearn.database;

import com.onlinelearn.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SeedProperties seedProperties;
    private final UserRepository userRepository;
    private final SettingSeeder settingSeeder;
    private final UserSeeder userSeeder;
    private final SubjectSeeder subjectSeeder;
    private final QuestionSeeder questionSeeder;
    private final RegistrationSeeder registrationSeeder;

    @Override
    public void run(String... args) {
        if (!seedProperties.isSeedData()) {
            log.info("ℹ️ Data seeding is disabled via configuration (app.seed-data=false).");
            return;
        }

        if (userRepository.count() > 0) {
            log.info("ℹ️ Database already contains data (users count: {}). Skipping initial seeding.", userRepository.count());
            printLoginCredentials();
            return;
        }

        log.info("🚀 Starting demo database seeding process...");

        try {
            // 1. Settings (Roles, Categories, Question Levels, Lesson Types, Test Types, Dimension Types)
            settingSeeder.seedSettings();

            // 2. Users (Admin, Customer, Expert, Sale, Marketing)
            userSeeder.seedUsers();

            // 3. Subjects, Price Packages, Dimensions, Lessons
            subjectSeeder.seedSubjects();

            // 4. Questions, Answer Options, Quizzes, link with Lessons
            questionSeeder.seedQuestionsAndQuizzes();

            // 5. Registrations & Course Access
            registrationSeeder.seedRegistrations();

            printLoginCredentials();

        } catch (Exception e) {
            log.error("❌ Error occurred during data seeding: {}", e.getMessage(), e);
        }
    }

    private void printLoginCredentials() {
        System.out.println("""
                
                ====================================================================================
                ✅ Seed data completed.
                👉 Admin login:     admin@onlinelearn.com / Admin@123
                👉 Customer login:  customer@onlinelearn.com / Customer@123
                👉 Expert login:    expert@onlinelearn.com / Expert@123
                👉 Sale login:      sale@onlinelearn.com / Sale@123
                👉 Marketing login: marketing@onlinelearn.com / Marketing@123
                ====================================================================================
                """);
    }
}
