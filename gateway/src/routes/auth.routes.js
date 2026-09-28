import { Router } from 'express';
import { asyncHandler } from '../utils/async-handler.js';
import { validate } from '../middleware/validate.middleware.js';
import { registerSchema, resendSchema, verifyEmailQuerySchema } from './schemas/auth.schemas.js';

function relay(res, result) {
  if (result.location) {
    res.location(result.location);
  }
  return res.status(result.status).json(result.data);
}

export function createAuthRouter(authService) {
  const router = Router();

  router.post(
    '/register',
    validate(registerSchema),
    asyncHandler(async (req, res) =>
      relay(res, await authService.register(req.validated.body, req.traceId)),
    ),
  );

  router.post(
    '/register/resend',
    validate(resendSchema),
    asyncHandler(async (req, res) =>
      relay(res, await authService.resendConfirmation(req.validated.body, req.traceId)),
    ),
  );

  router.post(
    '/verify/email',
    validate(verifyEmailQuerySchema, 'query'),
    asyncHandler(async (req, res) =>
      relay(res, await authService.verifyEmail(req.validated.query, req.traceId)),
    ),
  );

  return router;
}
