import { validateEmail, validatePassword, validateRequired } from './validation';

describe('validation utilities', () => {
  test('validateEmail accepts valid and rejects invalid addresses', () => {
    expect(validateEmail('student@example.com')).toBe(true);
    expect(validateEmail('invalid-email')).toBe(false);
  });

  test('validatePassword requires minimum length', () => {
    expect(validatePassword('12345678')).toBe(true);
    expect(validatePassword('short')).toBe(false);
  });

  test('validateRequired checks trimmed values', () => {
    expect(validateRequired('  course  ')).toBe(true);
    expect(validateRequired('   ')).toBe(false);
  });
});
