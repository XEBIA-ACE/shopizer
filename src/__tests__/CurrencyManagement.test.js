```javascript
import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import '@testing-library/jest-dom/extend-expect';
import CurrencyManagement from '../components/CurrencyManagement';

describe('Currency Management UI', () => {
  beforeEach(() => {
    render(<CurrencyManagement />);
  });

  test('renders currency management component', () => {
    const addCurrencyButton = screen.getByText('Add Currency');
    expect(addCurrencyButton).toBeInTheDocument();
  });

  test('can add a currency', () => {
    const addButton = screen.getByText('Add Currency');
    fireEvent.click(addButton);

    const currencyInput = screen.getByPlaceholderText('Currency Code');
    fireEvent.change(currencyInput, { target: { value: 'USD' } });
    const confirmButton = screen.getByText('Confirm');
    fireEvent.click(confirmButton);

    const currencyList = screen.getByTestId('currency-list');
    expect(currencyList).toHaveTextContent('USD');
  });

  test('can remove a currency', () => {
    const removeButton = screen.getByText('Remove USD');
    fireEvent.click(removeButton);

    const currencyList = screen.getByTestId('currency-list');
    expect(currencyList).not.toHaveTextContent('USD');
  });
});
```

### Middleware Integration Test for Currency Management

These tests will ensure that the middleware correctly handles currency management requests and communicates with the backend services as expected.