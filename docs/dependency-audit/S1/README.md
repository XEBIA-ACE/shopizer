# S1 – Java 17/21 build environment and dependency audit

Story `b2ec327d-0260-5051-b2b9-e8e046c072a4` · FR-S1-001..FR-S1-012 · S1-AC1..S1-AC6 ·
grounding `ee3892481247ba19` · accepted impact `sha256:d902b5781ec11fb85317c9711a980454011769f03d3605c2460f0405a621de86`

| Artifact | Purpose |
|---|---|
| `reports/dependency-tree-<module>.txt` | Resolved dependency graph per module (`mvn dependency:tree`) – FR-S1-005 |
| `reports/dependency-tree-verbose-<module>.txt` | Same graph with omitted duplicates/conflicts (`-Dverbose`, maven-dependency-plugin 3.6.1) – FR-S1-005, FR-S1-007 |
| `reports/dependency-summary.md` | Generated tables: Spring Boot, AspectJ, SLF4J, Logback, JUnit 5, javax/jakarta, logging, conflicts – FR-S1-005..FR-S1-009 |
| [spring-boot-3-migration-conflicts.md](spring-boot-3-migration-conflicts.md) | Spring Boot 2.x → 3.x conflicts and javax.* → jakarta.* mapping – FR-S1-007, FR-S1-008 |
| [logging-bindings.md](logging-bindings.md) | SLF4J/logging binding evaluation and exclusion candidates for S3 – FR-S1-009, FR-S1-010 |
| `scripts/s1/dependency-audit.sh` | Regenerates everything under `reports/` (deterministic, no timestamps) – FR-S1-006 |
| `scripts/s1/check-build-warnings.sh` | Repeatable warning gate for the build – FR-S1-011, FR-S1-012 |
| `scripts/s1/test-s1-acceptance.sh` | Acceptance tests `test_S1_AC1` .. `test_S1_AC6` |

The report location `docs/dependency-audit/S1/reports` is fixed and versioned in git, so the S1 run is
identified by the commit that contains it (FR-S1-006). `scripts/s1/test-s1-acceptance.sh S1-AC3` fails if the
committed report is stale.

## Build configuration change (FR-S1-001, FR-S1-002)

* `pom.xml`: `java.version` 11 → **17**; `maven.compiler.source`/`maven.compiler.target` and the
  `maven-compiler-plugin` `<source>`/`<target>` are derived from it and inherited by every module.
* `sm-core-model/pom.xml`: removed the module-local `java.version=11` override so the module inherits 17.
* `sm-shop-model/pom.xml`: `maven-javadoc-plugin` `<source>` uses `${java.version}` instead of a hard-coded 11.

Java 17 was chosen (not 21) because it is the minimum required by Spring Boot 3.x and the bytecode level that
runs on both JDK 17 and JDK 21; the build is verified on both JDKs (FR-S1-003, FR-S1-004).

**In-scope "monitoring" modules (FR-S1-002).** The packages named by FR-S1-011
(`com.salesmanager.core.business.monitoring`, `com.salesmanager.shop.monitoring`) do not exist yet in this
repository. They would live in `sm-core` and `sm-shop`, which depend on `sm-core-model`, `sm-core-modules` and
`sm-shop-model`, so **all reactor modules** are treated as in scope: `shopizer` (root), `sm-core-model`,
`sm-core-modules`, `sm-core`, `sm-shop-model`, `sm-shop`.

Verify with `scripts/s1/test-s1-acceptance.sh S1-AC1` (reads `mvn help:effective-pom`) or manually:

```bash
mvn -q org.apache.maven.plugins:maven-help-plugin:3.5.1:effective-pom -Doutput=build/s1-logs/effective-pom.xml
```

## Verification results (this commit)

| Check | JDK 17.0.19 | JDK 21.0.13 |
|---|---|---|
| `mvn -B clean compile` (S1-AC2) | BUILD SUCCESS, exit 0 | BUILD SUCCESS, exit 0 |
| Class file major version of compiled classes | 61 (Java 17) | 61 (Java 17) |
| `[WARNING]` lines in Maven output (S1-AC6) | 0 | 0 |

Baseline before S1 (Java 11 target, JDK 21, `mvn -B clean compile`): BUILD SUCCESS with **0** `[WARNING]`
lines. The only compiler notes, before and after, are javac `[INFO]` notes about deprecated/removal API usage in
`sm-shop` (`ReadableOrderPopulator.java`, `ProductItemsFacadeImpl.java`); they are not language-level,
compiler-configuration or plugin warnings and are not in monitoring packages (FR-S1-011).

## Warning gate mechanism (FR-S1-012)

`scripts/s1/check-build-warnings.sh` runs `mvn -B clean compile` with the current `JAVA_HOME`, stores the log in
`build/s1-logs/compile-jdk<major>.log` and fails when either:

* a `[WARNING]`/`WARNING:` line matches the language-level/compiler/plugin pattern `LEVEL_RE`
  (`source value`, `target value`, `bootstrap classpath`, `obsolete`, `--release`, `compiler`,
  `Plugin validation`, `deprecated … plugin|mojo|parameter|goal`, …), or
* a `[WARNING]` line references a monitoring package (`MONITORING_RE`:
  `com[./]salesmanager[./]core[./]business[./]monitoring|com[./]salesmanager[./]shop[./]monitoring`).

```bash
JAVA_HOME=/path/to/jdk17 scripts/s1/check-build-warnings.sh
JAVA_HOME=/path/to/jdk21 scripts/s1/check-build-warnings.sh
```

## Running the acceptance tests

```bash
scripts/s1/test-s1-acceptance.sh                 # all of S1-AC1..S1-AC6
scripts/s1/test-s1-acceptance.sh S1-AC3 S1-AC4   # a subset
JAVA17_HOME=... JAVA21_HOME=... scripts/s1/test-s1-acceptance.sh S1-AC2
```

Requires Maven 3.9+, Python 3 and (for S1-AC2/S1-AC6) a JDK 17 and a JDK 21. No test command is declared for
this repository in `darkfactory.yml`, so these tests are not run by the platform automatically.

## Gaps, assumptions and deviations

| # | Impacted IDs | Item | Status |
|---|---|---|---|
| G1 | FR-S1-001, FR-S1-002, S1-AC1 | The accepted impact analysis (`sha256:d902b578…`, grounding `ee3892481247ba19`) only grounds `sm-shop/src/main/java/com/salesmanager/shop/utils/FilePathUtils.java` and an unknown caller; it reports that no `pom.xml` was found. The repository does contain `pom.xml`, `sm-core-model/pom.xml` and `sm-shop-model/pom.xml`, and FR-S1-001 cannot be met without editing them. The three edits listed above are the minimum required and are made **outside the grounded impact scope**. | Implemented, flagged for review |
| G2 | FR-S1-003, FR-S1-004, S1-AC2 (CI obligation) | `.circleci/config.yml` builds with the `shopizerecomm/ci:java11` image. With a Java 17 target this image cannot compile the project. A Java 17/21 image tag is not evidenced; CI is not grounded by the impact analysis, so it was **not changed**. Needs a CI image with JDK 17 or 21. | Open |
| G3 | FR-S1-003 (runtime) | `sm-shop/Dockerfile` uses `adoptopenjdk/openjdk11-openj9:alpine`; Java 17 class files (major 61) will not run on it. Not grounded, **not changed**. Needs a JRE 17/21 base image. | Open |
| G4 | FR-S1-011, FR-S1-012, S1-AC6 | The monitoring packages do not exist, so the "monitoring package" part of the gate is vacuously satisfied today; the gate is also applied to the whole build so it remains meaningful. | Assumption |
| G5 | S1-AC3 | Size of the audited graph: 343 unique `groupId:artifactId:version` coordinates across all modules (889 resolved nodes counted per module, see `reports/dependency-summary.md`). No reference figure is evidenced to compare against. | Informational |
| G6 | T001..T018 | The task list and `plan.md` under `specs/b2ec327d-0260-5051-b2b9-e8e046c072a4` are not present in this repository, so work is traced to FR/AC IDs only. | Open |
| G7 | FR-S1-007 | Spring Boot 3.x candidate versions are taken from `org.springframework.boot:spring-boot-dependencies:3.2.12` (the BOM pom, not applied to the build). The actual 3.x target is a later-story decision. | Assumption |

## Rollback

All build changes are confined to three POM properties/values. Reverting this commit (or setting
`java.version` back to 11 in `pom.xml` and `sm-core-model/pom.xml`) restores the previous Java 11 build;
the reports and scripts are additive and have no effect on the build.
