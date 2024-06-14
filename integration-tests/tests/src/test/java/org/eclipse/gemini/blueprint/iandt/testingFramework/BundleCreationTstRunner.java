package org.eclipse.gemini.blueprint.iandt.testingFramework;

import java.lang.reflect.Method;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

public class BundleCreationTstRunner extends Runner {

	private BundleCreationTstListener listener;

	public BundleCreationTstRunner(BundleCreationTstListener listener) {
		super();
		this.listener = listener;
	}

	@Override
	public Description getDescription() {
		return Description.createTestDescription(BundleCreationTst.class, "BundleCreationTstRunner");
	}

	@Override
	public void run(RunNotifier notifier) {
		System.out.println("Running the tests from BundleCreationTstRunner: " + BundleCreationTst.class);
		notifier.addListener(listener);
		Object testObject;
		try {
			testObject = BundleCreationTst.class.newInstance();

			for (Method method : BundleCreationTst.class.getMethods()) {
				if (method.getName().startsWith("test")) {
					try {
						notifier.fireTestStarted(
								Description.createTestDescription(BundleCreationTst.class, method.getName()));
						method.invoke(testObject);
						notifier.fireTestFinished(
								Description.createTestDescription(BundleCreationTst.class, method.getName()));
					} catch (Exception e) {
						// we're ignoring assertions at this level and handling them in the listener
					}
				}
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
