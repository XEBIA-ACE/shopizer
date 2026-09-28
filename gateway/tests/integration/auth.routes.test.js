import { describe, expect, it, jest } from '@jest/globals';
import request from 'supertest';
import { buildApp } from '../helpers.js';

function fakeAuthService(overrides = {}) {
  return {
    register: jest.fn(),
    resendConfirmation: jest.fn(),
    verifyEmail: jest.fn(),
    ...overrides,
  };
}

describe('POST /api/v1/auth/register', () => {
  it('returns 202 with the confirmation message when registration succeeds', async () => {
    const authService = fakeAuthService({
      register: jest.fn().mockResolvedValue({
        status: 202,
        data: {
          message:
            'Registration received. A confirmation link has been sent to your email address.',
        },
      }),
    });

    const res = await request(buildApp(authService))
      .post('/api/v1/auth/register')
      .set('x-trace-id', 'trace-1')
      .send({ email: ' Shopper@Example.com ', password: 'Str0ngPass' });

    expect(res.status).toBe(202);
    expect(res.body.message).toMatch(/confirmation link has been sent/);
    expect(res.headers['x-trace-id']).toBe('trace-1');
    expect(authService.register).toHaveBeenCalledWith(
      { email: 'Shopper@Example.com', password: 'Str0ngPass' },
      'trace-1',
    );
  });

  it('relays 409 when the email is already registered', async () => {
    const body = {
      error: 'email_already_in_use',
      message: 'An account with this email address already exists.',
    };
    const authService = fakeAuthService({
      register: jest.fn().mockResolvedValue({ status: 409, data: body }),
    });

    const res = await request(buildApp(authService))
      .post('/api/v1/auth/register')
      .send({ email: 'dup@example.com', password: 'Str0ngPass' });

    expect(res.status).toBe(409);
    expect(res.body).toEqual(body);
  });

  it('returns 422 with field_errors and does not call core for invalid input', async () => {
    const authService = fakeAuthService();

    const res = await request(buildApp(authService))
      .post('/api/v1/auth/register')
      .send({ email: 'bad', password: 'weak' });

    expect(res.status).toBe(422);
    expect(res.body.error).toBe('validation_failed');
    expect(res.body.field_errors).toEqual({
      email: 'Email must be a valid email address',
      password: 'Password must be between 8 and 72 characters',
    });
    expect(authService.register).not.toHaveBeenCalled();
  });

  it('returns 422 when the body is empty', async () => {
    const res = await request(buildApp(fakeAuthService())).post('/api/v1/auth/register');

    expect(res.status).toBe(422);
    expect(res.body.field_errors).toEqual({
      email: 'Email is required',
      password: 'Password is required',
    });
  });

  it('reports top-level errors when the body is not an object', async () => {
    const res = await request(buildApp(fakeAuthService())).post('/api/v1/auth/register').send([]);

    expect(res.status).toBe(422);
    expect(Object.keys(res.body.field_errors)).toEqual(['_']);
  });

  it('returns 400 for malformed JSON', async () => {
    const res = await request(buildApp(fakeAuthService()))
      .post('/api/v1/auth/register')
      .set('Content-Type', 'application/json')
      .send('{');

    expect(res.status).toBe(400);
    expect(res.body.error).toBe('malformed_request');
  });

  it('returns 502 when the core service is unreachable', async () => {
    const error = Object.assign(new Error('connect ECONNREFUSED'), {
      isAxiosError: true,
      code: 'ECONNREFUSED',
      config: { url: '/internal/auth/register' },
    });
    const authService = fakeAuthService({ register: jest.fn().mockRejectedValue(error) });

    const res = await request(buildApp(authService))
      .post('/api/v1/auth/register')
      .send({ email: 'a@example.com', password: 'Str0ngPass' });

    expect(res.status).toBe(502);
    expect(res.body.error).toBe('upstream_unavailable');
  });

  it('returns 500 for unexpected errors', async () => {
    const authService = fakeAuthService({
      register: jest.fn().mockRejectedValue(new Error('boom')),
    });

    const res = await request(buildApp(authService))
      .post('/api/v1/auth/register')
      .send({ email: 'a@example.com', password: 'Str0ngPass' });

    expect(res.status).toBe(500);
    expect(res.body.error).toBe('internal_error');
  });
});

describe('POST /api/v1/auth/verify/email', () => {
  it('relays the session and Location: /dashboard on success', async () => {
    const authService = fakeAuthService({
      verifyEmail: jest.fn().mockResolvedValue({
        status: 200,
        data: { session_token: 'jwt', expires_at: '2026-09-28T11:00:00Z' },
        location: '/dashboard',
      }),
    });

    const res = await request(buildApp(authService)).post('/api/v1/auth/verify/email?token=abc');

    expect(res.status).toBe(200);
    expect(res.headers.location).toBe('/dashboard');
    expect(res.body).toEqual({ session_token: 'jwt', expires_at: '2026-09-28T11:00:00Z' });
    expect(authService.verifyEmail).toHaveBeenCalledWith({ token: 'abc' }, expect.any(String));
  });

  it.each([
    [410, 'token_expired'],
    [409, 'token_consumed'],
  ])('relays %s %s from core', async (status, error) => {
    const authService = fakeAuthService({
      verifyEmail: jest.fn().mockResolvedValue({ status, data: { error, message: 'x' } }),
    });

    const res = await request(buildApp(authService)).post('/api/v1/auth/verify/email?token=abc');

    expect(res.status).toBe(status);
    expect(res.body.error).toBe(error);
    expect(res.headers.location).toBeUndefined();
  });

  it('returns 422 when the token query parameter is missing', async () => {
    const res = await request(buildApp(fakeAuthService())).post('/api/v1/auth/verify/email');

    expect(res.status).toBe(422);
    expect(res.body.field_errors).toEqual({ token: 'token is required' });
  });
});

describe('POST /api/v1/auth/register/resend', () => {
  it('relays 202 on success', async () => {
    const authService = fakeAuthService({
      resendConfirmation: jest.fn().mockResolvedValue({ status: 202, data: { message: 'sent' } }),
    });

    const res = await request(buildApp(authService))
      .post('/api/v1/auth/register/resend')
      .send({ email: 'p@example.com' });

    expect(res.status).toBe(202);
    expect(authService.resendConfirmation).toHaveBeenCalledWith(
      { email: 'p@example.com' },
      expect.any(String),
    );
  });

  it('relays 429 during cooldown', async () => {
    const authService = fakeAuthService({
      resendConfirmation: jest
        .fn()
        .mockResolvedValue({ status: 429, data: { error: 'resend_cooldown', message: 'wait' } }),
    });

    const res = await request(buildApp(authService))
      .post('/api/v1/auth/register/resend')
      .send({ email: 'p@example.com' });

    expect(res.status).toBe(429);
  });
});

describe('misc routes', () => {
  it('serves health and 404 for unknown routes', async () => {
    const app = buildApp(fakeAuthService());

    expect((await request(app).get('/health')).body).toEqual({ status: 'UP' });
    const res = await request(app).get('/nope');
    expect(res.status).toBe(404);
    expect(res.body.error).toBe('not_found');
  });
});
