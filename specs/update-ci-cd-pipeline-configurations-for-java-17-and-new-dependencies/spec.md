## Authoritative Pipeline Facts
These facts are generated from Tech Analysis and the selected Upgrade Option and take precedence over narrative text.

### Current State
| Category | Component | Current value | Source |
| --- | --- | --- | --- |
| language | Java | 11 | Tech Analysis |
| runtime | Java | 11 | Tech Analysis |
| build_tool | Build tool | Maven | Tech Analysis |
| package_manager | Package manager | Maven | Tech Analysis |
| framework | Spring Boot | 2.5.12 | Tech Analysis |
| dependency | Elasticsearch | 7.5.2 | Tech Analysis |
| dependency | Google Maps Services | 0.1.6 | Tech Analysis |
| dependency | Swagger | 2.9.2 | Tech Analysis |

### Target State
| Component | Current | Explicit target | Source |
| --- | --- | --- | --- |
| Maven | 3.8.4 | 3.8.4 | Selected Upgrade Option |
| Java | 11 | 17 | Selected Upgrade Option |
| Upgrade Java | 11 | 17 | Selected Upgrade Option |
| Upgrade Spring Boot | 2.5.12 | 3.1.4 | Selected Upgrade Option |
| Upgrade Elasticsearch | 7.5.2 | 7.17.9 | Selected Upgrade Option |

## Authoritative Modernization Decision
- Selected option: Framework and Runtime Upgrade (`moderate`)
- Effort: 30 person-days
- Risk score: 6/10
- Blockers: Ensure all key dependencies are compatible with Java 17 and Spring Boot 3.1.4, Code refactoring for compatibility
- Impacted areas: source code, CI/CD, infrastructure, tests

### Open Questions
- Verify the target requirement for Build tool; current value `Maven` is intentionally omitted from Target State.
- Verify the target requirement for Package manager; current value `Maven` is intentionally omitted from Target State.

---

# Specification

## Summary
The modernization will involve upgrading the Java runtime to version 17, alongside an upgrade of Spring Boot to version 3.1.4, among other dependencies. This approach aims to leverage new language features, improve security, and maintain compatibility with future updates.

## Motivation
Upgrading ensures continued support and access to security patches, as Java 11 is nearing end-of-life. The latest Spring Boot provides performance improvements and aligns with current best practices.

## Repository Evidence
- Language: Java 11, with 1210 Java source files.
- Build tool: Maven with primary configurations.
- Existing significant dependencies include outdated Elasticsearch and AWS Java SDK S3.

## Current State
- Java 11 runtime.
- Spring Boot 2.5.12.
- Key dependencies include Elasticsearch 7.5.2.

## Target State
- Java 17 runtime.
- Spring Boot 3.1.4.
- Latest compatible versions of Elasticsearch and other dependencies.

## Compatibility Matrix
| Component      | Current Version | Target Version |
|----------------|-----------------|----------------|
| Java           | 11              | 17             |
| Spring Boot    | 2.5.12          | 3.1.4          |
| Elasticsearch  | 7.5.2           | TBD            |

## Scope
Includes source code modifications, CI/CD pipeline updates, and comprehensive testing to ensure seamless upgrades.

## Affected Components/Interfaces
- Primary backend services within the monolithic architecture.
- REST API integrations.

## Risks
- Possible incompatibility with existing integrations.
- Potential need for significant refactoring in areas dependent on legacy APIs.

## Out-of-Scope Items
- Frontend code updates are not included.
- No additional feature development besides upgrade compliance.

## Open Questions
- Are there any undiscovered dependencies within the codebase that are incompatible with Java 17?