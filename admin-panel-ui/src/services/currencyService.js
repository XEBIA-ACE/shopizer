```js
export const fetchCurrencies = async () => {
  const response = await fetch('/api/currencies');
  return response.json();
};

export const addCurrency = async (currency) => {
  await fetch('/api/currencies', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ currency }),
  });
};

export const removeCurrency = async (currency) => {
  await fetch(`/api/currencies/${currency}`, {
    method: 'DELETE',
  });
};
```