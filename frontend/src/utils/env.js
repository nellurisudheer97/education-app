export function getApiBaseUrl() {
  const value = process.env.REACT_APP_API_BASE_URL;
  if (!value) {
    return 'http://localhost:8080/api';
  }

  const normalized = value.replace(/\/+$/, '');
  return normalized.endsWith('/api') ? normalized : `${normalized}/api`;
}
