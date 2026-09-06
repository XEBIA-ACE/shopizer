## Authoritative Upgrade Scope
1. Upgrade Java runtime 11 → 21 LTS (prerequisite for Spring Boot 3.3.x)
2. Update Maven Compiler Plugin source/target/release to 21
3. Replace Docker base image adoptopenjdk/openjdk11-openj9:alpine → eclipse-temurin:21-jre-alpine
4. Upgrade spring-boot-starter-parent 2.5.12 → 3.3.5
5. Perform javax → jakarta EE namespace migration across all source modules (sm-core, sm-shop, sm-shop-model, sm-core-model, sm-core-modules)
6. Remove springfox-swagger2 2.9.2 and springfox-swagger-ui; add springdoc-openapi-starter-webmvc-ui 2.6.0
7. Migrate Swagger/SpringFox annotations to springdoc/OpenAPI 3 annotations in all REST controllers
8. Upgrade Spring Security (transitive via Spring Boot 3.3.5) 5.5.x → 6.3.x; migrate deprecated WebSecurityConfigurerAdapter to SecurityFilterChain
9. Upgrade Spring Data JPA (transitive via Spring Boot 3.3.5) 2.5.x → 3.3.x
10. Upgrade Hibernate (transitive via Spring Boot 3.3.5) 5.4.x → 6.5.x; review HQL/Criteria API changes
11. Upgrade commons-fileupload 1.3.3 → 1.5 (CVE-2023-24998)
12. Upgrade commons-io 2.7 → 2.16.1 (CVE-2021-29425)
13. Upgrade guava 27.1-jre → 33.2.1-jre (CVE-2023-2976)
14. Upgrade jjwt 0.8.0 → 0.12.6 (algorithm confusion risk; API migration required)
15. Upgrade org.apache.httpcomponents:httpclient 4.5.2 → 4.5.14 (CVE-2020-13956)
16. Upgrade postgresql driver 42.2.18 → 42.7.3 (CVE-2022-21724, CVE-2022-26520)
17. Upgrade mysql-connector-java 8.0.21 → 8.4.0 (compatible with Java 21 and Spring Boot 3.3.x)
18. Upgrade jackson-core 2.10.2 → 2.17.2 (managed by Spring Boot 3.3.x BOM)
19. Upgrade jackson-databind 2.12.6.1 → 2.17.2 (managed by Spring Boot 3.3.x BOM)
20. Upgrade MapStruct 1.3.0.Final → 1.6.2
21. Upgrade commons-lang3 3.5 → 3.14.0
22. Upgrade commons-collections4 4.1 → 4.4
23. Upgrade commons-validator 1.5.1 → 1.9.0
24. Upgrade org.owasp.antisamy:antisamy 1.6.7 → 1.7.5
25. Remove stale elasticsearch 7.5.2 property declaration from pom.xml
26. Add OWASP Dependency-Check Maven plugin to CI pipeline
27. Add Dockerfile HEALTHCHECK instruction
28. Raise JaCoCo line coverage threshold in sm-shop module to 20%

- Blockers: javax → jakarta namespace migration touches all five Maven modules and all third-party libraries must also support jakarta namespace; verify Drools 7.32.0.Final compatibility with Jakarta EE — Drools 7.x uses javax namespace and may require upgrade to 8.x/9.x or a compatibility shim, Drools 7.32.0.Final is not compatible with Spring Boot 3.x / Jakarta EE; must evaluate upgrade path to Drools 8.x or Kogito before or alongside this migration, Infinispan 9.4.18.Final does not support Jakarta EE; must be upgraded to Infinispan 14.x or 15.x which supports Jakarta EE — verify Spring Boot 3.3.x integration, WebSecurityConfigurerAdapter removed in Spring Security 6.x; all security configuration classes must be rewritten, jjwt 0.8.0 → 0.12.x has breaking API changes; all JWT creation and parsing code must be refactored, Hibernate 6.x introduces breaking changes in HQL, Criteria API, and type mappings; full regression testing of persistence layer required, Very low test coverage (sm-shop: 4% line, 1% branch) significantly increases regression risk; recommend writing integration tests for critical paths before migration, MapStruct 1.3.x → 1.6.x may require annotation processor configuration updates in Maven
- Impacted areas: source code, CI/CD, infrastructure, tests, docs

---

# Migration Plan — Shopizer Spring Boot 3.3 + Java 21 LTS Upgrade

## Preconditions

1. Repository `https://github.com/XEBIA-ACE/shopizer` ref `3.2.5` (commit `052d2ed3c026525329405cef433c0aca7dd2cee3`) is checked out and builds cleanly on Java 11 [Q-1]. _(Unverified: no Code Insights evidence ID supplied.)_
2. A feature branch `upgrade/spring-boot-3` is created from `3.2.5`. _(Unverified: no Code Insights evidence ID supplied.)_
3. Java 21 LTS (Eclipse Temurin) is installed on all developer machines and CI agents.
4. Maven 3.9.x is available (required for Java 21 compatibility).
5. OpenRewrite Maven plugin is available for automated namespace migration.
6. All five Maven modules (sm-core, sm-shop, sm-shop-model, sm-core-model, sm-core-modules) are confirmed to build and test-pass on Java 11 before migration begins [Q-7].

## Strategy

The migration follows a strict dependency-ordered sequence of six phases. Each phase must reach a green build before the next begins. Phases 1 and 2 are prerequisites that must complete before the Spring Boot parent version is changed.

```
Phase 1: Java 21 + Toolchain + Docker base image
Phase 2: Blocker dependencies (Drools, Infinispan, JJWT, CVE deps)
Phase 3: Spring Boot parent 3.3.5 + javax→jakarta namespace migration
Phase 4: Spring Security 6.x rewrite
Phase 5: Springfox → springdoc-openapi migration
Phase 6: Remaining dependency upgrades + CI/quality gates
```

---

## Phase 1 — Java 21 Toolchain and Infrastructure

**Goal**: All five modules compile and test-pass on Java 21. Docker base image updated.

### Affected Files
- Root `pom.xml`: `maven.compiler.source`, `maven.compiler.target`, `maven.compiler.release` → `21`; `java.version` property → `21` _(Unverified: no Code Insights evidence ID supplied.)_
- All module `pom.xml` files: inherit compiler settings from root _(Unverified: no Code Insights evidence ID supplied.)_
- `maven-compiler-plugin` configuration: `source`/`target`/`release` → `21` _(Unverified: no Code Insights evidence ID supplied.)_
- Dockerfile: `FROM adoptopenjdk/openjdk11-openj9:alpine` → `FROM eclipse-temurin:21-jre-alpine` _(Unverified: no Code Insights evidence ID supplied.)_
- MapStruct version in root `pom.xml`: `1.3.0.Final` → `1.6.2` (required for Java 21 annotation processor compatibility) _(Unverified: no Code Insights evidence ID supplied.)_
- `maven-compiler-plugin` annotation processor configuration: update MapStruct processor path to `1.6.2` _(Unverified: no Code Insights evidence ID supplied.)_

### Actions
1. Update `java.version` (or equivalent property) in root `pom.xml` to `21`. _(Unverified: no Code Insights evidence ID supplied.)_
2. Set `maven.compiler.release=21` in root `pom.xml` (preferred over source/target for Java 9+). _(Unverified: no Code Insights evidence ID supplied.)_
3. Update MapStruct version to `1.6.2` and update annotation processor path in `maven-compiler-plugin`. _(Unverified: no Code Insights evidence ID supplied.)_
4. Replace Docker base image in Dockerfile.
5. Add `HEALTHCHECK` instruction to Dockerfile (e.g., `HEALTHCHECK --interval=30s --timeout=3s CMD curl -f http://localhost:8080/actuator/health || exit 1`). _(Unverified: no Code Insights evidence ID supplied.)_
6. Run `mvn compile` across all modules; fix any Java 21 compilation warnings/errors. _(Unverified: no Code Insights evidence ID supplied.)_
7. Run `mvn test` across all modules; fix any test failures. _(Unverified: no Code Insights evidence ID supplied.)_

### Acceptance Criteria
- AC-1: All five modules compile with Java 21.
- AC-2: All existing tests pass.
- AC-10: Docker image builds with `eclipse-temurin:21-jre-alpine`. _(Unverified: no Code Insights evidence ID supplied.)_
- AC-11: Dockerfile HEALTHCHECK present.

---

## Phase 2 — Blocker Dependency Upgrades

**Goal**: Resolve all hard blockers before the Spring Boot parent is changed. This phase is the most complex and may require spikes.

### Sub-phase 2a: Drools Upgrade (Blocker R-1)

**Affected files**: `sm-core/pom.xml`, `sm-core/src/main/java/com/salesmanager/core/business/configuration/DroolsBeanFactory.java` [Q-5] _(Unverified: no Code Insights evidence ID supplied.)_

**Actions**:
1. Spike: Evaluate Drools 8.x / Kogito compatibility with the existing `DroolsBeanFactory` (lines 24–112). Determine exact target version (OQ-1). _(Unverified: no Code Insights evidence ID supplied.)_
2. Update Drools/KIE version in `sm-core/pom.xml` to the determined target (minimum 8.0.0). _(Unverified: no Code Insights evidence ID supplied.)_
3. Migrate `DroolsBeanFactory` to Drools 8.x KIE API (KieServices, KieContainer, KieSession API changes). _(Unverified: no Code Insights evidence ID supplied.)_
4. Run `mvn test -pl sm-core` to verify Drools rules still execute correctly. _(Unverified: no Code Insights evidence ID supplied.)_

### Sub-phase 2b: Infinispan Upgrade (Blocker R-2)

**Affected files**: `sm-core/pom.xml`, `CmsImageFileManagerImpl` (infinispan), `CmsStaticContentFileManagerImpl` (infinispan) [Q-6] _(Unverified: no Code Insights evidence ID supplied.)_

**Actions**:
1. Spike: Evaluate Infinispan 14.x or 15.x compatibility with Spring Boot 3.3.x. Determine exact target version (OQ-2).
2. Update Infinispan version in `sm-core/pom.xml` to the determined target (minimum 14.0.0.Final). _(Unverified: no Code Insights evidence ID supplied.)_
3. Migrate `CmsImageFileManagerImpl` (infinispan path) to Infinispan 14.x API. _(Unverified: no Code Insights evidence ID supplied.)_
4. Migrate `CmsStaticContentFileManagerImpl` (infinispan path) to Infinispan 14.x API. _(Unverified: no Code Insights evidence ID supplied.)_
5. Run `mvn test -pl sm-core` to verify cache operations. _(Unverified: no Code Insights evidence ID supplied.)_

### Sub-phase 2c: JJWT Upgrade (Blocker R-5)

**Affected files**: `sm-shop/pom.xml`, `sm-shop/src/main/java/com/salesmanager/shop/store/security/JWTTokenUtil.java` [Q-3] _(Unverified: no Code Insights evidence ID supplied.)_

**Actions**:
1. Update jjwt version in `sm-shop/pom.xml` from `0.8.0` to `0.12.6`. Replace `io.jsonwebtoken:jjwt` with `io.jsonwebtoken:jjwt-api`, `io.jsonwebtoken:jjwt-impl`, `io.jsonwebtoken:jjwt-jackson`. _(Unverified: no Code Insights evidence ID supplied.)_
2. Refactor `JWTTokenUtil` (lines 26–193): migrate all `Jwts.builder()`, `Jwts.parser()`, `Claims` usages to jjwt 0.12.6 fluent API. Key handling must use `Keys.hmacShaKeyFor()` or `Keys.secretKeyFor()`. _(Unverified: no Code Insights evidence ID supplied.)_
3. Verify `generateToken`, `validateToken`, `refreshToken`, `getAllClaimsFromToken`, `doGenerateToken`, `getClaimFromToken` methods. _(Unverified: no Code Insights evidence ID supplied.)_
4. Run `mvn test -pl sm-shop` to verify JWT operations. _(Unverified: no Code Insights evidence ID supplied.)_

### Sub-phase 2d: CVE Dependency Upgrades

**Affected files**: Root `pom.xml` and/or module `pom.xml` files _(Unverified: no Code Insights evidence ID supplied.)_

**Actions** (update version properties or `<dependency>` entries): _(Unverified: no Code Insights evidence ID supplied.)_
1. `commons-fileupload`: `1.3.3` → `1.5` (CVE-2023-24998) _(Unverified: no Code Insights evidence ID supplied.)_
2. `commons-io`: `2.7` → `2.16.1` (CVE-2021-29425) _(Unverified: no Code Insights evidence ID supplied.)_
3. `guava`: `27.1-jre` → `33.2.1-jre` (CVE-2023-2976) _(Unverified: no Code Insights evidence ID supplied.)_
4. `httpclient` (org.apache.httpcomponents): `4.5.2` → `4.5.14` (CVE-2020-13956) _(Unverified: no Code Insights evidence ID supplied.)_
5. `postgresql`: `42.2.18` → `42.7.3` (CVE-2022-21724, CVE-2022-26520) _(Unverified: no Code Insights evidence ID supplied.)_
6. `mysql-connector-java`: `8.0.21` → `8.4.0` _(Unverified: no Code Insights evidence ID supplied.)_
7. Remove stale `elasticsearch` version property (`7.5.2`) from root `pom.xml` _(Unverified: no Code Insights evidence ID supplied.)_
8. Run `mvn verify` across all modules. _(Unverified: no Code Insights evidence ID supplied.)_

### Sub-phase 2e: Test Coverage Raise (Quality Gate QG-1)

**Affected files**: `sm-shop/pom.xml` (JaCoCo configuration), new test files in `sm-shop/src/test/` _(Unverified: no Code Insights evidence ID supplied.)_

**Actions**:
1. Write integration tests for critical paths: `AuthenticateCustomerApi.authenticate` [Q-16], `AuthenticateUserApi.authenticate` [Q-17], `OrderFacadeImpl.processOrder` [Q-12]. _(Unverified: no Code Insights evidence ID supplied.)_
2. Raise JaCoCo `<line>` threshold in `sm-shop/pom.xml` from `0.04` to `0.20`. _(Unverified: no Code Insights evidence ID supplied.)_
3. Raise JaCoCo `<branch>` threshold from `0.01` to `0.05`. _(Unverified: no Code Insights evidence ID supplied.)_
4. Run `mvn verify -pl sm-shop` and confirm coverage gates pass. _(Unverified: no Code Insights evidence ID supplied.)_

---

## Phase 3 — Spring Boot 3.3.5 + javax→jakarta Namespace Migration

**Goal**: Bump the Spring Boot parent and complete the namespace migration across all five modules.

### Affected Files
- Root `pom.xml`: `spring-boot-starter-parent` version `2.5.12` → `3.3.5` _(Unverified: no Code Insights evidence ID supplied.)_
- All Java source files in all five modules: `javax.*` → `jakarta.*` imports _(Unverified: no Code Insights evidence ID supplied.)_
- `application.properties` / `application.yml` in `sm-shop/src/main/resources/`: review Spring Boot 3.x property key changes _(Unverified: no Code Insights evidence ID supplied.)_
- Spring Data JPA repository interfaces: review for 3.x API changes
- Hibernate entity annotations: review for 6.x type mapping changes

### Actions
1. Run OpenRewrite recipe `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_3` on all five modules. _(Unverified: no Code Insights evidence ID supplied.)_
2. Manually review and fix any OpenRewrite misses, particularly in `DroolsBeanFactory` [Q-5] and Infinispan CMS impls [Q-6]. _(Unverified: no Code Insights evidence ID supplied.)_
3. Remove explicit `jackson-core` and `jackson-databind` version pins from `pom.xml` files; let Spring Boot 3.3.5 BOM manage to `2.17.2`. _(Unverified: no Code Insights evidence ID supplied.)_
4. Review all JPA entities in `sm-core-model` for Hibernate 6.x type mapping changes (e.g., `@Type` annotation changes, `@Enumerated` handling). _(Unverified: no Code Insights evidence ID supplied.)_
5. Review all Spring Data JPA repository interfaces for 3.x API changes.
6. Review `application.properties`/`application.yml` for renamed Spring Boot 3.x properties. _(Unverified: no Code Insights evidence ID supplied.)_
7. Run `mvn compile` across all modules; fix compilation errors. _(Unverified: no Code Insights evidence ID supplied.)_
8. Run `mvn test` across all modules; fix test failures. _(Unverified: no Code Insights evidence ID supplied.)_
9. Verify AC-4: application starts and `/health` returns UP. _(Unverified: no Code Insights evidence ID supplied.)_

### Acceptance Criteria
- AC-1, AC-2, AC-4, AC-12, AC-15.

---

## Phase 4 — Spring Security 6.x Migration

**Goal**: Replace `WebSecurityConfigurerAdapter` with `SecurityFilterChain` beans. _(Unverified: no Code Insights evidence ID supplied.)_

### Affected Files
- `sm-shop/src/main/java/com/salesmanager/shop/application/config/MultipleEntryPointsSecurityConfig.java` [Q-4] _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/java/com/salesmanager/shop/store/security/AuthenticationTokenFilter.java` _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/java/com/salesmanager/shop/store/security/admin/JWTAdminAuthenticationProvider.java` _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/java/com/salesmanager/shop/store/security/customer/JWTCustomerAuthenticationProvider.java` _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/java/com/salesmanager/shop/store/security/ServicesAuthenticationSuccessHandler.java` _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/java/com/salesmanager/shop/admin/security/UserAuthenticationSuccessHandler.java` _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/java/com/salesmanager/shop/store/security/services/CredentialsServiceImpl.java` _(Unverified: no Code Insights evidence ID supplied.)_

### Actions
1. Rewrite `MultipleEntryPointsSecurityConfig`: replace all inner classes extending `WebSecurityConfigurerAdapter` with `@Bean SecurityFilterChain` methods. The three `authenticationManagerBean` methods (lines 87, 295, 380) [Q-4] must be replaced with `AuthenticationManager` beans. _(Unverified: no Code Insights evidence ID supplied.)_
2. Update `AuthenticationTokenFilter` to use Spring Security 6.x `OncePerRequestFilter` pattern (jakarta namespace). _(Unverified: no Code Insights evidence ID supplied.)_
3. Update `JWTAdminAuthenticationProvider` and `JWTCustomerAuthenticationProvider` for Spring Security 6.x `AuthenticationProvider` interface. _(Unverified: no Code Insights evidence ID supplied.)_
4. Update `ServicesAuthenticationSuccessHandler` and `UserAuthenticationSuccessHandler` for Spring Security 6.x. _(Unverified: no Code Insights evidence ID supplied.)_
5. Update `CredentialsServiceImpl` for Spring Security 6.x `UserDetailsService`. _(Unverified: no Code Insights evidence ID supplied.)_
6. Run `mvn test -pl sm-shop`; verify AC-5 and AC-6 (JWT auth end-to-end). _(Unverified: no Code Insights evidence ID supplied.)_

### Acceptance Criteria
- AC-2, AC-5, AC-6.

---

## Phase 5 — Springfox → springdoc-openapi Migration

**Goal**: Remove Springfox; add springdoc-openapi 2.6.0; migrate all API documentation annotations.

### Affected Files
- `sm-shop/pom.xml`: remove `springfox-swagger2`, `springfox-swagger-ui`; add `springdoc-openapi-starter-webmvc-ui:2.6.0` _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/java/com/salesmanager/shop/application/config/DocumentationConfiguration.java` [Q-10]: replace `Docket` bean with `OpenAPI` bean _(Unverified: no Code Insights evidence ID supplied.)_
- All REST controllers in `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/`: replace `@Api`, `@ApiOperation`, `@ApiParam`, `@ApiResponse` with `@Tag`, `@Operation`, `@Parameter`, `@ApiResponse` (OpenAPI 3) _(Unverified: no Code Insights evidence ID supplied.)_
- `sm-shop/src/main/resources/application.properties` or `application.yml`: add `springdoc.api-docs.path=/v3/api-docs` and `springdoc.swagger-ui.path=/swagger-ui.html` if needed _(Unverified: no Code Insights evidence ID supplied.)_

### Actions
1. Remove Springfox dependencies from `sm-shop/pom.xml`. _(Unverified: no Code Insights evidence ID supplied.)_
2. Add `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0` to `sm-shop/pom.xml`. _(Unverified: no Code Insights evidence ID supplied.)_
3. Rewrite `DocumentationConfiguration` to use springdoc-openapi `OpenAPI` bean. _(Unverified: no Code Insights evidence ID supplied.)_
4. Migrate all `@Api*` annotations in REST controllers to OpenAPI 3 equivalents. _(Unverified: no Code Insights evidence ID supplied.)_
5. Run `mvn test -pl sm-shop`; verify AC-7 and AC-8. _(Unverified: no Code Insights evidence ID supplied.)_

### Acceptance Criteria
- AC-2, AC-7, AC-8, AC-13.

---

## Phase 6 — Remaining Upgrades and Quality Gates

**Goal**: Complete all remaining dependency upgrades; add OWASP Dependency-Check to CI.

### Affected Files
- Root `pom.xml` or module `pom.xml` files: `commons-lang3` `3.5`→`3.14.0`, `commons-collections4` `4.1`→`4.4`, `commons-validator` `1.5.1`→`1.9.0`, `antisamy` `1.6.7`→`1.7.5` _(Unverified: no Code Insights evidence ID supplied.)_
- CI configuration (Jenkinsfile / CircleCI config — paths to be confirmed per OQ-5): add OWASP Dependency-Check Maven plugin execution
- Root `pom.xml`: add `org.owasp:dependency-check-maven` plugin configuration _(Unverified: no Code Insights evidence ID supplied.)_

### Actions
1. Update remaining dependency versions in `pom.xml` files. _(Unverified: no Code Insights evidence ID supplied.)_
2. Add OWASP Dependency-Check Maven plugin to root `pom.xml` `<build><plugins>` section with `<failBuildOnCVSS>9</failBuildOnCVSS>`. _(Unverified: no Code Insights evidence ID supplied.)_
3. Add OWASP Dependency-Check execution to CI pipeline.
4. Run `mvn verify` across all modules. _(Unverified: no Code Insights evidence ID supplied.)_
5. Run OWASP Dependency-Check; verify AC-9.
6. Final end-to-end smoke test: start application, exercise all critical paths.

### Acceptance Criteria
- AC-2, AC-9, AC-14, AC-15.

---

## Testing Strategy

| Layer | Approach |
|-------|---------|
| Unit tests | Existing tests in `sm-core/src/test/` and `sm-shop/src/test/` [Q-18]; fix regressions per phase | _(Unverified: no Code Insights evidence ID supplied.)_
| Integration tests | New tests for `AuthenticateCustomerApi`, `AuthenticateUserApi`, `OrderFacadeImpl.processOrder` [Q-12, Q-16, Q-17] | _(Unverified: no Code Insights evidence ID supplied.)_
| Security tests | JWT token issuance and validation (AC-5, AC-6); Spring Security filter chain |
| API smoke tests | Swagger UI (AC-7, AC-8); key REST endpoints |
| Coverage gate | JaCoCo ≥20% line, ≥5% branch in sm-shop (QG-1, QG-2) |
| Dependency scan | OWASP Dependency-Check (QG-3, AC-9) |

---

## Deployment

1. Build Docker image with `eclipse-temurin:21-jre-alpine` base. _(Unverified: no Code Insights evidence ID supplied.)_
2. Verify HEALTHCHECK instruction is present and functional.
3. Deploy to staging environment; run smoke tests.
4. Deploy to production with rollback plan active.

## Rollback Plan

- Each phase is committed on a separate branch/PR. Rollback = revert the PR.
- Database schema is not changed; rollback does not require schema migration.
- Docker image tags are versioned; rollback = redeploy previous image tag.

## Monitoring

- Actuator `/health` endpoint (enabled by Spring Boot 3.x default). _(Unverified: no Code Insights evidence ID supplied.)_
- JVM metrics via Actuator `/actuator/metrics` (Java 21 JVM metrics). _(Unverified: no Code Insights evidence ID supplied.)_
- Application logs: watch for `ClassNotFoundException`, `NoSuchMethodError`, `jakarta.persistence.*` errors post-deployment. _(Unverified: no Code Insights evidence ID supplied.)_
- OWASP Dependency-Check report in CI pipeline on every build.