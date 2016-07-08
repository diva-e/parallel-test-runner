package com.divae.paralleltestrunner;

public class SimpleParallelTestInfo implements ParallelTestInfo {

	@Override
	public int getCurrentThread() {
		return 0;
	}

	@Override
	public int getCurrentRepetition() {
		return 0;
	}

	@Override
	public boolean isFirstRepetition() {
		return true;
	}

}
