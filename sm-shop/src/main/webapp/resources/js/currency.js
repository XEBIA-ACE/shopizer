```js
$(document).ready(function() {
    function formatPrice(price, currencySymbol) {
        return new Intl.NumberFormat(navigator.language, {
            style: 'currency',
            currencyDisplay: 'symbol',
            currency: currencySymbol
        }).format(price);
    }

    $(".currency-selector").change(function() {
        const selectedCurrency = $(this).val();
        $.ajax({
            url: '/api/currency/rate',
            type: 'GET',
            data: { currency: selectedCurrency },
            success: function(data) {
                $(".price").each(function() {
                    const originalPrice = $(this).data('base-price');
                    const convertedPrice = originalPrice * data.exchangeRate;
                    $(this).text(formatPrice(convertedPrice, selectedCurrency));
                });
            },
            error: function() {
                alert("Could not update the currency. Please try again later.");
            }
        });
    });
});
```