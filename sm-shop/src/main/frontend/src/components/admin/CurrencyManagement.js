```javascript
import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Button, Table, Modal, notification } from 'antd';
import 'antd/dist/antd.css';

const CurrencyManagement = () => {
  const [currencies, setCurrencies] = useState([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchCurrencies();
    const interval = setInterval(() => {
      fetchCurrencies();
    }, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchCurrencies = async () => {
    setLoading(true);
    try {
      const response = await axios.get('/api/v1/private/currencies');
      setCurrencies(response.data);
    } catch (error) {
      notification.error({ message: 'Error fetching currencies' });
    } finally {
      setLoading(false);
    }
  };

  const addCurrency = async (currency) => {
    try {
      await axios.post('/api/v1/private/currencies/add', currency);
      notification.success({ message: 'Currency added successfully' });
      fetchCurrencies();
    } catch (error) {
      notification.error({ message: 'Error adding currency' });
    }
  };

  const removeCurrency = async (currencyCode) => {
    Modal.confirm({
      title: 'Are you sure you want to remove this currency?',
      onOk: async () => {
        try {
          await axios.delete(`/api/v1/private/currencies/remove/${currencyCode}`);
          notification.success({ message: 'Currency removed successfully' });
          fetchCurrencies();
          if (currencyCode === getCurrentSessionCurrency()) {
            notification.warning({ message: 'The selected currency has been removed. Please select another currency.' });
          }
        } catch (error) {
          notification.error({ message: 'Error removing currency' });
        }
      }
    });
  };

  const getCurrentSessionCurrency = () => {
    // This function should return the current session's currency code
    return localStorage.getItem('currencyCode');
  };

  return (
    <div>
      <h2>Currency Management</h2>
      <Table
        dataSource={currencies}
        loading={loading}
        columns={[
          { title: 'Code', dataIndex: 'code', key: 'code' },
          { title: 'Name', dataIndex: 'name', key: 'name' },
          {
            title: 'Action',
            key: 'action',
            render: (text, record) => (
              <Button type="danger" onClick={() => removeCurrency(record.code)}>Remove</Button>
            ),
          },
        ]}
      />
      <Button type="primary" onClick={() => addCurrency({ code: 'EUR', name: 'Euro' })}>
        Add Euro
      </Button>
    </div>
  );
};

export default CurrencyManagement;
```