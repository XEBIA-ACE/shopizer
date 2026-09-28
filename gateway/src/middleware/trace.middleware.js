import { randomUUID } from 'node:crypto';

export const TRACE_HEADER = 'x-trace-id';

export function traceMiddleware(req, res, next) {
  const incoming = req.get(TRACE_HEADER);
  req.traceId = incoming && incoming.length <= 128 ? incoming : randomUUID();
  res.set(TRACE_HEADER, req.traceId);
  next();
}
