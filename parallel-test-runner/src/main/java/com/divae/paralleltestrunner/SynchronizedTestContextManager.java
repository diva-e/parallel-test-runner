package com.divae.paralleltestrunner;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestExecutionListener;

public class SynchronizedTestContextManager extends TestContextManager {

	public SynchronizedTestContextManager(Class<?> testClass) {
		super(testClass);
	}

	@Override
	public synchronized void registerTestExecutionListeners(List<TestExecutionListener> testExecutionListeners) {
		super.registerTestExecutionListeners(testExecutionListeners);
	}

	@Override
	public synchronized void registerTestExecutionListeners(TestExecutionListener... testExecutionListeners) {
		super.registerTestExecutionListeners(testExecutionListeners);
	}

	@Override
	public synchronized void beforeTestClass() throws Exception {
		super.beforeTestClass();
	}

	@Override
	public synchronized void prepareTestInstance(Object testInstance) throws Exception {
		super.prepareTestInstance(testInstance);
	}

	@Override
	public synchronized void beforeTestMethod(Object testInstance, Method testMethod) throws Exception {
		super.beforeTestMethod(testInstance, testMethod);
	}

	@Override
	public synchronized void afterTestMethod(Object testInstance, Method testMethod, Throwable exception)
			throws Exception {
		super.afterTestMethod(testInstance, testMethod, exception);
	}

	@Override
	public synchronized void afterTestClass() throws Exception {
		super.afterTestClass();
	}

}
