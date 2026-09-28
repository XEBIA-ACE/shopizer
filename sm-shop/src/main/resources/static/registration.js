const form = document.getElementById('register');
const resend = document.getElementById('resend');
const email = document.getElementById('email');
const password = document.getElementById('password');
const result = document.getElementById('result');

function validate() {
  document.getElementById('email-error').textContent = email.validity.valid ? '' : 'Enter a valid email address';
  document.getElementById('password-error').textContent =
    password.value.length >= 8 && /[A-Z]/.test(password.value) && /\d/.test(password.value)
      ? '' : 'Use at least 8 characters, one uppercase letter and one digit';
  return email.validity.valid && !document.getElementById('password-error').textContent;
}

email.addEventListener('input', validate);
password.addEventListener('input', validate);

async function submit(path, body) {
  const response = await fetch(path, {
    method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body)
  });
  const data = await response.json();
  result.textContent = data.message || (response.ok ? 'Confirmation link sent' : 'Please try again');
  return response.ok;
}

form.addEventListener('submit', async event => {
  event.preventDefault();
  if (!validate()) return;
  try {
    resend.hidden = !await submit('/api/v1/auth/register', { email: email.value, password: password.value });
  } catch {
    result.textContent = 'Unable to send the link. Please try again.';
  }
});

resend.addEventListener('submit', async event => {
  event.preventDefault();
  try {
    await submit('/api/v1/auth/register/resend', { email: email.value });
  } catch {
    result.textContent = 'Unable to resend the link. Please try again.';
  }
});
