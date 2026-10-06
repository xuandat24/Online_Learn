package com.morrow.learning.service;

import com.morrow.learning.domain.Course;
import com.morrow.learning.domain.Post;
import com.morrow.learning.domain.PricePackage;
import com.morrow.learning.domain.QuestionBankItem;
import com.morrow.learning.domain.Role;
import com.morrow.learning.domain.Slider;
import com.morrow.learning.domain.Subject;
import com.morrow.learning.domain.SystemSetting;
import com.morrow.learning.domain.User;
import com.morrow.learning.repository.CourseRepository;
import com.morrow.learning.repository.PostRepository;
import com.morrow.learning.repository.QuestionBankRepository;
import com.morrow.learning.repository.SliderRepository;
import com.morrow.learning.repository.SubjectRepository;
import com.morrow.learning.repository.SystemSettingRepository;
import com.morrow.learning.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataSeeder {
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final PostRepository postRepository;
    private final SliderRepository sliderRepository;
    private final QuestionBankRepository questionBankRepository;
    private final SystemSettingRepository systemSettingRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminFullName;

    public DemoDataSeeder(CourseRepository courseRepository, UserRepository userRepository,
                          SubjectRepository subjectRepository,
                          PostRepository postRepository,
                          SliderRepository sliderRepository,
                          QuestionBankRepository questionBankRepository,
                          SystemSettingRepository systemSettingRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.admin.email}") String adminEmail,
                          @Value("${app.admin.password}") String adminPassword,
                          @Value("${app.admin.full-name}") String adminFullName) {
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.postRepository = postRepository;
        this.sliderRepository = sliderRepository;
        this.questionBankRepository = questionBankRepository;
        this.systemSettingRepository = systemSettingRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminFullName = adminFullName;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (subjectRepository.count() == 0) {
            subjectRepository.saveAll(List.of(
                    new Subject("Design", "Visual design, product design, and design systems."),
                    new Subject("Business", "Business, analytics, and marketing."),
                    new Subject("Development", "Software development and technology."),
                    new Subject("Creative", "Writing, photography, and creative practice.")
            ));
        }
        Map<String, Subject> subjects = subjectRepository.findAll().stream()
                .collect(Collectors.toMap(Subject::getName, Function.identity()));

        if (courseRepository.count() == 0) {
            courseRepository.saveAll(List.of(
                    CourseService.sample("Design systems that scale", "Build a thoughtful visual language, reusable components, and a system your whole team can trust.", "Design", "Maya Nguyen", "Intermediate", "6h 20m", "39.00", 4.9, 1240, "photo-1498050108023-c5249f4df085", "sage"),
                    CourseService.sample("The curious data analyst", "Turn messy data into clear decisions with practical SQL, visual storytelling, and real projects.", "Business", "Jordan Lee", "Beginner", "8h 10m", "49.00", 4.8, 2180, "photo-1460925895917-afdab827c52f", "blue"),
                    CourseService.sample("Frontend, from first principles", "A hands-on path through modern web interfaces, from the first component to a polished product.", "Development", "Alex Tran", "Beginner", "12h 40m", "59.00", 5.0, 3560, "photo-1516321318423-f06f85e504b3", "peach"),
                    CourseService.sample("Make space for better writing", "Find your voice, shape a strong narrative, and edit your words until every sentence earns its place.", "Creative", "Sam Rivera", "All levels", "4h 35m", "29.00", 4.9, 890, "photo-1455390582262-044cdead277a", "yellow"),
                    CourseService.sample("Small business, stronger brand", "Make clear positioning and useful marketing plans for the business you are building today.", "Business", "Priya Shah", "Intermediate", "5h 50m", "44.00", 4.7, 1620, "photo-1454165804606-c3d57bc86b40", "rose"),
                    CourseService.sample("Photography in natural light", "Learn to notice, shape, and capture the light that makes an ordinary moment feel alive.", "Creative", "Linh Pham", "All levels", "7h 05m", "35.00", 4.9, 970, "photo-1452587925148-ce544e77e70d", "lavender")
            ));
        }
        List<Course> courses = courseRepository.findAll();
        for (Course course : courses) {
            if (course.getSubject() == null && subjects.containsKey(course.getCategory())) {
                course.setSubject(subjects.get(course.getCategory()));
            }
            if (course.getPricePackages().isEmpty()) {
                course.addPricePackage(new PricePackage("Full course access", course.getPrice(), "USD", 0, true));
            }
        }
        courseRepository.saveAll(courses);

        // Seed Admin Account
        if (!adminEmail.isBlank() && !adminPassword.isBlank()
                && !userRepository.existsByEmailIgnoreCase(adminEmail)) {
            userRepository.save(new User(adminFullName, adminEmail, passwordEncoder.encode(adminPassword), Role.ADMIN, "0911000111", "Nam"));
        }

        // Seed Role Demo Accounts for Sale, Marketing, Expert, Customer
        seedUserIfNotExists("Nguyễn Văn Sale", "sale@example.com", "password123", Role.SALE, "0922000222", "Nam");
        seedUserIfNotExists("Lê Thị Marketing", "marketing@example.com", "password123", Role.MARKETING, "0933000333", "Nữ");
        seedUserIfNotExists("Alex Tran (Chuyên gia)", "expert@example.com", "password123", Role.EXPERT, "0944000444", "Nam");
        seedUserIfNotExists("Trần Học Viên", "customer@example.com", "password123", Role.CUSTOMER, "0955000555", "Nữ");

        // Seed Posts
        if (postRepository.count() == 0) {
            postRepository.saveAll(List.of(
                    new Post("Lộ trình tự học Frontend từ số 0 đến khi có việc làm", "Development", "Alex Tran", "PUBLISHED",
                            "Những nguyên lý cốt lõi cần nắm vững về HTML/CSS/JS và Next.js thay vì học thuộc lòng framework.",
                            "Nội dung bài viết hướng dẫn chi tiết lộ trình học tập từ căn bản đến nâng cao...",
                            "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?auto=format&fit=crop&w=800&q=80"),
                    new Post("5 nguyên tắc thiết kế Design System cho sản phẩm thực tế", "Design", "Maya Nguyen", "PUBLISHED",
                            "Tạo ra ngôn ngữ thị giác nhất quán mà lập trình viên và designer đều yêu thích sử dụng.",
                            "Design system không chỉ là bảng màu hay UI kit mà là cầu nối giao tiếp...",
                            "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?auto=format&fit=crop&w=800&q=80"),
                    new Post("Ứng dụng SQL trong phân tích dữ liệu kinh doanh hàng ngày", "Business", "Jordan Lee", "PUBLISHED",
                            "Khám phá các truy vấn thực tế giúp bạn chuyển đổi dữ liệu thô thành quyết định chiến lược hiệu quả.",
                            "Phân tích dữ liệu bằng SQL mang lại giá trị to lớn cho doanh nghiệp...",
                            "https://images.unsplash.com/photo-1551288049-bebda4e38f71?auto=format&fit=crop&w=800&q=80")
            ));
        }

        // Seed Sliders
        if (sliderRepository.count() == 0) {
            sliderRepository.saveAll(List.of(
                    new Slider("Làm chủ Kỹ năng Số & Bứt phá Sự nghiệp 2026", "Khóa học nổi bật",
                            "https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&w=1200&q=80",
                            "/#courses", "ACTIVE", 1),
                    new Slider("Đăng ký Gói Trọn đời - Tiết kiệm đến 40%", "Học phí ưu đãi",
                            "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80",
                            "/#courses", "ACTIVE", 2),
                    new Slider("Hệ thống Thi Trắc nghiệm & Cấp Chứng chỉ", "Đánh giá năng lực",
                            "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=1200&q=80",
                            "/learn", "ACTIVE", 3)
            ));
        }

        // Seed Settings
        if (systemSettingRepository.count() == 0) {
            systemSettingRepository.saveAll(List.of(
                    new SystemSetting("USER_ROLES", "Quản trị viên", "ADMIN", 1, true),
                    new SystemSetting("USER_ROLES", "Chuyên viên Bán hàng", "SALE", 2, true),
                    new SystemSetting("USER_ROLES", "Chuyên viên Tiếp thị", "MARKETING", 3, true),
                    new SystemSetting("USER_ROLES", "Giảng viên / Chuyên gia", "EXPERT", 4, true),
                    new SystemSetting("USER_ROLES", "Học viên", "CUSTOMER", 5, true),
                    new SystemSetting("QUESTION_LEVELS", "Dễ (Easy)", "EASY", 1, true),
                    new SystemSetting("QUESTION_LEVELS", "Trung bình (Medium)", "MEDIUM", 2, true),
                    new SystemSetting("QUESTION_LEVELS", "Khó (Hard)", "HARD", 3, true)
            ));
        }

        // Seed Question Bank
        if (questionBankRepository.count() == 0) {
            questionBankRepository.saveAll(List.of(
                    new QuestionBankItem("Phát triển Phần mềm", "React Fundamentals", "MEDIUM",
                            "Hook nào trong React dùng để ghi nhớ giá trị tính toán tốn kém?",
                            "[\"useEffect\", \"useMemo\", \"useCallback\", \"useRef\"]",
                            1, "useMemo lưu trữ giá trị tính toán tốn kém giữa các render.", "ACTIVE"),
                    new QuestionBankItem("Phát triển Phần mềm", "Web Architecture", "EASY",
                            "Phương thức HTTP nào an toàn và Idempotent để truy vấn danh sách dữ liệu?",
                            "[\"POST\", \"DELETE\", \"GET\", \"PATCH\"]",
                            2, "GET là phương thức an toàn và idempotent.", "ACTIVE"),
                    new QuestionBankItem("Kinh doanh & Phân tích", "SQL Queries", "HARD",
                            "Mệnh đề nào dùng để lọc kết quả sau khi đã áp dụng GROUP BY?",
                            "[\"WHERE\", \"HAVING\", \"ORDER BY\", \"DISTINCT\"]",
                            1, "HAVING dùng để lọc nhóm sau khi GROUP BY.", "ACTIVE")
            ));
        }
    }

    private void seedUserIfNotExists(String fullName, String email, String password, Role role, String phone, String gender) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            userRepository.save(new User(fullName, email, passwordEncoder.encode(password), role, phone, gender));
        }
    }
}