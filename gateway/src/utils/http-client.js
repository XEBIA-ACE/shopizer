import axios from 'axios';

const IDEMPOTENT_METHODS = new Set(['get', 'head', 'options', 'put', 'delete']);

export function isRetryable(error) {
  const method = error.config?.method?.toLowerCase();
  return !error.response && IDEMPOTENT_METHODS.has(method);
}

export function createHttpClient({ baseURL, timeout, retries = 0 }) {
  const client = axios.create({
    baseURL,
    timeout,
    validateStatus: () => true,
    headers: { 'Content-Type': 'application/json' },
  });

  client.interceptors.response.use(undefined, async (error) => {
    const config = error.config;
    if (!config || !isRetryable(error)) {
      throw error;
    }
    config.retryCount = (config.retryCount ?? 0) + 1;
    if (config.retryCount > retries) {
      throw error;
    }
    return client.request(config);
  });

  return client;
}
