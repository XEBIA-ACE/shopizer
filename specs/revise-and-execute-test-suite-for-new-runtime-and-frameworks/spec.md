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

# Specification Document

## Summary

## Motivation
The current tech stack is outdated, posing security risks and missing out on recent improvements. The identified upgrades are necessary to maintain industry standards and leverage new capabilities.

## Repository Evidence
Due to tooling limitations, the codebase's architecture details weren't fully extracted. Therefore, known configurations from tech analysis are relied upon.

## Current State
- **Runtime**: Java 11
- **Framework**: Spring Boot 2.5.12
- **Dependencies**: Elasticsearch 7.5.2, unversioned Hibernate

## Target State
- **Runtime**: Java 17 (latest LTS)
- **Framework**: Spring Boot 3.1.4
- **Dependencies**: Update Elasticsearch and Hibernate to compatible versions

## Compatibility Matrix
The upgrades maintain backward compatibility with existing infrastructure constraints but require revisions for API touchpoints and configuration files impacted by version changes.

## Scope
Focus on adapting all Java 17 updates in Java files, Spring Boot framework updates, and aligning Elasticsearch functionalities with the updated environment.

## Affected Components/Interfaces
- Source Code: Java classes interfacing with the runtime and framework
- Configuration: Maven build files updating versions
- Infrastructure: CircleCI config adjustments

## Compatibility and Breaking Changes
Code-level refactoring may be necessary for deprecated methods and API updates within both Java SE and Spring Boot reflecting changes from JDK 11 to 17 and Spring Boot 2.5 to 3.x API.

## Testable Acceptance Criteria
- Application compiles and runs on Java 17
- Integration tests pass on Spring Boot 3.1.4
- Elasticsearch services operate on updated configurations

## Risks
- Incompatibility with existing code configurations, requiring significant refactoring.
- Risk of downtime during upgrade phased releases.
- Dependency on Elasticsearch update availability matching system requirements.

## Out-of-Scope Items
- Frontend interface changes are not covered in this upgrade.

## Open Questions
- Real-time operational deployment mechanics, e.g., Kubernetes manifests not explicitly identified.