const CORE_AUTH_BASE = '/internal/auth';

function withTrace(traceId) {
  return { headers: { 'x-trace-id': traceId } };
}

function toResult(response) {
  return {
    status: response.status,
    data: response.data,
    location: response.headers?.location,
  };
}

export function createAuthService(httpClient) {
  return {
    async register({ email, password }, traceId) {
      const response = await httpClient.post(
        `${CORE_AUTH_BASE}/register`,
        { email, password },
        withTrace(traceId),
      );
      return toResult(response);
    },

    async resendConfirmation({ email }, traceId) {
      const response = await httpClient.post(
        `${CORE_AUTH_BASE}/register/resend`,
        { email },
        withTrace(traceId),
      );
      return toResult(response);
    },

    async verifyEmail({ token }, traceId) {
      const response = await httpClient.post(`${CORE_AUTH_BASE}/verify/email`, undefined, {
        ...withTrace(traceId),
        params: { token },
      });
      return toResult(response);
    },
  };
}
