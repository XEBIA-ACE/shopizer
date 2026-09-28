import { z } from 'zod';

const configSchema = z.object({
  PORT: z.coerce.number().int().positive().default(8080),
  CORE_SERVICE_URL: z.string().url().default('http://localhost:8081'),
  UPSTREAM_TIMEOUT_MS: z.coerce.number().int().positive().default(5000),
  UPSTREAM_RETRIES: z.coerce.number().int().min(0).default(2),
  LOG_LEVEL: z.enum(['error', 'warn', 'info', 'http', 'debug']).default('info'),
  SERVICE_NAME: z.string().min(1).default('gateway'),
});

export function loadConfig(env = process.env) {
  const parsed = configSchema.safeParse(env);
  if (!parsed.success) {
    const details = parsed.error.issues.map((i) => `${i.path.join('.')}: ${i.message}`).join('; ');
    throw new Error(`Invalid gateway configuration: ${details}`);
  }
  const c = parsed.data;
  return {
    port: c.PORT,
    coreServiceUrl: c.CORE_SERVICE_URL,
    upstreamTimeoutMs: c.UPSTREAM_TIMEOUT_MS,
    upstreamRetries: c.UPSTREAM_RETRIES,
    logLevel: c.LOG_LEVEL,
    serviceName: c.SERVICE_NAME,
  };
}
