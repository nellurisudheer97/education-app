import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  FiArrowLeft,
  FiBookOpen,
  FiCheckCircle,
  FiChevronRight,
  FiFileText,
  FiPaperclip,
  FiPlayCircle,
  FiTrash2,
  FiPlus,
  FiUserPlus,
  FiUpload,
  FiX
} from 'react-icons/fi';
import ConfirmDialog from '../components/ConfirmDialog';
import StatusBanner from '../components/StatusBanner';
import ToastStack from '../components/ToastStack';
import useToasts from '../hooks/useToasts';
import api from '../services/api';
import { getCurrentUser, isStaffRole } from '../services/session';
import '../styles/course.css';

const emptyLesson = { title: '', content: '', videoUrl: '' };
const emptyQuestion = {
  questionText: '',
  questionType: 'MCQ',
  optionA: '',
  optionB: '',
  optionC: '',
  optionD: '',
  correctAnswer: 'A',
  sampleAnswer: '',
  marks: 1
};
const emptyQuiz = {
  title: '',
  description: '',
  timerMinutes: 30,
  passingScore: 70,
  questions: [{ ...emptyQuestion }]
};

export default function CourseDetail() {
  const { courseId } = useParams();
  const [course, setCourse] = useState(null);
  const [lessons, setLessons] = useState([]);
  const [selectedLesson, setSelectedLesson] = useState(null);
  const [quizzes, setQuizzes] = useState([]);
  const [activeQuiz, setActiveQuiz] = useState(null);
  const [reviewQuiz, setReviewQuiz] = useState(null);
  const [quizResults, setQuizResults] = useState([]);
  const [studentQuizResultsMap, setStudentQuizResultsMap] = useState({});
  const [quizAnswers, setQuizAnswers] = useState({});
  const [quizStartedAt, setQuizStartedAt] = useState(null);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const [quizResult, setQuizResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showAddLesson, setShowAddLesson] = useState(false);
  const [showCreateQuiz, setShowCreateQuiz] = useState(false);
  const [showAssignStudents, setShowAssignStudents] = useState(false);
  const [showAssignQuiz, setShowAssignQuiz] = useState(false);
  const [newLesson, setNewLesson] = useState(emptyLesson);
  const [newQuiz, setNewQuiz] = useState(emptyQuiz);
  const [selectedQuizId, setSelectedQuizId] = useState(null);
  const [students, setStudents] = useState([]);
  const [selectedStudentId, setSelectedStudentId] = useState('');
  const [videoFile, setVideoFile] = useState(null);
  const [resourceFile, setResourceFile] = useState(null);
  const [saving, setSaving] = useState(false);
  const [pendingDeleteQuizId, setPendingDeleteQuizId] = useState(null);
  const [pendingDeleteLessonId, setPendingDeleteLessonId] = useState(null);
  const navigate = useNavigate();
  const { toasts, pushToast, removeToast } = useToasts();
  const user = getCurrentUser();
  const canManageLessons = isStaffRole(user.role);
  const canAssignOrReviewQuizzes = isStaffRole(user.role);

  const fetchCourseDetails = useCallback(async () => {
    setLoading(true);
    setError('');

    try {
      const [courseData, lessonsData] = await Promise.all([
        api.getCourseById(courseId),
        api.getLessonsByCourse(courseId)
      ]);
      const normalizedLessons = Array.isArray(lessonsData) ? lessonsData : [];

      setCourse(courseData);
      setLessons(normalizedLessons);
      setSelectedLesson((current) =>
        normalizedLessons.find((lesson) => lesson.id === current?.id) || normalizedLessons[0] || null
      );
    } catch (err) {
      setError(err.message || 'Course details could not be loaded.');
    } finally {
      setLoading(false);
    }
  }, [courseId]);

  useEffect(() => {
    fetchCourseDetails();
  }, [fetchCourseDetails]);

  const fetchLessonQuizzes = useCallback(async (lessonId) => {
    try {
      const data = await api.getQuizzesByLesson(lessonId);
      setQuizzes(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Quizzes could not be loaded.');
    }
  }, []);

  const submitActiveQuiz = useCallback(async () => {
    if (!activeQuiz || saving) return;
    setSaving(true);

    try {
      const startedAt = quizStartedAt || Date.now();
      const timeTakenSeconds = Math.max(0, Math.round((Date.now() - startedAt) / 1000));
      const result = await api.submitQuiz(activeQuiz.id, {
        timeTakenSeconds,
        answers: (activeQuiz.questions || []).map((question) => ({
          questionId: question.id,
          selectedOption: quizAnswers[question.id]?.selectedOption || '',
          textAnswer: quizAnswers[question.id]?.textAnswer || ''
        }))
      });
      setQuizResult(result);
      setStudentQuizResultsMap((prev) => ({
        ...prev,
        [activeQuiz.id]: result
      }));
      setActiveQuiz(null);
      setQuizAnswers({});
      setQuizStartedAt(null);
      setSecondsLeft(0);
      pushToast('Quiz submitted successfully.', 'success');
    } catch (err) {
      setError(err.message || 'Quiz could not be submitted.');
    } finally {
      setSaving(false);
    }
  }, [activeQuiz, quizAnswers, quizStartedAt, pushToast, saving]);

  useEffect(() => {
    if (!selectedLesson?.id) {
      setQuizzes([]);
      return;
    }

    fetchLessonQuizzes(selectedLesson.id);
  }, [fetchLessonQuizzes, selectedLesson?.id]);

  useEffect(() => {
    if (!activeQuiz || !quizStartedAt || secondsLeft <= 0) return undefined;

    const intervalId = window.setInterval(() => {
      setSecondsLeft((current) => Math.max(current - 1, 0));
    }, 1000);

    return () => window.clearInterval(intervalId);
  }, [activeQuiz, quizStartedAt, secondsLeft]);

  useEffect(() => {
    if (activeQuiz && quizStartedAt && secondsLeft === 0) {
      submitActiveQuiz();
    }
  }, [activeQuiz, quizStartedAt, secondsLeft, submitActiveQuiz]);

  const selectedIndex = useMemo(
    () => lessons.findIndex((lesson) => lesson.id === selectedLesson?.id),
    [lessons, selectedLesson]
  );

  const completionPercent = lessons.length ? Math.round(((selectedIndex + 1) / lessons.length) * 100) : 0;

  const handleAddLesson = async (e) => {
    e.preventDefault();
    if (!newLesson.title.trim()) return;

    setSaving(true);
    setError('');

    try {
      let videoUrl = newLesson.videoUrl.trim();
      let resourceUrl = '';

      if (videoFile) {
        const uploadedVideo = await api.uploadVideo(videoFile);
        videoUrl = uploadedVideo?.url || '';
      }

      if (resourceFile) {
        const uploadedResource = await api.uploadResource(resourceFile);
        resourceUrl = uploadedResource?.url || '';
      }

      await api.createLesson({
        title: newLesson.title.trim(),
        content: newLesson.content.trim(),
        videoUrl,
        resourceUrl,
        courseId: Number(courseId)
      });
      setNewLesson(emptyLesson);
      setVideoFile(null);
      setResourceFile(null);
      setShowAddLesson(false);
      await fetchCourseDetails();
    } catch (err) {
      setError(err.message || 'Lesson could not be added.');
    } finally {
      setSaving(false);
    }
  };

  const handleCreateQuiz = async (e) => {
    e.preventDefault();
    if (!newQuiz.title.trim()) {
      setError('Quiz title is required.');
      return;
    }
    if (!newQuiz.questions.length) {
      setError('At least one question is required.');
      return;
    }
    setSaving(true);
    setError('');

    try {
      await api.createQuiz({
        ...newQuiz,
        lessonId: selectedLesson.id,
        timerMinutes: Number(newQuiz.timerMinutes),
        passingScore: Number(newQuiz.passingScore),
        questions: newQuiz.questions.map((question, index) => ({
          ...question,
          marks: Number(question.marks),
          orderIndex: index
        }))
      });
      setNewQuiz({ ...emptyQuiz, questions: [{ ...emptyQuestion }] });
      setShowCreateQuiz(false);
      await fetchLessonQuizzes(selectedLesson.id);
      pushToast('Quiz created.', 'success');
    } catch (err) {
      setError(err.message || 'Quiz could not be created.');
    } finally {
      setSaving(false);
    }
  };

  const updateQuizQuestion = (index, patch) => {
    setNewQuiz((current) => ({
      ...current,
      questions: current.questions.map((question, questionIndex) =>
        questionIndex === index ? { ...question, ...patch } : question
      )
    }));
  };

  const addQuizQuestion = () => {
    setNewQuiz((current) => ({
      ...current,
      questions: [...current.questions, { ...emptyQuestion }]
    }));
  };

  const removeQuizQuestion = (index) => {
    setNewQuiz((current) => ({
      ...current,
      questions: current.questions.filter((_, questionIndex) => questionIndex !== index)
    }));
  };

  const startQuiz = async (quiz) => {
    let quizWithQuestions = quiz;
    
    // Ensure we have questions before starting. If the list view didn't include them, fetch now.
    if (!quiz.questions || quiz.questions.length === 0) {
      setSaving(true);
      try {
        quizWithQuestions = await api.getQuizById(quiz.id);
      } catch (err) {
        setError('Could not load quiz questions. Please try again.');
        return;
      } finally {
        setSaving(false);
      }
    }

    setActiveQuiz(quizWithQuestions);
    setQuizAnswers({});
    setQuizResult(null);
    setQuizStartedAt(Date.now());
    setSecondsLeft((quizWithQuestions.timerMinutes || 30) * 60);
  };

  const openAssignStudents = async () => {
    setShowAssignStudents(true);
    setSelectedStudentId('');
    setError('');

    try {
      const data = await api.getStudents();
      setStudents(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Students could not be loaded.');
    }
  };

  const handleAssignStudent = async (e) => {
    e.preventDefault();
    if (!selectedStudentId) return;

    setSaving(true);
    setError('');

    try {
      await api.assignCourse(courseId, selectedStudentId);
      setSelectedStudentId('');
      setShowAssignStudents(false);
    } catch (err) {
      setError(err.message || 'Course could not be assigned.');
    } finally {
      setSaving(false);
    }
  };

  const openAssignQuiz = async (quizId) => {
    if (!canAssignOrReviewQuizzes) {
      setError('Only instructors and admins can assign quizzes.');
      return;
    }
    setSelectedQuizId(quizId);
    setShowAssignQuiz(true);
    setSelectedStudentId('');
    setError('');
    try {
      const data = await api.getStudents();
      setStudents(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Students could not be loaded.');
    }
  };

  const handleAssignQuiz = async (e) => {
    e.preventDefault();
    if (!selectedStudentId) return;

    setSaving(true);
    setError('');

    try {
      await api.assignQuiz(selectedQuizId, selectedStudentId);
      setShowAssignQuiz(false);
      setSelectedStudentId('');
    } catch (err) {
      setError(err.message || 'Quiz could not be assigned.');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteQuiz = async (quizId) => {
    if (!quizId) return;
    setSaving(true);
    try {
      await api.deleteQuiz(quizId);
      await fetchLessonQuizzes(selectedLesson.id);
      pushToast('Quiz deleted.', 'success');
    } catch (err) {
      setError(err.message || 'Failed to delete quiz.');
    } finally {
      setPendingDeleteQuizId(null);
      setSaving(false);
    }
  };

  const handleDeleteLesson = async (lessonId) => {
    if (!lessonId) return;
    setSaving(true);
    try {
      await api.deleteLesson(lessonId);
      await fetchCourseDetails();
      pushToast('Lesson deleted.', 'success');
    } catch (err) {
      setError(err.message || 'Failed to delete lesson.');
    } finally {
      setPendingDeleteLessonId(null);
      setSaving(false);
    }
  };

  const openQuizReview = async (quiz) => {
    if (!canAssignOrReviewQuizzes) {
      setError('Only instructors and admins can review quiz submissions.');
      return;
    }
    setReviewQuiz(quiz);
    setError('');

    try {
      const data = await api.getQuizResults(quiz.id);
      setQuizResults(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Quiz results could not be loaded.');
    }
  };

  const handleReviewAnswer = async (answerId, marks) => {
    if (!reviewQuiz) return;
    setSaving(true);

    try {
      await api.reviewAnswer(answerId, Number(marks));
      const data = await api.getQuizResults(reviewQuiz.id);
      setQuizResults(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.message || 'Answer could not be reviewed.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="course-page">
        <div className="course-loading" />
      </div>
    );
  }

  if (error && !course) {
    return (
      <div className="course-page">
        <button className="btn-back" onClick={() => navigate('/dashboard')}>
          <FiArrowLeft /> Back to dashboard
        </button>
        <div className="empty-state">
          <FiBookOpen />
          <h3>Course unavailable</h3>
          <p>{error}</p>
          <button className="btn-primary" onClick={fetchCourseDetails}>Retry</button>
        </div>
      </div>
    );
  }

  const goToNextLesson = () => {
    if (selectedIndex < lessons.length - 1) {
      setSelectedLesson(lessons[selectedIndex + 1]);
      window.scrollTo(0, 0);
    }
  };

  return (
    <div className="course-page">
      <button className="btn-back" onClick={() => navigate('/dashboard')}>
        <FiArrowLeft /> Back to dashboard
      </button>

      <header className="course-header">
        <div>
          <p className="eyebrow">{course?.instructorName || 'EduForge course'}</p>
          <h1>{course?.title}</h1>
          <p>{course?.description || 'This course is ready for lessons and learning materials.'}</p>
        </div>

        {canManageLessons && (
          <div className="course-header-actions">
            <button className="btn-secondary" onClick={openAssignStudents}>
              <FiUserPlus /> Assign students
            </button>
            <button className="btn-secondary" onClick={() => setShowCreateQuiz(true)} disabled={!selectedLesson}>
              <FiFileText /> Create quiz
            </button>
            <button className="btn-primary" onClick={() => setShowAddLesson(true)}>
              <FiPlus /> Add lesson
            </button>
            <small className="permission-note">Instructors and admins can assign and review quizzes.</small>
          </div>
        )}
      </header>

      <StatusBanner type="error" message={error} actionLabel="Retry" onAction={fetchCourseDetails} />

      <div className="course-body">
        <aside className="lessons-sidebar">
          <div className="lesson-progress">
            <span>{completionPercent}%</span>
            <div>
              <strong>Course progress</strong>
              <small>{lessons.length ? `Lesson ${selectedIndex + 1} of ${lessons.length}` : 'No lessons yet'}</small>
            </div>
          </div>

          <div className="progress-track">
            <span style={{ width: `${completionPercent}%` }} />
          </div>

          <h2>Lessons</h2>
          {lessons.length === 0 ? (
            <p className="sidebar-empty">Add the first lesson to give this course structure.</p>
          ) : (
            <ul>
              {lessons.map((lesson, index) => (
                <li key={lesson.id}>
                  <button
                    className={selectedLesson?.id === lesson.id ? 'active' : ''}
                    onClick={() => setSelectedLesson(lesson)}
                  >
                    <span>{index + 1}</span>
                    <strong>{lesson.title}</strong>
                    {canManageLessons && (
                      <FiTrash2 
                        className="delete-icon" 
                        onClick={(e) => { e.stopPropagation(); setPendingDeleteLessonId(lesson.id); }} 
                      />
                    )}
                    {index <= selectedIndex && <FiCheckCircle />}
                  </button>
                </li>
              ))}
            </ul>
          )}
        </aside>

        <main className="lesson-content">
          {selectedLesson ? (
            <>
              <div className="lesson-title-row">
                <div>
                  <p className="eyebrow">Lesson {selectedIndex + 1}</p>
                  <h2>{selectedLesson.title}</h2>
                </div>
                <span className="lesson-type">
                  {selectedLesson.videoUrl ? <FiPlayCircle /> : <FiFileText />}
                  {selectedLesson.videoUrl ? 'Video lesson' : 'Reading'}
                </span>
              </div>

              {selectedLesson.videoUrl && (
                <VideoPlayer url={selectedLesson.videoUrl} title={selectedLesson.title} />
              )}

              <article className="content">
                {selectedLesson.content || 'Lesson notes have not been added yet.'}
              </article>

              {selectedLesson.resourceUrl && (
                <a href={api.getAssetUrl(selectedLesson.resourceUrl)} className="resource-link" target="_blank" rel="noreferrer">
                  <FiFileText /> Open resource
                </a>
              )}

              {selectedIndex < lessons.length - 1 && (
                <div className="next-lesson-prompt">
                  <button className="btn-secondary" onClick={goToNextLesson}>
                    Next: {lessons[selectedIndex + 1].title} <FiChevronRight />
                  </button>
                </div>
              )}

              <section className="quiz-section">
                <div className="quiz-section-header">
                  <div>
                    <p className="eyebrow">Exam</p>
                    <h3>Lesson quizzes</h3>
                    {!canAssignOrReviewQuizzes && (
                      <small className="permission-note">Only instructors/admins can assign or review quizzes.</small>
                    )}
                  </div>
                  {canManageLessons && (
                    <button className="btn-secondary" onClick={() => setShowCreateQuiz(true)}>
                      <FiPlus /> New quiz
                    </button>
                  )}
                </div>

                {quizResult && (
                  <div className="quiz-result">
                    <strong>{quizResult.status === 'PENDING_REVIEW' ? 'Submitted for review' : `Grade: ${quizResult.grade}`}</strong>
                    <span>{quizResult.score}/{quizResult.totalMarks} marks · {quizResult.percentage}%</span>
                  </div>
                )}

                {quizzes.length === 0 ? (
                  <p className="sidebar-empty">No quiz is attached to this lesson yet.</p>
                ) : (
                  <div className="quiz-list">
                    {quizzes.map((quiz) => {
                      const quizResult = studentQuizResultsMap[quiz.id];
                      const isPassed = quizResult && quizResult.percentage >= quiz.passingScore;
                      
                      return (
                        <article className="quiz-card" key={quiz.id}>
                          <div>
                            <h4>{quiz.title}</h4>
                            <p>{quiz.description || 'Complete this exam to check your understanding.'}</p>
                            <small>{quiz.totalQuestions} questions · {quiz.totalMarks} marks · {quiz.timerMinutes} min · Pass {quiz.passingScore}%</small>
                          </div>
                          <div className="quiz-card-actions">
                            {!canManageLessons && (
                              <>
                                {quizResult ? (
                                  <div className="quiz-result-card">
                                    <div className="result-status">
                                      <strong className={isPassed ? 'pass' : 'fail'}>
                                        {quizResult.status === 'PENDING_REVIEW' ? 'Submitted for review' : (isPassed ? '✓ PASS' : '✗ FAIL')}
                                      </strong>
                                      <span className="grade">{quizResult.grade}</span>
                                    </div>
                                    <div className="result-details">
                                      <span className="score">{quizResult.score}/{quizResult.totalMarks} marks</span>
                                      <span className="percentage">{quizResult.percentage}%</span>
                                    </div>
                                  </div>
                                ) : (
                                  <button className="btn-primary" onClick={() => startQuiz(quiz)}>
                                    Start exam
                                  </button>
                                )}
                              </>
                            )}
                            {canAssignOrReviewQuizzes && (
                              <>
                                <button className="btn-secondary" onClick={() => openAssignQuiz(quiz.id)}>Assign</button>
                                <button className="btn-secondary" onClick={() => openQuizReview(quiz)}>Review</button>
                                <button className="btn-danger-outline" onClick={() => setPendingDeleteQuizId(quiz.id)} title="Delete Quiz"><FiTrash2 /></button>
                              </>
                            )}
                          </div>
                        </article>
                      );
                    })}
                  </div>
                )}
              </section>
            </>
          ) : (
            <div className="empty-state compact">
              <FiBookOpen />
              <h3>No lesson selected</h3>
              <p>Select a lesson from the left to begin.</p>
            </div>
          )}
        </main>
      </div>

      {showAddLesson && canManageLessons && (
        <div className="modal" role="dialog" aria-modal="true" aria-labelledby="add-lesson-title">
          <form className="modal-content" onSubmit={handleAddLesson}>
            <div className="modal-header">
              <div>
                <p className="eyebrow">Course content</p>
                <h3 id="add-lesson-title">Add new lesson</h3>
              </div>
              <button className="icon-button" type="button" onClick={() => setShowAddLesson(false)} aria-label="Close">
                <FiX />
              </button>
            </div>

            <label>
              Lesson title
              <input
                type="text"
                placeholder="Example: Setting up your workspace"
                value={newLesson.title}
                onChange={(e) => setNewLesson({ ...newLesson, title: e.target.value })}
                required
              />
            </label>

            <label>
              Lesson content
              <textarea
                placeholder="Write the lesson summary, instructions, or transcript."
                value={newLesson.content}
                onChange={(e) => setNewLesson({ ...newLesson, content: e.target.value })}
                rows="6"
              />
            </label>

            <label>
              Video URL
              <input
                type="url"
                placeholder="https://youtu.be/... or https://example.com/lesson.mp4"
                value={newLesson.videoUrl}
                onChange={(e) => setNewLesson({ ...newLesson, videoUrl: e.target.value })}
              />
            </label>

            <div className="file-drop-wrap">
              <label className="file-drop">
              <FiUpload />
              <span>
                Upload video file
                <small>{videoFile ? `${videoFile.name} (${formatBytes(videoFile.size)})` : 'MP4, WebM, MOV, or another browser-supported video file'}</small>
              </span>
              <input
                type="file"
                accept="video/*"
                onChange={(e) => setVideoFile(e.target.files?.[0] || null)}
              />
              </label>
              {videoFile && (
                <button className="file-clear" type="button" onClick={() => setVideoFile(null)}>
                  Remove video file
                </button>
              )}
            </div>

            <div className="file-drop-wrap">
              <label className="file-drop">
                <FiPaperclip />
                <span>
                  Attach resource file
                  <small>{resourceFile ? `${resourceFile.name} (${formatBytes(resourceFile.size)})` : 'PDF, DOCX, PPT, ZIP, image, code file, or any supporting material'}</small>
                </span>
                <input
                  type="file"
                  onChange={(e) => setResourceFile(e.target.files?.[0] || null)}
                />
              </label>
              {resourceFile && (
                <button className="file-clear" type="button" onClick={() => setResourceFile(null)}>
                  Remove resource file
                </button>
              )}
            </div>

            <div className="modal-actions">
              <button className="btn-secondary" type="button" onClick={() => setShowAddLesson(false)}>
                Cancel
              </button>
              <button className="btn-primary" type="submit" disabled={saving}>
                {saving ? 'Adding...' : 'Add lesson'}
              </button>
            </div>
          </form>
        </div>
      )}

      {showCreateQuiz && canManageLessons && selectedLesson && (
        <div className="modal" role="dialog" aria-modal="true" aria-labelledby="create-quiz-title">
          <form className="modal-content quiz-modal" onSubmit={handleCreateQuiz}>
            <div className="modal-header">
              <div>
                <p className="eyebrow">Assessment</p>
                <h3 id="create-quiz-title">Create quiz</h3>
              </div>
              <button className="icon-button" type="button" onClick={() => setShowCreateQuiz(false)} aria-label="Close">
                <FiX />
              </button>
            </div>

            <label>
              Quiz title
              <input
                value={newQuiz.title}
                onChange={(e) => setNewQuiz({ ...newQuiz, title: e.target.value })}
                placeholder="Example: Module 1 assessment"
                required
              />
            </label>

            <label>
              Instructions
              <textarea
                value={newQuiz.description}
                onChange={(e) => setNewQuiz({ ...newQuiz, description: e.target.value })}
                placeholder="Tell students what this exam covers."
                rows="3"
              />
            </label>

            <div className="quiz-settings-grid">
              <label>
                Timer minutes
                <input
                  type="number"
                  min="1"
                  value={newQuiz.timerMinutes}
                  onChange={(e) => setNewQuiz({ ...newQuiz, timerMinutes: e.target.value })}
                  required
                />
              </label>
              <label>
                Passing score %
                <input
                  type="number"
                  min="1"
                  max="100"
                  value={newQuiz.passingScore}
                  onChange={(e) => setNewQuiz({ ...newQuiz, passingScore: e.target.value })}
                  required
                />
              </label>
            </div>

            <div className="question-builder-list">
              {newQuiz.questions.map((question, index) => (
                <fieldset className="question-builder" key={index}>
                  <div className="question-builder-header">
                    <strong>Question {index + 1}</strong>
                    {newQuiz.questions.length > 1 && (
                      <button type="button" className="file-clear" onClick={() => removeQuizQuestion(index)}>
                        Remove
                      </button>
                    )}
                  </div>

                  <label>
                    Question
                    <textarea
                      value={question.questionText}
                      onChange={(e) => updateQuizQuestion(index, { questionText: e.target.value })}
                      rows="3"
                      required
                    />
                  </label>

                  <div className="quiz-settings-grid">
                    <label>
                      Type
                      <select
                        value={question.questionType}
                        onChange={(e) => updateQuizQuestion(index, { questionType: e.target.value })}
                      >
                        <option value="MCQ">MCQ</option>
                        <option value="DESCRIPTIVE">Descriptive</option>
                      </select>
                    </label>
                    <label>
                      Marks
                      <input
                        type="number"
                        min="1"
                        value={question.marks}
                        onChange={(e) => updateQuizQuestion(index, { marks: e.target.value })}
                        required
                      />
                    </label>
                  </div>

                  {question.questionType === 'MCQ' ? (
                    <>
                      {['A', 'B', 'C', 'D'].map((option) => (
                        <label key={option}>
                          Option {option}
                          <input
                            value={question[`option${option}`]}
                            onChange={(e) => updateQuizQuestion(index, { [`option${option}`]: e.target.value })}
                            required
                          />
                        </label>
                      ))}
                      <label>
                        Correct answer
                        <select
                          value={question.correctAnswer}
                          onChange={(e) => updateQuizQuestion(index, { correctAnswer: e.target.value })}
                        >
                          <option value="A">A</option>
                          <option value="B">B</option>
                          <option value="C">C</option>
                          <option value="D">D</option>
                        </select>
                      </label>
                    </>
                  ) : (
                    <label>
                      Evaluation guide
                      <textarea
                        value={question.sampleAnswer}
                        onChange={(e) => updateQuizQuestion(index, { sampleAnswer: e.target.value })}
                        placeholder="Private notes for the instructor while reviewing."
                        rows="3"
                      />
                    </label>
                  )}
                </fieldset>
              ))}
            </div>

            <button className="btn-secondary" type="button" onClick={addQuizQuestion}>
              <FiPlus /> Add question
            </button>

            <div className="modal-actions">
              <button className="btn-secondary" type="button" onClick={() => setShowCreateQuiz(false)}>
                Cancel
              </button>
              <button className="btn-primary" type="submit" disabled={saving}>
                {saving ? 'Creating...' : 'Create quiz'}
              </button>
            </div>
          </form>
        </div>
      )}

      {activeQuiz && (
        <div className="modal" role="dialog" aria-modal="true" aria-labelledby="take-quiz-title">
          <form className="modal-content quiz-modal" onSubmit={(e) => { e.preventDefault(); submitActiveQuiz(); }}>
            <div className="modal-header">
              <div>
                <p className="eyebrow">Timed exam</p>
                <h3 id="take-quiz-title">{activeQuiz.title}</h3>
              </div>
              <strong className={secondsLeft <= 60 ? 'timer danger-timer' : 'timer'}>{formatTime(secondsLeft)}</strong>
            </div>

            <p className="sidebar-empty">{activeQuiz.description}</p>

            {(activeQuiz.questions || []).map((question, index) => (
              <fieldset className="exam-question" key={question.id}>
                <legend>Question {index + 1} · {question.marks} marks</legend>
                <p>{question.questionText}</p>
                {question.questionType === 'MCQ' ? (
                  <div className="exam-options">
                    {['A', 'B', 'C', 'D'].map((option) => (
                      <label key={option}>
                        <input
                          type="radio"
                          name={`question-${question.id}`}
                          value={option}
                          checked={quizAnswers[question.id]?.selectedOption === option}
                          onChange={() => setQuizAnswers({
                            ...quizAnswers,
                            [question.id]: { selectedOption: option }
                          })}
                        />
                        <span>{option}. {question[`option${option}`]}</span>
                      </label>
                    ))}
                  </div>
                ) : (
                  <textarea
                    rows="6"
                    placeholder="Write your answer here."
                    value={quizAnswers[question.id]?.textAnswer || ''}
                    onChange={(e) => setQuizAnswers({
                      ...quizAnswers,
                      [question.id]: { textAnswer: e.target.value }
                    })}
                  />
                )}
              </fieldset>
            ))}

            <div className="modal-actions">
              <button
                className="btn-secondary"
                type="button"
                onClick={() => {
                  setActiveQuiz(null);
                  setQuizAnswers({});
                  setQuizStartedAt(null);
                  setSecondsLeft(0);
                }}
              >
                Exit
              </button>
              <button className="btn-primary" type="submit" disabled={saving}>
                {saving ? 'Submitting...' : 'Submit exam'}
              </button>
            </div>
          </form>
        </div>
      )}

      {reviewQuiz && canManageLessons && (
        <div className="modal" role="dialog" aria-modal="true" aria-labelledby="review-quiz-title">
          <div className="modal-content quiz-modal">
            <div className="modal-header">
              <div>
                <p className="eyebrow">Evaluation</p>
                <h3 id="review-quiz-title">{reviewQuiz.title} results</h3>
              </div>
              <button className="icon-button" type="button" onClick={() => setReviewQuiz(null)} aria-label="Close">
                <FiX />
              </button>
            </div>

            {quizResults.length === 0 ? (
              <p className="sidebar-empty">No submissions yet.</p>
            ) : (
              <div className="result-review-list">
                {quizResults.map((result) => (
                  <article className="result-review-card" key={result.id}>
                    <div className="quiz-result">
                      <div className="result-header">
                        <div>
                          <strong>{result.status === 'PENDING_REVIEW' ? 'Pending review' : `Grade ${result.grade}`}</strong>
                          <span className="student-name">{result.studentName}</span>
                          <span className="student-email">{result.studentEmail}</span>
                        </div>
                        <div className="result-meta">
                          <span className="result-score">{result.score}/{result.totalMarks} marks · {result.percentage}%</span>
                          <span className="result-time">Submitted: {formatSubmitDate(result.completedAt)}</span>
                          <span className="result-duration">Time taken: {formatTimeTaken(result.timeTakenSeconds)}</span>
                        </div>
                      </div>
                    </div>

                    {(result.answers || [])
                      .filter((answer) => answer.questionType === 'DESCRIPTIVE')
                      .map((answer) => (
                        <div className="answer-review" key={answer.id}>
                          <strong>{answer.questionText}</strong>
                          <p>{answer.textAnswer || 'No answer submitted.'}</p>
                          <label>
                            Marks out of {answer.maxMarks}
                            <input
                              type="number"
                              min="0"
                              max={answer.maxMarks}
                              defaultValue={answer.awardedMarks}
                              onBlur={(e) => handleReviewAnswer(answer.id, e.target.value)}
                              disabled={saving}
                            />
                          </label>
                        </div>
                      ))}
                  </article>
                ))}
              </div>
            )}
          </div>
        </div>
      )}

      {showAssignQuiz && (
        <div className="modal" role="dialog" aria-modal="true">
          <form className="modal-content" onSubmit={handleAssignQuiz}>
            <div className="modal-header">
              <h3>Assign Quiz to Student</h3>
              <button className="icon-button" type="button" onClick={() => setShowAssignQuiz(false)}><FiX /></button>
            </div>
            <label>
              Select Student
              <select 
                value={selectedStudentId} 
                onChange={(e) => setSelectedStudentId(e.target.value)}
                required
              >
                <option value="">Select a student</option>
                {students.map(s => (
                  <option key={s.id} value={s.id}>{s.fullName} ({s.email})</option>
                ))}
              </select>
            </label>
            <div className="modal-actions">
              <button className="btn-secondary" type="button" onClick={() => setShowAssignQuiz(false)}>
                Cancel
              </button>
              <button className="btn-primary" type="submit" disabled={saving || !selectedStudentId}>
                {saving ? 'Assigning...' : 'Assign Quiz'}
              </button>
            </div>
          </form>
        </div>
      )}

      {showAssignStudents && canManageLessons && (
        <div className="modal" role="dialog" aria-modal="true" aria-labelledby="assign-students-title">
          <form className="modal-content" onSubmit={handleAssignStudent}>
            <div className="modal-header">
              <div>
                <p className="eyebrow">Course access</p>
                <h3 id="assign-students-title">Assign course to student</h3>
              </div>
              <button className="icon-button" type="button" onClick={() => setShowAssignStudents(false)} aria-label="Close">
                <FiX />
              </button>
            </div>

            <label>
              Student
              <select
                value={selectedStudentId}
                onChange={(e) => setSelectedStudentId(e.target.value)}
                required
              >
                <option value="">Select a student</option>
                {students.map((student) => (
                  <option key={student.id} value={student.id}>
                    {student.fullName} ({student.email})
                  </option>
                ))}
              </select>
            </label>

            {students.length === 0 && (
              <p className="sidebar-empty">No active student accounts found.</p>
            )}

            <div className="modal-actions">
              <button className="btn-secondary" type="button" onClick={() => setShowAssignStudents(false)}>
                Cancel
              </button>
              <button className="btn-primary" type="submit" disabled={saving || students.length === 0}>
                {saving ? 'Assigning...' : 'Assign course'}
              </button>
            </div>
          </form>
        </div>
      )}

      <ConfirmDialog
        open={Boolean(pendingDeleteQuizId)}
        title="Delete quiz"
        message="This will permanently remove the quiz and its submissions. Continue?"
        confirmLabel={saving ? 'Deleting...' : 'Delete quiz'}
        danger
        onCancel={() => setPendingDeleteQuizId(null)}
        onConfirm={() => handleDeleteQuiz(pendingDeleteQuizId)}
      />

      <ConfirmDialog
        open={Boolean(pendingDeleteLessonId)}
        title="Delete lesson"
        message="This will remove the lesson and associated quizzes. Continue?"
        confirmLabel={saving ? 'Deleting...' : 'Delete lesson'}
        danger
        onCancel={() => setPendingDeleteLessonId(null)}
        onConfirm={() => handleDeleteLesson(pendingDeleteLessonId)}
      />

      <ToastStack toasts={toasts} onDismiss={removeToast} />
    </div>
  );
}

function VideoPlayer({ url, title }) {
  const youtubeEmbedUrl = getYouTubeEmbedUrl(url);

  if (youtubeEmbedUrl) {
    return (
      <div className="video-frame">
        <iframe
          src={youtubeEmbedUrl}
          title={title}
          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
          allowFullScreen
        />
      </div>
    );
  }

  return (
    <div className="video-frame">
      <video controls>
        <source src={api.getAssetUrl(url)} type="video/mp4" />
        Your browser does not support the video tag.
      </video>
    </div>
  );
}

function formatBytes(bytes) {
  if (!bytes) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  const index = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1);
  const value = bytes / Math.pow(1024, index);
  return `${value.toFixed(value >= 10 || index === 0 ? 0 : 1)} ${units[index]}`;
}

function formatTime(totalSeconds) {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${String(seconds).padStart(2, '0')}`;
}

function formatTimeTaken(totalSeconds) {
  if (!totalSeconds) return '-';
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  
  if (hours > 0) {
    return `${hours}h ${minutes}m ${seconds}s`;
  }
  if (minutes > 0) {
    return `${minutes}m ${seconds}s`;
  }
  return `${seconds}s`;
}

function formatSubmitDate(dateString) {
  if (!dateString) return '-';
  const date = new Date(dateString);
  const options = { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' };
  return date.toLocaleDateString('en-US', options);
}

function getYouTubeEmbedUrl(url) {
  try {
    const parsedUrl = new URL(url);
    const host = parsedUrl.hostname.replace('www.', '');
    let videoId = '';

    if (host === 'youtube.com' || host === 'm.youtube.com') {
      if (parsedUrl.pathname.startsWith('/watch')) {
        videoId = parsedUrl.searchParams.get('v') || '';
      } else if (parsedUrl.pathname.startsWith('/shorts/')) {
        videoId = parsedUrl.pathname.split('/')[2] || '';
      } else if (parsedUrl.pathname.startsWith('/embed/')) {
        videoId = parsedUrl.pathname.split('/')[2] || '';
      }
    }

    if (host === 'youtu.be') {
      videoId = parsedUrl.pathname.slice(1);
    }

    return videoId ? `https://www.youtube.com/embed/${videoId}` : null;
  } catch (err) {
    return null;
  }
}
