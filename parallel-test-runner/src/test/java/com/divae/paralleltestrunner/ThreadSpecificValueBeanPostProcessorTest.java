package com.divae.paralleltestrunner;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(ParallelTestRunner.class)
@ContextConfiguration
public class ThreadSpecificValueBeanPostProcessorTest {

	@Autowired
	private ParallelTestInfo testInfo;

	@ThreadSpecificValue("${threadSpecificValueTest}")
	private String threadSpecificValue;

	private static Set<String> presentedValues = new ConcurrentSkipListSet<>();

	@Test
	public void testThreadSpecificValue() {
		if (testInfo.getCurrentThread() == 0) {
			assertThat(threadSpecificValue, is("thread 0"));
		} else {
			assertThat(threadSpecificValue, is("default"));
		}
		presentedValues.add(threadSpecificValue);
	}

	@AfterClass
	public static void assertPresentedValues() {
		assertThat(presentedValues, containsInAnyOrder("thread 0", "default"));
	}

}
