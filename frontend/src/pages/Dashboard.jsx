import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { FiBookOpen, FiLogOut, FiPlus, FiSearch, FiTrendingUp, FiUsers } from 'react-icons/fi';
import AppModal from '../components/AppModal';
import StatusBanner from '../components/StatusBanner';
import ToastStack from '../components/ToastStack';
import useToasts from '../hooks/useToasts';
import api from '../services/api';
import { clearSession, getCurrentUser, getRoleLabel, isStaffRole } from '../services/session';
import { validateRequired } from '../utils/validation';
import '../styles/dashboard.css';

const emptyCourse = { title: '', description: '' };

export default function Dashboard() {
  const [courses, setCourses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [query, setQuery] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newCourse, setNewCourse] = useState(emptyCourse);
  const [saving, setSaving] = useState(false);
  const [sortBy, setSortBy] = useState('title');
  const [activityFeed, setActivityFeed] = useState([]);
  const navigate = useNavigate();
  const { toasts, pushToast, removeToast } = useToasts();
  const user = getCurrentUser();
  const canManageCourses = isStaffRole(user.role);

  const roleWorkspace = useMemo(() => {
    if (user.role === 'ADMIN') return 'Admin command center';
    if (user.role === 'INSTRUCTOR') return 'Instructor workspace';
    return 'Learning dashboard';
  }, [user.role]);

  const addActivity = useCallback((action) => {
    const entry = { id: Date.now(), action, at: new Date().toISOString() };
    setActivityFeed((current) => [entry, ...current.slice(0, 5)]);
  }, []);

  const fetchCourses = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = canManageCourses
        ? await api.getInstructorCourses(user.id)
        : await api.getAssignedCourses(user.id);
      const normalized = Array.isArray(data) ? data : [];
      setCourses(normalized);
      addActivity(`Loaded ${normalized.length} courses`);
    } catch (err) {
      setError(err.message || 'Courses could not be loaded right now.');
    } finally {
      setLoading(false);
    }
  }, [addActivity, canManageCourses, user.id]);

  useEffect(() => {
    fetchCourses();
  }, [fetchCourses]);

  const filteredCourses = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    const searchable = normalizedQuery
      ? courses.filter((course) =>
          [course.title, course.description, course.instructorName]
            .filter(Boolean)
            .some((value) => value.toLowerCase().includes(normalizedQuery))
        )
      : courses;
    return [...searchable].sort((a, b) => {
      if (sortBy === 'lessons') return (b.totalLessons || 0) - (a.totalLessons || 0);
      return (a.title || '').localeCompare(b.title || '');
    });
  }, [courses, query, sortBy]);

  const stats = useMemo(() => {
    const lessonCount = courses.reduce((total, course) => total + (course.totalLessons || 0), 0);
    const instructorCount = new Set(courses.map((course) => course.instructorName).filter(Boolean)).size;
    return [
      { label: 'Courses', value: courses.length, icon: <FiBookOpen /> },
      { label: 'Lessons', value: lessonCount, icon: <FiTrendingUp /> },
      { label: canManageCourses ? 'Managed by you' : 'Instructors', value: canManageCourses ? courses.length : instructorCount, icon: <FiUsers /> }
    ];
  }, [courses, canManageCourses]);

  const handleCreateCourse = async (e) => {
    e.preventDefault();
    if (!validateRequired(newCourse.title)) {
      setError('Course title is required.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      await api.createCourse(
        { title: newCourse.title.trim(), description: newCourse.description.trim() },
        user.id
      );
      setNewCourse(emptyCourse);
      setShowCreateModal(false);
      pushToast('Course created successfully.', 'success');
      addActivity(`Created course: ${newCourse.title.trim()}`);
      await fetchCourses();
    } catch (err) {
      setError(err.message || 'Course could not be created.');
    } finally {
      setSaving(false);
    }
  };

  const handleLogout = () => {
    clearSession();
    navigate('/login', { replace: true });
  };

  return (
    <div className="dashboard-container">
      <nav className="app-nav">
        <button className="brand-button" onClick={() => navigate('/dashboard')} aria-label="Go to dashboard">
          <span className="brand-mark"><FiBookOpen /></span>
          <span>EduForge</span>
        </button>
        <div className="nav-actions">
          <div className="user-chip">
            <span>{user.fullName}</span>
            <small>{getRoleLabel(user.role)}</small>
          </div>
          <button className="icon-button danger" onClick={handleLogout} aria-label="Sign out" title="Sign out">
            <FiLogOut />
          </button>
        </div>
      </nav>

      <main className="dashboard-content">
        <section className="dashboard-hero">
          <div>
            <p className="eyebrow">{roleWorkspace}</p>
            <h1>{canManageCourses ? 'Shape courses people want to finish.' : 'Pick up where your learning left off.'}</h1>
            <p>Browse courses, review lessons, and keep the next learning action obvious.</p>
          </div>
          {canManageCourses && (
            <button className="btn-primary hero-action" onClick={() => setShowCreateModal(true)}>
              <FiPlus /> New course
            </button>
          )}
        </section>

        <section className="stats-grid" aria-label="Dashboard summary">
          {stats.map((stat) => (
            <div className="stat-card" key={stat.label}>
              <span>{stat.icon}</span>
              <div>
                <strong>{stat.value}</strong>
                <small>{stat.label}</small>
              </div>
            </div>
          ))}
        </section>

        <section className="toolbar">
          <div>
            <h2>Course catalog</h2>
            <p>{filteredCourses.length} {filteredCourses.length === 1 ? 'course' : 'courses'} available</p>
          </div>
          <label className="search-box">
            <FiSearch />
            <input type="search" placeholder="Search courses" value={query} onChange={(e) => setQuery(e.target.value)} />
          </label>
          <label className="search-box">
            <select value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="title">Sort: Title</option>
              <option value="lessons">Sort: Most lessons</option>
            </select>
          </label>
        </section>

        <StatusBanner type="error" message={error} actionLabel="Retry" onAction={fetchCourses} />

        {loading ? (
          <div className="courses-grid">{[1, 2, 3].map((item) => <div className="course-card skeleton-card" key={item} />)}</div>
        ) : filteredCourses.length === 0 ? (
          <div className="empty-state">
            <FiBookOpen />
            <h3>{query ? 'No matching courses' : 'No courses yet'}</h3>
            <p>{query ? 'Try a different search term.' : canManageCourses ? 'Create the first course to make this workspace useful.' : 'No courses have been assigned to you yet.'}</p>
            {canManageCourses && !query && (
              <button className="btn-primary" onClick={() => setShowCreateModal(true)}>
                <FiPlus /> Create course
              </button>
            )}
          </div>
        ) : (
          <div className="courses-grid">
            {filteredCourses.map((course) => (
              <article
                key={course.id}
                className="course-card"
                onClick={() => navigate(`/course/${course.id}`)}
                tabIndex="0"
                onKeyDown={(e) => e.key === 'Enter' && navigate(`/course/${course.id}`)}
              >
                <div className="course-thumbnail">{course.thumbnailUrl ? <img src={course.thumbnailUrl} alt="" /> : <span>{getInitials(course.title)}</span>}</div>
                <div className="course-card-body">
                  <p className="course-kicker">{course.totalLessons || 0} lessons</p>
                  <h3>{course.title}</h3>
                  <p>{course.description || 'No description has been added yet.'}</p>
                </div>
                <footer className="course-meta">
                  <span>{course.instructorName || 'EduForge instructor'}</span>
                  <strong>{canManageCourses ? 'Manage course' : 'Open course'}</strong>
                </footer>
              </article>
            ))}
          </div>
        )}

        <section className="activity-panel">
          <h3>Recent activity</h3>
          {activityFeed.length === 0 ? (
            <p className="sidebar-empty">No activity yet.</p>
          ) : (
            <ul>
              {activityFeed.map((entry) => (
                <li key={entry.id}>
                  <span>{entry.action}</span>
                  <small>{new Date(entry.at).toLocaleTimeString()}</small>
                </li>
              ))}
            </ul>
          )}
        </section>
      </main>

      {showCreateModal && (
        <AppModal
          title="Create new course"
          subtitle="Course setup"
          labelledBy="create-course-title"
          onClose={() => setShowCreateModal(false)}
          actions={
            <>
              <button className="btn-secondary" type="button" onClick={() => setShowCreateModal(false)}>Cancel</button>
              <button className="btn-primary" type="submit" form="create-course-form" disabled={saving}>{saving ? 'Creating...' : 'Create course'}</button>
            </>
          }
        >
          <form id="create-course-form" onSubmit={handleCreateCourse}>
            <label>
              Course title
              <input type="text" placeholder="Example: React fundamentals" value={newCourse.title} onChange={(e) => setNewCourse({ ...newCourse, title: e.target.value })} required />
            </label>
            <label>
              Course description
              <textarea placeholder="What will learners be able to do after this course?" value={newCourse.description} onChange={(e) => setNewCourse({ ...newCourse, description: e.target.value })} rows="5" />
            </label>
          </form>
        </AppModal>
      )}

      <ToastStack toasts={toasts} onDismiss={removeToast} />
    </div>
  );
}

function getInitials(title = 'Course') {
  return title
    .split(' ')
    .filter(Boolean)
    .slice(0, 2)
    .map((word) => word[0])
    .join('')
    .toUpperCase();
}
