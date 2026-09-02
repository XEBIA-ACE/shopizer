## Upgrade Elasticsearch from 7.5.2 to 8.10.0

### CAST Investigation Requirements
You MUST perform the following queries during your investigation:
1. Call `objects` with type filters relevant to this migration to get EXACT file counts.
   - For Spring Boot/Java upgrades: query `type:contains:JPA Entity`, `type:contains:Spring Bean`, `type:contains:Spring MVC`
   - For any framework: query object types that use the framework's annotations/APIs
2. Call `stats` to get the application's technology inventory and element counts.
3. For major version upgrades (e.g., Spring Boot 2→3), explicitly identify:
   - Namespace/import migrations required (e.g., javax.* → jakarta.*)
   - The EXACT count of affected files per migration category
   - Build tool type and number of build manifest files (e.g., '6 pom.xml files' not 'pom.xml or build.gradle')
4. Include these concrete numbers in your spec — never write '~X files' or 'several files'.
   Use the actual count from CAST query results.

### Tech Analysis Summary
- Language: Java 
- Language latest: Unknown
- Runtime: Spring Boot
- Build tool: Presumably Maven (based on the structure of the application)
- Upgrade urgency: Medium
- Tech debt: Elevated due to multiple outdated dependencies

Target CAST application: Shopizer-3.2.5

BCM (Business Capability Model) scope: None provided — Flag as standing compliance gap per GR-08