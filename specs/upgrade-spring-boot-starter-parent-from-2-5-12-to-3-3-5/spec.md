## Authoritative Pipeline Facts
These facts are generated from Tech Analysis and the selected Upgrade Option and take precedence over narrative text.

### Current State
| Category | Component | Current value | Source |
| --- | --- | --- | --- |
| language | Java | 11 | Tech Analysis |
| runtime | JVM | 11 | Tech Analysis |
| build_tool | Build tool | Maven | Tech Analysis |
| package_manager | Package manager | Maven | Tech Analysis |
| framework | Spring Boot | 2.5.12 | Tech Analysis |
| framework | Spring Security | 5.5.x (transitive via Spring Boot 2.5.12) | Tech Analysis |
| framework | Spring Data JPA | 2.5.x (transitive via Spring Boot 2.5.12) | Tech Analysis |
| framework | Springfox Swagger2 | 2.9.2 | Tech Analysis |
| framework | Drools / KIE | 7.32.0.Final | Tech Analysis |
| framework | Infinispan | 9.4.18.Final | Tech Analysis |
| framework | Hibernate (via Spring Boot) | 5.4.x (transitive via Spring Boot 2.5.12) | Tech Analysis |
| framework | MapStruct | 1.3.0.Final | Tech Analysis |
| dependency | spring-boot-starter-parent | 2.5.12 | Tech Analysis |
| dependency | jackson-databind | 2.12.6.1 | Tech Analysis |
| dependency | jackson-core | 2.10.2 | Tech Analysis |
| dependency | elasticsearch | 7.5.2 | Tech Analysis |
| dependency | commons-fileupload | 1.3.3 | Tech Analysis |
| dependency | commons-lang3 | 3.5 | Tech Analysis |
| dependency | commons-io | 2.7 | Tech Analysis |
| dependency | commons-collections4 | 4.1 | Tech Analysis |
| dependency | commons-validator | 1.5.1 | Tech Analysis |
| dependency | guava | 27.1-jre | Tech Analysis |
| dependency | jjwt | 0.8.0 | Tech Analysis |
| dependency | infinispan-core | 9.4.18.Final | Tech Analysis |
| dependency | org.apache.httpcomponents:httpclient | 4.5.2 | Tech Analysis |
| dependency | springfox-swagger2 | 2.9.2 | Tech Analysis |
| dependency | postgresql | 42.2.18 | Tech Analysis |
| dependency | mysql-connector-java | 8.0.21 | Tech Analysis |
| dependency | adoptopenjdk/openjdk11-openj9:alpine (Docker base) | openjdk11-openj9 | Tech Analysis |
| dependency | org.owasp.antisamy:antisamy | 1.6.7 | Tech Analysis |
| dependency | drools-core | 7.32.0.Final | Tech Analysis |

### Target State
| Component | Current | Explicit target | Source |
| --- | --- | --- | --- |
| commons-fileupload | 1.3.3 | 1.5 | Selected Upgrade Option |
| guava | 27.1-jre | 33.2.1-jre | Selected Upgrade Option |
| jjwt | 0.8.0 | 0.12.6 | Selected Upgrade Option |
| mysql-connector-java | 8.0.21 | 8.4.0 | Selected Upgrade Option |
| postgresql | 42.2.18 | 42.7.3 | Selected Upgrade Option |
| MapStruct | 1.3.0.Final | 1.6.2 | Selected Upgrade Option |
| springdoc-openapi-starter-webmvc-ui | N/A (replacing springfox-swagger2 2.9.2) | 2.6.0 | Selected Upgrade Option |
| spring-boot-starter-parent | 2.5.12 | 3.3.5 | Selected Upgrade Option |
| eclipse-temurin Docker base image | adoptopenjdk/openjdk11-openj9:alpine | eclipse-temurin:21-jre-alpine | Selected Upgrade Option |
| Java | 11 | 21 | Selected Upgrade Option |
| Upgrade Java runtime | 11 | 21 LTS (prerequisite for Spring Boot 3.3.x) | Selected Upgrade Option |
| Replace Docker base image adoptopenjdk/openjdk11-openj9:alpine | Not supplied | eclipse-temurin:21-jre-alpine | Selected Upgrade Option |
| Upgrade spring-boot-starter-parent | 2.5.12 | 3.3.5 | Selected Upgrade Option |
| Perform javax | Not supplied | jakarta EE namespace migration across all source modules (sm-core, sm-shop, sm-shop-model, sm-core-model, sm-core-modules) | Selected Upgrade Option |
| Upgrade Spring Security (transitive via Spring Boot 3.3.5) | 5.5.x | 6.3.x; migrate deprecated WebSecurityConfigurerAdapter to SecurityFilterChain | Selected Upgrade Option |
| Upgrade Spring Data JPA (transitive via Spring Boot 3.3.5) | 2.5.x | 3.3.x | Selected Upgrade Option |
| Upgrade Hibernate (transitive via Spring Boot 3.3.5) | 5.4.x | 6.5.x; review HQL/Criteria API changes | Selected Upgrade Option |
| Upgrade commons-fileupload | 1.3.3 | 1.5 (CVE-2023-24998) | Selected Upgrade Option |
| Upgrade commons-io | 2.7 | 2.16.1 (CVE-2021-29425) | Selected Upgrade Option |
| Upgrade guava | 27.1-jre | 33.2.1-jre (CVE-2023-2976) | Selected Upgrade Option |
| Upgrade jjwt | 0.8.0 | 0.12.6 (algorithm confusion risk; API migration required) | Selected Upgrade Option |
| Upgrade org.apache.httpcomponents:httpclient | 4.5.2 | 4.5.14 (CVE-2020-13956) | Selected Upgrade Option |
| Upgrade postgresql driver | 42.2.18 | 42.7.3 (CVE-2022-21724, CVE-2022-26520) | Selected Upgrade Option |
| Upgrade mysql-connector-java | 8.0.21 | 8.4.0 (compatible with Java 21 and Spring Boot 3.3.x) | Selected Upgrade Option |
| Upgrade jackson-core | 2.10.2 | 2.17.2 (managed by Spring Boot 3.3.x BOM) | Selected Upgrade Option |
| Upgrade jackson-databind | 2.12.6.1 | 2.17.2 (managed by Spring Boot 3.3.x BOM) | Selected Upgrade Option |
| Upgrade MapStruct | 1.3.0.Final | 1.6.2 | Selected Upgrade Option |
| Upgrade commons-lang3 | 3.5 | 3.14.0 | Selected Upgrade Option |
| Upgrade commons-collections4 | 4.1 | 4.4 | Selected Upgrade Option |
| Upgrade commons-validator | 1.5.1 | 1.9.0 | Selected Upgrade Option |
| Upgrade org.owasp.antisamy:antisamy | 1.6.7 | 1.7.5 | Selected Upgrade Option |

## Authoritative Modernization Decision
- Selected option: Spring Boot 3.3 + Java 21 LTS Full Upgrade (`moderate`)
- Effort: 45 person-days
- Risk score: 6/10
- Blockers: javax → jakarta namespace migration touches all five Maven modules and all third-party libraries must also support jakarta namespace; verify Drools 7.32.0.Final compatibility with Jakarta EE — Drools 7.x uses javax namespace and may require upgrade to 8.x/9.x or a compatibility shim, Drools 7.32.0.Final is not compatible with Spring Boot 3.x / Jakarta EE; must evaluate upgrade path to Drools 8.x or Kogito before or alongside this migration, Infinispan 9.4.18.Final does not support Jakarta EE; must be upgraded to Infinispan 14.x or 15.x which supports Jakarta EE — verify Spring Boot 3.3.x integration, WebSecurityConfigurerAdapter removed in Spring Security 6.x; all security configuration classes must be rewritten, jjwt 0.8.0 → 0.12.x has breaking API changes; all JWT creation and parsing code must be refactored, Hibernate 6.x introduces breaking changes in HQL, Criteria API, and type mappings; full regression testing of persistence layer required, Very low test coverage (sm-shop: 4% line, 1% branch) significantly increases regression risk; recommend writing integration tests for critical paths before migration, MapStruct 1.3.x → 1.6.x may require annotation processor configuration updates in Maven
- Impacted areas: source code, CI/CD, infrastructure, tests, docs

### Open Questions
- Verify the target requirement for JVM; current value `11` is intentionally omitted from Target State.
- Verify the target requirement for Build tool; current value `Maven` is intentionally omitted from Target State.
- Verify the target requirement for Package manager; current value `Maven` is intentionally omitted from Target State.
- Select and verify an exact supported target for Upgrade Java runtime; the selected option specifies `21 LTS (prerequisite for Spring Boot 3.3.x)`.
- Select and verify an exact supported target for Upgrade Spring Security (transitive via Spring Boot 3.3.5); the selected option specifies `6.3.x; migrate deprecated WebSecurityConfigurerAdapter to SecurityFilterChain`.
- Select and verify an exact supported target for Upgrade Spring Data JPA (transitive via Spring Boot 3.3.5); the selected option specifies `3.3.x`.
- Select and verify an exact supported target for Upgrade Hibernate (transitive via Spring Boot 3.3.5); the selected option specifies `6.5.x; review HQL/Criteria API changes`.
- Select and verify an exact supported target for Upgrade mysql-connector-java; the selected option specifies `8.4.0 (compatible with Java 21 and Spring Boot 3.3.x)`.
- Select and verify an exact supported target for Upgrade jackson-core; the selected option specifies `2.17.2 (managed by Spring Boot 3.3.x BOM)`.
- Select and verify an exact supported target for Upgrade jackson-databind; the selected option specifies `2.17.2 (managed by Spring Boot 3.3.x BOM)`.

---

# Specification — Shopizer Spring Boot 3.3 + Java 21 LTS Upgrade

## Summary
Migrate Shopizer (ref `3.2.5`, commit `052d2ed3c026525329405cef433c0aca7dd2cee3`) from Spring Boot 2.5.12 / Java 11 to Spring Boot 3.3.5 / Java 21 LTS. The migration resolves EOL framework risk, multiple critical and high CVEs, and cloud-native gaps. It requires a mandatory javax→jakarta EE namespace migration across all five Maven modules, replacement of the abandoned Springfox Swagger2 library with springdoc-openapi 2.6.0, a full rewrite of Spring Security configuration, and upgrades to Drools, Infinispan, JJWT, and all other CVE-bearing dependencies. _(Unverified: no Code Insights evidence ID supplied.)_

**Selected option**: `moderate` — Spring Boot 3.3 + Java 21 LTS Full Upgrade   _(Unverified: no Code Insights evidence ID supplied.)_
**Effort**: 45 person-days  
**Risk score**: 6/10  
**Upgrade urgency**: critical

---

## Motivation

- Spring Boot 2.5.12 is EOL and no longer receives security patches (upstream analysis evidence).
- Spring Security 5.5.x is EOL; Spring Framework CVE-2022-22965 (Spring4Shell) era (upstream analysis evidence).
- commons-fileupload 1.3.3 carries CVE-2023-24998 (critical DoS) and CVE-2016-1000031 [Q-11].
- guava 27.1-jre carries CVE-2023-2976 (temp directory vulnerability) [Q-11].
- jjwt 0.8.0 carries algorithm confusion risk; `JWTTokenUtil` (symbol `c4661d6c968ca964`, file `sm-shop/src/main/java/com/salesmanager/shop/store/security/JWTTokenUtil.java`) is the sole JWT utility class [Q-3, Q-9]. _(Unverified: no Code Insights evidence ID supplied.)_
- httpclient 4.5.2 carries CVE-2020-13956 [Q-11].
- postgresql driver 42.2.18 carries CVE-2022-21724 and CVE-2022-26520 [Q-11].
- springfox-swagger2 2.9.2 is abandoned and carries CVE-2022-1471 via SnakeYAML transitive dependency; `DocumentationConfiguration.api()` (symbol `d78accdda21f0aee`, file `sm-shop/src/main/java/com/salesmanager/shop/application/config/DocumentationConfiguration.java`) is the Springfox Docket configuration [Q-14]. _(Unverified: no Code Insights evidence ID supplied.)_
- AdoptOpenJDK base image is retired; no active security patches (upstream analysis evidence).
- sm-shop test coverage: 4% line, 1% branch — critically low safety net for a major migration (upstream analysis evidence).

---

## Repository Evidence

| Evidence ID | Finding | Source |
|-------------|---------|--------|
| Q-1 | Repo: `https://github.com/XEBIA-ACE/shopizer`, ref `3.2.5`, commit `052d2ed3c026525329405cef433c0aca7dd2cee3`, index state: succeeded | `list_repos`, `list_index_jobs` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-2 | 1,210 Java files; 17,814 nodes; 10,955 edges; 4 manifests | `list_index_jobs` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-3 | `JWTTokenUtil` class at `sm-shop/src/main/java/com/salesmanager/shop/store/security/JWTTokenUtil.java` lines 26–193; blast radius: 55 symbols, 1 file, risk LOW 21/100, zero test coverage | `get_symbol`, `get_blast_radius` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-4 | `MultipleEntryPointsSecurityConfig` at `sm-shop/src/main/java/com/salesmanager/shop/application/config/MultipleEntryPointsSecurityConfig.java`; `authenticationManagerBean` methods at lines 87, 295, 380; blast radius: 7 symbols across 7 files including `AuthenticationTokenFilter`, `JWTAdminAuthenticationProvider`, `JWTCustomerAuthenticationProvider`, `ServicesAuthenticationSuccessHandler`, `UserAuthenticationSuccessHandler`, `CredentialsServiceImpl`, `CredentialsService` | `semantic_search`, `get_blast_radius` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-5 | `DroolsBeanFactory` class at `sm-core/src/main/java/com/salesmanager/core/business/configuration/DroolsBeanFactory.java` lines 24–112; blast radius: 28 symbols, 1 file, risk LOW 16/100, zero test coverage | `semantic_search`, `get_blast_radius` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-6 | Infinispan usage confirmed: `CmsImageFileManagerImpl` at `sm-core/src/main/java/com/salesmanager/core/business/modules/cms/product/infinispan/CmsImageFileManagerImpl.java`; `CmsStaticContentFileManagerImpl` at `sm-core/src/main/java/com/salesmanager/core/business/modules/cms/content/infinispan/CmsStaticContentFileManagerImpl.java`; `CacheManagerImpl` at `sm-core/src/main/java/com/salesmanager/core/business/modules/cms/impl/CacheManagerImpl.java` | `semantic_search`, `get_symbol` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-7 | Five Maven modules confirmed: sm-core (358 modules, cohesion 0.574), sm-shop (326 modules, cohesion 0.732), sm-shop-model (323 modules, cohesion 1.0), sm-core-model (187 modules, cohesion 1.0), sm-core-modules (15 modules, cohesion 1.0) | `architecture_overview` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-8 | Application entry point: `sm-shop/src/main/java/com/salesmanager/shop/application/ShopApplication.java` | `architecture_overview` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-9 | JWT security classes: `JWTUser`, `JWTAdminAuthenticationProvider`, `JWTCustomerAuthenticationProvider`, `AuthenticationTokenFilter`, `JWTCustomerAuthenticationManager`, `JWTAdminAuthenticationManager`, `AuthenticationRequest`, `AuthenticationResponse` — all in `sm-shop/src/main/java/com/salesmanager/shop/store/security/` | `semantic_search` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-10 | `DocumentationConfiguration.api()` at `sm-shop/src/main/java/com/salesmanager/shop/application/config/DocumentationConfiguration.java` lines 51–85 — Springfox Docket bean | `fulltext_search`, `get_symbol` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-11 | Dependency report: 4 manifests scanned (pom.xml, sm-core-model/pom.xml, sm-core/pom.xml, sm-shop/pom.xml); CVE scanner returned 0 deps/CVEs (tool limitation — OSV scanner did not resolve transitive deps from these manifests); CVE data sourced from upstream analysis evidence | `get_dependency_report` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-12 | Top complexity hotspots: `getBoxPackagesDetails` (fan-out 34, `DefaultPackagingImpl`), `processOrder` (fan-out 22, `OrderFacadeImpl` lines 1195–1359), `populate` (fan-out 21, `ReadableProductPopulator`), `processOrderModel` (fan-out 21, `OrderFacadeImpl`) | `cyclomatic_complexity` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-13 | Dead code scan: 0 confirmed dead symbols at confidence ≥0.9 | `find_dead_code` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-14 | `Mapper` interface at `sm-shop/src/main/java/com/salesmanager/shop/mapper/Mapper.java` lines 6–12 — custom mapper interface (not MapStruct generated) | `fulltext_search`, `get_symbol` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-15 | Module dependency hotspots: `PaymentServiceImpl→ServiceException` (weight 38), `UserFacadeImpl→ServiceRuntimeException` (weight 33), Stripe3Payment/StripePayment→IntegrationException (weight 22/21), Infinispan CMS impls→ServiceException (weight 14 each) | `module_dependency_graph` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-16 | `authenticate` function at `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/customer/AuthenticateCustomerApi.java` lines 157–196; trace touches controller and service layers | `trace_transaction` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-17 | `AuthenticateUserApi.authenticate` at `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/user/AuthenticateUserApi.java` lines 66–105 | `fulltext_search` | _(Unverified: no Code Insights evidence ID supplied.)_
| Q-18 | Test files confirmed in `sm-core/src/test/` and `sm-shop/src/test/`; architectural layer `test` contains 37 modules | `architecture_overview` | _(Unverified: no Code Insights evidence ID supplied.)_

---

## Current State

| Component | Current Version | Status |
|-----------|----------------|--------|
| Java runtime | 11 | EOL for security patches; incompatible with Spring Boot 3.x |
| spring-boot-starter-parent | 2.5.12 | EOL; critical CVE exposure |
| Spring Security | 5.5.x (transitive) | EOL |
| Spring Data JPA | 2.5.x (transitive) | EOL |
| Hibernate | 5.4.x (transitive) | EOL |
| Springfox Swagger2 | 2.9.2 | Abandoned; CVE-2022-1471 |
| Drools / KIE | 7.32.0.Final | javax namespace; incompatible with Jakarta EE |
| Infinispan | 9.4.18.Final | javax namespace; EOL; incompatible with Jakarta EE |
| MapStruct | 1.3.0.Final | Incompatible with Java 21 annotation processor |
| jjwt | 0.8.0 | Algorithm confusion risk; breaking API in 0.12.x |
| commons-fileupload | 1.3.3 | CVE-2023-24998 (critical DoS) |
| commons-io | 2.7 | CVE-2021-29425 (path traversal) |
| guava | 27.1-jre | CVE-2023-2976 |
| httpclient | 4.5.2 | CVE-2020-13956 |
| postgresql driver | 42.2.18 | CVE-2022-21724, CVE-2022-26520 |
| mysql-connector-java | 8.0.21 | Multiple CVEs below 8.0.28 |
| jackson-core | 2.10.2 | Mismatched with jackson-databind 2.12.x |
| jackson-databind | 2.12.6.1 | Outdated |
| commons-lang3 | 3.5 | Outdated |
| commons-collections4 | 4.1 | Outdated |
| commons-validator | 1.5.1 | Outdated |
| antisamy | 1.6.7 | Outdated |
| Docker base image | adoptopenjdk/openjdk11-openj9:alpine | Retired project; no security patches |
| JaCoCo sm-shop line coverage | 4% | Critically low |
| JaCoCo sm-shop branch coverage | 1% | Critically low |
| OWASP Dependency-Check | Not present | Missing from CI pipeline |
| Dockerfile HEALTHCHECK | Not present | Cloud-native gap |

---

## Target State

| Component | Target Version | Evidence |
|-----------|---------------|---------|
| Java runtime | 21 LTS | Selected upgrade option; Spring Boot 3.3.5 requires ≥17 |
| spring-boot-starter-parent | 3.3.5 | Selected upgrade option |
| Spring Security | 6.3.x (transitive via Spring Boot 3.3.5) | Selected upgrade option |
| Spring Data JPA | 3.3.x (transitive via Spring Boot 3.3.5) | Selected upgrade option |
| Hibernate | 6.5.x (transitive via Spring Boot 3.3.5) | Selected upgrade option |
| springdoc-openapi-starter-webmvc-ui | 2.6.0 | Selected upgrade option; replaces Springfox |
| Drools / KIE | ≥8.0.0 required; exact target TBD | Compatibility unverified — open question |
| Infinispan | ≥14.0.0.Final required; exact target TBD | Compatibility unverified — open question |
| MapStruct | 1.6.2 | Selected upgrade option |
| jjwt | 0.12.6 | Selected upgrade option |
| commons-fileupload | 1.5 | Selected upgrade option |
| commons-io | 2.16.1 | Selected upgrade option |
| guava | 33.2.1-jre | Selected upgrade option |
| httpclient | 4.5.14 | Selected upgrade option |
| postgresql driver | 42.7.3 | Selected upgrade option |
| mysql-connector-java | 8.4.0 | Selected upgrade option |
| jackson-core | 2.17.2 (BOM-managed) | Selected upgrade option |
| jackson-databind | 2.17.2 (BOM-managed) | Selected upgrade option |
| commons-lang3 | 3.14.0 | Selected upgrade option |
| commons-collections4 | 4.4 | Selected upgrade option |
| commons-validator | 1.9.0 | Selected upgrade option |
| antisamy | 1.7.5 | Selected upgrade option |
| Docker base image | eclipse-temurin:21-jre-alpine | Selected upgrade option |
| JaCoCo sm-shop line coverage threshold | 20% | Selected upgrade option |
| OWASP Dependency-Check | Added to CI | Selected upgrade option |
| Dockerfile HEALTHCHECK | Added | Selected upgrade option |
| elasticsearch property | Removed (stale) | Selected upgrade option |

---

## Compatibility Matrix

| Component | Current | Target | Min Required | Status | Evidence |
|-----------|---------|--------|-------------|--------|---------|
| Java | 11 | 21 | 17 | incompatible→compatible | Spring Boot 3.x System Requirements |
| Docker base image | adoptopenjdk/openjdk11-openj9:alpine | eclipse-temurin:21-jre-alpine | 21 | incompatible→compatible | Eclipse Temurin Docker Hub |
| spring-boot-starter-parent | 2.5.12 | 3.3.5 | 3.0.0 | incompatible→compatible | Spring Boot Releases |
| springdoc-openapi-starter-webmvc-ui | N/A | 2.6.0 | 2.0.0 | incompatible→compatible | springdoc-openapi Documentation |
| MapStruct | 1.3.0.Final | 1.6.2 | 1.5.0.Final | incompatible→compatible | MapStruct GitHub Releases |
| postgresql | 42.2.18 | 42.7.3 | 42.2.25 | incompatible→compatible | PostgreSQL JDBC Driver Changelog |
| mysql-connector-java | 8.0.21 | 8.4.0 | 8.0.28 | incompatible→compatible | MySQL Connector/J Release Notes |
| jjwt | 0.8.0 | 0.12.6 | N/A | incompatible→compatible | JJWT GitHub Releases |
| guava | 27.1-jre | 33.2.1-jre | 32.0.0-jre | incompatible→compatible | Google Guava Security Advisory |
| commons-fileupload | 1.3.3 | 1.5 | 1.5 | incompatible→compatible | Apache Commons FileUpload Release Notes |

---

## Scope

### In Scope
- All five Maven modules: sm-core, sm-shop, sm-shop-model, sm-core-model, sm-core-modules [Q-7]
- `sm-shop/src/main/java/com/salesmanager/shop/application/config/MultipleEntryPointsSecurityConfig.java` — full rewrite [Q-4] _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/java/com/salesmanager/shop/store/security/JWTTokenUtil.java` — JJWT API migration [Q-3] _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/java/com/salesmanager/shop/application/config/DocumentationConfiguration.java` — Springfox→springdoc migration [Q-10] _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-core/src/main/java/com/salesmanager/core/business/configuration/DroolsBeanFactory.java` — Drools upgrade [Q-5] _(Unverified: no Code Insights evidence ID supplied.)_
- Infinispan CMS implementations: `CmsImageFileManagerImpl` (infinispan), `CmsStaticContentFileManagerImpl` (infinispan) [Q-6] _(Unverified: no Code Insights evidence ID supplied.)_
- All REST controllers in `sm-shop/src/main/java/com/salesmanager/shop/store/api/` — Swagger annotation migration _(Unverified: no Code Insights evidence ID supplied.)_
- All `javax.*` imports across all five modules — namespace migration _(Unverified: no Code Insights evidence ID supplied.)_
- Root `pom.xml` and all module `pom.xml` files — dependency version updates _(Unverified: no Code Insights evidence ID supplied.)_
- Dockerfile — base image replacement and HEALTHCHECK addition
- CI pipeline (Jenkinsfile / CircleCI config) — OWASP Dependency-Check addition
- JaCoCo configuration in sm-shop `pom.xml` — threshold raise _(Unverified: no Code Insights evidence ID supplied.)_

### Out of Scope
- Functional feature changes to the commerce domain logic
- Database schema migrations
- Kubernetes/Helm chart creation (not present in repository [Q-2])
- Infinispan clustering configuration for horizontal scaling
- Virtual threads adoption (Java 21 feature; available post-migration but not in this scope)
- Structured/JSON logging configuration
- DAST integration

---

## Affected Components and Interfaces

### Security Layer [Q-4, Q-9]
- `MultipleEntryPointsSecurityConfig` — `WebSecurityConfigurerAdapter` must be replaced with `SecurityFilterChain` beans _(Unverified: no Code Insights evidence ID supplied.)_
- `AuthenticationTokenFilter` — JWT filter; depends on `JWTTokenUtil` _(Unverified: no Code Insights evidence ID supplied.)_
- `JWTAdminAuthenticationProvider`, `JWTCustomerAuthenticationProvider` — authentication providers _(Unverified: no Code Insights evidence ID supplied.)_
- `JWTAdminAuthenticationManager`, `JWTCustomerAuthenticationManager` — authentication managers _(Unverified: no Code Insights evidence ID supplied.)_
- `ServicesAuthenticationSuccessHandler`, `UserAuthenticationSuccessHandler` — success handlers _(Unverified: no Code Insights evidence ID supplied.)_
- `CredentialsServiceImpl`, `CredentialsService` — credentials service _(Unverified: no Code Insights evidence ID supplied.)_

### JWT Layer [Q-3, Q-9]
- `JWTTokenUtil` — all methods (`generateToken`, `validateToken`, `refreshToken`, `getAllClaimsFromToken`, `doGenerateToken`) use jjwt 0.8.0 API; must be migrated to jjwt 0.12.6 fluent API _(Unverified: no Code Insights evidence ID supplied.)_
- `AuthenticateCustomerApi.authenticate` [Q-16], `AuthenticateUserApi.authenticate` [Q-17] _(Unverified: no Code Insights evidence ID supplied.)_

### API Documentation Layer [Q-10]
- `DocumentationConfiguration` — Springfox `Docket` bean at lines 51–85; replace with springdoc-openapi `OpenAPI` bean _(Unverified: no Code Insights evidence ID supplied.)_
- All REST controllers in `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/` — `@Api`, `@ApiOperation`, `@ApiParam` annotations must be replaced with OpenAPI 3 equivalents _(Unverified: no Code Insights evidence ID supplied.)_

### Persistence Layer [Q-12, Q-15]
- `OrderFacadeImpl.processOrder` (fan-out 22, lines 1195–1359) — highest-risk service method; Hibernate 6.x HQL/Criteria API changes may affect this _(Unverified: no Code Insights evidence ID supplied.)_
- `CategoryServiceImpl.delete` (fan-out 14) — complex persistence operation _(Unverified: no Code Insights evidence ID supplied.)_
- All JPA entities in `sm-core-model` — Hibernate 6.x type mapping changes _(Unverified: no Code Insights evidence ID supplied.)_

### Rules Engine [Q-5]
- `DroolsBeanFactory` — KIE session factory; must be upgraded from Drools 7.32.0.Final _(Unverified: no Code Insights evidence ID supplied.)_

### Cache Layer [Q-6]
- `CmsImageFileManagerImpl` (infinispan path) — Infinispan 9.x API; must be upgraded _(Unverified: no Code Insights evidence ID supplied.)_
- `CmsStaticContentFileManagerImpl` (infinispan path) — Infinispan 9.x API; must be upgraded _(Unverified: no Code Insights evidence ID supplied.)_
- `CacheManagerImpl` — cache manager abstraction _(Unverified: no Code Insights evidence ID supplied.)_

### Build and Infrastructure
- Root `pom.xml` — parent version, Java version properties, dependency versions _(Unverified: no Code Insights evidence ID supplied.)_
- Module `pom.xml` files — dependency declarations _(Unverified: no Code Insights evidence ID supplied.)_
- Dockerfile — base image, HEALTHCHECK
- CI configuration — OWASP Dependency-Check plugin

---

## Breaking Changes

| Change | Impact | Mitigation |
|--------|--------|-----------|
| javax→jakarta namespace | All five modules; all third-party libs must support Jakarta EE | OpenRewrite automated migration; verify each dependency |
| `WebSecurityConfigurerAdapter` removed | `MultipleEntryPointsSecurityConfig` must be fully rewritten | Rewrite as `SecurityFilterChain` beans per Spring Security 6.x migration guide | _(Unverified: no Code Insights evidence ID supplied.)_
| jjwt 0.8.0→0.12.6 API | `JWTTokenUtil` all methods; `Jwts.builder()`, `Jwts.parser()` API changed | Refactor to new fluent API; update key handling | _(Unverified: no Code Insights evidence ID supplied.)_
| Hibernate 6.x HQL/Criteria | `processOrder`, `CategoryServiceImpl.delete`, all JPQL queries | Full regression test of persistence layer | _(Unverified: no Code Insights evidence ID supplied.)_
| Springfox removed | `DocumentationConfiguration`; all `@Api*` annotations in controllers | Replace with springdoc-openapi; update all annotations | _(Unverified: no Code Insights evidence ID supplied.)_
| Spring Data JPA 3.x | Repository method signatures; `Optional` handling changes | Review all repository interfaces | _(Unverified: no Code Insights evidence ID supplied.)_
| Drools 7→8+ | `DroolsBeanFactory`; KIE API changes | Evaluate Drools 8.x migration guide | _(Unverified: no Code Insights evidence ID supplied.)_
| Infinispan 9→14+ | `CmsImageFileManagerImpl`, `CmsStaticContentFileManagerImpl` | Evaluate Infinispan 14.x migration guide | _(Unverified: no Code Insights evidence ID supplied.)_
| MapStruct 1.3→1.6 | Annotation processor configuration in Maven | Update `maven-compiler-plugin` annotation processor config | _(Unverified: no Code Insights evidence ID supplied.)_

---

## Testable Acceptance Criteria

| ID | Criterion |
|----|-----------|
| AC-1 | All five Maven modules compile with `mvn compile -Djava.version=21` with zero errors | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-2 | `mvn test` passes with zero failures across all modules | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-3 | sm-shop JaCoCo line coverage ≥ 20% as reported by `mvn verify` | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-4 | Spring Boot application starts: `ShopApplication.main()` runs; actuator `/health` returns HTTP 200 with status UP | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-5 | JWT authentication works: POST to customer authenticate endpoint returns a valid JWT token |
| AC-6 | JWT validation works: authenticated request with token returns HTTP 200; invalid token returns HTTP 401 |
| AC-7 | Swagger UI accessible: GET `/swagger-ui.html` or `/swagger-ui/index.html` returns HTTP 200 | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-8 | OpenAPI spec accessible: GET `/v3/api-docs` returns HTTP 200 with valid JSON | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-9 | OWASP Dependency-Check reports zero CRITICAL (CVSS ≥9.0) vulnerabilities |
| AC-10 | Docker image builds successfully with `eclipse-temurin:21-jre-alpine` base | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-11 | Dockerfile HEALTHCHECK instruction present in final image |
| AC-12 | No `javax.*` imports remain in any source file across all five modules (except intentional exclusions documented) | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-13 | `mvn dependency:tree` shows no `springfox-swagger2` or `springfox-swagger-ui` artifacts | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-14 | `mvn dependency:tree` shows no `adoptopenjdk` references | _(Unverified: no Code Insights evidence ID supplied.)_
| AC-15 | Stale `elasticsearch` version property absent from root `pom.xml` | _(Unverified: no Code Insights evidence ID supplied.)_

---

## Risks

| ID | Risk | Likelihood | Impact | Mitigation |
|----|------|-----------|--------|-----------|
| R-1 | Drools 7.32.0.Final incompatible with Jakarta EE; upgrade path to 8.x/Kogito unknown | High | High | Spike task before Phase 2; evaluate Drools 8.x or compatibility shim |
| R-2 | Infinispan 9.4.18.Final incompatible with Jakarta EE; Spring Boot 3.3.x integration unverified | High | High | Spike task before Phase 2; evaluate Infinispan 14.x/15.x |
| R-3 | sm-shop test coverage 4% line / 1% branch — high regression risk | High | High | Raise coverage to 20% (QG-1) before Phase 3 |
| R-4 | Hibernate 6.x breaking changes in HQL/Criteria API affect `processOrder` (fan-out 22) and other complex queries [Q-12] | Medium | High | Full persistence layer regression testing in Phase 3 | _(Unverified: no Code Insights evidence ID supplied.)_
| R-5 | jjwt 0.8.0→0.12.6 breaking API changes in `JWTTokenUtil` [Q-3] | High | High | Refactor all JWT methods; end-to-end auth test (AC-5, AC-6) | _(Unverified: no Code Insights evidence ID supplied.)_
| R-6 | MapStruct 1.3→1.6 annotation processor configuration changes | Medium | Medium | Update `maven-compiler-plugin` config; verify generated mappers compile | _(Unverified: no Code Insights evidence ID supplied.)_
| R-7 | Springfox `@Api*` annotations in all REST controllers — volume of changes | Medium | Medium | Use OpenRewrite or sed-based migration; review each controller | _(Unverified: no Code Insights evidence ID supplied.)_
| R-8 | jackson-core 2.10.2 / jackson-databind 2.12.6.1 version mismatch may cause runtime issues before BOM takes over | Medium | Medium | Remove explicit version pins early; let Spring Boot BOM manage |

---

## Open Questions

| ID | Question |
|----|---------|
| OQ-1 | What is the exact compatible target version of Drools/KIE for Spring Boot 3.3.5 / Jakarta EE? (upstream analysis: minimum 8.0.0; target unverified) |
| OQ-2 | What is the exact compatible target version of Infinispan for Spring Boot 3.3.5 / Jakarta EE? (upstream analysis: minimum 14.0.0.Final; target unverified) |
| OQ-3 | Does the Dockerfile exist at the repository root or in a subdirectory? (IAC index tool returned 502; could not verify path) |
| OQ-4 | Are there additional `pom.xml` files in sm-core-modules or sm-shop-model not captured by the dependency report? | _(Unverified: no Code Insights evidence ID supplied.)_
| OQ-5 | What CI configuration files exist (Jenkinsfile path, CircleCI config path)? (search_code returned 404; could not verify) |