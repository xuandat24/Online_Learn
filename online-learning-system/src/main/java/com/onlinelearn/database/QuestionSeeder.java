package com.onlinelearn.database;

import com.onlinelearn.entity.*;
import com.onlinelearn.entity.enums.LessonTypeEnum;
import com.onlinelearn.entity.enums.MediaType;
import com.onlinelearn.entity.enums.QuestionStatus;
import com.onlinelearn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionSeeder {

    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuestionLevelRepository questionLevelRepository;
    private final TestTypeRepository testTypeRepository;
    private final LessonRepository lessonRepository;

    @Transactional
    public void seedQuestionsAndQuizzes() {
        log.info("--> Seeding Quizzes, Questions, and Answer Options...");

        if (quizRepository.count() > 0) {
            log.info("Quizzes already seeded, skipping.");
            return;
        }

        QuestionLevel easyLevel = questionLevelRepository.findByCode("EASY").orElse(null);
        QuestionLevel mediumLevel = questionLevelRepository.findByCode("MEDIUM").orElse(null);
        QuestionLevel hardLevel = questionLevelRepository.findByCode("HARD").orElse(null);

        TestType practiceType = testTypeRepository.findByCode("PRACTICE").orElse(null);
        TestType simType = testTypeRepository.findByCode("SIMULATION").orElse(null);

        // 1. JAVA CORE QUIZ & QUESTIONS
        seedJavaQuiz(mediumLevel, practiceType);

        // 2. SPRING BOOT QUIZ & QUESTIONS
        seedSpringBootQuiz(mediumLevel, simType);

        // 3. ENGLISH QUIZ & QUESTIONS
        seedEnglishQuiz(easyLevel, practiceType);

        // 4. EXCEL QUIZ & QUESTIONS
        seedExcelQuiz(easyLevel, practiceType);

        log.info("✓ Quizzes and Questions seeded successfully.");
    }

    private void seedJavaQuiz(QuestionLevel level, TestType testType) {
        Subject javaSubject = subjectRepository.findByName("Java Core cơ bản").orElse(null);
        if (javaSubject == null) return;

        Quiz quiz = quizRepository.save(Quiz.builder()
                .subject(javaSubject)
                .name("Kiểm tra trắc nghiệm Java Core 15 phút")
                .level(level)
                .quizType(testType)
                .duration(15)
                .passRate(60.0)
                .description("Bài kiểm tra gồm 5 câu hỏi trắc nghiệm đánh giá kiến thức cơ bản về Java và OOP.")
                .build());

        // Create 5 questions
        List<Question> questions = new ArrayList<>();

        questions.add(createQuestionWithAnswers(
                javaSubject, level,
                "Trong Java, từ khóa nào được sử dụng để một lớp con kế thừa từ một lớp cha?",
                "Từ khóa 'extends' dùng cho kế thừa lớp; 'implements' dùng cho cài đặt interface.",
                List.of(
                        new OptionData("implements", false),
                        new OptionData("extends", true),
                        new OptionData("inherits", false),
                        new OptionData("super", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                javaSubject, level,
                "Phương thức nào là điểm khởi đầu (entry point) để chạy một chương trình Java độc lập?",
                "Chương trình Java bắt đầu từ 'public static void main(String[] args)'.",
                List.of(
                        new OptionData("public void main(String[] args)", false),
                        new OptionData("public static void main(String[] args)", true),
                        new OptionData("static void start()", false),
                        new OptionData("public abstract void run()", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                javaSubject, level,
                "Kiểu dữ liệu nào sau đây là kiểu nguyên thủy (primitive data type) trong Java?",
                "boolean là primitive type; String, Integer, Array là reference type.",
                List.of(
                        new OptionData("String", false),
                        new OptionData("Integer", false),
                        new OptionData("boolean", true),
                        new OptionData("ArrayList", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                javaSubject, level,
                "Cấu trúc dữ liệu nào trong Java Collections Framework không cho phép chứa phần tử trùng lặp?",
                "Set (và các implementation như HashSet, TreeSet) đảm bảo các phần tử là duy nhất.",
                List.of(
                        new OptionData("List", false),
                        new OptionData("LinkedList", false),
                        new OptionData("Set", true),
                        new OptionData("Vector", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                javaSubject, level,
                "Trong OOP, tính chất nào cho phép che giấu trạng thái nội bộ của đối tượng và bảo vệ dữ liệu?",
                "Đóng gói (Encapsulation) giấu data fields bằng private và cung cấp getter/setter.",
                List.of(
                        new OptionData("Đóng gói (Encapsulation)", true),
                        new OptionData("Đa hình (Polymorphism)", false),
                        new OptionData("Kế thừa (Inheritance)", false),
                        new OptionData("Trừu tượng (Abstraction)", false)
                )
        ));

        linkQuizQuestions(quiz, questions);
        linkQuizToSubjectLesson(javaSubject, quiz);
    }

    private void seedSpringBootQuiz(QuestionLevel level, TestType testType) {
        Subject springSubject = subjectRepository.findByName("Spring Boot từ cơ bản đến nâng cao").orElse(null);
        if (springSubject == null) return;

        Quiz quiz = quizRepository.save(Quiz.builder()
                .subject(springSubject)
                .name("Bài kiểm tra đánh giá kiến thức Spring Boot")
                .level(level)
                .quizType(testType)
                .duration(20)
                .passRate(70.0)
                .description("Đánh giá toàn diện kiến thức Spring Boot 3, Dependency Injection và Spring Data JPA.")
                .build());

        List<Question> questions = new ArrayList<>();

        questions.add(createQuestionWithAnswers(
                springSubject, level,
                "Annotation nào được sử dụng để đánh dấu lớp khởi động chính của ứng dụng Spring Boot?",
                "@SpringBootApplication là tổ hợp của @Configuration, @EnableAutoConfiguration, @ComponentScan.",
                List.of(
                        new OptionData("@Configuration", false),
                        new OptionData("@SpringBootApplication", true),
                        new OptionData("@EnableAutoConfiguration", false),
                        new OptionData("@ComponentScan", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                springSubject, level,
                "Cách thực hiện Dependency Injection nào được cộng đồng Spring khuyến nghị sử dụng nhất?",
                "Constructor Injection đảm bảo immutability và dễ dàng viết unit test không cần reflection.",
                List.of(
                        new OptionData("Field Injection bằng @Autowired", false),
                        new OptionData("Constructor Injection", true),
                        new OptionData("Setter Injection", false),
                        new OptionData("Lookup Method Injection", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                springSubject, level,
                "Annotation nào kết hợp cả @Controller và @ResponseBody trong Spring Web MVC?",
                "@RestController tự động trả về body dạng JSON/XML cho tất cả các endpoint handler.",
                List.of(
                        new OptionData("@Service", false),
                        new OptionData("@RestController", true),
                        new OptionData("@Repository", false),
                        new OptionData("@Component", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                springSubject, level,
                "Trong Spring Data JPA, interface nào cung cấp sẵn các phương thức CRUD chuẩn?",
                "JpaRepository kế thừa PagingAndSortingRepository và CrudRepository.",
                List.of(
                        new OptionData("EntityManager", false),
                        new OptionData("JpaRepository", true),
                        new OptionData("JdbcTemplate", false),
                        new OptionData("SessionBean", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                springSubject, level,
                "Mặc định, Spring Security bảo vệ ứng dụng khỏi kiểu tấn công nào bằng cách yêu cầu Token trong request POST?",
                "CSRF (Cross-Site Request Forgery) là cơ chế bảo vệ mặc định của Spring Security.",
                List.of(
                        new OptionData("SQL Injection", false),
                        new OptionData("CSRF (Cross-Site Request Forgery)", true),
                        new OptionData("XSS", false),
                        new OptionData("DDoS", false)
                )
        ));

        linkQuizQuestions(quiz, questions);
        linkQuizToSubjectLesson(springSubject, quiz);
    }

    private void seedEnglishQuiz(QuestionLevel level, TestType testType) {
        Subject englishSubject = subjectRepository.findByName("Tiếng Anh giao tiếp cơ bản").orElse(null);
        if (englishSubject == null) return;

        Quiz quiz = quizRepository.save(Quiz.builder()
                .subject(englishSubject)
                .name("Trắc nghiệm Tiếng Anh giao tiếp hàng ngày")
                .level(level)
                .quizType(testType)
                .duration(15)
                .passRate(60.0)
                .description("Kiểm tra phản xạ các câu giao tiếp cơ bản thông dụng.")
                .build());

        List<Question> questions = new ArrayList<>();

        questions.add(createQuestionWithAnswers(
                englishSubject, level,
                "Khi gặp một người lần đầu tiên trong buổi gặp gỡ xã giao, câu nói lịch sự nhất là:",
                "'Nice to meet you!' là lời chào chuẩn mực khi mới quen biết.",
                List.of(
                        new OptionData("Nice to meet you!", true),
                        new OptionData("See you yesterday!", false),
                        new OptionData("Where were you?", false),
                        new OptionData("What is your time?", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                englishSubject, level,
                "Để hỏi đường đến trạm xe buýt gần nhất một cách lịch sự, bạn nên nói:",
                "'Excuse me, could you tell me the way...' là cấu trúc hỏi đường lịch thiệp.",
                List.of(
                        new OptionData("Give me bus stop!", false),
                        new OptionData("Excuse me, could you tell me the way to the nearest bus stop?", true),
                        new OptionData("Is bus coming here?", false),
                        new OptionData("Where is car?", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                englishSubject, level,
                "Khi muốn xin hóa đơn thanh toán trong nhà hàng, bạn nói:",
                "'Can I have the bill, please?' là cách phổ biến để xin hóa đơn.",
                List.of(
                        new OptionData("Can I have the bill, please?", true),
                        new OptionData("I have money now!", false),
                        new OptionData("Give me price!", false),
                        new OptionData("Where is ticket?", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                englishSubject, level,
                "Từ nào dưới đây đồng nghĩa với 'Opportunity'?",
                "'Opportunity' đồng nghĩa với 'Chance' (cơ hội).",
                List.of(
                        new OptionData("Chance", true),
                        new OptionData("Danger", false),
                        new OptionData("Obstacle", false),
                        new OptionData("Mistake", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                englishSubject, level,
                "Cách đáp lại phổ biến và lịch sự nhất cho câu 'Thank you very much' là:",
                "'You're welcome!' là câu đáp lễ thông dụng nhất khi được cảm ơn.",
                List.of(
                        new OptionData("You're welcome!", true),
                        new OptionData("Never mind me.", false),
                        new OptionData("It is bad.", false),
                        new OptionData("No way.", false)
                )
        ));

        linkQuizQuestions(quiz, questions);
        linkQuizToSubjectLesson(englishSubject, quiz);
    }

    private void seedExcelQuiz(QuestionLevel level, TestType testType) {
        Subject excelSubject = subjectRepository.findByName("Excel văn phòng cho người mới").orElse(null);
        if (excelSubject == null) return;

        Quiz quiz = quizRepository.save(Quiz.builder()
                .subject(excelSubject)
                .name("Kiểm tra kỹ năng Excel văn phòng")
                .level(level)
                .quizType(testType)
                .duration(15)
                .passRate(60.0)
                .description("Kiểm tra kỹ năng sử dụng hàm tính toán cơ bản và bảng tính.")
                .build());

        List<Question> questions = new ArrayList<>();

        questions.add(createQuestionWithAnswers(
                excelSubject, level,
                "Hàm nào trong Excel dùng để tính tổng một vùng các ô chứa dữ liệu số?",
                "Hàm SUM() dùng để tính tổng các ô được chỉ định.",
                List.of(
                        new OptionData("COUNT()", false),
                        new OptionData("AVERAGE()", false),
                        new OptionData("SUM()", true),
                        new OptionData("TOTAL()", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                excelSubject, level,
                "Tất cả các công thức trong bảng tính Excel đều phải bắt đầu bằng ký tự nào?",
                "Mọi công thức trong Excel luôn bắt đầu bằng dấu '='.",
                List.of(
                        new OptionData("Dấu bằng (=)", true),
                        new OptionData("Dấu cộng (+)", false),
                        new OptionData("Dấu hai chấm (:)", false),
                        new OptionData("Dấu thăng (#)", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                excelSubject, level,
                "Công cụ nào trong Excel cho phép tổng hợp, tóm tắt và phân tích dữ liệu lớn đa chiều nhanh chóng?",
                "PivotTable là công cụ mạnh mẽ nhất để tổng hợp và báo cáo dữ liệu trong Excel.",
                List.of(
                        new OptionData("Conditional Formatting", false),
                        new OptionData("PivotTable", true),
                        new OptionData("Data Validation", false),
                        new OptionData("Freeze Panes", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                excelSubject, level,
                "Phím tắt nào trong Excel dùng để lưu lại file làm việc hiện tại?",
                "Ctrl + S là phím tắt lưu file chuẩn trên Windows.",
                List.of(
                        new OptionData("Ctrl + S", true),
                        new OptionData("Ctrl + C", false),
                        new OptionData("Ctrl + V", false),
                        new OptionData("Ctrl + P", false)
                )
        ));

        questions.add(createQuestionWithAnswers(
                excelSubject, level,
                "Hàm VLOOKUP trong Excel thường được sử dụng để làm gì?",
                "VLOOKUP dò tìm một giá trị theo cột đầu tiên của bảng và trả về giá trị ở cột tương ứng.",
                List.of(
                        new OptionData("Tìm kiếm giá trị theo cột dọc trong bảng dữ liệu", true),
                        new OptionData("Tính trung bình cộng các số âm", false),
                        new OptionData("Xóa các hàng trùng lặp", false),
                        new OptionData("Định dạng màu sắc cho ô", false)
                )
        ));

        linkQuizQuestions(quiz, questions);
        linkQuizToSubjectLesson(excelSubject, quiz);
    }

    private Question createQuestionWithAnswers(Subject subject, QuestionLevel level, String content, String explanation, List<OptionData> options) {
        Question question = Question.builder()
                .subject(subject)
                .level(level)
                .content(content)
                .explanation(explanation)
                .status(QuestionStatus.ACTIVE)
                .mediaType(MediaType.NONE)
                .build();
        question = questionRepository.save(question);

        List<AnswerOption> answerOptions = new ArrayList<>();
        for (OptionData opt : options) {
            answerOptions.add(AnswerOption.builder()
                    .question(question)
                    .content(opt.content())
                    .isCorrect(opt.isCorrect())
                    .build());
        }
        answerOptionRepository.saveAll(answerOptions);

        return question;
    }

    private void linkQuizQuestions(Quiz quiz, List<Question> questions) {
        List<QuizQuestion> quizQuestions = new ArrayList<>();
        int order = 1;
        for (Question q : questions) {
            quizQuestions.add(QuizQuestion.builder()
                    .quiz(quiz)
                    .question(q)
                    .orderNum(order++)
                    .build());
        }
        quizQuestionRepository.saveAll(quizQuestions);
    }

    private void linkQuizToSubjectLesson(Subject subject, Quiz quiz) {
        List<Lesson> lessons = lessonRepository.findBySubjectIdOrderByOrderNumAsc(subject.getId());
        for (Lesson lesson : lessons) {
            if (lesson.getType() == LessonTypeEnum.QUIZ && lesson.getQuiz() == null) {
                lesson.setQuiz(quiz);
                lessonRepository.save(lesson);
                break;
            }
        }
    }

    private record OptionData(String content, boolean isCorrect) {}
}
