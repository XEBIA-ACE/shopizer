# Maven Dependency Baseline

Story `176fafc5-80b9-5910-86f2-5152a6832c17` — Maven Dependency Tree and pom.xml Analysis.

Captured at commit `6a4a0a65a3408ee8f62597b51d1b3aac24b77dee` (branch `3.2.7`, project version `3.2.5`).

| File | Purpose |
|------|---------|
| `dependency-tree.txt` | Raw `mvn dependency:tree` output for all 6 reactor projects (S1-AC1) |
| `verify-baseline.sh` | Characterization check: regenerates the tree and asserts the versions below |

Regenerate / verify:

```bash
mvn -B dependency:tree -DoutputFile=/tmp/dependency-tree.txt -DappendOutput=true
docs/dependency-analysis/verify-baseline.sh          # add MVN_ARGS=-o to run offline
```

Tooling used: Apache Maven 3.9.9, `maven-dependency-plugin` 3.1.2 (managed by Spring Boot parent). Result: `BUILD SUCCESS` for `shopizer`, `sm-core-model`, `sm-core-modules`, `sm-core`, `sm-shop-model`, `sm-shop`.

## Summary (S1-AC7)

| Item | Version | Source |
|------|---------|--------|
| Spring Boot | **2.5.12** | Root `pom.xml` `<parent>` = `org.springframework.boot:spring-boot-starter-parent:2.5.12` |
| Spring Framework | **5.3.18** | Transitive via Spring Boot BOM (`spring-framework.version`) |
| Spring Data JPA | 2.5.10 (BOM `spring-data-bom` 2021.0.10) | Transitive via `spring-boot-starter-data-jpa` |
| Spring Security | 5.5.5 | Transitive via `spring-boot-starter-security` (sm-shop) |
| JPA provider | **Hibernate ORM 5.4.33** (`hibernate-core`, plus `hibernate-ehcache` 5.4.33 2nd-level cache) | `spring-boot-starter-data-jpa` + direct `hibernate-ehcache` in sm-core |
| JPA API | `jakarta.persistence:jakarta.persistence-api:2.2.3` (JPA 2.2, `javax.persistence` package) | Transitive via Hibernate |
| Jakarta EE | **Jakarta EE 8 / Java EE 8 – `javax.*` namespace** (not yet Jakarta EE 9+ `jakarta.*`) | See javax/Jakarta table below |
| Servlet container | Tomcat embed 9.0.60 (Servlet 4.0), `provided` scope | `spring-boot-starter-tomcat` in sm-shop |
| Java compiler target | **11** (`maven.compiler.source`/`target` = `${java.version}` = 11) | Root `pom.xml`, re-declared in `sm-core-model/pom.xml` |
| maven-compiler-plugin | 3.8.1 | Managed by Spring Boot parent |

## S1-AC2 — Spring Boot version per pom.xml

| pom.xml | Parent | Spring Boot declaration |
|---------|--------|-------------------------|
| `pom.xml` (root, `com.shopizer:shopizer:3.2.5`) | `org.springframework.boot:spring-boot-starter-parent:2.5.12` | Defines Spring Boot version for whole reactor |
| `sm-core-model/pom.xml` | `com.shopizer:shopizer:3.2.5` | `spring-boot-starter-data-jpa` (version inherited → 2.5.12) |
| `sm-core-modules/pom.xml` | `com.shopizer:shopizer:3.2.5` | No direct Boot dependency; inherits 2.5.12 via `sm-core-model` |
| `sm-core/pom.xml` | `com.shopizer:shopizer:3.2.5` | `spring-boot-starter-data-jpa`, `spring-boot-starter-test` (inherited → 2.5.12) |
| `sm-shop-model/pom.xml` | `com.shopizer:shopizer:3.2.5` | No direct Boot dependency; inherits 2.5.12 via `sm-core-model` |
| `sm-shop/pom.xml` | `com.shopizer:shopizer:3.2.5` | `spring-boot-starter-security`, `-aop`, `-actuator`, `-tomcat`, `-test`, `spring-boot-maven-plugin` (inherited → 2.5.12) |

No module overrides the Spring Boot version.

## S1-AC3 — Spring Framework

No module pins a Spring Framework version. Direct Spring Framework declarations (version-less, resolved from the Boot BOM):

- `sm-core-model`: `org.springframework:spring-webmvc`
- `sm-core`: `org.springframework:spring-context-support`
- `sm-shop-model`: `org.springframework:spring-core`, `org.springframework:spring-webmvc`

All `org.springframework:*` artifacts in the tree resolve to **5.3.18** (`spring-aop`, `spring-aspects`, `spring-beans`, `spring-context`, `spring-context-support`, `spring-core`, `spring-expression`, `spring-jcl`, `spring-jdbc`, `spring-orm`, `spring-test`, `spring-tx`, `spring-web`, `spring-webmvc`).

Other Spring artifacts: `spring-plugin-core`/`spring-plugin-metadata` 1.2.0.RELEASE (via springfox 2.9.2).

## S1-AC4 — Hibernate / JPA

| Artifact | Version | Scope / path |
|----------|---------|--------------|
| `org.hibernate:hibernate-core` | 5.4.33 | via `spring-boot-starter-data-jpa` (sm-core-model and downstream) |
| `org.hibernate:hibernate-ehcache` | 5.4.33 | direct in `sm-core` |
| `org.hibernate.common:hibernate-commons-annotations` | 5.1.2.Final | via hibernate-core |
| `jakarta.persistence:jakarta.persistence-api` | 2.2.3 | via hibernate-core |
| `org.springframework.data:spring-data-jpa` | 2.5.10 | via `spring-boot-starter-data-jpa` |
| `net.sf.ehcache:ehcache` | 2.10.9.2 | direct in root `<dependencies>` (all modules) |
| `com.zaxxer:HikariCP` | 4.0.3 | via `spring-boot-starter-jdbc` |
| `net.bytebuddy:byte-buddy` | 1.10.22 | via hibernate-core |
| `org.infinispan:infinispan-core` | 9.4.18.Final | direct in `sm-core` (root-managed) |
| `mysql:mysql-connector-java` | 8.0.21 | root `<dependencies>` |
| `com.h2database:h2` | 1.4.200 | sm-core, sm-shop |

Provider: **Hibernate ORM 5.4.33** implementing **JPA 2.2**. `hibernate-validator` is not on the dependency tree; only the Bean Validation API (`javax.validation:validation-api:2.0.1.Final`) is present.

## S1-AC5 — Jakarta EE / javax.*

The project is on the **pre-Jakarta (`javax.*`) stack**. The `jakarta.*` Maven coordinates below are Jakarta EE 8 artifacts that still ship `javax.*` packages.

| Artifact (resolved) | Version | Java package |
|---------------------|---------|--------------|
| `jakarta.persistence:jakarta.persistence-api` | 2.2.3 | `javax.persistence` |
| `jakarta.transaction:jakarta.transaction-api` | 1.3.3 | `javax.transaction` |
| `jakarta.annotation:jakarta.annotation-api` | 1.3.5 | `javax.annotation` |
| `jakarta.xml.bind:jakarta.xml.bind-api` | 2.3.3 | `javax.xml.bind` |
| `jakarta.activation:jakarta.activation-api` | 1.2.2 | `javax.activation` |
| `org.glassfish.jaxb:jaxb-runtime` | 2.3.6 | — |
| `javax.validation:validation-api` | 2.0.1.Final | `javax.validation` |
| `javax.annotation:javax.annotation-api` | 1.3.2 | `javax.annotation` |
| `javax.inject:javax.inject` | 1 | `javax.inject` |
| `javax.mail:javax.mail-api` | 1.6.2 | `javax.mail` |
| `javax.mail:mail` | 1.4.7 | `javax.mail` |
| `javax.activation:activation` | 1.1 | `javax.activation` |
| `javax.enterprise:cdi-api` | 1.0 | `javax.enterprise` (via Drools/KIE, sm-core) |
| `javax.transaction:jboss-transaction-api_1.2_spec` | 1.1.1.Final | `javax.transaction` (via Infinispan) |
| `javax.ws.rs:jsr311-api` | 1.1.1 | `javax.ws.rs` |
| `javax.xml.stream:stax-api` | 1.0-2 | `javax.xml.stream` |
| `org.apache.tomcat.embed:tomcat-embed-core` / `-el` / `-websocket` / `-jasper` | 9.0.60 (`provided`) | `javax.servlet` (Servlet 4.0), `javax.el` |

Source usage (count of `.java` files with a top-level import): `javax.inject` 179, `javax.persistence` 107, `javax.validation` 84, `javax.servlet` 59, `javax.annotation` 12, `jakarta.*` 0.

Root properties `javax.el.version` (2.2.4) and `javax.servlet-api-version` (3.0.1) are declared but not referenced by any dependency.

## S1-AC6 — Java compiler target

- Root `pom.xml`: `<java.version>11</java.version>`, `<maven.compiler.source>${java.version}</maven.compiler.source>`, `<maven.compiler.target>${java.version}</maven.compiler.target>`; `maven-compiler-plugin` configured with `<source>${maven.compiler.source}</source>` / `<target>${maven.compiler.target}</target>` and the MapStruct 1.3.0.Final annotation processor. This overrides the Spring Boot parent default (`java.version` 1.8).
- `sm-core-model/pom.xml` re-declares the same three properties with value 11.
- `sm-shop-model/pom.xml` sets `<source>11</source>` for `maven-javadoc-plugin` (release profile).
- No `<release>` flag is set. Effective compiler target: **Java 11**.

## S1-AC7 — Version management constraining transitive versions

**Inherited BOM**: `spring-boot-dependencies:2.5.12` (via `spring-boot-starter-parent`). Key managed properties: `spring-framework.version` 5.3.18, `spring-data-bom.version` 2021.0.10, `spring-security.version` 5.5.5, `hibernate.version` 5.4.33, `hibernate-validator.version` 6.2.3.Final, `tomcat.version` 9.0.60, `jakarta-persistence.version` 2.2.3, `jakarta-transaction.version` 1.3.3, `jakarta-annotation.version` 1.3.5, `jakarta-servlet.version` 4.0.4, `javax-validation.version` 2.0.1.Final, `javax-mail.version` 1.6.2, `jackson-bom.version` 2.12.6.20220326.

**Root `pom.xml` `<dependencyManagement>`** (overrides / additions to the Boot BOM):

| Managed artifact | Version | Note |
|------------------|---------|------|
| `org.springframework.boot:spring-boot-starter-web`, `spring-boot-starter-cache` | (Boot BOM) | declared without version |
| `com.shopizer:sm-core`, `sm-core-model`, `sm-core-modules`, `sm-shop-model` | 3.2.5 | |
| `javax.inject:javax.inject` | 1 | |
| `org.apache.commons:commons-lang3` | 3.5 | **overrides** Boot BOM |
| `org.mapstruct:mapstruct` | 1.3.0.Final | |
| `io.jsonwebtoken:jjwt` | 0.8.0 | |
| `com.fasterxml.jackson.core:jackson-databind` | 2.13.4.1 | **overrides** Boot jackson-bom 2.12.6 |
| `com.fasterxml.jackson.core:jackson-core`, `jackson-annotations` | 2.13.4 | **overrides** Boot jackson-bom 2.12.6 |
| `javax.mail:mail` | 1.4.7 | |
| `com.googlecode.json-simple:json-simple` | 1.1.1 | |
| `mysql:mysql-connector-java` | 8.0.21 | **overrides** Boot BOM |
| `com.google.maps:google-maps-services` | 0.1.6 | |
| `org.kie:kie-ci`, `kie-spring`, `org.drools:drools-decisiontables`, `drools-core`, `drools-compiler` | 7.32.0.Final | |
| `org.infinispan:infinispan-core`, `infinispan-cachestore-jdbc`, `infinispan-tree` | 9.4.18.Final | **overrides** Boot BOM |
| `org.apache.commons:commons-collections4` | 4.1 | |
| `commons-validator:commons-validator` | 1.5.1 | |
| `com.amazonaws:aws-java-sdk-s3`, `aws-java-sdk-ses` | 1.11.640 | |
| `com.google.cloud:google-cloud-storage` | 1.74.0 | |
| `com.paypal.sdk:merchantsdk` | 2.6.109 | |
| `com.stripe:stripe-java` | 19.5.0 | |
| `com.braintreepayments.gateway:braintree-java` | 2.73.0 | |
| `com.maxmind.geoip2:geoip2` | 2.7.0 | |
| `com.google.guava:guava` | 27.1-jre | |
| `commons-io:commons-io` | 2.7 | |
| `commons-fileupload:commons-fileupload` | 1.3.3 | |
| `io.springfox:springfox-swagger2`, `springfox-swagger-ui` | 2.9.2 | |
| `javax.annotation:javax.annotation-api` | 1.3.2 | |

Module poms declare no `<dependencyManagement>` section. Module-level versions declared directly: `org.json:json` 20211205 (sm-core-model); `shipping-canadapost-spring-boot-starter` 2.17.0, `shopizer-commons` 1.0.6, `shopizer-search-opensearch-spring-boot-starter` 1.0.3, `shopizer-payment-square-spring-boot-starter` 1.0.0 (sm-core); `commons-collections` 3.2.2, `commons-rng-simple` 1.3, `antisamy` 1.6.7, `passay` 1.6.0 (sm-shop).

## Observations / gaps for downstream upgrade stories

- `sm-core` declares `spring-boot-starter-test` without `<scope>test</scope>`, so it resolves at **compile** scope in sm-core (sm-shop declares it as `test`).
- Root properties `elasticsearch.version`, `javax.el.version`, `javax.servlet-api-version`, `shopizer.search.version`, `shopizer-canadapost.version`, `postgresql.version`, `oracle.version` are declared but not referenced by an active dependency (canadapost starter uses a hard-coded 2.17.0).
- Root project version is `3.2.5` while the branch is named `3.2.7`.
- The technical design lists component `sm-core/src/main/java/com/salesmanager/core/business/modules/cms/impl/CacheManagerImpl.java` and `Tree`; this story is analysis-only and required no changes to that class. It uses `org.infinispan:infinispan-tree` 9.4.18.Final, relevant to a later upgrade.
- No build/configuration change was made, so no migration or rollback steps are required for this story.
