import winston from 'winston';

export function createLogger({ level = 'info', serviceName = 'gateway', silent = false } = {}) {
  return winston.createLogger({
    level,
    silent,
    defaultMeta: { service: serviceName },
    format: winston.format.combine(winston.format.timestamp(), winston.format.json()),
    transports: [new winston.transports.Console()],
  });
}
