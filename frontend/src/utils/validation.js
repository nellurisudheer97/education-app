export function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

export function validatePassword(password) {
  return typeof password === 'string' && password.length >= 8;
}

export function validatePasswordMatch(password, confirmPassword) {
  return password === confirmPassword && password.length > 0;
}

export function validateRequired(value) {
  return typeof value === 'string' && value.trim().length > 0;
}
