package vn.onlinelearn.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Repository bundle; business decisions remain in services. */
@Component
public class Data {
    @Autowired public UserRepository users;
    @Autowired public AuthTokenRepository tokens;
    @Autowired public SettingRepository settings;
    @Autowired public SubjectRepository subjects;
    @Autowired public SubjectDimensionRepository dimensions;
    @Autowired public PricePackageRepository packages;
    @Autowired public LessonRepository lessons;
    @Autowired public RegistrationRepository registrations;
    @Autowired public CourseAccessRepository access;
    @Autowired public PaymentTransactionRepository payments;
    @Autowired public LessonProgressRepository progress;
    @Autowired public QuestionRepository questions;
    @Autowired public QuestionOptionRepository options;
    @Autowired public QuizRepository quizzes;
    @Autowired public QuizAttemptRepository attempts;
    @Autowired public QuizAnswerRepository answers;
    @Autowired public PostRepository posts;
    @Autowired public SliderRepository sliders;
}
