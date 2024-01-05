## Improving Spring Boot Test efficiency

### How it works
TODO

### Supported versions
Java 8+

Spring Boot 2.1.0+, 3.x

TestNG 7.7.0+

JUnit 5 Jupiter TODO (work in progress)

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
```kotlin
testImplementation("com.github.seregamorph:spring-test-smart-context:0.1-SNAPSHOT")
```
For projects with TestNG tests this will automatically setup
[SmartDirtiesSuiteListener](spring-test-smart-context/src/main/java/com/github/seregamorph/testsmartcontext/testng/SmartDirtiesSuiteListener.java) 
which will automatically reorder test classes and prepare the list of last test class per context configuration.
The integration test classes should add
[SmartDirtiesContextTestExecutionListener](spring-test-smart-context/src/main/java/com/github/seregamorph/testsmartcontext/SmartDirtiesContextTestExecutionListener.java):
```java
@TestExecutionListeners(SmartDirtiesContextTestExecutionListener.class)
```
Note: the annotation is inherited, so it makes sense to annotate the base test class or use
[AbstractTestNGSpringIntegrationTest](spring-test-smart-context/src/main/java/com/github/seregamorph/testsmartcontext/testng/AbstractTestNGSpringIntegrationTest.java)
parent.
