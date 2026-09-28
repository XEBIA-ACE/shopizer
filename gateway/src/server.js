import { createApp } from './app.js';
import { loadConfig } from './config/index.js';
import { createLogger } from './config/logger.js';
import { createAuthService } from './services/auth.service.js';
import { createHttpClient } from './utils/http-client.js';

const config = loadConfig();
const logger = createLogger({ level: config.logLevel, serviceName: config.serviceName });
const coreClient = createHttpClient({
  baseURL: config.coreServiceUrl,
  timeout: config.upstreamTimeoutMs,
  retries: config.upstreamRetries,
});
const app = createApp({ authService: createAuthService(coreClient), logger });

app.listen(config.port, () => {
  logger.info('Gateway listening', { port: config.port });
});
