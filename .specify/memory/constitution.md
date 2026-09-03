## Non-functional Requirements and Constraints (Constitution)

1. **Security**: All exchange rate API calls must be made using secure channels, adhering to the security standards.
2. **Reliability**: If the exchange rate API becomes unavailable, the system must revert to using the most recent valid rate, logging errors for administrative attention.
3. **Volume Handling**: System should handle payment integration without degrading performance or user experience.
4. **Maintainability**: Changes to exchange rate handling should be clear and easy to manipulate without system-wide risk.
5. **Testability**: Test suites must be developed to cover all enhancements, ensuring every code path is verified.