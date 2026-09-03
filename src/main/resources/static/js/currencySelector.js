```javascript
document.addEventListener('DOMContentLoaded', () => {
    const currencySelect = document.getElementById('currency-select');

    currencySelect.addEventListener('change', (event) => {
        const selectedCurrency = event.target.value;
        updateCurrency(selectedCurrency);
    });

    function updateCurrency(currency) {
        fetch(`/api/currency/change`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="csrf-token"]').getAttribute('content')
            },
            body: JSON.stringify({currency: currency}),
        })
        .then(response => response.json())
        .then(data => {
            if(data.success) {
                updatePrices(data);
            } else {
                console.error("Failed to update currency");
            }
        })
        .catch(error => console.error('Error updating currency:', error));
    }

    function updatePrices(data) {
        document.querySelectorAll('.product-price').forEach(priceElement => {
            const priceInBaseCurrency = parseFloat(priceElement.getAttribute('data-base-price'));
            const convertedPrice = priceInBaseCurrency * data.exchangeRate;
            priceElement.textContent = new Intl.NumberFormat(data.locale, { style: 'currency', currency: data.currency }).format(convertedPrice);
        });
    }
});
```