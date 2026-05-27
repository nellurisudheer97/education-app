package com.eduapp.service;

import com.eduapp.dto.*;
import com.eduapp.model.*;
import com.eduapp.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;
    private final QuestionRepository questionRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final QuizAssignmentRepository quizAssignmentRepository; // Assuming this is created

    public QuizService(
            QuizRepository quizRepository,
            LessonRepository lessonRepository,
            QuestionRepository questionRepository,
            QuizResultRepository quizResultRepository,
            QuizAnswerRepository quizAnswerRepository,
            UserRepository userRepository,
            UserProgressRepository userProgressRepository,
            QuizAssignmentRepository quizAssignmentRepository) {
        this.quizRepository = quizRepository;
        this.lessonRepository = lessonRepository;
        this.questionRepository = questionRepository;
        this.quizResultRepository = quizResultRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
        this.quizAssignmentRepository = quizAssignmentRepository;
    }

    @Transactional
    public QuizDTO createQuiz(QuizDTO quizDTO) {
        Lesson lesson = lessonRepository.findById(quizDTO.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        ensureCurrentUserCanManageCourse(lesson.getCourse());

        Quiz quiz = new Quiz();
        quiz.setTitle(quizDTO.getTitle());
        quiz.setDescription(quizDTO.getDescription());
        quiz.setLesson(lesson);
        quiz.setPassingScore(quizDTO.getPassingScore() != null ? quizDTO.getPassingScore() : 70);
        quiz.setTimerMinutes(quizDTO.getTimerMinutes() != null ? quizDTO.getTimerMinutes() : 30);

        Quiz savedQuiz = quizRepository.save(quiz);
        saveQuestions(savedQuiz, quizDTO.getQuestions());
        recalculateQuizTotals(savedQuiz);

        return convertToDTO(savedQuiz, true);
    }

    public List<QuizDTO> getQuizzesByLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
        ensureCurrentUserCanViewCourse(lesson.getCourse());

        User user = getCurrentUser();
        List<Quiz> allQuizzes = quizRepository.findByLessonId(lessonId);

        if (isStudent(user)) {
            // Students only see quizzes assigned to them
            List<Long> assignedQuizIds = quizAssignmentRepository.findByUserId(user.getId())
                    .stream()
                    .map(a -> a.getQuiz().getId())
                    .collect(Collectors.toList());

            return allQuizzes.stream()
                    .filter(q -> assignedQuizIds.contains(q.getId()))
                    .map(quiz -> convertToDTO(quiz, false))
                    .collect(Collectors.toList());
        }

        return allQuizzes.stream()
                .map(quiz -> convertToDTO(quiz, true))
                .collect(Collectors.toList());
    }

    public QuizDTO getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        
        User user = getCurrentUser();
        ensureCurrentUserCanViewCourse(quiz.getLesson().getCourse());

        if (isStudent(user) && !quizAssignmentRepository.existsByUserIdAndQuizId(user.getId(), quizId)) {
            throw new RuntimeException("This quiz has not been assigned to you.");
        }

        return convertToDTO(quiz, !isStudent(getCurrentUser()));
    }

    @Transactional
    public QuizDTO updateQuiz(Long quizId, QuizDTO quizDTO) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        ensureCurrentUserCanManageCourse(quiz.getLesson().getCourse());

        quiz.setTitle(quizDTO.getTitle());
        quiz.setDescription(quizDTO.getDescription());
        quiz.setPassingScore(quizDTO.getPassingScore());
        quiz.setTimerMinutes(quizDTO.getTimerMinutes());
        quiz.setUpdatedAt(LocalDateTime.now());

        questionRepository.deleteAll(questionRepository.findByQuizId(quizId));
        saveQuestions(quiz, quizDTO.getQuestions());
        recalculateQuizTotals(quiz);

        return convertToDTO(quiz, true);
    }

    @Transactional
    public void deleteQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        ensureCurrentUserCanManageCourse(quiz.getLesson().getCourse());
        quizRepository.deleteById(quizId);
    }

    @Transactional
    public QuizResultDTO submitQuiz(Long quizId, QuizSubmissionRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        User currentUser = getCurrentUser();
        ensureCurrentUserCanViewCourse(quiz.getLesson().getCourse());

        if (isStudent(currentUser) && !quizAssignmentRepository.existsByUserIdAndQuizId(currentUser.getId(), quizId)) {
            throw new RuntimeException("This quiz has not been assigned to you.");
        }

        List<Question> questions = questionRepository.findByQuizId(quizId);
        Map<Long, SubmittedAnswerDTO> submittedAnswers = request.getAnswers() == null
                ? Map.of()
                : request.getAnswers().stream()
                    .collect(Collectors.toMap(SubmittedAnswerDTO::getQuestionId, Function.identity(), (first, second) -> second));

        QuizResult result = new QuizResult();
        result.setQuiz(quiz);
        result.setUser(currentUser);
        result.setTotalMarks(quiz.getTotalMarks());
        result.setTimeTakenSeconds(request.getTimeTakenSeconds() != null ? request.getTimeTakenSeconds() : 0);

        boolean needsReview = false;
        int score = 0;
        QuizResult savedResult = quizResultRepository.save(result);

        for (Question question : questions) {
            SubmittedAnswerDTO submitted = submittedAnswers.get(question.getId());
            QuizAnswer answer = new QuizAnswer();
            answer.setResult(savedResult);
            answer.setQuestion(question);
            answer.setMaxMarks(question.getMarks());

            if (question.getQuestionType() == QuestionType.MCQ) {
                String selectedOption = normalizeOption(submitted == null ? null : submitted.getSelectedOption());
                boolean correct = selectedOption != null && selectedOption.equalsIgnoreCase(question.getCorrectAnswer());
                int awardedMarks = correct ? question.getMarks() : 0;
                answer.setSelectedOption(selectedOption);
                answer.setIsCorrect(correct);
                answer.setAwardedMarks(awardedMarks);
                answer.setReviewed(true);
                score += awardedMarks;
            } else {
                needsReview = true;
                answer.setTextAnswer(submitted == null ? "" : submitted.getTextAnswer());
                answer.setAwardedMarks(0);
                answer.setReviewed(false);
                answer.setIsCorrect(false);
            }

            quizAnswerRepository.save(answer);
        }

        savedResult.setScore(score);
        savedResult.setPercentage(calculatePercentage(score, quiz.getTotalMarks()));
        savedResult.setStatus(needsReview ? "PENDING_REVIEW" : "GRADED");
        savedResult.setGrade(needsReview ? "PENDING_REVIEW" : calculateGrade(savedResult.getPercentage()));
        savedResult.setIsPassed(!needsReview && savedResult.getPercentage() >= quiz.getPassingScore());

        return convertResultToDTO(quizResultRepository.save(savedResult));
    }

    @Transactional
    public QuizResultDTO reviewAnswer(Long answerId, ReviewAnswerRequest request) {
        QuizAnswer answer = quizAnswerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer not found"));
        ensureCurrentUserCanAssignOrReviewQuiz();

        int maxMarks = answer.getMaxMarks() == null ? 0 : answer.getMaxMarks();
        int awardedMarks = Math.max(0, Math.min(request.getAwardedMarks(), maxMarks));
        answer.setAwardedMarks(awardedMarks);
        answer.setReviewed(true);
        answer.setIsCorrect(awardedMarks > 0);
        quizAnswerRepository.save(answer);

        QuizResult result = answer.getResult();
        List<QuizAnswer> answers = quizAnswerRepository.findByResultId(result.getId());
        int score = answers.stream().mapToInt(item -> item.getAwardedMarks() == null ? 0 : item.getAwardedMarks()).sum();
        boolean pendingReview = answers.stream().anyMatch(item -> !Boolean.TRUE.equals(item.getReviewed()));

        result.setScore(score);
        result.setPercentage(calculatePercentage(score, result.getTotalMarks()));
        result.setStatus(pendingReview ? "PENDING_REVIEW" : "GRADED");
        result.setGrade(pendingReview ? "PENDING_REVIEW" : calculateGrade(result.getPercentage()));
        result.setIsPassed(!pendingReview && result.getPercentage() >= result.getQuiz().getPassingScore());

        return convertResultToDTO(quizResultRepository.save(result));
    }

    public List<QuizResultDTO> getResultsForQuiz(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        ensureCurrentUserCanAssignOrReviewQuiz();
        return quizResultRepository.findByQuizId(quizId)
                .stream()
                .map(this::convertResultToDTO)
                .toList();
    }

    @Transactional
    public void assignQuizToStudent(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
        ensureCurrentUserCanAssignOrReviewQuiz();
        
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!quizAssignmentRepository.existsByUserIdAndQuizId(studentId, quizId)) {
            QuizAssignment assignment = new QuizAssignment();
            assignment.setQuiz(quiz);
            assignment.setUser(student);
            quizAssignmentRepository.save(assignment);
        }
    }

    private void saveQuestions(Quiz quiz, List<QuestionDTO> questionDTOs) {
        if (questionDTOs == null || questionDTOs.isEmpty()) {
            throw new RuntimeException("A quiz needs at least one question");
        }

        int index = 0;
        for (QuestionDTO dto : questionDTOs) {
            Question question = new Question();
            question.setQuiz(quiz);
            question.setQuestionText(dto.getQuestionText());
            QuestionType type = "DESCRIPTIVE".equalsIgnoreCase(dto.getQuestionType()) ? QuestionType.DESCRIPTIVE : QuestionType.MCQ;
            question.setQuestionType(type);
            question.setOptionA(dto.getOptionA());
            question.setOptionB(dto.getOptionB());
            question.setOptionC(dto.getOptionC());
            question.setOptionD(dto.getOptionD());
            question.setCorrectAnswer(type == QuestionType.MCQ ? normalizeOption(dto.getCorrectAnswer()) : null);
            question.setSampleAnswer(dto.getSampleAnswer());
            question.setMarks(dto.getMarks() != null && dto.getMarks() > 0 ? dto.getMarks() : 1);
            question.setOrderIndex(dto.getOrderIndex() != null ? dto.getOrderIndex() : index);
            validateQuestion(question);
            questionRepository.save(question);
            index++;
        }
    }

    private void validateQuestion(Question question) {
        if (question.getQuestionText() == null || question.getQuestionText().isBlank()) {
            throw new RuntimeException("Question text is required");
        }
        if (question.getQuestionType() == QuestionType.MCQ) {
            if (question.getOptionA() == null || question.getOptionB() == null || question.getOptionC() == null || question.getOptionD() == null) {
                throw new RuntimeException("MCQ questions need four options");
            }
            if (question.getCorrectAnswer() == null || !List.of("A", "B", "C", "D").contains(question.getCorrectAnswer())) {
                throw new RuntimeException("MCQ questions need a correct answer from A, B, C, or D");
            }
        }
    }

    private void recalculateQuizTotals(Quiz quiz) {
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        quiz.setTotalQuestions(questions.size());
        quiz.setTotalMarks(questions.stream().mapToInt(Question::getMarks).sum());
        quizRepository.save(quiz);
    }

    private QuizDTO convertToDTO(Quiz quiz, boolean includeAnswers) {
        return new QuizDTO(
                quiz.getId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getLesson().getId(),
                quiz.getTotalQuestions(),
                quiz.getPassingScore(),
                quiz.getTimerMinutes(),
                quiz.getTotalMarks(),
                questionRepository.findByQuizId(quiz.getId())
                        .stream()
                        .sorted(Comparator.comparing(Question::getOrderIndex, Comparator.nullsLast(Integer::compareTo)))
                        .map(question -> convertQuestionToDTO(question, includeAnswers))
                        .toList()
        );
    }

    private QuestionDTO convertQuestionToDTO(Question question, boolean includeAnswers) {
        return new QuestionDTO(
                question.getId(),
                question.getQuestionText(),
                question.getQuestionType().name(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                includeAnswers ? question.getCorrectAnswer() : null,
                includeAnswers ? question.getSampleAnswer() : null,
                question.getMarks(),
                question.getOrderIndex()
        );
    }

    private QuizResultDTO convertResultToDTO(QuizResult result) {
        return new QuizResultDTO(
                result.getId(),
                result.getQuiz().getId(),
                result.getUser().getId(),
                result.getUser().getFullName(),
                result.getUser().getEmail(),
                result.getScore(),
                result.getTotalMarks(),
                result.getPercentage(),
                result.getIsPassed(),
                result.getGrade(),
                result.getStatus(),
                result.getTimeTakenSeconds(),
                result.getCompletedAt(),
                quizAnswerRepository.findByResultId(result.getId())
                        .stream()
                        .map(this::convertAnswerToDTO)
                        .toList()
        );
    }

    private QuizAnswerDTO convertAnswerToDTO(QuizAnswer answer) {
        return new QuizAnswerDTO(
                answer.getId(),
                answer.getQuestion().getId(),
                answer.getQuestion().getQuestionText(),
                answer.getQuestion().getQuestionType().name(),
                answer.getSelectedOption(),
                answer.getTextAnswer(),
                answer.getAwardedMarks(),
                answer.getMaxMarks(),
                answer.getIsCorrect(),
                answer.getReviewed()
        );
    }

    private String normalizeOption(String option) {
        if (option == null || option.isBlank()) return null;
        return option.trim().substring(0, 1).toUpperCase();
    }

    private Double calculatePercentage(Integer score, Integer totalMarks) {
        if (totalMarks == null || totalMarks == 0) return 0.0;
        return Math.round((score * 10000.0 / totalMarks)) / 100.0;
    }

    private String calculateGrade(Double percentage) {
        if (percentage >= 90) return "A+";
        if (percentage >= 80) return "A";
        if (percentage >= 70) return "B";
        if (percentage >= 60) return "C";
        if (percentage >= 50) return "D";
        return "F";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Authentication required");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private boolean isStudent(User user) {
        return user.getRole() == UserRole.STUDENT;
    }

    private void ensureCurrentUserCanViewCourse(Course course) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == UserRole.ADMIN || course.getInstructor().getId().equals(currentUser.getId())) {
            return;
        }
        if (!userProgressRepository.existsByUserIdAndCourseId(currentUser.getId(), course.getId())) {
            throw new RuntimeException("This course is not assigned to you");
        }
    }

    private void ensureCurrentUserCanManageCourse(Course course) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == UserRole.ADMIN) {
            return;
        }
        if (!course.getInstructor().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only manage your own courses");
        }
    }

    private void ensureCurrentUserCanAssignOrReviewQuiz() {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == UserRole.ADMIN || currentUser.getRole() == UserRole.INSTRUCTOR) {
            return;
        }
        throw new RuntimeException("Only instructors and admins can assign or review quizzes");
    }
}
