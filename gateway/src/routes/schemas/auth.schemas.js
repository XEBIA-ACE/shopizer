import { z } from 'zod';

const email = z
  .string({ required_error: 'Email is required', invalid_type_error: 'Email must be a string' })
  .trim()
  .min(1, 'Email is required')
  .max(320, 'Email must be at most 320 characters')
  .email('Email must be a valid email address');

const password = z
  .string({
    required_error: 'Password is required',
    invalid_type_error: 'Password must be a string',
  })
  .min(8, 'Password must be between 8 and 72 characters')
  .max(72, 'Password must be between 8 and 72 characters')
  .regex(
    /(?=.*[A-Z])(?=.*\d)/,
    'Password must contain at least one uppercase letter and one digit',
  );

export const registerSchema = z.object({ email, password });

export const resendSchema = z.object({ email });

export const verifyEmailQuerySchema = z.object({
  token: z
    .string({ required_error: 'token is required', invalid_type_error: 'token must be a string' })
    .min(1, 'token is required')
    .max(256, 'token is invalid'),
});
