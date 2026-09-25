package com.onlinelearn.database;

import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.SettingType;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettingSeeder {

    private final RoleRepository roleRepository;
    private final SubjectCategoryRepository subjectCategoryRepository;
    private final QuestionLevelRepository questionLevelRepository;
    private final LessonTypeRepository lessonTypeRepository;
    private final DimensionTypeRepository dimensionTypeRepository;
    private final TestTypeRepository testTypeRepository;
    private final PostCategoryRepository postCategoryRepository;
    private final SettingRepository settingRepository;

    @Transactional
    public void seedSettings() {
        log.info("--> Seeding Settings and Metadata...");

        seedRoles();
        seedSubjectCategories();
        seedQuestionLevels();
        seedLessonTypes();
        seedDimensionTypes();
        seedTestTypes();
        seedPostCategories();
        seedGeneralSettings();

        log.info("✓ Settings seeded successfully.");
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    Role.builder().code("ADMIN").name("System Administrator").description("Toàn quyền quản trị hệ thống").build(),
                    Role.builder().code("CUSTOMER").name("Customer / Learner").description("Học viên tham gia khóa học").build(),
                    Role.builder().code("EXPERT").name("Subject Expert").description("Chuyên gia quản lý nội dung môn học và câu hỏi").build(),
                    Role.builder().code("SALE").name("Sale Staff").description("Nhân viên tư vấn và quản lý đơn đăng ký").build(),
                    Role.builder().code("MARKETING").name("Marketing Staff").description("Nhân viên quản lý tiếp thị, bài viết và banner").build()
            ));
        }
    }

    private void seedSubjectCategories() {
        if (subjectCategoryRepository.count() == 0) {
            subjectCategoryRepository.saveAll(List.of(
                    SubjectCategory.builder().name("Lập trình").description("Các khóa học lập trình Web, Mobile, Java, Backend").status(true).build(),
                    SubjectCategory.builder().name("Ngoại ngữ").description("Tiếng Anh giao tiếp, TOEIC, IELTS cho sinh viên và người đi làm").status(true).build(),
                    SubjectCategory.builder().name("Kỹ năng mềm").description("Kỹ năng tin học văn phòng, thuyết trình, làm việc nhóm").status(true).build(),
                    SubjectCategory.builder().name("Thiết kế").description("UI/UX Design, Figma, Photoshop căn bản và nâng cao").status(true).build()
            ));
        }
    }

    private void seedQuestionLevels() {
        if (questionLevelRepository.count() == 0) {
            questionLevelRepository.saveAll(List.of(
                    QuestionLevel.builder().code("EASY").name("Dễ").description("Mức độ cơ bản, kiểm tra kiến thức nhận biết").status(true).build(),
                    QuestionLevel.builder().code("MEDIUM").name("Trung bình").description("Mức độ thông hiểu, áp dụng kiến thức vào bài tập").status(true).build(),
                    QuestionLevel.builder().code("HARD").name("Khó").description("Mức độ vận dụng cao, phân tích và tư duy logic").status(true).build()
            ));
        }
    }

    private void seedLessonTypes() {
        if (lessonTypeRepository.count() == 0) {
            lessonTypeRepository.saveAll(List.of(
                    LessonType.builder().code("SUBJECT_TOPIC").name("Chương mục / Topic").description("Chương chứa các bài học và bài kiểm tra").status(true).build(),
                    LessonType.builder().code("LESSON").name("Bài học").description("Bài học video hoặc nội dung lý thuyết HTML").status(true).build(),
                    LessonType.builder().code("QUIZ").name("Bài kiểm tra / Quiz").description("Bài trắc nghiệm đánh giá kiến thức").status(true).build()
            ));
        }
    }

    private void seedDimensionTypes() {
        if (dimensionTypeRepository.count() == 0) {
            dimensionTypeRepository.saveAll(List.of(
                    DimensionType.builder().name("Domain Knowledge").description("Kiến thức lý thuyết và nền tảng chuyên môn").status(true).build(),
                    DimensionType.builder().name("Skill & Practice").description("Kỹ năng thực hành và vận dụng thực tế").status(true).build()
            ));
        }
    }

    private void seedTestTypes() {
        if (testTypeRepository.count() == 0) {
            testTypeRepository.saveAll(List.of(
                    TestType.builder().code("SIMULATION").name("Thi thử / Simulation").description("Bài thi mô phỏng tính giờ và chấm điểm chuẩn").status(true).build(),
                    TestType.builder().code("PRACTICE").name("Luyện tập / Practice").description("Luyện tập tự do ôn luyện kiến thức").status(true).build()
            ));
        }
    }

    private void seedPostCategories() {
        if (postCategoryRepository.count() == 0) {
            postCategoryRepository.saveAll(List.of(
                    PostCategory.builder().name("Tin tức công nghệ").description("Cập nhật xu hướng công nghệ mới nhất").status(true).build(),
                    PostCategory.builder().name("Khuyến mãi & Học bổng").description("Các chương trình ưu đãi học phí và sự kiện").status(true).build(),
                    PostCategory.builder().name("Góc học tập").description("Kinh nghiệm học lập trình và phát triển sự nghiệp").status(true).build()
            ));
        }
    }

    private void seedGeneralSettings() {
        if (settingRepository.count() == 0) {
            settingRepository.saveAll(List.of(
                    Setting.builder().type(SettingType.SYSTEM_CONFIG).code("APP_NAME").value("Online Learning System").description("Tên hệ thống").orderNum(1).status(true).build(),
                    Setting.builder().type(SettingType.SYSTEM_CONFIG).code("HOTLINE").value("1900 1234").description("Hotline hỗ trợ học viên").orderNum(2).status(true).build(),
                    Setting.builder().type(SettingType.SYSTEM_CONFIG).code("CONTACT_EMAIL").value("support@onlinelearn.com").description("Email hỗ trợ").orderNum(3).status(true).build(),
                    Setting.builder().type(SettingType.SUBJECT_CATEGORY).code("DEV").value("Lập trình").description("Danh mục lập trình").orderNum(1).status(true).build(),
                    Setting.builder().type(SettingType.SUBJECT_CATEGORY).code("LANG").value("Ngoại ngữ").description("Danh mục ngoại ngữ").orderNum(2).status(true).build(),
                    Setting.builder().type(SettingType.SUBJECT_CATEGORY).code("SOFT_SKILL").value("Kỹ năng mềm").description("Danh mục kỹ năng").orderNum(3).status(true).build()
            ));
        }
    }
}
