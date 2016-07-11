# Introduction
JUnit Runner implementation to run tests n times in parallel with m repetitions. 

# Installation
You can download the project and compile with Maven:

	~/parallel-test-runner $ mvn install

# Usage
## Add dependency
You can include the Parallel Test Runner into your pom.xml:

	<dependency>
		<groupId>com.divae.parallel-test-runner</groupId>
		<artifactId>parallel-test-runner</artifactId>
		<version>1.0-SNAPSHOT</version>
		<scope>test</scope>
	</dependency>


Currently you have to download the code and install the Parallel Test Runner into your local repository. We are trying to push the artifact into Maven Central soon.

## Use in your Test

Add the Parallel Test Runner to your JUnit Test:

	@RunWith(ParallelTestRunner.class)
	@ContextConfiguration("classpath:test-context.xml")
	public class TestWithParallelTestRunner {
		...	
	}

Create a spring context with configured property placeholder e.g. `test-context.xml` :

    <?xml version="1.0" encoding="UTF-8"?>
	<beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns="http://www.springframework.org/schema/beans"
		xmlns:context="http://www.springframework.org/schema/context"
		xsi:schemaLocation="
			http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
			http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
		">
	
		<context:property-placeholder
			location="classpath:test.properties" />
	
	</beans>

Configure the threads and repetitions in the properties file e.g. `test.properties`:

    test.threads = 3
    test.repetitions = 5
    
Run the test.

See [Spring documentation](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/integration-testing.html) for more information about configuring spring and tests.