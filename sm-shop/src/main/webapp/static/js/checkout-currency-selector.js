/**
 * checkout-currency-selector.js
 *
 * Populates the currency selector dropdown on the checkout start page.
 * Reads store context from a server-rendered hidden element to avoid
 * client-supplied parameter injection (FR-05 / NFR-03).
 *
 * Dependencies: none (vanilla JS, ES5-compatible)
 */
(function () {
  'use strict';

  /**
   * Reads a cookie value by name.
   * @param {string} name
   * @returns {string|null}
   */
  function getCookieValue(name) {
    var nameEQ = encodeURIComponent(name) + '=';
    var cookies = document.cookie.split(';');
    for (var i = 0; i < cookies.length; i++) {
      var cookie = cookies[i];
      while (cookie.charAt(0) === ' ') {
        cookie = cookie.substring(1);
      }
      if (cookie.indexOf(nameEQ) === 0) {
        return decodeURIComponent(cookie.substring(nameEQ.length));
      }
    }
    return null;
  }

  /**
   * Returns the active session currency from the server-rendered context element
   * or from a session cookie, in that order of preference.
   * @returns {string|null}
   */
  function getCookieOrSessionCurrency() {
    var ctx = document.getElementById('store-context');
    if (!ctx) {
      return null;
    }
    // Prefer the server-injected session currency attribute
    var sessionCurrency = ctx.dataset.sessionCurrency;
    if (sessionCurrency && sessionCurrency.trim() !== '') {
      return sessionCurrency.trim();
    }
    // Fall back to a cookie named 'CURRENCY' (set by server on prior visits)
    return getCookieValue('CURRENCY');
  }

  /**
   * Returns the default currency from the currencies array (isDefault === true).
   * @param {Array<{currencyCode: string, symbol: string, isDefault: boolean}>} currencies
   * @returns {string|null}
   */
  function getDefaultCurrency(currencies) {
    if (!Array.isArray(currencies)) {
      return null;
    }
    for (var i = 0; i < currencies.length; i++) {
      if (currencies[i].isDefault === true) {
        return currencies[i].currencyCode;
      }
    }
    return null;
  }

  /**
   * Determines which currency code should be pre-selected in the dropdown.
   * Priority:
   *   1. If fallback was applied server-side, use the fallback currency.
   *   2. Otherwise use the session/cookie currency if it exists in the list.
   *   3. Otherwise use the store default currency.
   *
   * @param {boolean} fallbackApplied
   * @param {string|null} fallbackCurrency
   * @param {Array<{currencyCode: string, symbol: string, isDefault: boolean}>} currencies
   * @returns {string|null}
   */
  function resolveActiveCurrency(fallbackApplied, fallbackCurrency, currencies) {
    if (fallbackApplied && fallbackCurrency) {
      return fallbackCurrency;
    }

    var sessionCurrency = getCookieOrSessionCurrency();
    if (sessionCurrency) {
      // Verify the session currency is actually in the list for this store
      for (var i = 0; i < currencies.length; i++) {
        if (currencies[i].currencyCode === sessionCurrency) {
          return sessionCurrency;
        }
      }
    }

    // Fall back to the store default
    return getDefaultCurrency(currencies);
  }

  /**
   * Populates the currency <select> element with options from the API response.
   *
   * @param {HTMLSelectElement} selectEl
   * @param {Array<{currencyCode: string, symbol: string, isDefault: boolean}>} currencies
   * @param {string|null} activeCurrency
   */
  function populateSelect(selectEl, currencies, activeCurrency) {
    // Clear any existing options
    while (selectEl.firstChild) {
      selectEl.removeChild(selectEl.firstChild);
    }

    if (!Array.isArray(currencies) || currencies.length === 0) {
      var emptyOpt = document.createElement('option');
      emptyOpt.value = '';
      emptyOpt.text = '—';
      emptyOpt.disabled = true;
      emptyOpt.selected = true;
      selectEl.appendChild(emptyOpt);
      return;
    }

    for (var i = 0; i < currencies.length; i++) {
      var c = currencies[i];
      var opt = document.createElement('option');
      opt.value = c.currencyCode;
      opt.text = c.currencyCode + ' (' + c.symbol + ')';
      if (c.currencyCode === activeCurrency) {
        opt.selected = true;
      }
      selectEl.appendChild(opt);
    }
  }

  /**
   * Displays the fallback informational banner with the given currency name.
   * @param {string} fallbackCurrency
   */
  function showFallbackBanner(fallbackCurrency) {
    var banner = document.getElementById('currency-fallback-banner');
    var nameSpan = document.getElementById('fallback-currency-name');
    if (!banner || !nameSpan) {
      return;
    }
    nameSpan.textContent = fallbackCurrency;
    banner.style.display = '';
  }

  /**
   * Hides the fallback informational banner.
   */
  function hideFallbackBanner() {
    var banner = document.getElementById('currency-fallback-banner');
    if (banner) {
      banner.style.display = 'none';
    }
  }

  /**
   * Main initialisation: reads store context, fetches currencies, populates UI.
   */
  function init() {
    var ctx = document.getElementById('store-context');
    if (!ctx) {
      console.warn('checkout-currency-selector: #store-context element not found. ' +
        'Currency selector will not be initialised.');
      return;
    }

    var storeCode = ctx.dataset.storeCode;
    if (!storeCode || storeCode.trim() === '') {
      console.warn('checkout-currency-selector: data-store-code is empty. ' +
        'Currency selector will not be initialised.');
      return;
    }

    var fallbackApplied = ctx.dataset.fallbackApplied === 'true';
    var fallbackCurrency = ctx.dataset.fallbackCurrency || null;

    var selectEl = document.getElementById('currency-selector');
    if (!selectEl) {
      console.warn('checkout-currency-selector: #currency-selector element not found.');
      return;
    }

    // Disable the select while loading to prevent premature interaction
    selectEl.disabled = true;

    var apiUrl = '/api/v1/store/' + encodeURIComponent(storeCode.trim()) + '/currencies';

    fetch(apiUrl, {
      method: 'GET',
      headers: {
        'Accept': 'application/json'
      },
      credentials: 'same-origin'
    })
      .then(function (res) {
        if (!res.ok) {
          throw new Error('Currency API responded with HTTP ' + res.status +
            ' for store: ' + storeCode);
        }
        return res.json();
      })
      .then(function (data) {
        var currencies = (data && Array.isArray(data.currencies)) ? data.currencies : [];

        if (currencies.length === 0) {
          console.warn('checkout-currency-selector: No currencies returned for store: ' +
            storeCode);
        }

        var activeCurrency = resolveActiveCurrency(fallbackApplied, fallbackCurrency, currencies);

        populateSelect(selectEl, currencies, activeCurrency);

        if (fallbackApplied && fallbackCurrency) {
          showFallbackBanner(fallbackCurrency);
        } else {
          hideFallbackBanner();
        }

        selectEl.disabled = false;
      })
      .catch(function (err) {
        console.error('checkout-currency-selector: Failed to load currencies.', err);
        // Re-enable the select so the page is not broken; it will be empty
        selectEl.disabled = false;
      });
  }

  // Initialise after DOM is ready
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    // DOMContentLoaded has already fired (e.g. script loaded async/defer)
    init();
  }

}());