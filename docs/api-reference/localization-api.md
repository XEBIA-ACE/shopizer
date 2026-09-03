# Localization API Reference

## Overview
The localization API enables clients to interact with multi-language and locale-aware functionality, expanding the accessibility and adaptability of Shopizer applications.

## Endpoints

### `/api/products`
- **Description**: Fetches a list of products with localized names and descriptions.
- **Parameters**:
  - `locale` (optional): Specifies the language and regional settings for the response data.
  
### `/api/customer`
- **Description**: Retrieves customer details with locale-specific data formatting.
- **Parameters**:
  - `locale` (optional): Determines the language for textual data and formatting for numeric/date data.

## Example Usage

### Fetch Products in French Locale
```http
GET /api/products?locale=fr-FR
```
**Response**:
The response will include product names and descriptions in French, with prices and dates formatted according to French regional settings.

## Implementation Notes
- Ensure all requests specify the `locale` parameter to take full advantage of localization features.
- Consult the framework's internationalization library for guidelines on adding new languages and locales.

## Backward Compatibility
In the absence of a `locale` parameter, the API defaults to the system's primary language and formatting settings.

## Approval
This document has been reviewed and approved by the technical writing team.