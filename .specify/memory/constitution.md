## Authoritative Modernization Decision
- Selected option: Spring Boot 3.3 + Java 21 LTS Full Upgrade (`moderate`)
- Effort: 45 person-days
- Risk score: 6/10
- Blockers: javax → jakarta namespace migration touches all five Maven modules and all third-party libraries must also support jakarta namespace; verify Drools 7.32.0.Final compatibility with Jakarta EE — Drools 7.x uses javax namespace and may require upgrade to 8.x/9.x or a compatibility shim, Drools 7.32.0.Final is not compatible with Spring Boot 3.x / Jakarta EE; must evaluate upgrade path to Drools 8.x or Kogito before or alongside this migration, Infinispan 9.4.18.Final does not support Jakarta EE; must be upgraded to Infinispan 14.x or 15.x which supports Jakarta EE — verify Spring Boot 3.3.x integration, WebSecurityConfigurerAdapter removed in Spring Security 6.x; all security configuration classes must be rewritten, jjwt 0.8.0 → 0.12.x has breaking API changes; all JWT creation and parsing code must be refactored, Hibernate 6.x introduces breaking changes in HQL, Criteria API, and type mappings; full regression testing of persistence layer required, Very low test coverage (sm-shop: 4% line, 1% branch) significantly increases regression risk; recommend writing integration tests for critical paths before migration, MapStruct 1.3.x → 1.6.x may require annotation processor configuration updates in Maven
- Impacted areas: source code, CI/CD, infrastructure, tests, docs

---

# Constitution — Shopizer Spring Boot 3.3 + Java 21 LTS Upgrade

## Objective
Migrate the Shopizer headless commerce monolith (ref `3.2.5`, commit `052d2ed3c026525329405cef433c0aca7dd2cee3`) from Spring Boot 2.5.12 / Java 11 to Spring Boot 3.3.5 / Java 21 LTS, resolving all known EOL frameworks, critical and high CVEs, and cloud-native gaps identified in the tech analysis. _(Unverified: no Code Insights evidence ID supplied.)_

## Guiding Principles

1. **Evidence-first**: Every architectural claim must be grounded in a Code Insights tool result or explicitly labelled as upstream analysis evidence. No assumptions about file contents or symbol behaviour.
2. **Safety before speed**: The migration is executed in dependency-ordered phases. No phase begins until the previous phase's acceptance criteria are met and the build is green.
3. **Backward-compatible API surface**: The REST API contract (routes, request/response shapes) must not change as a result of this migration. Breaking changes require a separate versioning decision.
4. **Minimal blast radius per commit**: Each task targets the smallest coherent change unit. Large namespace migrations use automated tooling (OpenRewrite) to reduce manual error. _(Unverified: no Code Insights evidence ID supplied.)_
5. **Test coverage as a quality gate**: The sm-shop module's JaCoCo line coverage threshold is raised from 4% to 20% before the Spring Boot parent upgrade is merged to main.
6. **Security by default**: All CVE-bearing dependencies are upgraded before or alongside the Spring Boot parent upgrade. No known critical or high CVE may remain in the final dependency tree.
7. **No silent version invention**: Target versions are taken exclusively from the selected upgrade option or verified compatibility evidence. Missing targets are recorded as open questions.
8. **Preserve upstream facts**: Where Code Insights contradicts upstream analysis, both values are recorded and the conflict is noted. Upstream analysis evidence is retained and labelled.

## Constraints

- Java minimum: 17 (Spring Boot 3.3.5 requirement); selected target: 21 LTS.
- Spring Boot minimum: 3.0.0; selected target: 3.3.5.
- All five Maven modules (sm-core, sm-shop, sm-shop-model, sm-core-model, sm-core-modules) must compile and pass tests after each phase.
- Drools 7.32.0.Final is a hard blocker: it must be upgraded or shimmed before the javax→jakarta migration can complete.
- Infinispan 9.4.18.Final is a hard blocker: it must be upgraded to ≥14.0.0.Final before the javax→jakarta migration can complete.
- WebSecurityConfigurerAdapter is removed in Spring Security 6.x; all security configuration classes must be rewritten before the Spring Boot parent is bumped.
- The Docker base image must be replaced with `eclipse-temurin:21-jre-alpine` before any container build. _(Unverified: no Code Insights evidence ID supplied.)_
- The OWASP Dependency-Check Maven plugin must be added to CI before the final release.

## Measurable Quality Gates

| Gate | Metric | Threshold | Phase |
|------|--------|-----------|-------|
| QG-1 | sm-shop JaCoCo line coverage | ≥ 20% | Before Phase 3 |
| QG-2 | sm-shop JaCoCo branch coverage | ≥ 5% (current 1%) | Before Phase 3 |
| QG-3 | OWASP Dependency-Check CVSS threshold | No CRITICAL (≥9.0) | Phase 6 |
| QG-4 | All five modules compile with Java 21 | Zero compile errors | Phase 1 |
| QG-5 | All existing tests pass | Zero regressions | Each phase |
| QG-6 | Spring Boot 3.3.5 application starts | Actuator /health returns UP | Phase 3 |
| QG-7 | JWT authentication end-to-end | Token issued and validated | Phase 4 |
| QG-8 | Swagger UI accessible at /swagger-ui.html | HTTP 200 | Phase 5 |

## Decision Log

| ID | Decision | Rationale | Date |
|----|----------|-----------|------|
| D-1 | Upgrade Java to 21 LTS (not 17) | 21 is the current LTS; longer support window to 2031; virtual threads available | At spec creation |
| D-2 | Replace Springfox with springdoc-openapi 2.6.0 | Springfox is abandoned; CVE-2022-1471; incompatible with Spring Boot 3.x | At spec creation |
| D-3 | Upgrade Drools before javax→jakarta migration | Drools 7.x uses javax namespace; cannot coexist with Jakarta EE migration | At spec creation |
| D-4 | Upgrade Infinispan before javax→jakarta migration | Infinispan 9.x uses javax namespace; must reach ≥14.x for Jakarta EE | At spec creation |
| D-5 | Use OpenRewrite for namespace migration | Reduces manual error across 1,210 Java files in five modules | At spec creation |
| D-6 | Raise JaCoCo threshold before Spring Boot bump | sm-shop at 4% line / 1% branch is insufficient safety net for a major migration | At spec creation |
| D-7 | jackson-core and jackson-databind managed by Spring Boot 3.3.5 BOM | Remove explicit version pins; let BOM manage to 2.17.2 | At spec creation |