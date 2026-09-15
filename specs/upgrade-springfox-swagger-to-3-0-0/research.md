# CAST MCP Research Findings

## Summary
A detailed analysis was conducted on the Shopizer-3.2.5 application to prepare for an upgrade to Springfox Swagger 3.0.0:
- **JPA Entities**: Several files contain JPA entities (`Group.java`, `CustomerReview.java`, etc.).
- **Spring Beans**: Key Spring beans include `OrderTotalService` and various authentication entry points.
- **Spring MVC Operations**: Numerous MVC operations like `/api/v1/auth/customer/profile/` were found.
- **Application Statistics**: 91,162 lines of code, 16,572 elements with interactions across AWS S3, Google Cloud, Java EE, Spring.

## Appendix
- See findings section for details on JPA Entities, Spring Beans, and MVC Operations.

(Sources: CAST MCP results on `objects`, `stats` queries)