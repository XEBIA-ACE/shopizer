import { createApp } from '../src/app.js';
import { createLogger } from '../src/config/logger.js';

export const silentLogger = createLogger({ silent: true });

export function buildApp(authService) {
  return createApp({ authService, logger: silentLogger });
}
