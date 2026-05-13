export async function readApiError(res, context) {
  let bodyText = '';
  try {
    bodyText = await res.text();
  } catch {
    bodyText = '';
  }

  let payload = null;
  if (bodyText) {
    try {
      payload = JSON.parse(bodyText);
    } catch {
      payload = null;
    }
  }

  const fromBody =
    payload && (payload.message || payload.detail || payload.title);
  if (typeof fromBody === 'string' && fromBody.trim().length > 0) {
    return fromBody.trim();
  }

  return defaultMessageFor(res.status, context);
}


export function defaultMessageFor(status, context) {
  const subject = context ? ` while we tried to ${context}` : '';

  switch (status) {
    case 400:
      return `The request was invalid${subject}.`;
    case 401:
      if (context === 'login' || context === 'register') {
        return 'Invalid username or password.';
      }
      return 'Your session has expired. Please log in again.';
    case 403:
      if (context === 'login') {
        return 'Account is suspended. Please contact support.';
      }
      return 'You are not allowed to do that.';
    case 404:
      return 'Not found.';
    case 409:
      if (context === 'register') {
        return 'An account with this username already exists.';
      }
      return 'Conflict — that operation is not allowed in the current state.';
    case 422:
      return 'Some fields are invalid. Please check the form.';
    case 502:
    case 503:
    case 504:
      return 'The service is temporarily unavailable. Please try again shortly.';
    default:
      if (status >= 500) return 'Server error. Please try again.';
      return `Request failed (HTTP ${status}).`;
  }
}
