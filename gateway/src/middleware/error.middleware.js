export function notFoundHandler(req, res) {
  res.status(404).json({ error: 'not_found', message: 'Route not found.' });
}

export function createErrorHandler(logger) {
  // eslint-disable-next-line no-unused-vars
  return (err, req, res, next) => {
    if (err.type === 'entity.parse.failed') {
      return res
        .status(400)
        .json({ error: 'malformed_request', message: 'Request body is missing or malformed.' });
    }
    if (err.isAxiosError) {
      logger.error('Upstream request failed', {
        traceId: req.traceId,
        code: err.code,
        url: err.config?.url,
      });
      return res.status(502).json({
        error: 'upstream_unavailable',
        message: 'The service is temporarily unavailable.',
      });
    }
    logger.error('Unhandled error', { traceId: req.traceId, error: err.message, stack: err.stack });
    return res
      .status(500)
      .json({ error: 'internal_error', message: 'An unexpected error occurred.' });
  };
}
