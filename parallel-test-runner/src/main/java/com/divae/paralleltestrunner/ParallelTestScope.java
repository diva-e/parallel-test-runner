package com.divae.paralleltestrunner;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import com.divae.paralleltestrunner.ParallelTestRunner.ParallelTestThread;

/**
 * {@link Scope} implementation to allow bean instantiation by thread.
 *
 */
public class ParallelTestScope implements Scope {

	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		ParallelTestThread testThread = currentParallelTestThread();

		Object bean = testThread.getBean(name);
		if (bean == null) {
			bean = objectFactory.getObject();
			testThread.addBean(name, bean);
		}
		return bean;
	}

	@Override
	public Object remove(String name) {
		return currentParallelTestThread().removeBean(name);
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		currentParallelTestThread().registerDestructionCallback(name, callback);
	}

	@Override
	public Object resolveContextualObject(String key) {
		return currentParallelTestThread();
	}

	@Override
	public String getConversationId() {
		return currentParallelTestThread().getName();
	}

	private ParallelTestThread currentParallelTestThread() {
		return (ParallelTestThread) Thread.currentThread();
	}

}
