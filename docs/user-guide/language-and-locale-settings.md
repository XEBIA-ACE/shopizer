# Language and Locale Settings

## Overview
Shopizer now supports full localization of UI and content elements in addition to locale-based formatting for dates and numbers. This guide will help you understand how to use and configure these features in your Shopizer application.

## User-Facing Features
### Language Selection
Users can select their preferred language directly from the interface. Once set, all UI elements and content will be displayed in the chosen language. If a specific translation is unavailable, the system will fall back to the default language.

### Locale-Based Formatting
Numbers and dates will automatically format based on the detected or selected locale of the user. This ensures that users view data in a manner consistent with their regional expectations.

### Configuring Language and Locale
1. **Access Language Settings**: Navigate to the settings section in your user dashboard.
2. **Choose Language**: Select your desired language from the dropdown menu. This setting will immediately take effect.
3. **Select Locale**: Similar to language, choose your locale to ensure date and number formats match your preferences.

## Developer-Facing Features
### API Changes
- **Endpoints**: API endpoints now support an optional `locale` parameter. This should be included in requests to receive data in the appropriate language and with the correct formatting.
- **Response Localization**: Data returned from endpoints such as `/products` and `/customer` will respect the language and locale settings.

### Backend Support
- **Preference Storage**: Language and locale preferences are stored in the user profile schema. Ensure migration scripts are run to update existing databases.
- **Localization Service**: A new service handles language translation and locale-based formatting, facilitating quick integration of new languages.

## Technical Architecture
The localization architecture integrates directly with the existing backend infrastructure to leverage user profile information seamlessly. This ensures minimal impact on application performance while maximizing flexibility for language expansion.

## Getting Help
If you encounter any issues or require further assistance, please refer to our [support page](support-page-link).

## Approval
This document has been reviewed and approved by the technical writing team.