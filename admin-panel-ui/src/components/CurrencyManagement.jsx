```jsx
import React, { useState, useEffect } from 'react';
import { fetchCurrencies, addCurrency, removeCurrency } from '../services/currencyService';

const CurrencyManagement = () => {
  const [currencies, setCurrencies] = useState([]);
  const [newCurrency, setNewCurrency] = useState('');

  useEffect(() => {
    loadCurrencies();
  }, []);

  const loadCurrencies = async () => {
    const fetchedCurrencies = await fetchCurrencies();
    setCurrencies(fetchedCurrencies);
  };

  const handleAddCurrency = async () => {
    if (newCurrency) {
      await addCurrency(newCurrency);
      setNewCurrency('');
      loadCurrencies();
    }
  };

  const handleRemoveCurrency = async (currency) => {
    await removeCurrency(currency);
    loadCurrencies();
  };

  return (
    <div className="currency-management">
      <h1>Manage Currencies</h1>
      <div className="currency-input">
        <input 
          type="text" 
          placeholder="Add new currency code" 
          value={newCurrency} 
          onChange={(e) => setNewCurrency(e.target.value)} 
        />
        <button onClick={handleAddCurrency}>Add</button>
      </div>
      <ul className="currency-list">
        {currencies.map(currency => (
          <li key={currency}>
            {currency}
            <button onClick={() => handleRemoveCurrency(currency)}>Remove</button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default CurrencyManagement;
```