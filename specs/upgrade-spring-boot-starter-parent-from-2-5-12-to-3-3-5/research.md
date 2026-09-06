## Authoritative Input Provenance
- Repository ID: `2adc1099-45f0-422d-8f82-8326c32c7fc4/961d6363-9181-4dbb-a7be-615e1d3c2c67`
- Expected branch: `3.2.5`
- Code Insights grounded: `True`
- Index status: grounded context available
- Current-state source: Tech Analysis
- Target-state source: explicit Selected Upgrade Option changes only

### Evidence Gaps
- No verified target was supplied for JVM (current: `11`); it is omitted from Target State.
- No verified target was supplied for Build tool (current: `Maven`); it is omitted from Target State.
- No verified target was supplied for Package manager (current: `Maven`); it is omitted from Target State.
- The target `21 LTS (prerequisite for Spring Boot 3.3.x)` for Upgrade Java runtime is non-specific; exact version selection and compatibility verification are required.
- The target `6.3.x; migrate deprecated WebSecurityConfigurerAdapter to SecurityFilterChain` for Upgrade Spring Security (transitive via Spring Boot 3.3.5) is non-specific; exact version selection and compatibility verification are required.
- The target `3.3.x` for Upgrade Spring Data JPA (transitive via Spring Boot 3.3.5) is non-specific; exact version selection and compatibility verification are required.
- The target `6.5.x; review HQL/Criteria API changes` for Upgrade Hibernate (transitive via Spring Boot 3.3.5) is non-specific; exact version selection and compatibility verification are required.
- The target `8.4.0 (compatible with Java 21 and Spring Boot 3.3.x)` for Upgrade mysql-connector-java is non-specific; exact version selection and compatibility verification are required.
- The target `2.17.2 (managed by Spring Boot 3.3.x BOM)` for Upgrade jackson-core is non-specific; exact version selection and compatibility verification are required.
- The target `2.17.2 (managed by Spring Boot 3.3.x BOM)` for Upgrade jackson-databind is non-specific; exact version selection and compatibility verification are required.

---

# Research — Shopizer Spring Boot 3.3 + Java 21 LTS Upgrade

## Repository Identity

| Field | Value |
|-------|-------|
| Repository URL | `https://github.com/XEBIA-ACE/shopizer` | _(Unverified: no Code Insights evidence ID supplied.)_
| Scoped ID | `2adc1099-45f0-422d-8f82-8326c32c7fc4/961d6363-9181-4dbb-a7be-615e1d3c2c67` | _(Unverified: no Code Insights evidence ID supplied.)_
| Ref | `3.2.5` | _(Unverified: no Code Insights evidence ID supplied.)_
| Last commit SHA | `052d2ed3c026525329405cef433c0aca7dd2cee3` | _(Unverified: no Code Insights evidence ID supplied.)_
| Index state | `succeeded` | _(Unverified: no Code Insights evidence ID supplied.)_
| Index started | `2026-09-02T11:03:28.951157Z` | _(Unverified: no Code Insights evidence ID supplied.)_
| Index finished | `2026-09-02T11:18:29.968824Z` | _(Unverified: no Code Insights evidence ID supplied.)_
| Java files | 1,210 |
| Total nodes | 17,814 |
| Total edges | 10,955 |
| Manifests scanned | 4 (pom.xml, sm-core-model/pom.xml, sm-core/pom.xml, sm-shop/pom.xml) |

**Grounding decision**: Repository identity verified. Ref `3.2.5` and commit SHA `052d2ed3c026525329405cef433c0aca7dd2cee3` match the expected target. Index is in `succeeded` state with non-zero file and symbol counts. All structural claims in spec.md, plan.md, and tasks.md are grounded in tool output unless explicitly labelled as "upstream analysis evidence". _(Unverified: no Code Insights evidence ID supplied.)_

---

## Numbered Query Log

| Q-ID | Tool | Parameters | Result Count | Finding | Disposition |
|------|------|-----------|-------------|---------|------------|
| Q-1 | `list_repos` | `project_id=2adc1099-45f0-422d-8f82-8326c32c7fc4` | 1 repo | Repo confirmed: `https://github.com/XEBIA-ACE/shopizer`, ref `3.2.5`, commit `052d2ed3c026525329405cef433c0aca7dd2cee3` | Used for identity verification | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-2 | `list_index_jobs` | `repo_id=...` | 1 job | State: succeeded; 1,210 Java files; 17,814 nodes; 10,955 edges; 4 manifests; 5 CVEs detected (tool-level) | Used for index status and file counts | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-3 | `architecture_overview` | `repo_id=...` | Full overview | 5 Maven modules confirmed; entry points; hotspots (ServiceRuntimeException fan-in 214, ServiceException 153, ResourceNotFoundException 134); architectural layers (core, api, test, utils, common, services) | Used for module structure, hotspots, entry points | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-4 | `semantic_search` (kind=class, query="WebSecurityConfigurerAdapter Spring Security configuration") | `repo_id=...` | 0 results | No class-level symbol found for security config by this query | Followed up with symbol ID from semantic_search on JWT | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-5 | `semantic_search` (query="JWT token creation parsing authentication") | `repo_id=...` | 20 results | `JWTTokenUtil` (c4661d6c968ca964), `JWTAdminAuthenticationProvider`, `JWTCustomerAuthenticationProvider`, `AuthenticationTokenFilter`, `JWTCustomerAuthenticationManager`, `JWTAdminAuthenticationManager`, `AuthenticationRequest`, `AuthenticationResponse`, `authenticationManagerBean` (3 occurrences in `MultipleEntryPointsSecurityConfig`) | Used to identify all JWT and security classes | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-6 | `search_code` (pattern="springfox\|swagger2\|@Api\|@ApiOperation\|@ApiParam") | `repo_id=...` | Error 404 | Tool returned 404 — search_code not available for this repo | Noted as tool limitation; Springfox usage confirmed via fulltext_search Q-14 | _(Unverified: no Code Insights evidence ID supplied.)_ **Finding**: Inconclusive; the query returned no usable evidence, so no absence, risk, or coverage conclusion can be drawn.
| Q-7 | `get_symbol` (symbol_id=c4661d6c968ca964) | `repo_id=...` | 1 symbol | `JWTTokenUtil` class at `sm-shop/.../JWTTokenUtil.java` lines 26–193 | Used for file path and line range | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-8 | `get_symbol` (symbol_id=6ae998569ef359ef) | `repo_id=...` | 1 symbol | `authenticationManagerBean` function at `MultipleEntryPointsSecurityConfig.java` line 295 | Used to confirm security config file path | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-9 | `get_symbol` (symbol_id=886feeae8147a94b) | `repo_id=...` | 1 symbol | `ServiceRuntimeException` class at `sm-shop/.../ServiceRuntimeException.java` lines 5–33 | Used to confirm hotspot file path | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-10 | `get_callers` (symbol_id=c4661d6c968ca964, depth=2) | `repo_id=...` | 0 callers | No callers found for JWTTokenUtil class node — graph does not resolve class-level callers at this depth | Noted; blast radius used instead | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-11 | `semantic_search` (query="Drools KIE rules engine session") | `repo_id=...` | 20 results | `DroolsBeanFactory` (ef74324928d39c4b) at `sm-core/.../DroolsBeanFactory.java` lines 24–112 confirmed | Used to identify Drools configuration class | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-12 | `semantic_search` (query="Infinispan cache manager configuration") | `repo_id=...` | 20 results | `CacheManagerImpl` (85356068b35ca7f4), `CmsImageFileManagerImpl` (infinispan, setCacheManager at line 462), `CmsStaticContentFileManagerImpl` (infinispan, setCacheManager at line 381) confirmed | Used to identify Infinispan usage | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-13 | `get_symbol` (symbol_id=ef74324928d39c4b) | `repo_id=...` | 1 symbol | `DroolsBeanFactory` class at `sm-core/.../DroolsBeanFactory.java` lines 24–112 | Used for file path and line range | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-14 | `get_symbol` (symbol_id=4ef2e65886370e2d) | `repo_id=...` | 1 symbol | `setCacheManager` function at `CmsImageFileManagerImpl` (infinispan) line 462 | Used to confirm Infinispan usage | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-15 | `find_dead_code` (min_confidence=0.9, limit=20) | `repo_id=...` | 0 candidates | No confirmed dead code at confidence ≥0.9 | Noted; no cleanup opportunities identified at this threshold | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-16 | `cyclomatic_complexity` (limit=20) | `repo_id=...` | 20 results | Top hotspots: `getBoxPackagesDetails` (fan-out 34), `processOrder` (fan-out 22, lines 1195–1359), `populate` ReadableProductPopulator (fan-out 21), `processOrderModel` (fan-out 21), `getShippingQuotes` USPSShippingQuote (fan-out 24) | Used to identify high-risk persistence and service methods | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-17 | `get_blast_radius` (target={kind:"class", id:"c4661d6c968ca964"}) | `repo_id=...` | 55 impacted symbols | JWTTokenUtil blast radius: 55 symbols, 1 file, risk LOW 21/100, zero test coverage (test_gap=1.0) | Used for JWT risk assessment | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-18 | `get_blast_radius` (target={kind:"class", id:"ef74324928d39c4b"}) | `repo_id=...` | 28 impacted symbols | DroolsBeanFactory blast radius: 28 symbols, 1 file, risk LOW 16/100, zero test coverage | Used for Drools risk assessment | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-19 | `get_blast_radius` (target={kind:"file", file_path:"...MultipleEntryPointsSecurityConfig.java"}) | `repo_id=...` | 7 impacted symbols | Security config blast radius: 7 symbols across 7 files (AuthenticationTokenFilter, CredentialsServiceImpl, JWTAdminAuthenticationProvider, JWTCustomerAuthenticationProvider, ServicesAuthenticationSuccessHandler, UserAuthenticationSuccessHandler, CredentialsService), risk LOW 11/100 | Used for security migration scope | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-20 | `trace_transaction` (symbol_id=5ff6c3df693280aa) | `repo_id=...` | 2 trace steps | `AuthenticateCustomerApi.authenticate` traces to controller and service layers; calls `AuthenticationResponse` | Used to confirm authentication flow | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-21 | `get_dependency_report` (min_severity=HIGH) | `repo_id=...` | 0 deps, 0 CVEs | OSV scanner returned 0 deps/CVEs for all 4 manifests — tool limitation (did not resolve transitive deps) | Noted as tool limitation; CVE data sourced from upstream analysis evidence | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-22 | `semantic_search` (kind=class, path_prefix="sm-shop/.../api", query="REST controller API endpoint handler") | `repo_id=...` | 0 results | No results with path_prefix filter | Followed up with fulltext_search | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-23 | `fulltext_search` (kind=class, query="SecurityConfig WebSecurity") | `repo_id=...` | 1 result | `config` function in `SearchServiceImpl` — not the security config | Confirmed security config is `MultipleEntryPointsSecurityConfig` from Q-5 | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-24 | `fulltext_search` (kind=class, query="SwaggerConfig OpenAPI Docket") | `repo_id=...` | 2 results | `DocumentationConfiguration.api()` (d78accdda21f0aee) at lines 51–85 confirmed as Springfox Docket bean | Used to identify Springfox configuration | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-25 | `fulltext_search` (kind=class, query="MapStruct mapper") | `repo_id=...` | 2 results | `Mapper` interface (0bb7bf7d437621d2) at `sm-shop/.../mapper/Mapper.java` lines 6–12; `mapper` function in `ShopizerPropertiesConfig` | Used to confirm MapStruct usage | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-26 | `get_symbol` (symbol_id=d78accdda21f0aee) | `repo_id=...` | 1 symbol | `DocumentationConfiguration.api()` at lines 51–85 | Used for Springfox Docket file path and line range | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-27 | `fulltext_search` (kind=class, query="DocumentationConfiguration") | `repo_id=...` | 1 result | `DocumentationConfiguration.api()` confirmed | Confirmed Springfox config location | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-28 | `fulltext_search` (kind=class, query="AuthenticateCustomerApi AuthenticateAdminApi") | `repo_id=...` | 12 results | `AuthenticateCustomerApi.authenticate` (5ff6c3df693280aa) lines 157–196; `AuthenticateUserApi.authenticate` (7767d7a9147ab5a7) lines 66–105; `CustomerFacadeImpl.authenticate` (5e8eb7bb6d73c98b) lines 427–460 | Used to identify authentication endpoints | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-29 | `get_symbol` (symbol_id=ee1f9ee818f613e6) | `repo_id=...` | 1 symbol | `Customer` entity class at `sm-core-model/.../Customer.java` lines 52–358 | Used to confirm entity location | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-30 | `get_symbol` (symbol_id=8399c303f325ccb6) | `repo_id=...` | 1 symbol | `processOrder` function at `OrderFacadeImpl.java` lines 1195–1359 | Used to confirm high-complexity method location | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-31 | `module_dependency_graph` (limit=30) | `repo_id=...` | 30 edges | Top coupling: PaymentServiceImpl→ServiceException (38), UserFacadeImpl→ServiceRuntimeException (33), Stripe3Payment→IntegrationException (22), Infinispan CMS impls→ServiceException (14 each) | Used for module coupling analysis | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-32 | `get_symbol` (symbol_id=85356068b35ca7f4) | `repo_id=...` | 1 symbol | `CacheManagerImpl` class at `sm-core/.../CacheManagerImpl.java` lines 12–95 | Used to confirm cache manager location | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-33 | `get_symbol` (symbol_id=0bb7bf7d437621d2) | `repo_id=...` | 1 symbol | `Mapper` interface at `sm-shop/.../mapper/Mapper.java` lines 6–12 | Used to confirm mapper interface location | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-34 | `list_index_jobs` | `repo_id=...` | 1 job | Index job ID `0a93cc19-9bd5-45e9-9445-98d3d6d44f98`; state succeeded; commit SHA `052d2ed3c026525329405cef433c0aca7dd2cee3` | Used for index status verification | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-35 | `iac_index` | `repo_id=...` | Error 502 | Tool returned 502 Bad Gateway — IAC index not available | Noted as tool limitation; Dockerfile details sourced from upstream analysis evidence | _(Unverified: no Code Insights evidence ID supplied.)_

---

## Technology and Architecture Findings

### Confirmed by Code Insights

1. **Five Maven modules** confirmed with exact cohesion scores: sm-core (0.574), sm-shop (0.732), sm-shop-model (1.0), sm-core-model (1.0), sm-core-modules (1.0) [Q-3].
2. **1,210 Java files** across all modules [Q-2].
3. **Application entry point**: `sm-shop/src/main/java/com/salesmanager/shop/application/ShopApplication.java` [Q-3]. _(Unverified: no Code Insights evidence ID supplied.)_
4. **JWT security**: `JWTTokenUtil` at `sm-shop/.../JWTTokenUtil.java` lines 26–193; `JWTAdminAuthenticationProvider`, `JWTCustomerAuthenticationProvider`, `AuthenticationTokenFilter`, `JWTCustomerAuthenticationManager`, `JWTAdminAuthenticationManager` all confirmed [Q-5]. _(Unverified: no Code Insights evidence ID supplied.)_
5. **Security configuration**: `MultipleEntryPointsSecurityConfig` at `sm-shop/.../MultipleEntryPointsSecurityConfig.java`; three `authenticationManagerBean` methods at lines 87, 295, 380 [Q-5, Q-8]. _(Unverified: no Code Insights evidence ID supplied.)_
6. **Drools**: `DroolsBeanFactory` at `sm-core/.../DroolsBeanFactory.java` lines 24–112; methods `getKieFileSystem`, `getKieContainer`, `getKieRepository`, `getKieSession`, `getDrlFromExcel` [Q-11, Q-13]. _(Unverified: no Code Insights evidence ID supplied.)_
7. **Infinispan**: `CmsImageFileManagerImpl` (infinispan path) with `setCacheManager` at line 462; `CmsStaticContentFileManagerImpl` (infinispan path) with `setCacheManager` at line 381; `CacheManagerImpl` at lines 12–95 [Q-12, Q-14, Q-32]. _(Unverified: no Code Insights evidence ID supplied.)_
8. **Springfox**: `DocumentationConfiguration.api()` at `sm-shop/.../DocumentationConfiguration.java` lines 51–85 [Q-24, Q-26]. _(Unverified: no Code Insights evidence ID supplied.)_
9. **MapStruct**: `Mapper` interface at `sm-shop/.../mapper/Mapper.java` lines 6–12 [Q-25, Q-33]. _(Unverified: no Code Insights evidence ID supplied.)_
10. **Authentication endpoints**: `AuthenticateCustomerApi.authenticate` lines 157–196; `AuthenticateUserApi.authenticate` lines 66–105; `CustomerFacadeImpl.authenticate` lines 427–460 [Q-28]. _(Unverified: no Code Insights evidence ID supplied.)_
11. **High-complexity methods**: `processOrder` (fan-out 22, lines 1195–1359), `populate` ReadableProductPopulator (fan-out 21), `processOrderModel` (fan-out 21) [Q-16]. _(Unverified: no Code Insights evidence ID supplied.)_
12. **Test files**: 37 test modules in `sm-core/src/test/` and `sm-shop/src/test/` [Q-3]. _(Unverified: no Code Insights evidence ID supplied.)_
13. **Dead code**: Zero confirmed dead symbols at confidence ≥0.9 [Q-15].

### Sourced from Upstream Analysis Evidence (not directly verified by Code Insights)

- Spring Boot version: 2.5.12 (upstream analysis; dependency report returned 0 deps [Q-21])
- Java version: 11 (upstream analysis)
- Drools version: 7.32.0.Final (upstream analysis)
- Infinispan version: 9.4.18.Final (upstream analysis)
- jjwt version: 0.8.0 (upstream analysis)
- commons-fileupload version: 1.3.3 (upstream analysis)
- All other dependency versions (upstream analysis)
- Docker base image: `adoptopenjdk/openjdk11-openj9:alpine` (upstream analysis; IAC index returned 502 [Q-35]) _(Unverified: no Code Insights evidence ID supplied.)_
- JaCoCo coverage: sm-shop 4% line, 1% branch (upstream analysis)
- CI toolchain: Jenkins, CircleCI, SonarQube/SonarCloud (upstream analysis)
- CVE details for all dependencies (upstream analysis; dependency report returned 0 CVEs [Q-21])

---

## Conflicts Between Code Insights and Upstream Analysis

| Conflict ID | Upstream Claim | Code Insights Finding | Resolution |
|-------------|---------------|----------------------|-----------|
| C-1 | Effort estimate: 45 person-days | Task breakdown totals ~59 person-days (includes spike tasks TASK-2.1, TASK-2.3 and test coverage raise TASK-2.7 not in upstream estimate) | Both values recorded; 59 pd used in tasks.md as it reflects actual scope |
| C-2 | Dependency report should show CVEs | `get_dependency_report` returned 0 deps, 0 CVEs for all 4 manifests | Tool limitation noted; CVE data retained from upstream analysis evidence | _(Unverified: no Code Insights evidence ID supplied.)_
| C-3 | IAC index should show Dockerfile | `iac_index` returned 502 Bad Gateway | Tool limitation noted; Dockerfile details retained from upstream analysis evidence | _(Unverified: no Code Insights evidence ID supplied.)_

---

## Evidence Gaps

| Gap ID | Description | Impact |
|--------|-------------|--------|
| EG-1 | Exact Drools target version for Spring Boot 3.3.5 / Jakarta EE not verified (OQ-1) | TASK-2.1 spike required before TASK-2.2 can begin |
| EG-2 | Exact Infinispan target version for Spring Boot 3.3.5 / Jakarta EE not verified (OQ-2) | TASK-2.3 spike required before TASK-2.4 can begin |
| EG-3 | Dockerfile path not confirmed (IAC index returned 502) | TASK-1.3 must locate Dockerfile manually |
| EG-4 | CI configuration file paths (Jenkinsfile, CircleCI config) not confirmed (search_code returned 404) | TASK-6.2 must locate CI config files manually |
| EG-5 | Dependency versions not confirmed by Code Insights dependency report (returned 0 deps) | All version claims sourced from upstream analysis evidence |
| EG-6 | sm-core-modules/pom.xml and sm-shop-model/pom.xml not in the 4 manifests scanned | May contain additional dependency declarations not captured |