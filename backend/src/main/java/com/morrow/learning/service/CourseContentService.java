package com.morrow.learning.service;

import com.morrow.learning.domain.AttemptStatus;
import com.morrow.learning.domain.Course;
import com.morrow.learning.domain.Lesson;
import com.morrow.learning.domain.LessonProgress;
import com.morrow.learning.domain.Quiz;
import com.morrow.learning.domain.QuizAnswer;
import com.morrow.learning.domain.QuizAttempt;
import com.morrow.learning.domain.QuizQuestion;
import com.morrow.learning.domain.User;
import com.morrow.learning.dto.LearnerQuizView;
import com.morrow.learning.dto.LearningCourseView;
import com.morrow.learning.dto.LessonProgressView;
import com.morrow.learning.dto.LessonView;
import com.morrow.learning.dto.LessonWriteRequest;
import com.morrow.learning.dto.QuizAttemptView;
import com.morrow.learning.dto.QuizOverviewView;
import com.morrow.learning.dto.QuizQuestionWriteRequest;
import com.morrow.learning.dto.QuizResultView;
import com.morrow.learning.dto.QuizView;
import com.morrow.learning.dto.QuizWriteRequest;
import com.morrow.learning.dto.SelectedAnswerRequest;
import com.morrow.learning.repository.CourseRepository;
import com.morrow.learning.repository.LessonProgressRepository;
import com.morrow.learning.repository.LessonRepository;
import com.morrow.learning.repository.QuizAttemptRepository;
import com.morrow.learning.repository.QuizRepository;
import com.morrow.learning.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CourseContentService {
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserRepository userRepository;

    public CourseContentService(CourseService courseService, EnrollmentService enrollmentService,
                               CourseRepository courseRepository, LessonRepository lessonRepository,
                               QuizRepository quizRepository, QuizAttemptRepository quizAttemptRepository,
                               LessonProgressRepository lessonProgressRepository, UserRepository userRepository) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.quizRepository = quizRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<LessonView> manageLessons(Long courseId) {
        courseService.assertCourseExists(courseId);
        return lessonRepository.findAllByCourseIdOrderByDisplayOrderAsc(courseId).stream()
                .map(lesson -> LessonView.from(lesson, false)).toList();
    }

    @Transactional
    public LessonView createLesson(Long courseId, LessonWriteRequest request) {
        courseService.assertCourseExists(courseId);
        Course course = findCourse(courseId);
        Lesson lesson = new Lesson(course, request.title().trim(), request.summary().trim(),
                request.content().trim(), request.videoUrl(), request.displayOrder(), request.published());
        return LessonView.from(lessonRepository.save(lesson), false);
    }

    @Transactional
    public LessonView updateLesson(Long courseId, Long lessonId, LessonWriteRequest request) {
        courseService.assertCourseExists(courseId);
        Lesson lesson = findLesson(courseId, lessonId);
        lesson.update(request.title().trim(), request.summary().trim(), request.content().trim(),
                request.videoUrl(), request.displayOrder(), request.published());
        return LessonView.from(lesson, false);
    }

    @Transactional
    public void deleteLesson(Long courseId, Long lessonId) {
        courseService.assertCourseExists(courseId);
        Lesson lesson = findLesson(courseId, lessonId);
        lesson.update(lesson.getTitle(), lesson.getSummary(), lesson.getContent(), lesson.getVideoUrl(),
                lesson.getDisplayOrder(), false);
    }

    @Transactional(readOnly = true)
    public List<QuizView> manageQuizzes(Long courseId) {
        courseService.assertCourseExists(courseId);
        return quizRepository.findAllByCourseIdOrderByCreatedAtAsc(courseId).stream()
            .map(quiz -> QuizView.from(quiz, quizAttemptRepository.existsByQuizId(quiz.getId())))
            .toList();
    }

    @Transactional
    public QuizView createQuiz(Long courseId, QuizWriteRequest request) {
        courseService.assertCourseExists(courseId);
        Course course = findCourse(courseId);
        Quiz quiz = new Quiz(course, request.title().trim(), request.description().trim(),
                request.passingScore(), request.published());
        quiz.replaceQuestions(toQuestions(request.questions()));
        return QuizView.from(quizRepository.save(quiz));
    }

    @Transactional
    public QuizView updateQuiz(Long courseId, Long quizId, QuizWriteRequest request) {
        courseService.assertCourseExists(courseId);
        Quiz quiz = findQuiz(courseId, quizId);
        if (quizAttemptRepository.existsByQuizId(quizId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A quiz cannot be edited after a learner has attempted it");
        }
        quiz.update(request.title().trim(), request.description().trim(), request.passingScore(), request.published());
        quiz.replaceQuestions(toQuestions(request.questions()));
        return QuizView.from(quiz);
    }

    @Transactional
    public void deleteQuiz(Long courseId, Long quizId) {
        courseService.assertCourseExists(courseId);
        Quiz quiz = findQuiz(courseId, quizId);
        if (quizAttemptRepository.existsByQuizId(quizId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A quiz with learner attempts cannot be deleted");
        }
        quizRepository.delete(quiz);
    }

    @Transactional(readOnly = true)
    public LearningCourseView learningCourse(Long courseId, String email) {
        requireAccess(courseId, email);
        Course course = findCourse(courseId);
        User user = requireUser(email);
        Map<Long, Boolean> completed = lessonProgressRepository.findAllByUserIdAndLessonCourseId(user.getId(), courseId)
                .stream().collect(Collectors.toMap(progress -> progress.getLesson().getId(),
                        LessonProgress::isCompleted));
        List<LessonView> lessons = lessonRepository.findAllByCourseIdAndPublishedTrueOrderByDisplayOrderAsc(courseId)
                .stream().map(lesson -> LessonView.from(lesson, completed.getOrDefault(lesson.getId(), false))).toList();
        List<QuizOverviewView> quizzes = quizRepository.findAllByCourseIdAndPublishedTrueOrderByCreatedAtAsc(courseId)
                .stream().map(QuizOverviewView::from).toList();
        return new LearningCourseView(course.getId(), course.getTitle(), course.getDescription(), lessons, quizzes);
    }

    @Transactional(readOnly = true)
    public LessonView learningLesson(Long lessonId, String email) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(Lesson::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        requireAccess(lesson.getCourse().getId(), email);
        User user = requireUser(email);
        boolean completed = lessonProgressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .map(LessonProgress::isCompleted).orElse(false);
        return LessonView.from(lesson, completed);
    }

    @Transactional
    public LessonProgressView updateLessonProgress(Long lessonId, String email, boolean completed) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .filter(Lesson::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
        requireAccess(lesson.getCourse().getId(), email);
        User user = requireUser(email);
        LessonProgress progress = lessonProgressRepository.findByUserIdAndLessonId(user.getId(), lessonId)
                .orElseGet(() -> new LessonProgress(user, lesson, completed));
        progress.setCompleted(completed);
        return LessonProgressView.from(lessonProgressRepository.save(progress));
    }

    @Transactional(readOnly = true)
    public LearnerQuizView learnerQuiz(Long quizId, String email) {
        Quiz quiz = quizRepository.findById(quizId)
                .filter(Quiz::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        requireAccess(quiz.getCourse().getId(), email);
        return LearnerQuizView.from(quiz);
    }

    @Transactional
    public QuizAttemptView startOrResumeAttempt(Long quizId, String email) {
        Quiz quiz = quizRepository.findById(quizId)
                .filter(Quiz::isPublished)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
        requireAccess(quiz.getCourse().getId(), email);
        User user = requireUser(email);
        QuizAttempt attempt = quizAttemptRepository.findFirstByQuizIdAndUserIdAndStatusOrderByStartedAtDesc(
                        quizId, user.getId(), AttemptStatus.IN_PROGRESS)
                .orElseGet(() -> quizAttemptRepository.save(new QuizAttempt(user, quiz)));
        return QuizAttemptView.from(attempt);
    }

    @Transactional(readOnly = true)
    public QuizAttemptView getAttempt(Long attemptId, String email) {
        QuizAttempt attempt = findOwnedAttempt(attemptId, email);
        requireAccess(attempt.getQuiz().getCourse().getId(), email);
        return QuizAttemptView.from(attempt);
    }

    @Transactional
    public QuizAttemptView saveAnswer(Long attemptId, Long questionId, String email, int selectedOptionIndex) {
        QuizAttempt attempt = findOwnedAttempt(attemptId, email);
        requireAccess(attempt.getQuiz().getCourse().getId(), email);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Submitted quiz attempts cannot be changed");
        }
        QuizQuestion question = attempt.getQuiz().getQuestions().stream()
                .filter(item -> item.getId().equals(questionId))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found"));
        if (selectedOptionIndex >= question.getOptions().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected answer is outside the available options");
        }
        QuizAnswer answer = attempt.answerFor(questionId);
        if (answer == null) {
            attempt.addAnswer(new QuizAnswer(question, selectedOptionIndex));
        } else {
            answer.update(selectedOptionIndex);
        }
        return QuizAttemptView.from(attempt);
    }

    @Transactional
    public QuizResultView submitAttempt(Long attemptId, String email) {
        QuizAttempt attempt = findOwnedAttempt(attemptId, email);
        requireAccess(attempt.getQuiz().getCourse().getId(), email);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This quiz attempt is already submitted");
        }
        if (attempt.getAnswers().size() != attempt.getQuiz().getQuestions().size()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Answer every question before submitting the quiz");
        }
        long correct = attempt.getAnswers().stream().filter(answer ->
                answer.getSelectedOptionIndex() == answer.getQuestion().getCorrectOptionIndex()).count();
        int total = attempt.getQuiz().getQuestions().size();
        BigDecimal score = BigDecimal.valueOf(correct * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        attempt.submit(score);
        return new QuizResultView(attempt.getId(), attempt.getStatus().name(), score,
                attempt.getQuiz().getPassingScore(), score.compareTo(BigDecimal.valueOf(attempt.getQuiz().getPassingScore())) >= 0);
    }

    private List<QuizQuestion> toQuestions(List<QuizQuestionWriteRequest> requests) {
        return java.util.stream.IntStream.range(0, requests.size()).mapToObj(index -> {
            QuizQuestionWriteRequest request = requests.get(index);
            if (request.correctOptionIndex() >= request.options().size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correct answer must match an available option");
            }
            return new QuizQuestion(request.prompt().trim(), request.options().stream().map(String::trim).toList(),
                    request.correctOptionIndex(), index + 1);
        }).toList();
    }

    private QuizAttempt findOwnedAttempt(Long attemptId, String email) {
        User user = requireUser(email);
        return quizAttemptRepository.findByIdAndUserId(attemptId, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz attempt not found"));
    }

    private void requireAccess(Long courseId, String email) {
        if (!enrollmentService.hasCourseAccess(email, courseId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this course");
        }
    }

    private User requireUser(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
    }

    private Course findCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
    }

    private Lesson findLesson(Long courseId, Long lessonId) {
        return lessonRepository.findByIdAndCourseId(lessonId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lesson not found"));
    }

    private Quiz findQuiz(Long courseId, Long quizId) {
        return quizRepository.findByIdAndCourseId(quizId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    }
}