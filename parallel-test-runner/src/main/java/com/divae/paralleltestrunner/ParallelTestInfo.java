package com.divae.paralleltestrunner;

public interface ParallelTestInfo {

	int getCurrentThread();

	int getCurrentRepetition();

	boolean isFirstRepetition();

}