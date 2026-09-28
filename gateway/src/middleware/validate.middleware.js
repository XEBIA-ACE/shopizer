export const VALIDATION_FAILED = 'validation_failed';

export function toFieldErrors(zodError) {
  const fieldErrors = {};
  for (const issue of zodError.issues) {
    const field = issue.path.join('.') || '_';
    if (!(field in fieldErrors)) {
      fieldErrors[field] = issue.message;
    }
  }
  return fieldErrors;
}

export function validate(schema, source = 'body') {
  return (req, res, next) => {
    const result = schema.safeParse(req[source]);
    if (!result.success) {
      return res.status(422).json({
        error: VALIDATION_FAILED,
        message: 'One or more fields are invalid.',
        field_errors: toFieldErrors(result.error),
      });
    }
    req.validated = { ...req.validated, [source]: result.data };
    return next();
  };
}
