import { clearSession, getCurrentUser, hasActiveSession, saveSession } from './session';

describe('session service', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  test('saveSession stores and reads user identity', () => {
    saveSession({
      token: 'abc-token',
      userId: 10,
      role: 'INSTRUCTOR',
      fullName: 'Ada Lovelace'
    });

    expect(hasActiveSession()).toBe(true);
    expect(getCurrentUser()).toEqual({
      id: '10',
      role: 'INSTRUCTOR',
      fullName: 'Ada Lovelace'
    });
  });

  test('clearSession removes auth state only', () => {
    localStorage.setItem('theme', 'dark');
    saveSession({
      token: 'abc-token',
      userId: 20,
      role: 'STUDENT',
      fullName: 'Learner'
    });

    clearSession();

    expect(hasActiveSession()).toBe(false);
    expect(localStorage.getItem('theme')).toBe('dark');
  });
});
