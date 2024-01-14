## Improving Spring Boot Test efficiency

### Problem statement and solution
The full presentation is available here: https://www.youtube.com/watch?v=_Vci_5nr8R0 ,
it explains the problem and the suggested solution.

Spring test framework creates an application context according to test class configuration.
The context is cached and reused for all subsequent tests. If there is an existing context
with the same configuration, it will be reused. Otherwise, the new context will be created.
This is a very efficient and flexible approach, but it has a drawback: eventually this may
lead to out of memory errors if the number of unique configurations is too high and context
has a lot of heavyweight beans like TestContainers. In many cases simple static bean 
definition can help, but this project suggests another approach: reordering test classes
and eager context cleanup.

### Supported versions
`Java` 8+

`Spring Boot` 2.1.0+, 3.x

`TestNG` 7.0.0+

`JUnit 5 Jupiter` TODO (work in progress)

`Gradle Enterprise Maven Extension` (test execution caching) correctly supports changed behaviour

### Limitations
At the moment only single thread test execution per module is supported.

### How to use
Add maven dependency (not yet available in maven central):
```xml
<dependency>
    <groupId>com.github.seregamorph</groupId>
    <artifactId>spring-test-smart-context</artifactId>
    <version>0.1-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```
Or Gradle dependency:
```groovy
testImplementation("com.github.seregamorph:spring-test-smart-context:0.1-SNAPSHOT")
```
For projects with TestNG tests this will automatically setup
[SmartDirtiesSuiteListener](spring-test-smart-context/src/main/java/com/github/seregamorph/testsmartcontext/testng/SmartDirtiesSuiteListener.java) 
which will reorder test classes and prepare the list of last test class per context configuration.
The integration test classes should add
[SmartDirtiesContextTestExecutionListener](spring-test-smart-context/src/main/java/com/github/seregamorph/testsmartcontext/SmartDirtiesContextTestExecutionListener.java):
```java
@TestExecutionListeners(SmartDirtiesContextTestExecutionListener.class)
```
Note: the annotation is inherited, so it makes sense to annotate the base test class or use
[AbstractTestNGSpringIntegrationTest](spring-test-smart-context/src/main/java/com/github/seregamorph/testsmartcontext/testng/AbstractTestNGSpringIntegrationTest.java)
parent.
