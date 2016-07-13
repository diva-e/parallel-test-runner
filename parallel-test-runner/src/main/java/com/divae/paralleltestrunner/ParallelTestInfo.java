package com.divae.paralleltestrunner;

/**
 * Can be used to retrieve some information about the currently running test
 * execution.
 */
public interface ParallelTestInfo {

	int getCurrentThread();

	int getCurrentRepetition();

	boolean isFirstRepetition();

}