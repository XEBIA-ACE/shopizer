import { describe, expect, it } from '@jest/globals';
import { createHttpClient, isRetryable } from '../../src/utils/http-client.js';

describe('isRetryable', () => {
  it('retries idempotent requests that failed without a response', () => {
    expect(isRetryable({ config: { method: 'GET' } })).toBe(true);
  });

  it('never retries POST requests or responses from the server', () => {
    expect(isRetryable({ config: { method: 'post' } })).toBe(false);
    expect(isRetryable({ config: { method: 'get' }, response: { status: 500 } })).toBe(false);
    expect(isRetryable({})).toBe(false);
  });
});

describe('createHttpClient', () => {
  it('retries idempotent network failures up to the configured limit', async () => {
    let attempts = 0;
    const client = createHttpClient({ baseURL: 'http://upstream', timeout: 100, retries: 2 });
    client.defaults.adapter = async (config) => {
      attempts += 1;
      const error = new Error('ECONNREFUSED');
      error.config = config;
      error.isAxiosError = true;
      throw error;
    };

    await expect(client.get('/x')).rejects.toThrow('ECONNREFUSED');
    expect(attempts).toBe(3);
  });

  it('does not retry POST requests and resolves non-2xx statuses', async () => {
    let attempts = 0;
    const client = createHttpClient({ baseURL: 'http://upstream', timeout: 100, retries: 2 });
    client.defaults.adapter = async (config) => {
      attempts += 1;
      return { data: { error: 'x' }, status: 409, statusText: 'Conflict', headers: {}, config };
    };

    const response = await client.post('/x', {});
    expect(response.status).toBe(409);
    expect(attempts).toBe(1);
  });

  it('rethrows errors without config', async () => {
    const client = createHttpClient({ baseURL: 'http://upstream', timeout: 100 });
    const [, onRejected] = [null, client.interceptors.response.handlers[0].rejected];
    await expect(onRejected(new Error('boom'))).rejects.toThrow('boom');
  });
});
