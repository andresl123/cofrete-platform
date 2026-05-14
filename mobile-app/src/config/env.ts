const DEFAULT_CORE_API_BASE_URL = 'http://localhost:8080';

export function getCoreApiBaseUrl() {
  return process.env.EXPO_PUBLIC_CORE_API_BASE_URL ?? DEFAULT_CORE_API_BASE_URL;
}
