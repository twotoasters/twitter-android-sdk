package com.sugree.twitter;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class AlreadyDone<V> implements Future<V> {

	private final V result;
	
	AlreadyDone(V result){
		this.result = result;
	}
	
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	public V get() throws InterruptedException,
			ExecutionException {
		return result;
	}

	public V get(long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException,
			TimeoutException {
		return result;
	}

	public boolean isCancelled() {
		return false;
	}

	public boolean isDone() {
		return true;
	}

}
