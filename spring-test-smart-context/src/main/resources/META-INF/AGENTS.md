# spring-test-smart-context: Notes for AI Coding Agents

This file is bundled in the `com.github.seregamorph:spring-test-smart-context` jar. It is meant for agents working
in a **consumer project** that uses this library in test scope. Full documentation:
https://github.com/seregamorph/spring-test-smart-context

## What the library does
- Reorders test classes so that classes sharing the same Spring `MergedContextConfiguration` run back to back.
  Non-integration (unit) test classes run first.
- Closes each Spring application context right after the last test class that uses it ("smart DirtiesContext").
  As a result, at most one context per test execution thread is alive at a time (Testcontainers beans etc. are
  released early).

## Setup (usually nothing beyond the dependency)
- Everything is auto-registered when the jar is on the test classpath:
  - JUnit Jupiter: `SmartDirtiesClassOrderer` via the bundled `junit-platform.properties`
    (`junit.jupiter.testclass.order.default`).
  - TestNG: `SmartDirtiesSuiteListener` via `META-INF/services/org.testng.ITestNGListener`.
  - Kotest: `SmartDirtiesSpecExecutionOrderExtension` (requires Kotest autoscan enabled).
  - `SmartDirtiesContextTestExecutionListener` via `META-INF/spring.factories`.
- With **TestNG**, integration test classes must additionally declare the listener, typically on a common base class
  (the annotation is inherited), or extend `AbstractTestNGSpringIntegrationTest`:
  ```java
  @TestExecutionListeners(listeners = SmartDirtiesContextTestExecutionListener.class,
      mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS)
  ```
- Requirements: Java 8+, Spring Framework 5.3+ / Spring Boot 2.4+, 3.x, 4.x. JUnit 4 is not supported (use version
  `0.15` for JUnit 4).

## Rules when editing tests in a consumer project
- Don't add `@DirtiesContext` only to free resources; the library already closes contexts at the right time.
  Keep `@DirtiesContext` only where a test really corrupts shared context state. Classes with
  `@DirtiesContext(classMode = BEFORE_CLASS)` are ordered first in their group, `AFTER_CLASS` last.
- Don't add `@TestClassOrder` or a custom `junit.jupiter.testclass.order.default`. They would override the smart
  orderer. Method ordering (`@TestMethodOrder`) is fine.
- Prefer reusing the same context configuration across test classes (for example a common abstract base class with
  `@SpringBootTest`, the same `@MockBean`/`@MockitoBean` set, profiles and properties). Each distinct configuration
  creates a new context.
- Don't rely on test class execution order (for example alphabetical). The library changes it.
- `@Nested` Jupiter classes are supported. Nested classes with a different configuration (own
  `@ContextConfiguration`, `@Import`, `@MockitoBean`, etc.) get their own context group.

## Pitfall: multiple `junit-platform.properties`
JUnit reads only one `junit-platform.properties` from the classpath, and this jar already contains one. If the
consumer project has its own `src/test/resources/junit-platform.properties`, add this line to it:
```properties
junit.jupiter.testclass.order.default=com.github.seregamorph.testsmartcontext.jupiter.SmartDirtiesClassOrderer
```
Otherwise tests fail with `IllegalStateException: engineClassOrderStateMap is not initialized, because more than one
junit-platform.properties was found in the classpath`. Alternatively, pass it as the system property
`-Djunit.jupiter.testclass.order.default=...` (also in the IDE run configuration).

## Reading the test output
- At suite start, the library prints the planned order to stdout:
  `Running suite of N integration test classes grouped and reordered by their MergedContextConfiguration (M groups):`
  Each `---` block is one context. Its annotations show which class `creates context` and which `closes context`.
  Many single-class groups indicate configurations that could be unified.
- Set the `com.github.seregamorph.testsmartcontext` logger to `INFO` to see `Creating context for ...`,
  `Created ... in ... for ...`, `Auto-closing context after ...` and `Destroying context for ...`. Use `DEBUG` to
  also see the list of integration and non-integration classes and context reuse.
- The warning `Test ... was not recognized as spring integration test by ...` means that a custom extension or
  listener activates Spring in a way the default filter doesn't detect. Provide a custom filter (see below).

## Customization (Java `ServiceLoader` SPI)
Register a subclass in the consumer test resources, `META-INF/services/<fully.qualified.BaseClass>`:
- `com.github.seregamorph.testsmartcontext.jupiter.JupiterIntegrationTestFilter`,
  `com.github.seregamorph.testsmartcontext.testng.TestNGIntegrationTestFilter`,
  `com.github.seregamorph.testsmartcontext.kotest.KotestIntegrationTestFilter`: override `isIntegrationTest` (and
  `isSpringTestClass` for Jupiter `@Nested` handling) to decide which classes are Spring integration tests.
  By default, Jupiter detects `@ExtendWith(SpringExtension.class)` (directly or as a meta-annotation, such as
  `@SpringBootTest`).
- `com.github.seregamorph.testsmartcontext.SmartDirtiesTestsSorter`: override `getNonItOrder()` (where unit tests
  go) or `initialSort(...)`.
- System property `-Dtestsmartcontext.reverse=true` reverses the initial alphabetical order. This is useful to detect
  hidden inter-test dependencies.

## Other utilities
- `com.github.seregamorph.testsmartcontext.jdbc.LateInitDataSource` is a `DataSource` decorator that resolves its
  target lazily (for example, it starts a Testcontainers database only on first use) and closes the target together
  with the context.

## Limitations
- With parallel test class execution, the number of live contexts is bounded by the number of threads, not 1.
- Don't call internal classes (`SmartDirtiesTestsSupport`, `CurrentTestContext`, etc.) from consumer code.
