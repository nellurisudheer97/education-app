import { getApiBaseUrl } from '../utils/env';
import { clearSession } from './session';

const API_BASE_URL = getApiBaseUrl();
const API_ORIGIN = API_BASE_URL.replace(/\/api\/?$/, '');

const getToken = () => localStorage.getItem('token');

const buildHeaders = (headers = {}) => {
  const token = getToken();

  return {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...headers
  };
};

const handleResponse = async (response) => {
  const contentType = response.headers.get('content-type');
  const hasJson = contentType && contentType.includes('application/json');
  const rawBody = await response.text();
  const data = hasJson && rawBody ? JSON.parse(rawBody) : rawBody;

  if (response.status === 401) {
    clearSession();
    window.dispatchEvent(new CustomEvent('app:session-expired'));
    throw new Error('Your session expired. Please sign in again.');
  }

  if (response.status === 403) {
    throw new Error('You do not have permission to perform this action.');
  }

  if (!response.ok) {
    throw new Error(data?.message || data || `Request failed with status ${response.status}`);
  }

  return data;
};

const request = (path, options = {}) =>
  fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: buildHeaders(options.headers)
  }).then(handleResponse);

const uploadRequest = (path, body) =>
  fetch(`${API_BASE_URL}${path}`, {
    method: 'POST',
    headers: getToken() ? { Authorization: `Bearer ${getToken()}` } : {},
    body
  }).then(handleResponse);

const getAssetUrl = (url) => {
  if (!url) return '';
  if (/^https?:\/\//i.test(url)) return url;
  return `${API_ORIGIN}${url.startsWith('/') ? url : `/${url}`}`;
};

const normalizeUploadResponse = (response, type) => {
  if (!response) {
    throw new Error('Upload failed. Please try a smaller file or restart the backend.');
  }

  if (typeof response === 'string') {
    return {
      filename: response,
      url: `/api/upload/${type}/${response}`
    };
  }

  if (!response.url) {
    throw new Error('Upload finished, but the server did not return a file URL.');
  }

  return response;
};

const api = {
  register: (data) =>
    request('/auth/register', {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  login: (data) =>
    request('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  forgotPassword: (data) =>
    request('/auth/forgot-password', {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  verifyResetToken: (data) =>
    request('/auth/verify-reset-token', {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  resetPassword: (data) =>
    request('/auth/reset-password', {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  getCourseById: (id) => request(`/courses/${id}`),

  getInstructorCourses: (instructorId) => request(`/courses/instructor/${instructorId}`),

  getAssignedCourses: (studentId) => request(`/courses/student/${studentId}`),

  assignCourse: (courseId, studentId) =>
    request(`/courses/${courseId}/assign?studentId=${studentId}`, {
      method: 'POST'
    }),

  unassignCourse: (courseId, studentId) =>
    request(`/courses/${courseId}/assign?studentId=${studentId}`, {
      method: 'DELETE'
    }),

  getStudents: () => request('/users/students'),

  createCourse: (data, instructorId) =>
    request(`/courses?instructorId=${instructorId}`, {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  updateCourse: (id, data) =>
    request(`/courses/${id}`, {
      method: 'PUT',
      body: JSON.stringify(data)
    }),

  getLessonsByCourse: (courseId) => request(`/lessons/course/${courseId}`),

  createLesson: (data) =>
    request('/lessons', {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  getQuizzesByLesson: (lessonId) => request(`/quizzes/lesson/${lessonId}`),

  createQuiz: (data) =>
    request('/quizzes', {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  submitQuiz: (quizId, data) =>
    request(`/quizzes/${quizId}/submit`, {
      method: 'POST',
      body: JSON.stringify(data)
    }),

  getQuizResults: (quizId) => request(`/quizzes/${quizId}/results`),

  reviewAnswer: (answerId, awardedMarks) =>
    request(`/quizzes/answers/${answerId}/review`, {
      method: 'PUT',
      body: JSON.stringify({ awardedMarks })
    }),

  getQuizById: (quizId) => request(`/quizzes/${quizId}`),

  deleteQuiz: (quizId) =>
    request(`/quizzes/${quizId}`, {
      method: 'DELETE'
    }),

  deleteLesson: (lessonId) =>
    request(`/lessons/${lessonId}`, {
      method: 'DELETE'
    }),

  assignQuiz: (quizId, studentId) =>
    request(`/quizzes/${quizId}/assign?studentId=${studentId}`, {
      method: 'POST'
    }),

  uploadVideo: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return uploadRequest('/upload/video', formData).then((response) => normalizeUploadResponse(response, 'video'));
  },

  uploadResource: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return uploadRequest('/upload/resource', formData).then((response) => normalizeUploadResponse(response, 'resource'));
  },

  uploadSubmission: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return uploadRequest('/upload/submission', formData).then((response) => normalizeUploadResponse(response, 'submission'));
  },

  getAssetUrl
};

export default api;
