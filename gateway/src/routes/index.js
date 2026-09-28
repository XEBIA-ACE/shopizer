import { Router } from 'express';
import { createAuthRouter } from './auth.routes.js';

export function createRoutes({ authService }) {
  const router = Router();
  router.get('/health', (req, res) => res.json({ status: 'UP' }));
  router.use('/api/v1/auth', createAuthRouter(authService));
  return router;
}
