# S1 – Logging binding evaluation (FR-S1-009, FR-S1-010, S1-AC5)

Story `b2ec327d-0260-5051-b2b9-e8e046c072a4`. Source data: `reports/dependency-tree-*.txt` and the
"Logging implementations and bridges" / "SLF4J" / "Logback" sections of `reports/dependency-summary.md`.
Applies to all in-scope modules (see README); `sm-core` and `sm-shop` are the modules that would host the
monitoring packages.

## Result

* **SLF4J API:** `org.slf4j:slf4j-api:1.7.36` (managed by Spring Boot 2.5.12; `com.zaxxer:HikariCP:4.0.3`
  requests `2.0.0-alpha1`, managed down to 1.7.36).
* **SLF4J bindings:** exactly one – `ch.qos.logback:logback-classic:1.2.11` (+ `ch.qos.logback:logback-core:1.2.11`),
  transitive via `org.springframework.boot:spring-boot-starter-logging:2.5.12` in every module.
  No `slf4j-simple`, `slf4j-log4j12`, `slf4j-reload4j`, `slf4j-jdk14`, `slf4j-nop` or
  `log4j-slf4j-impl` is resolved, so there is **no multiple-SLF4J-binding conflict**.
* **Conflict found:** duplicate Jakarta Commons Logging (JCL) implementations of the
  `org.apache.commons.logging` package in `sm-core` and `sm-shop` (C1 below).

## Inventory

| Artifact (groupId:artifactId) | Version | Role | Modules | Introduced by | Conflict? |
|---|---|---|---|---|---|
| ch.qos.logback:logback-classic | 1.2.11 | SLF4J binding | all | spring-boot-starter-logging 2.5.12 | No (only binding) |
| ch.qos.logback:logback-core | 1.2.11 | Logback core | all | logback-classic 1.2.11 | No |
| org.apache.logging.log4j:log4j-to-slf4j | 2.17.2 | Log4j2 API → SLF4J bridge | all | spring-boot-starter-logging 2.5.12 | No (expected bridge) |
| org.apache.logging.log4j:log4j-api | 2.17.2 | Log4j2 API (no log4j-core present) | all | log4j-to-slf4j 2.17.2 | No |
| org.slf4j:jul-to-slf4j | 1.7.36 | java.util.logging → SLF4J bridge | all | spring-boot-starter-logging 2.5.12 | No (expected bridge) |
| org.springframework:spring-jcl | 5.3.18 | JCL implementation (Spring) | all | spring-core 5.3.18 | **C1** |
| org.slf4j:jcl-over-slf4j | 1.7.36 | JCL → SLF4J bridge | sm-core, sm-shop | org.kie:kie-ci 7.32.0.Final | **C1** |
| commons-logging:commons-logging | 1.2 (1.1.1, 1.1.3 omitted for conflict) | Original JCL | sm-core, sm-shop | commons-validator:commons-validator 1.5.1 (1.2); commons-beanutils:commons-beanutils 1.9.2 (1.1.1); com.amazonaws:aws-java-sdk-core 1.11.640 via aws-java-sdk-s3 (1.1.3) | **C1** |
| org.jboss.logging:jboss-logging | 3.4.3.Final | Logging facade used by Hibernate; delegates to SLF4J/Logback | all | hibernate-core / hibernate-ehcache 5.4.33 | No |

## C1 – duplicate `org.apache.commons.logging` providers (sm-core, sm-shop)

`org.springframework:spring-jcl:5.3.18`, `org.slf4j:jcl-over-slf4j:1.7.36` and
`commons-logging:commons-logging:1.2` all ship classes in `org.apache.commons.logging`. Which `LogFactory`
wins depends on classpath order. Recommended exclusions for S3 (keep `spring-jcl`, which Spring Boot 3 also uses):

| Exclude | From | Level | Note |
|---|---|---|---|
| commons-logging:commons-logging | commons-validator:commons-validator | **parent POM** (`pom.xml` `<dependencyManagement>`, which already declares exclusions for this dependency) | covers 1.2 and the 1.1.1 path via commons-beanutils |
| commons-logging:commons-logging | com.amazonaws:aws-java-sdk-s3, com.amazonaws:aws-java-sdk-ses | **parent POM** `<dependencyManagement>` | covers 1.1.3 via aws-java-sdk-core |
| org.slf4j:jcl-over-slf4j | org.kie:kie-ci | **parent POM** `<dependencyManagement>` (already declares an exclusion list for kie-ci) | spring-jcl already routes JCL to SLF4J |

Alternative (if a module needs a different choice): put the same exclusions on the dependency in the
**module POM** (`sm-core/pom.xml`); `sm-shop` inherits them transitively.

## Spring Boot 3.x notes (informational, for S3)

Spring Boot 3.2.12 manages `org.slf4j:slf4j-api` 2.0.16 and `ch.qos.logback:logback-classic` 1.4.14
(`log4j2.version` 2.21.1). SLF4J 2.x discovers providers through `ServiceLoader`; SLF4J 1.7-style bindings are
ignored. After the upgrade, re-run `scripts/s1/dependency-audit.sh` and `test-s1-acceptance.sh S1-AC5` to confirm
that `logback-classic` is still the only binding.
