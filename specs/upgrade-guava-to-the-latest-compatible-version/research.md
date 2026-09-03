## Authoritative Input Provenance
- Repository ID: `56de811a-7ba9-4a18-9b0e-e34dabc504a4/8da0e46c-5391-4c4c-9f9a-541ed70e7a27`
- Expected branch: `3.2.7`
- Code Insights grounded: `True`
- Index status: grounded context available
- Current-state source: Tech Analysis
- Target-state source: explicit Selected Upgrade Option changes only

### Evidence Gaps
- No verified target was supplied for Java (current: `11`); it is omitted from Target State.
- No verified target was supplied for Build tool (current: `Maven`); it is omitted from Target State.
- No verified target was supplied for Package manager (current: `Maven`); it is omitted from Target State.
- The target `latest compatible version` for Upgrade Elasticsearch is non-specific; exact version selection and compatibility verification are required.
- The target `latest compatible version` for Upgrade Guava is non-specific; exact version selection and compatibility verification are required.
- The target `latest compatible version` for Upgrade Jackson Databind is non-specific; exact version selection and compatibility verification are required.
- The target `latest compatible version` for Upgrade Commons Lang is non-specific; exact version selection and compatibility verification are required.

---

### Research Documentation

#### Repository Identity and Setup Confirmation
- **Identity:** Confirmed via repo ID `56de811a-7ba9-4a18-9b0e-e34dabc504a4/8da0e46c-5391-4c4c-9f9a-541ed70e7a27` _(Unverified: no Code Insights evidence ID supplied.)_
- **Branch:** Verified on ref 3.2.7

#### Technology and Architecture Findings
- **Languages:** Java
- **Build Tool:** Maven
- **Key Dependencies:** Guava, Elasticsearch, Jackson Databind

#### Affected Components
- No dead code; active codebase
- High fan-in metrics on ServiceExceptions indicate tight coupling

#### Testing and Risk Analytics
- **Blast Radius Analysis**: _(Unverified: no Code Insights evidence ID supplied.)_
  - Very low risk (score: 0, band: LOW) associated with `pom.xml` changes _(Unverified: no Code Insights evidence ID supplied.)_

### Research Query Log

1. **Architecture Overview**
   - **Tool:** architecture_overview
   - **Parameters:** repo_id=[repo-id]
   - **Finding:** Confirmed structure and component interactions.

2. **Blast Radius** _(Unverified: no Code Insights evidence ID supplied.)_
   - **Tool:** get_blast_radius
   - **Finding:** Low risk for dependency changes in `pom.xml` _(Unverified: no Code Insights evidence ID supplied.)_

3. **Module Dependency Graph**
   - **Tool:** module_dependency_graph
   - **Finding:** Inter-module dependencies and critical paths identified.

4. **Dead Code Analysis**
   - **Tool:** find_dead_code
   - **Finding:** No dead code present in codebase.