# Technical Appendix

## CAST Data Overview
- **JPA Entities:**
  - `Catalog` / `17945` at `catalog/catalog/Catalog.java`
  - `Category` / `17943` at `catalog/category/Category.java`
- **Spring Beans:**
  - `OrderTotalService` / `21201` in `services/order/ordertotal/OrderTotalServiceImpl.java`
  - `AppConfiguration` / `21295` in `application/config/AppConfiguration.java`
- **Spring MVC Operations:**
  - `api/v1/auth/cart/{}/checkout/` (Post) / `10560`
  - `api/v1/auth/cart/{}/total/` (Get) / `6710`

## Query Log
1. **Apps:** applications — returned Shopizer-3.2.5 app (run-returned)
2. **Stats:** stats for Shopizer-3.2.5 app (run-returned)
3. **JPA Entities:** objects filtered query (run-returned, partial display)
4. **Spring Beans:** objects filtered query (run-returned, filtered display)
5. **Spring MVC:** objects filtered query (run-returned, comprehensive partial list)