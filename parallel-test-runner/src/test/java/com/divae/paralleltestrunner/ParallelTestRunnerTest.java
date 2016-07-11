package com.divae.paralleltestrunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;

@RunWith(ParallelTestRunner.class)
@ContextConfiguration
public class ParallelTestRunnerTest {

	private static AtomicInteger firstCounter = new AtomicInteger();
	private static AtomicInteger secondCounter = new AtomicInteger();

	@Test
	public void firstTestMethod() throws InterruptedException {
		firstCounter.incrementAndGet();
	}

	@Test
	public void secondTestMethod() throws InterruptedException {
		secondCounter.incrementAndGet();
	}

	@AfterClass
	public static void assertCounters() {
		assertThat(firstCounter.get(), is(15));
		assertThat(secondCounter.get(), is(15));
	}

}
