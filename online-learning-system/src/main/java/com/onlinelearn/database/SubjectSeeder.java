package com.onlinelearn.database;

import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.LessonStatus;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import com.onlinelearn.entity.enums.PackageStatus;
import com.onlinelearn.entity.enums.SubjectStatus;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubjectSeeder {

    private final SubjectRepository subjectRepository;
    private final SubjectCategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final PricePackageRepository pricePackageRepository;
    private final SubjectDimensionRepository dimensionRepository;
    private final DimensionTypeRepository dimensionTypeRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public void seedSubjects() {
        log.info("--> Seeding Subjects, Packages, Dimensions, and Lessons...");

        if (subjectRepository.count() > 0) {
            log.info("Subjects already seeded, skipping.");
            return;
        }

        SubjectCategory devCat = categoryRepository.findByName("Lập trình")
                .orElse(categoryRepository.findAll().get(0));
        SubjectCategory langCat = categoryRepository.findByName("Ngoại ngữ")
                .orElse(categoryRepository.findAll().get(0));
        SubjectCategory softCat = categoryRepository.findByName("Kỹ năng mềm")
                .orElse(categoryRepository.findAll().get(0));

        User expertUser = userRepository.findByEmail("expert@onlinelearn.com")
                .orElse(userRepository.findByEmail("admin@onlinelearn.com").orElse(null));
        User adminUser = userRepository.findByEmail("admin@onlinelearn.com").orElse(null);

        DimensionType domainType = dimensionTypeRepository.findByName("Domain Knowledge").orElse(null);
        DimensionType skillType = dimensionTypeRepository.findByName("Skill & Practice").orElse(null);

        // ==========================================
        // 1. JAVA CORE CƠ BẢN
        // ==========================================
        Subject javaSubject = Subject.builder()
                .name("Java Core cơ bản")
                .category(devCat)
                .owner(expertUser)
                .status(SubjectStatus.PUBLISHED)
                .featured(true)
                .thumbnail("https://picsum.photos/seed/javacore101/400/250")
                .briefInfo("Nền tảng vững chắc về ngôn ngữ lập trình Java từ cú pháp cơ bản đến OOP nâng cao.")
                .description("""
                        Khóa học Java Core cơ bản cung cấp kiến thức toàn diện cho người mới bắt đầu:
                        - Cú pháp cơ bản, biến, kiểu dữ liệu, toán tử, cấu trúc rẽ nhánh và vòng lặp.
                        - Lập trình hướng đối tượng (OOP): Encapsulation, Inheritance, Polymorphism, Abstraction.
                        - Collections Framework: List, Set, Map, ArrayList, HashMap.
                        - Exception handling, Java I/O Streams và thực hành làm ứng dụng quản lý mini.
                        """)
                .build();
        javaSubject = subjectRepository.save(javaSubject);

        // Java Packages
        pricePackageRepository.saveAll(List.of(
                PricePackage.builder()
                        .subject(javaSubject)
                        .packageName("Trọn gói")
                        .accessDuration(180)
                        .listPrice(BigDecimal.valueOf(1200000))
                        .salePrice(BigDecimal.valueOf(899000))
                        .description("Toàn quyền truy cập 180 ngày toàn bộ bài giảng, tài liệu và bài tập trắc nghiệm.")
                        .status(PackageStatus.ACTIVE)
                        .build(),
                PricePackage.builder()
                        .subject(javaSubject)
                        .packageName("Gói Tiết Kiệm 3 Tháng")
                        .accessDuration(90)
                        .listPrice(BigDecimal.valueOf(800000))
                        .salePrice(BigDecimal.valueOf(599000))
                        .description("Truy cập 90 ngày cho người cần ôn tập cấp tốc.")
                        .status(PackageStatus.ACTIVE)
                        .build()
        ));

        // Java Dimensions
        if (domainType != null && skillType != null) {
            dimensionRepository.saveAll(List.of(
                    SubjectDimension.builder().subject(javaSubject).type(domainType).name("Kiến thức nền tảng Java").description("Cú pháp, luồng điều khiển, kiểu dữ liệu").build(),
                    SubjectDimension.builder().subject(javaSubject).type(skillType).name("Tư duy lập trình OOP").description("Thiết kế lớp, kế thừa, trừu tượng").build()
            ));
        }

        // Java Lessons
        createJavaLessons(javaSubject);

        // ==========================================
        // 2. SPRING BOOT TỪ CƠ BẢN ĐẾN NÂNG CAO
        // ==========================================
        Subject springSubject = Subject.builder()
                .name("Spring Boot từ cơ bản đến nâng cao")
                .category(devCat)
                .owner(expertUser)
                .status(SubjectStatus.PUBLISHED)
                .featured(true)
                .thumbnail("https://picsum.photos/seed/springboot202/400/250")
                .briefInfo("Học xây dựng RESTful API và ứng dụng web enterprise hoàn chỉnh với Spring Boot 3 và Spring Security.")
                .description("""
                        Khóa học thực chiến Spring Boot 3 bao gồm:
                        - Spring Framework Core: IoC Container, Dependency Injection, Beans.
                        - Spring Boot Starter, Spring Data JPA, Hibernate ORM, MySQL.
                        - Spring Security 6: Xác thực, phân quyền theo vai trò (Role-based access).
                        - Xây dựng hệ thống REST API chuẩn chỉnh, Exception Handling, DTOs, Unit Testing.
                        """)
                .build();
        springSubject = subjectRepository.save(springSubject);

        // Spring Packages
        pricePackageRepository.saveAll(List.of(
                PricePackage.builder()
                        .subject(springSubject)
                        .packageName("Tiêu chuẩn")
                        .accessDuration(180)
                        .listPrice(BigDecimal.valueOf(1500000))
                        .salePrice(BigDecimal.valueOf(1199000))
                        .description("Truy cập đầy đủ bài giảng và bài tập trong 6 tháng.")
                        .status(PackageStatus.ACTIVE)
                        .build(),
                PricePackage.builder()
                        .subject(springSubject)
                        .packageName("VIP Hỗ trợ 1-1")
                        .accessDuration(365)
                        .listPrice(BigDecimal.valueOf(3000000))
                        .salePrice(BigDecimal.valueOf(2499000))
                        .description("Truy cập 1 năm kèm quyền lợi hỏi đáp trực tiếp 1-1 với chuyên gia giảng dạy.")
                        .status(PackageStatus.ACTIVE)
                        .build()
        ));

        // Spring Dimensions
        if (domainType != null && skillType != null) {
            dimensionRepository.saveAll(List.of(
                    SubjectDimension.builder().subject(springSubject).type(domainType).name("Kiến trúc Spring Framework").description("Hiểu cơ chế IoC, AOP, Bean lifecycle").build(),
                    SubjectDimension.builder().subject(springSubject).type(skillType).name("Lập trình REST API & Security").description("Xây dựng API, bảo mật và phân quyền").build()
            ));
        }

        // Spring Lessons
        createSpringBootLessons(springSubject);

        // ==========================================
        // 3. TIẾNG ANH GIAO TIẾP CƠ BẢN
        // ==========================================
        Subject englishSubject = Subject.builder()
                .name("Tiếng Anh giao tiếp cơ bản")
                .category(langCat)
                .owner(adminUser)
                .status(SubjectStatus.PUBLISHED)
                .featured(false)
                .thumbnail("https://picsum.photos/seed/englishcomm303/400/250")
                .briefInfo("Tự tin giao tiếp tiếng Anh hàng ngày cho người mới bắt đầu hoặc mất gốc.")
                .description("""
                        Khóa học thiết kế chuyên biệt cho học viên muốn cải thiện phản xạ:
                        - Chuẩn hóa ngữ âm theo bảng IPA quốc tế.
                        - 30 chủ đề giao tiếp quen thuộc: Chào hỏi, mua sắm, gọi món, du lịch, phỏng vấn.
                        - Ngữ pháp ứng dụng trong hội thoại thực tế không gây nhàm chán.
                        """)
                .build();
        englishSubject = subjectRepository.save(englishSubject);

        // English Packages
        pricePackageRepository.save(PricePackage.builder()
                .subject(englishSubject)
                .packageName("Trọn gói 6 tháng")
                .accessDuration(180)
                .listPrice(BigDecimal.valueOf(990000))
                .salePrice(BigDecimal.valueOf(699000))
                .description("Học tập linh hoạt trong 180 ngày với lộ trình giao tiếp chuẩn phản xạ.")
                .status(PackageStatus.ACTIVE)
                .build());

        if (domainType != null && skillType != null) {
            dimensionRepository.save(
                    SubjectDimension.builder().subject(englishSubject).type(skillType).name("Phản xạ hội thoại").description("Khả năng giao tiếp tự nhiên").build()
            );
        }

        createEnglishLessons(englishSubject);

        // ==========================================
        // 4. EXCEL VĂN PHÒNG CHO NGƯỜI MỚI
        // ==========================================
        Subject excelSubject = Subject.builder()
                .name("Excel văn phòng cho người mới")
                .category(softCat)
                .owner(adminUser)
                .status(SubjectStatus.PUBLISHED)
                .featured(false)
                .thumbnail("https://picsum.photos/seed/exceloffice404/400/250")
                .briefInfo("Làm chủ Microsoft Excel từ cơ bản đến xử lý dữ liệu báo cáo chuyên nghiệp.")
                .description("""
                        Nâng cao hiệu suất công việc văn phòng với Excel:
                        - Các hàm tính toán thông dụng: SUM, AVERAGE, IF, VLOOKUP, XLOOKUP, INDEX-MATCH.
                        - Xử lý, lọc và làm sạch bảng dữ liệu lớn.
                        - Thiết kế báo cáo PivotTable và biểu đồ trực quan chuyên nghiệp.
                        """)
                .build();
        excelSubject = subjectRepository.save(excelSubject);

        // Excel Packages
        pricePackageRepository.save(PricePackage.builder()
                .subject(excelSubject)
                .packageName("Khóa học trọn gói")
                .accessDuration(180)
                .listPrice(BigDecimal.valueOf(600000))
                .salePrice(BigDecimal.valueOf(450000))
                .description("Toàn bộ video bài giảng và file mẫu bài tập thực hành.")
                .status(PackageStatus.ACTIVE)
                .build());

        if (domainType != null && skillType != null) {
            dimensionRepository.save(
                    SubjectDimension.builder().subject(excelSubject).type(skillType).name("Kỹ năng xử lý số liệu").description("Thành thạo hàm và PivotTable").build()
            );
        }

        createExcelLessons(excelSubject);

        log.info("✓ Subjects and Lessons seeded successfully.");
    }

    private void createJavaLessons(Subject subject) {
        // Topic 1
        Lesson topic1 = lessonRepository.save(Lesson.builder()
                .subject(subject)
                .name("Chương 1: Giới thiệu & Cài đặt môi trường")
                .orderNum(1)
                .type(LessonTypeEnum.SUBJECT_TOPIC)
                .status(LessonStatus.ACTIVE)
                .build());

        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic1)
                .name("Bài 1: Cài đặt JDK 17/21 và cấu hình IntelliJ IDEA")
                .orderNum(1)
                .type(LessonTypeEnum.LESSON)
                .videoLink("https://www.youtube.com/watch?v=sample_java_setup")
                .htmlContent("<h3>Cài đặt JDK và cấu hình môi trường</h3><p>Trong bài này chúng ta sẽ tải OpenJDK và cấu hình biến môi trường JAVA_HOME...</p>")
                .status(LessonStatus.ACTIVE)
                .build());

        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic1)
                .name("Bài 2: Cú pháp cơ bản & Viết chương trình Hello World")
                .orderNum(2)
                .type(LessonTypeEnum.LESSON)
                .videoLink("https://www.youtube.com/watch?v=sample_java_hello")
                .htmlContent("<h3>Cú pháp cơ bản của ngôn ngữ Java</h3><p>Mỗi chương trình Java bắt đầu từ hàm main: <code>public static void main(String[] args)</code>...</p>")
                .status(LessonStatus.ACTIVE)
                .build());

        // Topic 2
        Lesson topic2 = lessonRepository.save(Lesson.builder()
                .subject(subject)
                .name("Chương 2: Lập trình hướng đối tượng (OOP)")
                .orderNum(2)
                .type(LessonTypeEnum.SUBJECT_TOPIC)
                .status(LessonStatus.ACTIVE)
                .build());

        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic2)
                .name("Bài 3: 4 tính chất OOP trong Java")
                .orderNum(1)
                .type(LessonTypeEnum.LESSON)
                .videoLink("https://www.youtube.com/watch?v=sample_java_oop")
                .htmlContent("<h3>4 Trụ cột trong OOP</h3><ul><li>Đóng gói (Encapsulation)</li><li>Kế thừa (Inheritance)</li><li>Đa hình (Polymorphism)</li><li>Trừu tượng (Abstraction)</li></ul>")
                .status(LessonStatus.ACTIVE)
                .build());

        // Quiz Lesson placeholder
        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic2)
                .name("Bài 4: Kiểm tra trắc nghiệm Java Core 15 phút")
                .orderNum(2)
                .type(LessonTypeEnum.QUIZ)
                .htmlContent("<p>Vui lòng làm bài kiểm tra trắc nghiệm 5 câu hỏi để hoàn thành chương.</p>")
                .status(LessonStatus.ACTIVE)
                .build());
    }

    private void createSpringBootLessons(Subject subject) {
        Lesson topic1 = lessonRepository.save(Lesson.builder()
                .subject(subject)
                .name("Chương 1: Bắt đầu với Spring Boot 3")
                .orderNum(1)
                .type(LessonTypeEnum.SUBJECT_TOPIC)
                .status(LessonStatus.ACTIVE)
                .build());

        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic1)
                .name("Bài 1: Khởi tạo dự án với Spring Initializr")
                .orderNum(1)
                .type(LessonTypeEnum.LESSON)
                .videoLink("https://www.youtube.com/watch?v=sample_spring_init")
                .htmlContent("<p>Khởi tạo project Spring Boot với start.spring.io và các dependency cần thiết.</p>")
                .status(LessonStatus.ACTIVE)
                .build());

        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic1)
                .name("Bài 2: Dependency Injection & Spring Inversion of Control")
                .orderNum(2)
                .type(LessonTypeEnum.LESSON)
                .videoLink("https://www.youtube.com/watch?v=sample_spring_di")
                .htmlContent("<p>Tìm hiểu các annotation @Component, @Service, @Autowired và Constructor Injection.</p>")
                .status(LessonStatus.ACTIVE)
                .build());

        // Quiz Lesson
        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic1)
                .name("Bài 3: Bài kiểm tra đánh giá kiến thức Spring Boot")
                .orderNum(3)
                .type(LessonTypeEnum.QUIZ)
                .htmlContent("<p>Bài kiểm tra trắc nghiệm đánh giá kiến thức Spring Boot cơ bản.</p>")
                .status(LessonStatus.ACTIVE)
                .build());
    }

    private void createEnglishLessons(Subject subject) {
        Lesson topic1 = lessonRepository.save(Lesson.builder()
                .subject(subject)
                .name("Chủ đề 1: Giao tiếp hàng ngày")
                .orderNum(1)
                .type(LessonTypeEnum.SUBJECT_TOPIC)
                .status(LessonStatus.ACTIVE)
                .build());

        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic1)
                .name("Bài 1: Chào hỏi và giới thiệu bản thân bằng tiếng Anh")
                .orderNum(1)
                .type(LessonTypeEnum.LESSON)
                .videoLink("https://www.youtube.com/watch?v=sample_english_intro")
                .htmlContent("<p>Mẫu câu chào hỏi tự nhiên trong môi trường công sở và đời sống.</p>")
                .status(LessonStatus.ACTIVE)
                .build());

        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic1)
                .name("Bài 2: Trắc nghiệm Tiếng Anh giao tiếp hàng ngày")
                .orderNum(2)
                .type(LessonTypeEnum.QUIZ)
                .htmlContent("<p>Bài kiểm tra phản xạ câu hỏi và đáp lại trong giao tiếp.</p>")
                .status(LessonStatus.ACTIVE)
                .build());
    }

    private void createExcelLessons(Subject subject) {
        Lesson topic1 = lessonRepository.save(Lesson.builder()
                .subject(subject)
                .name("Chương 1: Các hàm tính toán cơ bản")
                .orderNum(1)
                .type(LessonTypeEnum.SUBJECT_TOPIC)
                .status(LessonStatus.ACTIVE)
                .build());

        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic1)
                .name("Bài 1: Sử dụng hàm SUM, AVERAGE, IF cơ bản")
                .orderNum(1)
                .type(LessonTypeEnum.LESSON)
                .videoLink("https://www.youtube.com/watch?v=sample_excel_sum")
                .htmlContent("<p>Cú pháp và cách ứng dụng hàm SUM, IF lồng nhau trong quản lý bảng tính.</p>")
                .status(LessonStatus.ACTIVE)
                .build());

        lessonRepository.save(Lesson.builder()
                .subject(subject)
                .parentLesson(topic1)
                .name("Bài 2: Kiểm tra kỹ năng Excel văn phòng")
                .orderNum(2)
                .type(LessonTypeEnum.QUIZ)
                .htmlContent("<p>Kiểm tra kỹ năng sử dụng hàm và phím tắt Excel.</p>")
                .status(LessonStatus.ACTIVE)
                .build());
    }
}
