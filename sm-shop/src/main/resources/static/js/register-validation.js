(function () {
  'use strict';

  var rules = JSON.parse(document.getElementById('registration-rules').textContent);
  var passwordRules = rules.password;
  var emailPattern = new RegExp(rules.emailPattern);

  var form = document.getElementById('registration-form');
  var emailInput = document.getElementById('emailAddress');
  var passwordInput = document.getElementById('password');
  var emailError = document.getElementById('email-error');
  var passwordError = document.getElementById('password-error');
  var confirmation = document.getElementById('form-confirmation');
  var formError = document.getElementById('form-error');
  var submitButton = document.getElementById('register-submit');
  var passwordTouched = false;

  function hasChar(value, test) {
    for (var i = 0; i < value.length; i++) {
      if (test(value.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  function validateEmail(value) {
    var email = (value || '').trim();
    return email && email.length <= rules.emailMaxLength && emailPattern.test(email) ? [] : [rules.emailMessage];
  }

  function validatePassword(value) {
    var password = value || '';
    var messages = passwordRules.messages;
    var errors = [];
    if (password.length < passwordRules.minLength) {
      errors.push(messages.minLength);
    }
    if (!hasChar(password, function (c) { return c >= 'A' && c <= 'Z'; })) {
      errors.push(messages.uppercase);
    }
    if (!hasChar(password, function (c) { return c >= 'a' && c <= 'z'; })) {
      errors.push(messages.lowercase);
    }
    if (!hasChar(password, function (c) { return c >= '0' && c <= '9'; })) {
      errors.push(messages.digit);
    }
    if (!hasChar(password, function (c) { return passwordRules.specialCharacters.indexOf(c) >= 0; })) {
      errors.push(messages.special);
    }
    return errors;
  }

  function showErrors(input, container, errors) {
    while (container.firstChild) {
      container.removeChild(container.firstChild);
    }
    errors.forEach(function (message) {
      var line = document.createElement('span');
      line.className = 'field-error-message';
      line.textContent = message;
      line.style.display = 'block';
      container.appendChild(line);
    });
    input.setAttribute('aria-invalid', errors.length ? 'true' : 'false');
    return errors.length === 0;
  }

  function checkEmail() {
    return showErrors(emailInput, emailError, validateEmail(emailInput.value));
  }

  function checkPassword() {
    return showErrors(passwordInput, passwordError, validatePassword(passwordInput.value));
  }

  function showMessage(element, message) {
    element.textContent = message;
    element.hidden = !message;
  }

  emailInput.addEventListener('blur', checkEmail);
  passwordInput.addEventListener('blur', function () {
    passwordTouched = true;
    checkPassword();
  });
  passwordInput.addEventListener('input', function () {
    if (passwordTouched) {
      checkPassword();
    }
  });

  form.addEventListener('submit', function (event) {
    event.preventDefault();
    showMessage(confirmation, '');
    showMessage(formError, '');

    var emailValid = checkEmail();
    passwordTouched = true;
    var passwordValid = checkPassword();
    if (!emailValid || !passwordValid) {
      return;
    }

    submitButton.disabled = true;
    var payload = JSON.stringify({ emailAddress: emailInput.value.trim(), password: passwordInput.value });

    fetch(form.getAttribute('action') + window.location.search, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
      credentials: 'same-origin',
      body: payload
    }).then(function (response) {
      return response.json().catch(function () { return {}; }).then(function (body) {
        return { status: response.status, body: body };
      });
    }).then(function (result) {
      if (result.status === 200) {
        form.reset();
        passwordTouched = false;
        showErrors(emailInput, emailError, []);
        showErrors(passwordInput, passwordError, []);
        showMessage(confirmation, result.body.message);
      } else if (result.status === 422 && result.body.fieldErrors) {
        showErrors(emailInput, emailError, result.body.fieldErrors.emailAddress || []);
        showErrors(passwordInput, passwordError, result.body.fieldErrors.password || []);
      } else {
        passwordInput.value = '';
        showMessage(formError, result.body.message || rules.genericErrorMessage);
      }
    }).catch(function () {
      passwordInput.value = '';
      showMessage(formError, rules.genericErrorMessage);
    }).then(function () {
      submitButton.disabled = false;
    });
  });
})();
