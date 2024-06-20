/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/

package org.eclipse.gemini.blueprint.internal.service.interceptor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.gemini.blueprint.service.importer.support.internal.support.DefaultRetryCallback;
import org.eclipse.gemini.blueprint.service.importer.support.internal.support.RetryCallback;
import org.eclipse.gemini.blueprint.service.importer.support.internal.support.RetryTemplate;

/**
 * 
 * @author Costin Leau
 * 
 */
public class RetryTemplateTest {

	private static class EventRecorderRetryTemplate extends RetryTemplate {

		private long missingTarget = 0;
		private long missingTargetInvocation = 0;
		private long successfulStop = 0;
		private long failedStop = 0;


		/**
		 * Constructs a new <code>EventRecorderRetryTemplate</code> instance.
		 * 
		 * @param waitTime
		 * @param notificationLock
		 */
		public EventRecorderRetryTemplate(long waitTime, Object notificationLock) {
			super(waitTime, notificationLock);
		}

		/**
		 * Constructs a new <code>EventRecorderRetryTemplate</code> instance.
		 * 
		 * @param notificationLock
		 */
		public EventRecorderRetryTemplate(Object notificationLock) {
			super(notificationLock);
		}

		protected void callbackFailed(long stop) {
			setFailedStop(stop);
		}

		protected void callbackSucceeded(long stop) {
			setSuccessfulStop(stop);
		}

		protected synchronized void onMissingTarget() {
			missingTargetInvocation++;
			setMissingTarget(System.currentTimeMillis());
		}

		/**
		 * Returns the missingTarget.
		 * 
		 * @return Returns the missingTarget
		 */
		public synchronized long getMissingTarget() {
			return missingTarget;
		}

		public synchronized long getMissingTargetInvocation() {
			return missingTarget;
		}

		/**
		 * @param missingTarget The missingTarget to set.
		 */
		public synchronized void setMissingTarget(long missingTarget) {
			this.missingTarget = missingTarget;
		}

		/**
		 * Returns the successfulStop.
		 * 
		 * @return Returns the successfulStop
		 */
		public synchronized long getSuccessfulStop() {
			return successfulStop;
		}

		/**
		 * @param successfulStop The successfulStop to set.
		 */
		public synchronized void setSuccessfulStop(long successfulStop) {
			this.successfulStop = successfulStop;
		}

		/**
		 * Returns the failedStop.
		 * 
		 * @return Returns the failedStop
		 */
		public synchronized long getFailedStop() {
			return failedStop;
		}

		/**
		 * @param failedStop The failedStop to set.
		 */
		public synchronized void setFailedStop(long failedStop) {
			this.failedStop = failedStop;
		}
	}


	private EventRecorderRetryTemplate template;
	private RetryCallback callback;
	private Object monitor;


	private static class CountingCallback implements RetryCallback<Object> {

		private int count = 0;
		public final static int WAKES_THRESHOLD = 7;


		public synchronized Object doWithRetry() {
			count++;
			return null;
		}

		public boolean isComplete(Object result) {
			// postpone completion X times
			if (getCount() == WAKES_THRESHOLD)
				return true;
			return false;
		}

		public synchronized int getCount() {
			return count;
		}
	}

	private static class FailingCallback implements RetryCallback<Object> {

		private static Object VALUE = new Object();


		public Object doWithRetry() {
			return VALUE;
		}

		public boolean isComplete(Object result) {
			return false;
		}

	}

	@Before
	public void setup() throws Exception {
		monitor = new Object();
		callback = new DefaultRetryCallback<Object>() {

			public Object doWithRetry() {
				return null;
			}
		};
	}

	@After
	public void tearDown() throws Exception {
		template = null;
	}

	// reset test - a separate thread reset a template that waits for a long time
	@Test
	public void testTemplateReset() throws Exception {
		long initialWaitTime = 20 * 1000;
		template = new EventRecorderRetryTemplate(initialWaitTime, monitor);

		long start = System.currentTimeMillis();

		Runnable shutdownTask = new Runnable() {

			public void run() {
				// wait a bit

				try {
					Thread.sleep(3 * 1000);
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				System.out.println("About to reset template...");
				template.reset(0);
				System.out.println("Resetted template...");
			}
		};

		Thread th = new Thread(shutdownTask, "shutdown-thread");
		th.start();
		assertNull(template.execute(callback));
		long stop = System.currentTimeMillis();

		long waitingTime = stop - start;
		assertTrue("Template not stopped in time", waitingTime < initialWaitTime);
	}

	// simple test that keeps waking up the template for a number of times
	// the callback counts the invocations and then returns nicely
	@Test
	public void testSpuriousWakeup() throws Exception {
		// wait 20s
		long initialWaitTime = 20 * 1000;
		final CountingCallback callback = new CountingCallback();
		template = new EventRecorderRetryTemplate(initialWaitTime, monitor);

		Runnable spuriousTask = new Runnable() {

			public void run() {
				try {
					// start sending notifications to the monitor
					do {
						// sleep for a while
						Thread.sleep(50);

						synchronized (monitor) {
							monitor.notifyAll();
						}
					} while (!callback.isComplete(null));
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		};

		Thread th = new Thread(spuriousTask, "wake-up thread");
		th.start();

		long start = System.currentTimeMillis();
		template.execute(callback);
		long stop = System.currentTimeMillis();
		long waited = stop - start;

		assertTrue(template.getMissingTarget() >= start);
		assertEquals(1, template.missingTargetInvocation);
		assertEquals("successful callback does not end in failure", 0, template.failedStop);

		assertEquals(CountingCallback.WAKES_THRESHOLD, callback.getCount());
		assertTrue(waited < initialWaitTime);
	}

	// test that checks the template keeps waiting until the waiting period elapses
	// if the callback returns falls even if there are (plenty) of wakeups
	@Test
	public void testFailingCallbackWithSpuriousWakeups() throws Exception {
		// wait 10 secs
		long initialWaitTime = 10 * 1000;
		template = new EventRecorderRetryTemplate(initialWaitTime, monitor);

		Runnable spuriousTask = new Runnable() {

			public void run() {
				try {
					// start sending notifications to the monitor
					do {
						// sleep for a while
						Thread.sleep(50);

						synchronized (monitor) {
							monitor.notifyAll();
						}
					} while (!callback.isComplete(null));
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		};

		Thread th = new Thread(spuriousTask, "wake-up thread");
		th.start();

		callback = new FailingCallback();
		long start = System.currentTimeMillis();
		assertNull("failing callback should always return null", template.execute(callback));
		long stop = System.currentTimeMillis();

		assertEquals("failed callback does not end succesful", 0, template.successfulStop);
		long waited = stop - start;

		assertTrue(waited >= template.getFailedStop());

		assertTrue(template.getMissingTarget() >= start);
		assertEquals(1, template.missingTargetInvocation);

		assertTrue(waited >= initialWaitTime - 3);
	}

	// test the retry with a thread that keeps waking up the template
	// then does a reset
	// the test checks the event method
	@Test
	public void testSpuriousWakeupWithReset() throws Exception {

		long initialWaitTime = 30 * 1000;
		template = new EventRecorderRetryTemplate(initialWaitTime, monitor);

		Runnable spuriousAndResetTask = new Runnable() {

			public void run() {

				try {

					// wait a bit
					int count = 0;
					// start sending notifications to the monitor
					do {
						// sleep for a while
						Thread.sleep(50);

						synchronized (monitor) {
							monitor.notifyAll();
						}
						count++;
					} while (count > 100);

					// sent enough, 
					System.out.println("About to reset template...");
					template.reset(0);
					System.out.println("Resetted template...");
				}
				catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		};

		Thread th = new Thread(spuriousAndResetTask, "spurious_reset-thread");
		th.start();
		long start = System.currentTimeMillis();
		assertNull(template.execute(callback));
		long stop = System.currentTimeMillis();

		long waitingTime = stop - start;
		assertTrue("Template not stopped in time", waitingTime < initialWaitTime);

		assertTrue(template.getMissingTarget() >= start);
		assertEquals(1, template.missingTargetInvocation);
		assertEquals("failed callback does not end succesful", 0, template.successfulStop);
		assertTrue(waitingTime >= template.getFailedStop());
	}
}