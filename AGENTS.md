# Repository Guidelines

## Project Structure & Module Organization
- `spring-test-smart-context/` is the only published artifact. It targets **Java 8** bytecode
  (`maven.compiler.target=1.8`), so don't use newer language or API features. It mixes Java (`src/main/java`)
  and Kotlin (`src/main/kotlin`, Kotest support only).
- The core flow has two parts. A framework-specific orderer sorts test classes so that classes sharing a Spring
  context run back to back: `jupiter/SmartDirtiesClassOrderer`, `testng/SmartDirtiesSuiteListener`, or
  `kotest/SmartDirtiesSpecExecutionOrderExtension`. Then `SmartDirtiesContextTestExecutionListener` closes each
  context after the last class that uses it. Shared logic lives in `SmartDirtiesTestsSorter` and
  `SmartDirtiesTestsSupport`.
- Auto-registration goes through `META-INF/spring.factories` and `META-INF/services` (JUnit Platform
  `TestExecutionListener`, TestNG `ITestNGListener`). If you add or rename a listener, update these files too.
- `org/springframework/test/context/BootstrapUtilsHelper.java` lives in a Spring package on purpose so it can reach
  package-private API.
- `tests/` holds integration and demo modules that are never deployed. Each one pins a framework and version
  combination (Spring 5/7, Boot 2.4–4.1, Jupiter, TestNG, Kotest, mixed). `tests/tests-testkit` provides shared
  assertion helpers (`TestEventTracker`, `TestSmartDirtiesTestsHolder`). Some modules have `leakage-detector/`
  outputs.
- `tests/tests-gradle-junit-platform-jupiter-boot32` is a Gradle build. It's only included with
  `-P include-gradle-build` and resolves the SNAPSHOT from `mavenLocal`.

## Build, Test, and Development Commands
- `./mvnw clean install`: full build, matching CI. CI runs it on JDK 17 and 21.
- `./mvnw clean install -P include-gradle-build`: also builds the Gradle demo module.
- `./mvnw -pl spring-test-smart-context test -Dtest=SmartDirtiesSkippedClassListenerTest`: runs a single unit test.
- `./mvnw -pl tests/tests-maven-junit-platform-jupiter-boot35 -am verify`: runs a single demo module.
- **Build JDK**: the enforcer plugin requires JDK `[17,22)`.
- **Docker**: several `tests/*` modules use Testcontainers, so Docker must be running.
- **Test-result caching**: `.mvn/extensions.xml` enables `surefire-cached-extension` (configured in
  `.mvn/surefire-cached.json`), so unchanged modules can skip their tests. To run the build without the cache, add
  `-DskipCache`, for example `./mvnw clean install -DskipCache`.

## Coding Style & Naming Conventions
These rules come from `.editorconfig`:
- 4-space indent, 120-character line limit, UTF-8, final newline.
- Single-class imports only (no wildcards). Static imports go first, followed by a blank line and then the other
  imports.

Naming in demo modules:
- `*Test` classes run under Surefire.
- `*IT` classes run under Failsafe (the `integration-test` phase).

## Commit & Pull Request Guidelines
- Commit subjects are short, imperative and capitalized, with no trailing period (for example
  `Add Testcontainers to integration tests`). Conventional `fix:` prefixes appear occasionally.
- Dependabot commits use the `chore: bump X from A to B (#N)` format.
- PRs are validated by `.github/workflows/build.yml`. Dependency updates are configured in `.github/dependabot.yml`.
- When you bump the version or change a supported framework version, update `README.md`, which lists the supported
  versions and the dependency snippet.
