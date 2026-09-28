const result = document.getElementById('result');
const token = new URLSearchParams(window.location.search).get('token');

if (!token) {
  result.textContent = 'Verification link is invalid';
} else {
  fetch(`/api/v1/auth/verify/email?token=${encodeURIComponent(token)}`, { method: 'POST' })
    .then(async response => {
      const data = await response.json();
      if (!response.ok) throw new Error(data.message || 'Verification link is invalid');
      window.location.replace(data.redirect);
    })
    .catch(error => { result.textContent = error.message; });
}
