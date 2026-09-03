### Implementation Plan

1. **Frontend Updates**
   - Implement AJAX-based currency selections.
   - Integrate locale-specific formatting logic.

2. **Exchange Rate Service Interface**
   - Establish secure connection protocols.
   - Implement retry logic in case of API unavailability.

3. **Backend Price Calculation Updates**
   - Fetch and serialize exchange rate updates.
   - Adjust price calculations accordingly.

4. **Testing**
   - Develop comprehensive tests ensuring price resilience to fluctuations in exchange rates.

5. **Deployment and Monitoring Enhancements**
   - Ensure logging alerts for fallback to last known exchange rates.
   - Monitor API rate limits.

## Blast Radius Summary
The measured blast radius count is 0, given there are no direct callers, nodes in the call graph, or impacted tests. Despite low risk, careful attention must be given to ensure test coverage improvements.