package com.divae.paralleltestrunner;

import com.divae.paralleltestrunner.ParallelTestRunner.ParallelTestThread;

public class ParallelTestInfoImpl implements ParallelTestInfo {

	private static final int FIRST_REPETITION = 0;

	@Override
	public int getCurrentThread() {
		return currentTestThread().getCurrentThread();
	}

	@Override
	public int getCurrentRepetition() {
		return currentTestThread().getCurrentRepetition();
	}

	@Override
	public boolean isFirstRepetition() {
		return getCurrentRepetition() == FIRST_REPETITION;
	}

	private ParallelTestThread currentTestThread() {
		return (ParallelTestThread) Thread.currentThread();
	}

}