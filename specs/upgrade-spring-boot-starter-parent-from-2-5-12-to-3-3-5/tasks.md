## Mandatory Upgrade Coverage
- [ ] **UPG-001: Upgrade Java runtime 11 → 21 LTS (prerequisite for Spring Boot 3.3.x)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-002: Update Maven Compiler Plugin source/target/release to 21**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-003: Replace Docker base image adoptopenjdk/openjdk11-openj9:alpine → eclipse-temurin:21-jre-alpine**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-004: Upgrade spring-boot-starter-parent 2.5.12 → 3.3.5**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-005: Perform javax → jakarta EE namespace migration across all source modules (sm-core, sm-shop, sm-shop-model, sm-core-model, sm-core-modules)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-006: Remove springfox-swagger2 2.9.2 and springfox-swagger-ui; add springdoc-openapi-starter-webmvc-ui 2.6.0**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-007: Migrate Swagger/SpringFox annotations to springdoc/OpenAPI 3 annotations in all REST controllers**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-008: Upgrade Spring Security (transitive via Spring Boot 3.3.5) 5.5.x → 6.3.x; migrate deprecated WebSecurityConfigurerAdapter to SecurityFilterChain**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-009: Upgrade Spring Data JPA (transitive via Spring Boot 3.3.5) 2.5.x → 3.3.x**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-010: Upgrade Hibernate (transitive via Spring Boot 3.3.5) 5.4.x → 6.5.x; review HQL/Criteria API changes**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-011: Upgrade commons-fileupload 1.3.3 → 1.5 (CVE-2023-24998)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-012: Upgrade commons-io 2.7 → 2.16.1 (CVE-2021-29425)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-013: Upgrade guava 27.1-jre → 33.2.1-jre (CVE-2023-2976)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-014: Upgrade jjwt 0.8.0 → 0.12.6 (algorithm confusion risk; API migration required)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-015: Upgrade org.apache.httpcomponents:httpclient 4.5.2 → 4.5.14 (CVE-2020-13956)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-016: Upgrade postgresql driver 42.2.18 → 42.7.3 (CVE-2022-21724, CVE-2022-26520)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-017: Upgrade mysql-connector-java 8.0.21 → 8.4.0 (compatible with Java 21 and Spring Boot 3.3.x)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-018: Upgrade jackson-core 2.10.2 → 2.17.2 (managed by Spring Boot 3.3.x BOM)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-019: Upgrade jackson-databind 2.12.6.1 → 2.17.2 (managed by Spring Boot 3.3.x BOM)**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-020: Upgrade MapStruct 1.3.0.Final → 1.6.2**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-021: Upgrade commons-lang3 3.5 → 3.14.0**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-022: Upgrade commons-collections4 4.1 → 4.4**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-023: Upgrade commons-validator 1.5.1 → 1.9.0**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-024: Upgrade org.owasp.antisamy:antisamy 1.6.7 → 1.7.5**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-025: Remove stale elasticsearch 7.5.2 property declaration from pom.xml**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-026: Add OWASP Dependency-Check Maven plugin to CI pipeline**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-027: Add Dockerfile HEALTHCHECK instruction**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **UPG-028: Raise JaCoCo line coverage threshold in sm-shop module to 20%**
  - Source: Selected Upgrade Option
  - Acceptance: Implemented change is verified by relevant build and test checks.
  - Estimate: Allocate within the selected option's total effort after repository impact review.
- [ ] **VER-029: Pin an exact target for Upgrade Java runtime**
  - Current selected target: `21 LTS (prerequisite for Spring Boot 3.3.x)`
  - Acceptance: An exact compatible version is selected and its compatibility evidence is recorded before manifest changes.
  - Estimate: Include within the selected option's total effort.
- [ ] **VER-030: Pin an exact target for Upgrade Spring Security (transitive via Spring Boot 3.3.5)**
  - Current selected target: `6.3.x; migrate deprecated WebSecurityConfigurerAdapter to SecurityFilterChain`
  - Acceptance: An exact compatible version is selected and its compatibility evidence is recorded before manifest changes.
  - Estimate: Include within the selected option's total effort.
- [ ] **VER-031: Pin an exact target for Upgrade Spring Data JPA (transitive via Spring Boot 3.3.5)**
  - Current selected target: `3.3.x`
  - Acceptance: An exact compatible version is selected and its compatibility evidence is recorded before manifest changes.
  - Estimate: Include within the selected option's total effort.
- [ ] **VER-032: Pin an exact target for Upgrade Hibernate (transitive via Spring Boot 3.3.5)**
  - Current selected target: `6.5.x; review HQL/Criteria API changes`
  - Acceptance: An exact compatible version is selected and its compatibility evidence is recorded before manifest changes.
  - Estimate: Include within the selected option's total effort.
- [ ] **VER-033: Pin an exact target for Upgrade mysql-connector-java**
  - Current selected target: `8.4.0 (compatible with Java 21 and Spring Boot 3.3.x)`
  - Acceptance: An exact compatible version is selected and its compatibility evidence is recorded before manifest changes.
  - Estimate: Include within the selected option's total effort.
- [ ] **VER-034: Pin an exact target for Upgrade jackson-core**
  - Current selected target: `2.17.2 (managed by Spring Boot 3.3.x BOM)`
  - Acceptance: An exact compatible version is selected and its compatibility evidence is recorded before manifest changes.
  - Estimate: Include within the selected option's total effort.
- [ ] **VER-035: Pin an exact target for Upgrade jackson-databind**
  - Current selected target: `2.17.2 (managed by Spring Boot 3.3.x BOM)`
  - Acceptance: An exact compatible version is selected and its compatibility evidence is recorded before manifest changes.
  - Estimate: Include within the selected option's total effort.

---

# Tasks — Shopizer Spring Boot 3.3 + Java 21 LTS Upgrade

## Phase 1 — Java 21 Toolchain and Infrastructure

---

### TASK-1.1 — Update Maven Compiler Plugin to Java 21
**Objective**: Set Java 21 as the compile target across all five Maven modules.  
**Components/Files**: Root `pom.xml`   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: None  
**Implementation**:
- Set `maven.compiler.source=21`, `maven.compiler.target=21`, `maven.compiler.release=21` (or equivalent property names used in the root POM). _(Unverified: no Code Insights evidence ID supplied.)_
- Set `java.version=21` property if present. _(Unverified: no Code Insights evidence ID supplied.)_
- Verify all five module `pom.xml` files inherit from root without overriding compiler settings. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: `mvn compile` succeeds across all five modules with Java 21 JDK.   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: `mvn -version` shows Java 21; `mvn compile -pl sm-core,sm-shop,sm-shop-model,sm-core-model,sm-core-modules` exits 0.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert root `pom.xml` compiler properties to `11`.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: Low — compiler version change only.  
**Estimate**: 0.5 person-days  
**Evidence**: Q-7 (five modules confirmed)

---

### TASK-1.2 — Upgrade MapStruct to 1.6.2
**Objective**: Update MapStruct to a version compatible with Java 21 annotation processing.  
**Components/Files**: Root `pom.xml`; `maven-compiler-plugin` annotation processor configuration   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-1.1  
**Implementation**:
- Update MapStruct version property from `1.3.0.Final` to `1.6.2`. _(Unverified: no Code Insights evidence ID supplied.)_
- Update `maven-compiler-plugin` `<annotationProcessorPaths>` entry for `mapstruct-processor` to `1.6.2`. _(Unverified: no Code Insights evidence ID supplied.)_
- Verify `sm-shop/src/main/java/com/salesmanager/shop/mapper/Mapper.java` [Q-14] and all generated mapper classes compile correctly. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: All MapStruct-generated mapper classes compile without errors under Java 21.  
**Validation**: `mvn compile -pl sm-shop` exits 0; no MapStruct annotation processor errors in build output.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert MapStruct version to `1.3.0.Final`.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: Medium — annotation processor configuration changes may break generated code.  
**Estimate**: 1 person-day  
**Evidence**: Q-14 (Mapper interface confirmed); upstream compatibility evidence (MapStruct GitHub Releases)

---

### TASK-1.3 — Replace Docker Base Image and Add HEALTHCHECK
**Objective**: Replace retired AdoptOpenJDK base image with Eclipse Temurin 21; add HEALTHCHECK.  
**Components/Files**: Dockerfile (path to be confirmed per OQ-3)  
**Dependencies**: None  
**Implementation**:
- Replace `FROM adoptopenjdk/openjdk11-openj9:alpine` with `FROM eclipse-temurin:21-jre-alpine`. _(Unverified: no Code Insights evidence ID supplied.)_
- Add `HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1`. _(Unverified: no Code Insights evidence ID supplied.)_
- Verify the application JAR is compatible with the new base image (no OpenJ9-specific JVM flags).

**Acceptance Criteria**: AC-10, AC-11.  
**Validation**: `docker build` succeeds; `docker inspect <image> | grep -i healthcheck` shows the instruction.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert Dockerfile to previous base image.  
**Risk**: Low — base image change only; application code unchanged.  
**Estimate**: 0.5 person-days  
**Evidence**: Upstream compatibility evidence (Eclipse Temurin Docker Hub)

---

## Phase 2 — Blocker Dependency Upgrades

---

### TASK-2.1 — Spike: Drools Upgrade Path Evaluation
**Objective**: Determine the exact compatible Drools/KIE version for Spring Boot 3.3.5 / Jakarta EE.  
**Components/Files**: `sm-core/src/main/java/com/salesmanager/core/business/configuration/DroolsBeanFactory.java` [Q-5]   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-1.1  
**Implementation**:
- Review Drools 8.x migration guide for KIE API changes affecting `DroolsBeanFactory` (lines 24–112, methods: `getKieFileSystem`, `getKieContainer`, `getKieRepository`, `getKieSession`, `getDrlFromExcel`). _(Unverified: no Code Insights evidence ID supplied.)_
- Evaluate Kogito as an alternative if Drools 8.x is not viable.
- Document the selected target version and required code changes.
- Update OQ-1 with the finding.

**Acceptance Criteria**: A documented decision on Drools target version with a list of required code changes.  
**Validation**: Spike report reviewed and approved before TASK-2.2 begins.  
**Rollback**: N/A (spike only).  
**Risk**: High — Drools 7→8 is a major version with breaking API changes.  
**Estimate**: 3 person-days  
**Evidence**: Q-5 (DroolsBeanFactory confirmed); R-1 (blocker risk)

---

### TASK-2.2 — Upgrade Drools to Target Version
**Objective**: Upgrade Drools/KIE from 7.32.0.Final to the version determined in TASK-2.1.  
**Components/Files**: `sm-core/pom.xml`; `sm-core/src/main/java/com/salesmanager/core/business/configuration/DroolsBeanFactory.java` [Q-5]   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-2.1  
**Implementation**:
- Update Drools/KIE version in `sm-core/pom.xml`. _(Unverified: no Code Insights evidence ID supplied.)_
- Migrate `DroolsBeanFactory` to the new KIE API as documented in TASK-2.1 spike. _(Unverified: no Code Insights evidence ID supplied.)_
- Verify all Drools rule files (`.drl`, `.xls` decision tables referenced in `DroolsBeanFactory`) still load correctly. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: `mvn test -pl sm-core` passes; Drools rules execute correctly.   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: Existing Drools-related tests pass; `DroolsBeanFactory` bean initialises without errors.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert `sm-core/pom.xml` and `DroolsBeanFactory.java` to Drools 7.32.0.Final.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: High — KIE API breaking changes.  
**Estimate**: 5 person-days  
**Evidence**: Q-5 (DroolsBeanFactory blast radius: 28 symbols, 1 file) _(Unverified: no Code Insights evidence ID supplied.)_

---

### TASK-2.3 — Spike: Infinispan Upgrade Path Evaluation
**Objective**: Determine the exact compatible Infinispan version for Spring Boot 3.3.5 / Jakarta EE.  
**Components/Files**: `sm-core/src/main/java/com/salesmanager/core/business/modules/cms/product/infinispan/CmsImageFileManagerImpl.java`; `sm-core/src/main/java/com/salesmanager/core/business/modules/cms/content/infinispan/CmsStaticContentFileManagerImpl.java` [Q-6]   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-1.1  
**Implementation**:
- Review Infinispan 14.x migration guide for API changes affecting `CmsImageFileManagerImpl` and `CmsStaticContentFileManagerImpl`. _(Unverified: no Code Insights evidence ID supplied.)_
- Verify Spring Boot 3.3.x integration (Spring Cache abstraction compatibility).
- Document the selected target version and required code changes.
- Update OQ-2 with the finding.

**Acceptance Criteria**: A documented decision on Infinispan target version with a list of required code changes.  
**Validation**: Spike report reviewed and approved before TASK-2.4 begins.  
**Rollback**: N/A (spike only).  
**Risk**: High — Infinispan 9→14 is a major version with breaking API changes.  
**Estimate**: 3 person-days  
**Evidence**: Q-6 (Infinispan usage confirmed); R-2 (blocker risk)

---

### TASK-2.4 — Upgrade Infinispan to Target Version
**Components/Files**: `sm-core/pom.xml`; `CmsImageFileManagerImpl` (infinispan path); `CmsStaticContentFileManagerImpl` (infinispan path) [Q-6]   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-2.3  
**Implementation**:
- Update Infinispan version in `sm-core/pom.xml`. _(Unverified: no Code Insights evidence ID supplied.)_
- Migrate `CmsImageFileManagerImpl` (infinispan): update `setCacheManager` (line 462) and `getCacheManager` (line 458) to Infinispan 14.x API. _(Unverified: no Code Insights evidence ID supplied.)_
- Migrate `CmsStaticContentFileManagerImpl` (infinispan): update `setCacheManager` (line 381) to Infinispan 14.x API. _(Unverified: no Code Insights evidence ID supplied.)_
- Verify `CacheManagerImpl` [Q-6] is compatible with the new Infinispan version. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: `mvn test -pl sm-core` passes; cache operations function correctly.   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: Existing cache-related tests pass; no `ClassNotFoundException` for Infinispan classes.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: High — Infinispan API breaking changes.  
**Estimate**: 5 person-days  
**Evidence**: Q-6 (Infinispan usage confirmed); Q-15 (Infinispan CMS impls in module dependency hotspots)

---

### TASK-2.5 — Upgrade JJWT 0.8.0 → 0.12.6 and Refactor JWTTokenUtil
**Objective**: Upgrade JJWT and migrate all JWT API usages in `JWTTokenUtil`.   _(Unverified: no Code Insights evidence ID supplied.)_
**Components/Files**: `sm-shop/pom.xml`; `sm-shop/src/main/java/com/salesmanager/shop/store/security/JWTTokenUtil.java` [Q-3]   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-1.1  
**Implementation**:
- In `sm-shop/pom.xml`: remove `io.jsonwebtoken:jjwt:0.8.0`; add `io.jsonwebtoken:jjwt-api:0.12.6`, `io.jsonwebtoken:jjwt-impl:0.12.6` (runtime), `io.jsonwebtoken:jjwt-jackson:0.12.6` (runtime). _(Unverified: no Code Insights evidence ID supplied.)_
- Refactor `JWTTokenUtil` (lines 26–193): _(Unverified: no Code Insights evidence ID supplied.)_
  - `getAllClaimsFromToken`: migrate from `Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody()` to `Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload()`. _(Unverified: no Code Insights evidence ID supplied.)_
  - `doGenerateToken`: migrate from `Jwts.builder().setClaims(claims).setSubject(subject)...signWith(SignatureAlgorithm.HS512, secret).compact()` to `Jwts.builder().claims(claims).subject(subject)...signWith(key).compact()`. _(Unverified: no Code Insights evidence ID supplied.)_
  - `generateToken`, `refreshToken`, `validateToken`, `canTokenBeRefreshed`, `canTokenBeRefreshedWithGrace`: update all method bodies. _(Unverified: no Code Insights evidence ID supplied.)_
  - Replace `String secret` with `SecretKey` using `Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))`. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: AC-5, AC-6 (JWT auth end-to-end); `mvn test -pl sm-shop` passes.   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: POST to authenticate endpoint returns valid JWT; authenticated request with token returns HTTP 200.  
**Rollback**: Revert `sm-shop/pom.xml` and `JWTTokenUtil.java` to jjwt 0.8.0.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: High — breaking API changes; JWT is the authentication mechanism for all API calls.  
**Estimate**: 3 person-days  
**Evidence**: Q-3 (JWTTokenUtil blast radius: 55 symbols, 1 file, zero test coverage); Q-9 (JWT security classes) _(Unverified: no Code Insights evidence ID supplied.)_

---

### TASK-2.6 — CVE Dependency Upgrades (commons-fileupload, commons-io, guava, httpclient, postgresql, mysql)
**Objective**: Resolve all critical and high CVEs in direct dependencies.  
**Components/Files**: Root `pom.xml` and/or module `pom.xml` files   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-1.1  
**Implementation**:
- `commons-fileupload`: `1.3.3` → `1.5` (CVE-2023-24998, CVE-2016-1000031) _(Unverified: no Code Insights evidence ID supplied.)_
- `commons-io`: `2.7` → `2.16.1` (CVE-2021-29425) _(Unverified: no Code Insights evidence ID supplied.)_
- `guava`: `27.1-jre` → `33.2.1-jre` (CVE-2023-2976) _(Unverified: no Code Insights evidence ID supplied.)_
- `org.apache.httpcomponents:httpclient`: `4.5.2` → `4.5.14` (CVE-2020-13956) _(Unverified: no Code Insights evidence ID supplied.)_
- `postgresql`: `42.2.18` → `42.7.3` (CVE-2022-21724, CVE-2022-26520) _(Unverified: no Code Insights evidence ID supplied.)_
- `mysql-connector-java`: `8.0.21` → `8.4.0` _(Unverified: no Code Insights evidence ID supplied.)_
- Remove stale `elasticsearch` version property (`7.5.2`) from root `pom.xml` _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: `mvn verify` passes across all modules; no compilation errors from API changes.   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: `mvn dependency:tree` shows updated versions; `mvn verify` exits 0.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert version properties in `pom.xml` files.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: Low-Medium — mostly drop-in upgrades; verify commons-fileupload API compatibility.  
**Estimate**: 2 person-days  
**Evidence**: Q-11 (CVE data from upstream analysis); upstream analysis evidence for all CVEs

---

### TASK-2.7 — Raise JaCoCo Coverage Threshold and Write Critical Path Tests
**Objective**: Raise sm-shop JaCoCo line coverage from 4% to ≥20% before Spring Boot parent upgrade.  
**Components/Files**: `sm-shop/pom.xml` (JaCoCo config); new test files in `sm-shop/src/test/`   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-2.5 (JWT tests require JJWT upgrade)  
**Implementation**:
- Write integration tests for:
  - `AuthenticateCustomerApi.authenticate` (lines 157–196) [Q-16] _(Unverified: no Code Insights evidence ID supplied.)_
  - `AuthenticateUserApi.authenticate` (lines 66–105) [Q-17] _(Unverified: no Code Insights evidence ID supplied.)_
  - `OrderFacadeImpl.processOrder` (lines 1195–1359, fan-out 22) [Q-12] _(Unverified: no Code Insights evidence ID supplied.)_
  - `CustomerFacadeImpl.authenticate` (lines 427–460) [Q-9] _(Unverified: no Code Insights evidence ID supplied.)_
- Update JaCoCo `<rule>` in `sm-shop/pom.xml`: `<line>` minimum from `0.04` to `0.20`; `<branch>` minimum from `0.01` to `0.05`. _(Unverified: no Code Insights evidence ID supplied.)_
- Run `mvn verify -pl sm-shop` and confirm coverage gates pass. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: AC-3 (sm-shop JaCoCo line ≥20%); QG-1, QG-2.  
**Validation**: `mvn verify -pl sm-shop` exits 0 with JaCoCo report showing ≥20% line coverage.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert JaCoCo thresholds; remove new test files.  
**Risk**: High — low baseline coverage means significant test writing effort.  
**Estimate**: 8 person-days  
**Evidence**: Upstream analysis evidence (sm-shop: 4% line, 1% branch); Q-12, Q-16, Q-17

---

## Phase 3 — Spring Boot 3.3.5 + javax→jakarta Migration

---

### TASK-3.1 — Bump spring-boot-starter-parent to 3.3.5
**Objective**: Update the Maven parent POM to Spring Boot 3.3.5.  
**Components/Files**: Root `pom.xml`   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-2.1 through TASK-2.7 (all Phase 2 tasks must be complete)  
**Implementation**:
- Change `<parent><version>2.5.12</version>` to `<version>3.3.5</version>` in root `pom.xml`. _(Unverified: no Code Insights evidence ID supplied.)_
- Remove explicit `jackson-core` and `jackson-databind` version pins; let Spring Boot 3.3.5 BOM manage to `2.17.2`. _(Unverified: no Code Insights evidence ID supplied.)_
- Run `mvn dependency:tree` to verify BOM-managed versions. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: Root `pom.xml` references `spring-boot-starter-parent:3.3.5`; `mvn compile` does not fail due to BOM conflicts.   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: `mvn dependency:tree | grep spring-boot` shows `3.3.5`.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert parent version to `2.5.12`; restore explicit Jackson version pins.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: High — triggers cascade of transitive dependency changes.  
**Estimate**: 1 person-day  
**Evidence**: Q-7 (five modules); upstream compatibility evidence (Spring Boot Releases)

---

### TASK-3.2 — javax→jakarta Namespace Migration (All Five Modules)
**Objective**: Migrate all `javax.*` imports to `jakarta.*` across all five Maven modules.   _(Unverified: no Code Insights evidence ID supplied.)_
**Components/Files**: All Java source files in sm-core, sm-shop, sm-shop-model, sm-core-model, sm-core-modules  
**Dependencies**: TASK-3.1, TASK-2.2 (Drools), TASK-2.4 (Infinispan)  
**Implementation**:
- Run OpenRewrite recipe `org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_3` on all five modules. _(Unverified: no Code Insights evidence ID supplied.)_
- Manually review and fix any OpenRewrite misses in:
  - `DroolsBeanFactory.java` [Q-5] _(Unverified: no Code Insights evidence ID supplied.)_
  - `CmsImageFileManagerImpl` (infinispan) [Q-6] _(Unverified: no Code Insights evidence ID supplied.)_
  - `CmsStaticContentFileManagerImpl` (infinispan) [Q-6] _(Unverified: no Code Insights evidence ID supplied.)_
  - All security classes in `sm-shop/src/main/java/com/salesmanager/shop/store/security/` [Q-9] _(Unverified: no Code Insights evidence ID supplied.)_
- Review all JPA entities in `sm-core-model` for Hibernate 6.x type mapping changes (e.g., `@Type`, `@Enumerated`, `@Column` length for `String` fields). _(Unverified: no Code Insights evidence ID supplied.)_
- Review all Spring Data JPA repository interfaces for 3.x API changes.
- Review `application.properties`/`application.yml` for renamed Spring Boot 3.x properties. _(Unverified: no Code Insights evidence ID supplied.)_
- Run `mvn compile` across all modules; fix all compilation errors. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: AC-1, AC-12 (no `javax.*` imports remain).   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: `grep -r "import javax\." --include="*.java" sm-core sm-shop sm-shop-model sm-core-model sm-core-modules` returns zero results (excluding intentional exclusions).   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert all source files to pre-OpenRewrite state via git.  
**Risk**: High — touches all 1,210 Java files across five modules.  
**Estimate**: 5 person-days  
**Evidence**: Q-2 (1,210 Java files); Q-7 (five modules)

---

### TASK-3.3 — Hibernate 6.x Persistence Layer Review and Fix
**Objective**: Identify and fix all Hibernate 6.x breaking changes in the persistence layer.  
**Components/Files**: All JPA entities in `sm-core-model`; `OrderFacadeImpl` [Q-12]; `CategoryServiceImpl` [Q-12]; all JPQL/HQL queries   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-3.2  
**Implementation**:
- Review `OrderFacadeImpl.processOrder` (lines 1195–1359, fan-out 22) [Q-12] for Hibernate 6.x Criteria API changes. _(Unverified: no Code Insights evidence ID supplied.)_
- Review `CategoryServiceImpl.delete` (fan-out 14) [Q-12] for Hibernate 6.x changes. _(Unverified: no Code Insights evidence ID supplied.)_
- Review all `@Entity` classes in `sm-core-model` for: _(Unverified: no Code Insights evidence ID supplied.)_
  - `@Type` annotation changes (Hibernate 6.x uses `@JdbcType`, `@JavaType`) _(Unverified: no Code Insights evidence ID supplied.)_
  - `@Enumerated` handling changes _(Unverified: no Code Insights evidence ID supplied.)_
  - `@Column` implicit type changes _(Unverified: no Code Insights evidence ID supplied.)_
  - Named query syntax changes
- Fix all identified issues.
- Run `mvn test` across all modules. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: AC-2; no Hibernate-related test failures.  
**Validation**: `mvn test` exits 0; no `HibernateException` or `MappingException` in test output.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert entity and facade changes.  
**Risk**: High — Hibernate 6.x has significant breaking changes in type system.  
**Estimate**: 5 person-days  
**Evidence**: Q-12 (processOrder fan-out 22; CategoryServiceImpl fan-out 14); Q-15 (module dependency hotspots)

---

### TASK-3.4 — Verify Application Startup
**Objective**: Confirm the application starts successfully with Spring Boot 3.3.5.  
**Components/Files**: `sm-shop/src/main/java/com/salesmanager/shop/application/ShopApplication.java` [Q-8]   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-3.3  
**Implementation**:
- Run `mvn spring-boot:run -pl sm-shop` or start the packaged JAR. _(Unverified: no Code Insights evidence ID supplied.)_
- Verify actuator `/health` returns HTTP 200 with status UP. _(Unverified: no Code Insights evidence ID supplied.)_
- Check application logs for any `ClassNotFoundException`, `BeanCreationException`, or `NoSuchMethodError`. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: AC-4.  
**Validation**: `curl http://localhost:8080/actuator/health` returns `{"status":"UP"}`.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert to Phase 2 state.  
**Risk**: Medium — startup failures may reveal missed migration issues.  
**Estimate**: 1 person-day  
**Evidence**: Q-8 (ShopApplication entry point)

---

## Phase 4 — Spring Security 6.x Migration

---

### TASK-4.1 — Rewrite MultipleEntryPointsSecurityConfig
**Objective**: Replace `WebSecurityConfigurerAdapter` with `SecurityFilterChain` beans.   _(Unverified: no Code Insights evidence ID supplied.)_
**Components/Files**: `sm-shop/src/main/java/com/salesmanager/shop/application/config/MultipleEntryPointsSecurityConfig.java` [Q-4]   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-3.4  
**Implementation**:
- Remove all inner classes extending `WebSecurityConfigurerAdapter`. _(Unverified: no Code Insights evidence ID supplied.)_
- Replace with `@Bean SecurityFilterChain` methods for each security configuration (admin, customer, services). _(Unverified: no Code Insights evidence ID supplied.)_
- Replace the three `authenticationManagerBean` methods (lines 87, 295, 380) [Q-4] with `@Bean AuthenticationManager` beans using `AuthenticationManagerBuilder`. _(Unverified: no Code Insights evidence ID supplied.)_
- Migrate `HttpSecurity` configuration to Spring Security 6.x lambda DSL. _(Unverified: no Code Insights evidence ID supplied.)_
- Update CSRF, session management, and CORS configuration for Spring Security 6.x defaults.

**Acceptance Criteria**: Application starts (AC-4); JWT authentication works (AC-5, AC-6).  
**Validation**: `mvn test -pl sm-shop` passes; POST to authenticate endpoint returns JWT.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert `MultipleEntryPointsSecurityConfig.java`.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: High — security configuration rewrite; incorrect configuration may break all authentication.  
**Estimate**: 5 person-days  
**Evidence**: Q-4 (blast radius: 7 files including all security classes) _(Unverified: no Code Insights evidence ID supplied.)_

---

### TASK-4.2 — Update Security Filter and Provider Classes
**Objective**: Update all security classes impacted by the `MultipleEntryPointsSecurityConfig` rewrite.   _(Unverified: no Code Insights evidence ID supplied.)_
**Components/Files**: `AuthenticationTokenFilter`, `JWTAdminAuthenticationProvider`, `JWTCustomerAuthenticationProvider`, `ServicesAuthenticationSuccessHandler`, `UserAuthenticationSuccessHandler`, `CredentialsServiceImpl` [Q-4]   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-4.1  
**Implementation**:
- Update `AuthenticationTokenFilter` for Spring Security 6.x `OncePerRequestFilter` (jakarta namespace already applied in TASK-3.2). _(Unverified: no Code Insights evidence ID supplied.)_
- Update `JWTAdminAuthenticationProvider` and `JWTCustomerAuthenticationProvider` for Spring Security 6.x `AuthenticationProvider`. _(Unverified: no Code Insights evidence ID supplied.)_
- Update `ServicesAuthenticationSuccessHandler` and `UserAuthenticationSuccessHandler`. _(Unverified: no Code Insights evidence ID supplied.)_
- Update `CredentialsServiceImpl` for Spring Security 6.x `UserDetailsService`. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: AC-5, AC-6; `mvn test -pl sm-shop` passes.   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: End-to-end JWT authentication test passes.  
**Rollback**: Revert all security class changes.  
**Risk**: High — security filter chain integrity.  
**Estimate**: 3 person-days  
**Evidence**: Q-4 (blast radius: 7 files); Q-9 (JWT security classes) _(Unverified: no Code Insights evidence ID supplied.)_

---

## Phase 5 — Springfox → springdoc-openapi Migration

---

### TASK-5.1 — Replace Springfox with springdoc-openapi 2.6.0
**Objective**: Remove Springfox dependencies; add springdoc-openapi 2.6.0.  
**Components/Files**: `sm-shop/pom.xml`; `sm-shop/src/main/java/com/salesmanager/shop/application/config/DocumentationConfiguration.java` [Q-10]   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-3.4  
**Implementation**:
- Remove `io.springfox:springfox-swagger2` and `io.springfox:springfox-swagger-ui` from `sm-shop/pom.xml`. _(Unverified: no Code Insights evidence ID supplied.)_
- Add `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0` to `sm-shop/pom.xml`. _(Unverified: no Code Insights evidence ID supplied.)_
- Rewrite `DocumentationConfiguration` (lines 51–85): replace `Docket` bean with `OpenAPI` bean using springdoc-openapi API. _(Unverified: no Code Insights evidence ID supplied.)_
- Add springdoc configuration to `application.properties`/`application.yml` if needed. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: AC-13 (no Springfox in dependency tree); AC-7, AC-8 (Swagger UI accessible).  
**Validation**: `mvn dependency:tree | grep springfox` returns empty; GET `/swagger-ui/index.html` returns HTTP 200.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert `sm-shop/pom.xml` and `DocumentationConfiguration.java`.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: Medium — Springfox Docket API is completely replaced.  
**Estimate**: 2 person-days  
**Evidence**: Q-10 (DocumentationConfiguration.api() at lines 51–85)

---

### TASK-5.2 — Migrate REST Controller Swagger Annotations
**Objective**: Replace all `@Api`, `@ApiOperation`, `@ApiParam`, `@ApiResponse` annotations with OpenAPI 3 equivalents.   _(Unverified: no Code Insights evidence ID supplied.)_
**Components/Files**: All REST controllers in `sm-shop/src/main/java/com/salesmanager/shop/store/api/v1/`   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-5.1  
**Implementation**:
- Replace `@Api(tags = "...")` with `@Tag(name = "...")`. _(Unverified: no Code Insights evidence ID supplied.)_
- Replace `@ApiOperation(value = "...")` with `@Operation(summary = "...")`. _(Unverified: no Code Insights evidence ID supplied.)_
- Replace `@ApiParam` with `@Parameter`. _(Unverified: no Code Insights evidence ID supplied.)_
- Replace `@ApiResponse` with `@io.swagger.v3.oas.annotations.responses.ApiResponse`. _(Unverified: no Code Insights evidence ID supplied.)_
- Remove `@ApiIgnore` (not needed in springdoc-openapi). _(Unverified: no Code Insights evidence ID supplied.)_
- Verify `AuthenticateCustomerApi` [Q-16] and `AuthenticateUserApi` [Q-17] annotations are correctly migrated. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: AC-8 (OpenAPI spec at `/v3/api-docs` is valid and complete).   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: GET `/v3/api-docs` returns HTTP 200 with valid JSON containing all expected endpoints.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert controller annotation changes.  
**Risk**: Medium — volume of changes across all REST controllers.  
**Estimate**: 3 person-days  
**Evidence**: Q-16, Q-17 (authenticate endpoints confirmed); Q-7 (sm-shop module)

---

## Phase 6 — Remaining Upgrades and Quality Gates

---

### TASK-6.1 — Upgrade Remaining Dependencies
**Objective**: Complete all remaining dependency version upgrades.  
**Components/Files**: Root `pom.xml` and/or module `pom.xml` files   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-3.1  
**Implementation**:
- `commons-lang3`: `3.5` → `3.14.0` _(Unverified: no Code Insights evidence ID supplied.)_
- `commons-collections4`: `4.1` → `4.4` _(Unverified: no Code Insights evidence ID supplied.)_
- `commons-validator`: `1.5.1` → `1.9.0` _(Unverified: no Code Insights evidence ID supplied.)_
- `org.owasp.antisamy:antisamy`: `1.6.7` → `1.7.5` _(Unverified: no Code Insights evidence ID supplied.)_
- Run `mvn verify` across all modules. _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: `mvn verify` passes; no API incompatibilities.   _(Unverified: no Code Insights evidence ID supplied.)_
**Validation**: `mvn dependency:tree` shows updated versions; `mvn verify` exits 0.   _(Unverified: no Code Insights evidence ID supplied.)_
**Rollback**: Revert version properties.  
**Risk**: Low — minor version upgrades with no known breaking changes.  
**Estimate**: 1 person-day  
**Evidence**: Upstream analysis evidence for all versions

---

### TASK-6.2 — Add OWASP Dependency-Check to CI Pipeline
**Objective**: Add OWASP Dependency-Check Maven plugin to CI pipeline with CVSS threshold.  
**Components/Files**: Root `pom.xml`; CI configuration files (Jenkinsfile / CircleCI config — paths per OQ-5)   _(Unverified: no Code Insights evidence ID supplied.)_
**Dependencies**: TASK-6.1  
**Implementation**:
- Add `org.owasp:dependency-check-maven` plugin to root `pom.xml` `<build><plugins>` section: _(Unverified: no Code Insights evidence ID supplied.)_
  ```xml
  <plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.x.x</version>
    <configuration>
      <failBuildOnCVSS>9</failBuildOnCVSS>
    </configuration>
    <executions>
      <execution>
        <goals><goal>check</goal></goals>
      </execution>
    </executions>
  </plugin>
  ```
- Add `mvn dependency-check:check` step to CI pipeline (Jenkinsfile and/or CircleCI config). _(Unverified: no Code Insights evidence ID supplied.)_

**Acceptance Criteria**: AC-9 (OWASP Dependency-Check reports zero CRITICAL CVEs); QG-3.  
**Validation**: CI pipeline runs OWASP check; report shows no CVSS ≥9.0 vulnerabilities.  
**Rollback**: Remove plugin from `pom.xml` and CI config.   _(Unverified: no Code Insights evidence ID supplied.)_
**Risk**: Low — additive change only.  
**Estimate**: 1 person-day  
**Evidence**: Upstream analysis evidence (no OWASP Dependency-Check in CI pipeline)

---

### TASK-6.3 — Final Verification and Documentation Update
**Objective**: Run full end-to-end verification; update README and migration notes.  
**Components/Files**: All modules; README.md; any migration notes  
**Dependencies**: All previous tasks  
**Implementation**:
- Run `mvn clean verify` across all modules. _(Unverified: no Code Insights evidence ID supplied.)_
- Verify all acceptance criteria AC-1 through AC-15.
- Run OWASP Dependency-Check; confirm AC-9.
- Build Docker image; confirm AC-10, AC-11.
- Start application; confirm AC-4, AC-5, AC-6, AC-7, AC-8.
- Update README.md with new Java 21 / Spring Boot 3.3.5 requirements.
- Document any known issues or deferred items.

**Acceptance Criteria**: All AC-1 through AC-15 pass.  
**Validation**: Full CI pipeline run exits 0.  
**Rollback**: N/A (verification task).  
**Risk**: Low.  
**Estimate**: 2 person-days  
**Evidence**: All Q-* evidence IDs

---

## Task Summary

| Task | Phase | Estimate | Risk | Dependencies |
|------|-------|----------|------|-------------|
| TASK-1.1 | 1 | 0.5 pd | Low | None |
| TASK-1.2 | 1 | 1 pd | Medium | TASK-1.1 |
| TASK-1.3 | 1 | 0.5 pd | Low | None |
| TASK-2.1 | 2 | 3 pd | High | TASK-1.1 |
| TASK-2.2 | 2 | 5 pd | High | TASK-2.1 |
| TASK-2.3 | 2 | 3 pd | High | TASK-1.1 |
| TASK-2.4 | 2 | 5 pd | High | TASK-2.3 |
| TASK-2.5 | 2 | 3 pd | High | TASK-1.1 |
| TASK-2.6 | 2 | 2 pd | Low-Med | TASK-1.1 |
| TASK-2.7 | 2 | 8 pd | High | TASK-2.5 |
| TASK-3.1 | 3 | 1 pd | High | All Phase 2 |
| TASK-3.2 | 3 | 5 pd | High | TASK-3.1, TASK-2.2, TASK-2.4 |
| TASK-3.3 | 3 | 5 pd | High | TASK-3.2 |
| TASK-3.4 | 3 | 1 pd | Medium | TASK-3.3 |
| TASK-4.1 | 4 | 5 pd | High | TASK-3.4 |
| TASK-4.2 | 4 | 3 pd | High | TASK-4.1 |
| TASK-5.1 | 5 | 2 pd | Medium | TASK-3.4 |
| TASK-5.2 | 5 | 3 pd | Medium | TASK-5.1 |
| TASK-6.1 | 6 | 1 pd | Low | TASK-3.1 |
| TASK-6.2 | 6 | 1 pd | Low | TASK-6.1 |
| TASK-6.3 | 6 | 2 pd | Low | All |
| **Total** | | **~59 pd** | | |

> Note: The upstream analysis estimates 45 person-days. The task breakdown above totals ~59 person-days due to the inclusion of spike tasks (TASK-2.1, TASK-2.3) and the test coverage raise (TASK-2.7) which are critical risk mitigations. The discrepancy is recorded in research.md as a conflict.