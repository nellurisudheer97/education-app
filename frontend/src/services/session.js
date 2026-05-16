const SESSION_KEYS = ['token', 'userId', 'role', 'fullName'];
const normalizeRole = (role) => {
  if (role === 'DEVELOPER') return 'INSTRUCTOR';
  return role || 'STUDENT';
};

export const saveSession = (response) => {
  localStorage.setItem('token', String(response.token || ''));
  localStorage.setItem('userId', String(response.userId || ''));
  localStorage.setItem('role', String(normalizeRole(response.role)));
  localStorage.setItem('fullName', String(response.fullName || 'Learner'));
};

export const clearSession = () => {
  SESSION_KEYS.forEach((key) => localStorage.removeItem(key));
};

export const hasActiveSession = () => Boolean(localStorage.getItem('token'));

export const getCurrentUser = () => ({
  id: localStorage.getItem('userId'),
  role: normalizeRole(localStorage.getItem('role')),
  fullName: localStorage.getItem('fullName') || 'Learner'
});

export const isStaffRole = (role) => {
  const normalized = normalizeRole(role);
  return normalized === 'ADMIN' || normalized === 'INSTRUCTOR';
};

export const getRoleLabel = (role) => {
  const normalized = normalizeRole(role);
  if (normalized === 'ADMIN') return 'Admin';
  if (normalized === 'INSTRUCTOR') return 'Instructor';
  return 'Student';
};
