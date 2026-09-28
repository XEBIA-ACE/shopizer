import { describe, expect, it } from '@jest/globals';
import { loadConfig } from '../../src/config/index.js';
import { createLogger } from '../../src/config/logger.js';

describe('loadConfig', () => {
  it('applies defaults', () => {
    expect(loadConfig({})).toEqual({
      port: 8080,
      coreServiceUrl: 'http://localhost:8081',
      upstreamTimeoutMs: 5000,
      upstreamRetries: 2,
      logLevel: 'info',
      serviceName: 'gateway',
    });
  });

  it('reads process.env by default', () => {
    expect(loadConfig().serviceName).toBeDefined();
  });

  it('reads values from the environment', () => {
    const config = loadConfig({ PORT: '9000', CORE_SERVICE_URL: 'http://core:8081' });
    expect(config.port).toBe(9000);
    expect(config.coreServiceUrl).toBe('http://core:8081');
  });

  it('rejects invalid values', () => {
    expect(() => loadConfig({ CORE_SERVICE_URL: 'not a url' })).toThrow(
      /Invalid gateway configuration: CORE_SERVICE_URL/,
    );
  });
});

describe('createLogger', () => {
  it('creates a JSON logger tagged with the service name by default', () => {
    const logger = createLogger();
    expect(logger.level).toBe('info');
    expect(logger.defaultMeta).toEqual({ service: 'gateway' });
  });
});
