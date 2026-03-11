# OSGi I18N - Project Documentation

## Project Overview


**Repository:** BlackBeltTechnology/osgi-i18n
**License:** Apache License 2.0
**Java Version:** 1.8 (source compatibility), 21 (build/runtime)
**Build System:** Maven with Maven Wrapper (`./mvnw`)

1. Provides internationalization (i18n) services for OSGi-based Java applications
2. Creates JDK dynamic proxies from user-defined interfaces, where each method call resolves to a localized message from `.properties` files
3. Supports enum message translation via a dedicated `EnumI18nService`
4. Uses Guava `LoadingCache` to lazily cache locale-specific message resolvers for performance
5. Integrates with OSGi Declarative Services for automatic service registration, configuration via Config Admin, and optional `LocaleProvider` injection

## Code Instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

## Directory Structure

```
osgi-i18n/
├── i18n-api/                  # API module — interfaces, annotations, SPI contracts
├── i18n-resourcebundle/       # Implementation module — properties-based resolution with caching
├── .github/workflows/         # GitHub Actions CI/CD pipelines
├── pom.xml                    # Parent POM (groupId: hu.blackbelt.osgi.i18n)
├── mvnw / mvnw.cmd            # Maven Wrapper scripts
└── logback-test.xml           # Shared test logging configuration
```

## Core Modules

| Module | Artifact ID | Type | Purpose |
|--------|-------------|------|---------|
| `i18n-api/` | `i18n-api` | OSGi bundle | Public API: `I18nService`, `EnumI18nService` interfaces; `MessageResolver`, `MessageStreamLoader`, `LocaleProvider` SPI; `@Message`, `@MessageByKey`, `@MessageByEnum` annotations; `I18NUtil` for BCP47 locale parsing |
| `i18n-resourcebundle/` | `i18n-impl` | OSGi bundle | Implementation: `I18nServiceImpl` and `EnumI18nServiceImpl` OSGi components; Guava-cached locale resolvers; `.properties` file loading with locale fallback chain; `SimpleParameterTemplater` for `{0}` placeholder substitution |

## Technology Stack

### Core Technologies
- **Java 1.8** — source/target compatibility level
- **OSGi R6** (6.0.0) — core framework, Declarative Services 1.3, Metatype annotations
- **Google Guava** (30.0-jre) — `LoadingCache` for lazy locale-based resolver caching (max 500 entries)
- **Lombok** (1.18.34) — `@Slf4j` logging, boilerplate reduction; excluded from bundle imports via `!lombok`

### Build & Quality
- **Maven** with Wrapper (targets Java 21 runtime)
- **maven-bundle-plugin** (5.1.2) — OSGi bundle packaging
- **JUnit 4** (4.12) + **JUnit Jupiter** (5.6.2) — unit testing
- **Mockito** (3.0.0) — mocking framework
- **hu.blackbelt.osgi.utils:osgi-test** (1.0.10) — `MockOsgi` utility for setting `@Reference` fields in tests
- **JaCoCo** (0.8.12) — code coverage reporting
- **Checkstyle** (8.10), **PMD** (6.3.0), **FindBugs** (3.0.1) — static analysis (configured in parent POM)
- **SonarQube** — quality metrics integration

## Build Commands

```bash
# Full build (compile, test, package, install)
./mvnw clean install

# Run all tests
./mvnw clean test

# Run a specific test class
./mvnw test -pl i18n-resourcebundle -Dtest=I18nServiceImplTest

# Run a specific test method
./mvnw test -pl i18n-resourcebundle -Dtest=I18nServiceImplTest#testChangeLanguage

# Generate coverage report
./mvnw clean test jacoco:report
# Report at: i18n-resourcebundle/target/site/jacoco/index.html
```

### Maven Profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Activates `i18n-api` and `i18n-resourcebundle` submodules (active by default unless `skipModules=true`) |
| `sign-artifacts` | Signs artifacts using `sign-maven-plugin` for release |
| `release-judong` | Deploys to JUDO Nexus (`nexus.judo.technology`) |
| `release-central` | Deploys to Maven Central via Sonatype OSSRH |
| `generate-github-asciidoc-diagrams` | Generates PNG diagrams from AsciiDoc files using AsciidoctorJ |
| `update-source-code-license` | Updates Apache 2.0 license headers in source files |

## Key Configuration Files

| File | Purpose |
|------|---------|
| `pom.xml` | Parent POM: shared dependencies, plugin config, profiles, version (`${revision}` = 1.1.0-SNAPSHOT) |
| `i18n-api/pom.xml` | API module POM, depends only on `osgi.core` |
| `i18n-resourcebundle/pom.xml` | Impl module POM, depends on `i18n-api`, Guava, OSGi DS/Metatype |
| `logback-test.xml` | Shared Logback config for test execution (referenced via `maven.multiModuleProjectDirectory`) |
| `.github/workflows/build.yml` | Main CI pipeline: build, deploy, tag, release |
| `.github/workflows/release.yml` | Manual release trigger: creates PRs for master and develop |

## Development Environment

**Required:**
- Java 21 JDK (source compiles to Java 8 bytecode)
- Maven 3.6+ (or use the included `./mvnw` wrapper)
- Git

**IDE Setup:**
- Enable Lombok annotation processing
- Import as Maven project
- Source level: Java 1.8

## Git Workflow

- **Main Branch:** `develop`
- **Release Branch:** `master`
- **Versioning:** `${revision}` property in parent POM (currently 1.1.0-SNAPSHOT), CI-friendly with `flatten-maven-plugin`
- **Branch Naming:** `feature/JNG-NUMBER_summary`, `bugfix/JNG-NUMBER_summary`, `hotfix/JNG-NUMBER_summary`
- **Commit Rule:** Every commit must reference a JIRA ticket (`JNG-xxx`)
- **CI:** GitHub Actions on self-hosted `judong` runner, 30-minute timeout, JDK 21 Zulu

## Important Notes

1. All OSGi bundles exclude Lombok from Import-Package (`!lombok`) — this is required in every module's `maven-bundle-plugin` configuration
2. The `I18nServiceImpl` registers proxies as OSGi services in the `BundleContext` — always call `unregister()` during cleanup to avoid service leaks
3. Properties files must be on the classpath at the path matching the interface's fully qualified name (dots → slashes), e.g. `hu/blackbelt/osgi/i18n/messages/FooMessages.properties`
4. Locale fallback in `LocalePropertiesFileLoader` tries variant → country → language → default (most specific first)
5. The Guava `LoadingCache` has a fixed max size of 500 locale entries — each unique locale creates a new `ClassBasedPropertiesFileMessageResolver`
6. `SimpleParameterTemplater` uses `{0}`, `{1}`, ... placeholders with regex-based replacement — not `java.text.MessageFormat`
7. Tests use `MockOsgi.setReference()` to inject OSGi `@Reference` fields without a running OSGi container
8. The `@Message` annotation supports both inline templates (`value`) and property key overrides (`key`) — if `key` is set, it takes precedence

## Related Documentation

- [README.md](README.md) — Project overview with architecture diagrams
- [CONTRIBUTING.md](CONTRIBUTING.md) — Contribution guidelines and development workflow
- [.github/CIFLOW.md](.github/CIFLOW.md) — Detailed CI/CD pipeline documentation with flow diagrams
