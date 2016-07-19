package com.divae.paralleltestrunner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.junit.runners.model.Statement;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * This {@link Runner} implementation is based on Spring's
 * {@link SpringJUnit4ClassRunner}. It can execute tests multiple times
 * ({@link #numberOfRepetitions}) and in parallel ({@link #numberOfThreads}). A
 * configured Spring setup is required. E.g.:
 * 
 * <pre>
 * <i>ExampleTest.java:</i>
 * <code class="java">
 * <b>&#064;RunWith(ParallelTestRunner.class)</b>
 * <b>@ContextConfiguration("spring-context.xml")</b>
 * public class ExampleTest {
 *     ... 
 * }
 * </code>
 * <i>spring-context.xml:</i>
 * <code class="xml">
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;beans xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 *     xmlns="http://www.springframework.org/schema/beans"
 *     xmlns:context="http://www.springframework.org/schema/context"
 *     xsi:schemaLocation="
 *         http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
 *         http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd
 *     "&gt;
 *
 *    &lt;context:property-placeholder location="test.properties" /&gt;
 *    
 * &lt;/beans&gt;
 * </code>
 * <i>test.properties:</i>
 * <code class="properties">
 * test.threads = 2
 * test.repetitions = 3 
 * </code>
 * </pre>
 * 
 * And the Spring context needs a configured
 * {@link PropertyPlaceholderConfigurer} with at least to properties being
 * replaced:
 * <ul>
 * <li>{@code test.threads}: the number of parallel executions
 * <li>{@code test.repetitions}: the number of repeated executions
 * </ul>
 */
public class ParallelTestRunner extends SpringJUnit4ClassRunner {

	private static final String TEXT_CONTEXT_FIELD_IN_SPRING_JUNIT4_CLASS_RUNNER = "testContext";

	@Value("${test.threads}")
	private int numberOfThreads = 1;

	@Value("${test.repetitions}")
	private int numberOfRepetitions = 1;

	public ParallelTestRunner(Class<?> testClass, RunnerBuilder runnerBuilder) throws InitializationError {
		super(testClass);

		injectOwnDependencies();
	}

	private void injectOwnDependencies() {
		TestContext testContext = (TestContext) ReflectionTestUtils.getField(getTestContextManager(),
				TEXT_CONTEXT_FIELD_IN_SPRING_JUNIT4_CLASS_RUNNER);
		ApplicationContext context = testContext.getApplicationContext();
		context.getBean(AutowiredAnnotationBeanPostProcessor.class).processInjection(this);
	}

	@Override
	protected TestContextManager createTestContextManager(Class<?> testClass) {
		return new SynchronizedTestContextManager(testClass);
	}

	@Override
	protected Description describeChild(FrameworkMethod method) {
		Description description = Description.createTestDescription(createPseudoClassName(method), testName(method),
				method.getAnnotations());

		for (int thread = 0; thread < numberOfThreads; thread++) {
			Description threadDescription = createThreadDescription(method, thread);
			for (int repetition = 0; repetition < numberOfRepetitions; repetition++) {
				threadDescription.addChild(createRepetitionDescription(method, thread, repetition));
			}
			description.addChild(threadDescription);
		}

		return description;
	}

	private String createPseudoClassName(FrameworkMethod method) {
		return getTestClass().getName() + "." + testName(method);
	}

	private Description createThreadDescription(FrameworkMethod method, int thread) {
		return Description.createTestDescription(createPseudoClassName(method) + ", thread=" + thread, testName(method),
				method.getAnnotations());
	}

	private Description createRepetitionDescription(FrameworkMethod method, int thread, int repetition) {
		return Description.createTestDescription(createPseudoClassName(method),
				testName(method) + ", thread=" + thread + ", repetition=" + repetition, method.getAnnotations());
	}

	@Override
	protected void runChild(FrameworkMethod method, RunNotifier notifier) {
		Thread[] threads = new Thread[numberOfThreads];

		spawnTestThreads(threads, method, notifier);
		waitForTestThreadsToFinish(threads);
	}

	private void spawnTestThreads(Thread[] threads, FrameworkMethod method, RunNotifier notifier) {
		for (int thread = 0; thread < threads.length; thread++) {
			threads[thread] = new ParallelTestThread(method, notifier, thread);
			threads[thread].start();
		}
	}

	private void waitForTestThreadsToFinish(Thread[] threads) {
		for (int thread = 0; thread < threads.length; thread++) {
			try {
				threads[thread].join();
			} catch (InterruptedException e) {
				throw new IllegalStateException("error waiting for future to finish processing of test", e);
			}
		}
	}

	final class ParallelTestThread extends Thread {

		private final FrameworkMethod method;
		private final RunNotifier notifier;
		private final int currentThread;
		private int currentRepetition;

		private final Map<String, Object> beans = new ConcurrentHashMap<>();
		private final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<>();

		private ParallelTestThread(FrameworkMethod method, RunNotifier notifier, int thread) {
			super("TestThread for " + method.getName() + " number " + thread);
			this.method = method;
			this.notifier = notifier;
			this.currentThread = thread;
		}

		@Override
		public void run() {
			for (int repetition = 0; repetition < numberOfRepetitions; repetition++) {
				currentRepetition = repetition;
				try {

					executeRepetition(repetition);

				} finally {
					
					cleanUpBeans();
					
				}
			}
		}

		private void executeRepetition(int repetition) {
			Description description = createRepetitionDescription(method, currentThread, repetition);
			if (isTestMethodIgnored(method)) {
				notifier.fireTestIgnored(description);
			} else {
				Statement statement;
				try {
					statement = methodBlock(method);
				} catch (Throwable ex) {
					statement = new Fail(ex);
				}
				runLeaf(statement, description, notifier);
			}
		}
		
		private void cleanUpBeans() {
			synchronized (destructionCallbacks) {
				for (Entry<String, Runnable> callback : destructionCallbacks.entrySet()) {
					callback.getValue().run();
				}
				destructionCallbacks.clear();
			}
			beans.clear();
		}

		public int getCurrentThread() {
			return currentThread;
		}

		public int getCurrentRepetition() {
			return currentRepetition;
		}

		public void addBean(String name, Object bean) {
			beans.put(name, bean);
		}

		public Object getBean(String name) {
			return beans.get(name);
		}

		public Object removeBean(String name) {
			return beans.remove(name);
		}

		public void registerDestructionCallback(String name, Runnable callback) {
			synchronized (destructionCallbacks) {
				destructionCallbacks.put(name, callback);
			}
		}

	}
}
