package org.eclipse.gemini.blueprint.iandt.testingFramework;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

public class BundleCreationTstListener extends RunListener {
	private int started;
	private int finished;
	private int failed;
	private int ignored;

	@Override
	public void testStarted(Description description) {
		started++;
		System.out.println(description.getMethodName() + " started");
	}

	@Override
	public void testFinished(Description description) {
		finished++;
		System.out.println(description.getMethodName() + " finished");
	}

	@Override
	public void testFailure(Failure failure) {
		failed++;
		System.out.println(failure.getDescription().getMethodName() + " failed");
	}
	
	@Override
	public void testAssumptionFailure(Failure failure) {
		failed++;
		System.out.println(failure.getDescription().getMethodName() + " failed");
    }

	@Override
	public void testIgnored(Description description) throws Exception {
		ignored++;
		System.out.println(description.getMethodName() + " ignored");
	}

	public int getStarted() {
		return started;
	}

	public int getFinished() {
		return finished;
	}

	public int getFailed() {
		return failed;
	}

	public int getIgnored() {
		return ignored;
	}
}
