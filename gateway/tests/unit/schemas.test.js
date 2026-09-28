import { describe, expect, it } from '@jest/globals';
import { registerSchema, resendSchema } from '../../src/routes/schemas/auth.schemas.js';

describe('registerSchema', () => {
  it('accepts a valid email and password and trims the email', () => {
    const result = registerSchema.parse({ email: '  a@example.com ', password: 'Str0ngPass' });
    expect(result.email).toBe('a@example.com');
  });

  it.each([
    ['short', 'Ab1', 'Password must be between 8 and 72 characters'],
    [
      'no uppercase',
      'weakpass1',
      'Password must contain at least one uppercase letter and one digit',
    ],
    [
      'no digit',
      'WeakPassword',
      'Password must contain at least one uppercase letter and one digit',
    ],
  ])('rejects a password that is %s', (_label, password, message) => {
    const result = registerSchema.safeParse({ email: 'a@example.com', password });
    expect(result.success).toBe(false);
    expect(result.error.issues[0].message).toBe(message);
  });

  it('rejects an invalid email', () => {
    const result = resendSchema.safeParse({ email: 'nope' });
    expect(result.error.issues[0].message).toBe('Email must be a valid email address');
  });
});
