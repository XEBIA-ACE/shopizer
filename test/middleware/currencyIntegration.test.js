```javascript
const request = require('supertest');
const app = require('../../app');  // path to your Express app

describe('Currency Management Integration Tests', () => {
  test('should add a currency via middleware to backend service', async () => {
    const response = await request(app)
      .post('/api/currencies')
      .send({ currencyCode: 'EUR' })
      .expect('Content-Type', /json/)
      .expect(200);

    expect(response.body).toEqual(expect.objectContaining({
      success: true,
      message: 'Currency added successfully',
    }));
  });

  test('should remove a currency via middleware to backend service', async () => {
    const response = await request(app)
      .delete('/api/currencies/EUR')
      .expect('Content-Type', /json/)
      .expect(200);

    expect(response.body).toEqual(expect.objectContaining({
      success: true,
      message: 'Currency removed successfully',
    }));
  });
  
  test('should handle session reselect prompt correctly', async () => {
    await request(app)
      .delete('/api/currencies/USD')
      .expect(200);

    // Simulate the session reselect
    const sessionResponse = await request(app)
      .get('/api/session')
      .expect('Content-Type', /json/)
      .expect(200);

    expect(sessionResponse.body).toEqual(expect.objectContaining({
      currencyCode: 'Select a new currency',
    }));
  });
});
```

These files provide coverage for the specified unit and integration scenarios. They ensure that adding and removing currencies, as well as session reselects, are handled correctly between the UI and middleware layers.