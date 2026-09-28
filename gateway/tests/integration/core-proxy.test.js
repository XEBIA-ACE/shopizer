import { afterAll, beforeAll, describe, expect, it } from '@jest/globals';
import express from 'express';
import request from 'supertest';
import { createAuthService } from '../../src/services/auth.service.js';
import { createHttpClient } from '../../src/utils/http-client.js';
import { buildApp } from '../helpers.js';

describe('gateway -> core-service contract', () => {
  let server;
  let gateway;
  const received = [];

  beforeAll(async () => {
    const core = express();
    core.use(express.json());
    core.post('/internal/auth/register', (req, res) => {
      received.push({ path: req.path, body: req.body, trace: req.get('x-trace-id') });
      res.status(202).json({ message: 'sent' });
    });
    core.post('/internal/auth/register/resend', (req, res) => {
      received.push({ path: req.path, body: req.body });
      res.status(404).json({ error: 'not_found', message: 'none' });
    });
    core.post('/internal/auth/verify/email', (req, res) => {
      received.push({ path: req.path, query: req.query });
      res.location('/dashboard').status(200).json({ session_token: 'jwt', expires_at: 'later' });
    });
    await new Promise((resolve) => {
      server = core.listen(0, resolve);
    });
    const client = createHttpClient({
      baseURL: `http://127.0.0.1:${server.address().port}`,
      timeout: 2000,
    });
    gateway = buildApp(createAuthService(client));
  });

  afterAll(() => new Promise((resolve) => server.close(resolve)));

  it('forwards register requests with the trace id', async () => {
    const res = await request(gateway)
      .post('/api/v1/auth/register')
      .set('x-trace-id', 't-42')
      .send({ email: 'a@example.com', password: 'Str0ngPass' });

    expect(res.status).toBe(202);
    expect(received.at(-1)).toEqual({
      path: '/internal/auth/register',
      body: { email: 'a@example.com', password: 'Str0ngPass' },
      trace: 't-42',
    });
  });

  it('forwards verify requests with the token as a query parameter', async () => {
    const res = await request(gateway).post('/api/v1/auth/verify/email?token=tok_123');

    expect(res.status).toBe(200);
    expect(res.headers.location).toBe('/dashboard');
    expect(received.at(-1)).toEqual({
      path: '/internal/auth/verify/email',
      query: { token: 'tok_123' },
    });
  });

  it('forwards resend requests and relays upstream status', async () => {
    const res = await request(gateway)
      .post('/api/v1/auth/register/resend')
      .send({ email: 'a@example.com' });

    expect(res.status).toBe(404);
    expect(received.at(-1)).toEqual({
      path: '/internal/auth/register/resend',
      body: { email: 'a@example.com' },
    });
  });
});
