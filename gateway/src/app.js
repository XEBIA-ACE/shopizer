import express from 'express';
import { traceMiddleware } from './middleware/trace.middleware.js';
import { createErrorHandler, notFoundHandler } from './middleware/error.middleware.js';
import { createRoutes } from './routes/index.js';

export function createApp({ authService, logger }) {
  const app = express();
  app.disable('x-powered-by');
  app.use(traceMiddleware);
  app.use(express.json({ limit: '10kb' }));
  app.use(createRoutes({ authService }));
  app.use(notFoundHandler);
  app.use(createErrorHandler(logger));
  return app;
}
