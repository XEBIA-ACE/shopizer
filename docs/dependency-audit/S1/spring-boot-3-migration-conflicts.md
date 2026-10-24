# S1 – Spring Boot 2.x → 3.x conflict analysis (FR-S1-007, FR-S1-008, S1-AC4)

Story `b2ec327d-0260-5051-b2b9-e8e046c072a4`. Current parent: `org.springframework.boot:spring-boot-starter-parent`
**2.5.12** (root `pom.xml`). Candidate 3.x versions are read from
`org.springframework.boot:spring-boot-dependencies:3.2.12` (see README gap G7). Per-path detail
(direct/transitive, full chain) is in `reports/dependency-summary.md`.

"Direct" = declared in a project POM; "transitive" = pulled by another dependency.
Rows marked **INCOMPLETE** have no replacement evidenced in dependency metadata and must not be inferred.

## 1. Spring Boot managed versions that change major line

| Artifact | Resolved now (SB 2.5.12) | SB 3.2.12 BOM | Relation |
|---|---|---|---|
| org.springframework.boot:spring-boot-starter-parent | 2.5.12 | 3.2.12 | direct (parent) |
| org.springframework:spring-core (Spring Framework) | 5.3.18 | 6.1.15 | transitive |
| org.springframework.security:spring-security-core | 5.5.5 | 6.2.8 | transitive (spring-boot-starter-security, sm-shop) |
| org.hibernate:hibernate-core | 5.4.33 | 6.4.10.Final (`org.hibernate.orm:hibernate-core`) | transitive (spring-boot-starter-data-jpa) |
| org.hibernate:hibernate-ehcache | 5.4.33 | not managed – **INCOMPLETE** (no Hibernate 6 artifact in BOM) | direct (sm-core) |
| org.apache.tomcat.embed:tomcat-embed-core | 9.0.60 | 10.1.33 | transitive, provided (sm-shop) |
| org.aspectj:aspectjweaver | 1.9.7 | 1.9.22.1 | transitive (spring-boot-starter-aop) |
| org.slf4j:slf4j-api | 1.7.36 | 2.0.16 | transitive |
| ch.qos.logback:logback-classic | 1.2.11 | 1.4.14 | transitive |
| org.junit.jupiter:junit-jupiter | 5.7.2 | 5.10.5 | transitive (spring-boot-starter-test) |
| org.mockito:mockito-core | 3.9.0 | 5.7.0 | transitive (spring-boot-starter-test) |
| com.fasterxml.jackson.core:jackson-databind | 2.13.4.1 (overridden in root POM) | 2.15.4 | direct (root) |
| mysql:mysql-connector-java | 8.0.21 (overridden in root POM) | 8.3.0 as `com.mysql:mysql-connector-j` | direct (root) |
| net.sf.ehcache:ehcache | 2.10.9.2 | not managed (BOM manages Ehcache 3 `ehcache3.version` 3.10.8) – **INCOMPLETE** | direct (root) |
| com.h2database:h2 | 1.4.200 | 2.2.224 | direct (sm-core) |

## 2. javax.* → jakarta.* mapping

| javax artifact (resolved) | Version | Relation / introduced by | Jakarta replacement (SB 3.2.12) | Source imports* | Status |
|---|---|---|---|---|---|
| jakarta.persistence:jakarta.persistence-api | 2.2.3 (EE 8, `javax.persistence`) | transitive – spring-boot-starter-data-jpa | jakarta.persistence:jakarta.persistence-api 3.1.0 | `javax.persistence` 1052 | complete |
| javax.inject:javax.inject | 1 | direct – root pom | jakarta.inject:jakarta.inject-api (not managed by SB BOM) | `javax.inject` 182 | **INCOMPLETE** (version not evidenced) |
| org.apache.tomcat.embed:tomcat-embed-core (bundles `javax.servlet`) | 9.0.60 | transitive, provided – spring-boot-starter-tomcat | tomcat-embed-core 10.1.33 / jakarta.servlet:jakarta.servlet-api 6.0.0 | `javax.servlet*` 110 | complete |
| javax.validation:validation-api | 2.0.1.Final | direct – root pom | jakarta.validation:jakarta.validation-api 3.0.2 | `javax.validation*` 123 | complete |
| javax.annotation:javax.annotation-api | 1.3.2 | direct – sm-shop-model; transitive – com.google.cloud:google-cloud-storage 1.74.0 (sm-core) | jakarta.annotation:jakarta.annotation-api 2.1.1 | `javax.annotation` 12 | complete |
| jakarta.annotation:jakarta.annotation-api | 1.3.5 (EE 8, `javax.annotation`) | transitive – spring-boot-starter | jakarta.annotation:jakarta.annotation-api 2.1.1 | (above) | complete |
| javax.mail:javax.mail-api | 1.6.2 | direct – root pom | jakarta.mail:jakarta.mail-api 2.1.3 | `javax.mail*` 8 | complete |
| javax.mail:mail | 1.4.7 | direct – root pom | org.eclipse.angus:angus-mail 2.0.3 | (above) | complete |
| javax.activation:activation | 1.1 | transitive – javax.mail:mail 1.4.7 | jakarta.activation:jakarta.activation-api 2.1.3 | – | complete |
| jakarta.activation:jakarta.activation-api | 1.2.2 (EE 8) | transitive – jakarta.xml.bind-api 2.3.3 (spring-boot-starter-test) | jakarta.activation:jakarta.activation-api 2.1.3 | – | complete |
| com.sun.activation:jakarta.activation | 1.2.2 | transitive, runtime – org.glassfish.jaxb:jaxb-runtime 2.3.6 ← hibernate-core | implementation not evidenced in BOM | – | **INCOMPLETE** |
| jakarta.xml.bind:jakarta.xml.bind-api | 2.3.3 (EE 8, `javax.xml.bind`) | transitive – hibernate-core (jaxb-runtime 2.3.6), spring-boot-starter-test | jakarta.xml.bind:jakarta.xml.bind-api 4.0.2 (glassfish-jaxb 4.0.5) | – | complete |
| jakarta.transaction:jakarta.transaction-api | 1.3.3 (EE 8, `javax.transaction`) | transitive – spring-boot-starter-data-jpa | jakarta.transaction:jakarta.transaction-api 2.0.1 | – | complete |
| org.jboss.spec.javax.transaction:jboss-transaction-api_1.2_spec | 1.1.1.Final | transitive – org.infinispan:infinispan-core 9.4.18.Final | jakarta.transaction:jakarta.transaction-api 2.0.1, but requires a Jakarta-based Infinispan (not evidenced) | – | **INCOMPLETE** |
| javax.ws.rs:jsr311-api | 1.1.1 | transitive – com.sun.jersey:jersey-client 1.19.4 ← com.shopizer:shipping-canadapost-spring-boot-starter 2.17.0 | jakarta.ws.rs:jakarta.ws.rs-api 3.1.0, but Jersey 1.x has no Jakarta line; needs a new canadapost starter (not evidenced) | `javax.ws.rs` 3 | **INCOMPLETE** |
| javax.enterprise:cdi-api | 1.0 | transitive – org.eclipse.sisu:org.eclipse.sisu.plexus 0.3.2 ← org.kie:kie-ci 7.32.0.Final | jakarta.enterprise:jakarta.enterprise.cdi-api (not managed by SB BOM); depends on Drools/KIE upgrade | – | **INCOMPLETE** |
| javax.xml.stream:stax-api | 1.0-2 | transitive – org.drools:drools-decisiontables 7.32.0.Final | none – StAX is part of the JDK (`java.xml`); exclusion candidate | – | **INCOMPLETE** (no Jakarta artifact) |

\* `import javax.<pkg>` occurrences in `sm-*/src` at this commit (`grep -rhoE '^import (static )?javax\.[a-z]+' --include=*.java sm-*/src`).
JDK-provided packages (`javax.crypto` 6, `javax.imageio` 2) are unaffected by the Jakarta migration.

## 3. Third-party artifacts without an evidenced Spring Boot 3 line

| Artifact | Version | Relation | Issue |
|---|---|---|---|
| io.springfox:springfox-swagger2, io.springfox:springfox-swagger-ui | 2.9.2 | direct (sm-shop-model / sm-shop) | built for Spring 4/5 (pulls org.springframework.plugin:spring-plugin-core 1.2.0.RELEASE requesting Spring 4.0.9.RELEASE); no SB 3 version – **INCOMPLETE** |
| com.shopizer:shipping-canadapost-spring-boot-starter | 2.17.0 | direct (sm-core) | built against Spring Boot 2 / Jersey 1 – **INCOMPLETE** |
| com.shopizer:shopizer-search-opensearch-spring-boot-starter | 1.0.3 | direct (sm-core) | Spring Boot 2 starter – **INCOMPLETE** |
| com.shopizer:shopizer-commons | 1.0.6 | direct (sm-core) | **INCOMPLETE** (compatibility not evidenced) |
| org.kie:kie-ci, org.kie:kie-spring, org.drools:drools-* | 7.32.0.Final | direct (sm-core) | javax-based (cdi-api, stax-api) – **INCOMPLETE** |
| org.infinispan:infinispan-core, infinispan-tree | 9.4.18.Final | direct (sm-core) | javax.transaction – **INCOMPLETE** |
| io.jsonwebtoken:jjwt | 0.8.0 | direct (sm-shop) | compatibility with Java 17 / Jakarta not evidenced – **INCOMPLETE** |
| com.zaxxer:HikariCP | 4.0.3 | transitive (spring-boot-starter-jdbc) | requests org.slf4j:slf4j-api 2.0.0-alpha1 (managed to 1.7.36); SB 3 manages a newer HikariCP |

## 4. Version conflicts in the resolved graph (`omitted for conflict`, sm-core and sm-shop)

The losing versions are always transitive; the winner is the nearest declaration (Maven "nearest wins"). Some also appear in sm-shop-model via springfox-swagger2 → guava; the full chains are in `reports/dependency-summary.md`. None is a Spring Boot artifact.

| Artifact | Resolved | Requested (omitted) | Origin |
|---|---|---|---|
| com.google.api.grpc:proto-google-common-protos | 1.15.0 | 1.11.0 | google-cloud-storage 1.74.0 |
| com.google.api:api-common | 1.8.1 | 1.7.0, 1.5.0 | google-cloud-storage 1.74.0 |
| com.google.code.findbugs:jsr305 | 3.0.2 | 1.3.9 | com.google.guava:guava 27.1-jre (nearest) |
| com.google.errorprone:error_prone_annotations | 2.2.0 | 2.10.0, 2.3.2 | com.google.guava:guava 27.1-jre (nearest) |
| com.google.http-client:google-http-client | 1.21.0 (downgrade) | 1.29.1, 1.29.0, 1.28.0 | com.maxmind.geoip2:geoip2 2.7.0 (nearest) wins over google-cloud-storage |
| com.google.http-client:google-http-client-jackson2 | 1.29.0 | 1.28.0 | google-cloud-storage 1.74.0 |
| com.google.protobuf:protobuf-java | 3.6.1 (sm-core) / 3.11.4 (sm-shop) | 3.5.1, 3.7.1, 3.11.4 / 3.5.1, 3.6.1, 3.7.1 | google-cloud / opensearch chains – differs per module |
| commons-logging:commons-logging | 1.2 | 1.1.1, 1.1.3 | see logging-bindings.md (C1) |
| joda-time:joda-time | 2.4 | 2.10.12, 2.8.1 | com.google.maps:google-maps-services 0.1.6 (nearest) |
| org.apache.maven.wagon:wagon-provider-api | 3.0.0 | 2.10, 1.0 | org.kie:kie-ci 7.32.0.Final |
| org.checkerframework:checker-qual | 2.5.2 | 3.19.0 | com.google.guava:guava 27.1-jre (nearest) |
| org.codehaus.plexus:plexus-component-annotations | 1.6 | 1.5.5 | org.kie:kie-ci |
| org.codehaus.plexus:plexus-utils | 3.0.22 | 3.0.24, 1.5.5, 3.0.17 | org.kie:kie-ci |
| org.eclipse.aether:aether-api | 1.1.0 | 1.0.2.v20150114 | org.kie:kie-ci |
| org.eclipse.aether:aether-impl | 1.1.0 | 1.0.2.v20150114 | org.kie:kie-ci |
| org.eclipse.aether:aether-spi | 1.1.0 | 1.0.2.v20150114 | org.kie:kie-ci |
| org.eclipse.aether:aether-util | 1.1.0 | 1.0.2.v20150114 | org.kie:kie-ci |
| org.json:json | 20211205 | 20160810 | direct in sm-core-model (inherited by all modules) |
| org.sonatype.plexus:plexus-cipher | 1.7 | 1.4 | org.kie:kie-ci |
