export function getApiBaseUrl() {
  const value = window.__APP_CONFIG__?.API_BASE_URL || process.env.REACT_APP_API_BASE_URL;
  if (!value) {
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
      return 'http://localhost:8080/api';
    }

    return '/api';
  }

  const normalized = value.replace(/\/+$/, '');
  return normalized.endsWith('/api') ? normalized : `${normalized}/api`;
}
