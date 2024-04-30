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

package org.eclipse.gemini.blueprint.extender.internal.util.concurrent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.util.Assert;

/**
 * Utility class that executes the given Runnable task on the given task executor or , if none is given, to a new
 * thread.
 * 
 * <p/> If the thread does not return in the given amount of time, it will be interrupted and a logging message sent.
 * 
 * <p/> This class is intended for usage inside the framework, mainly by the extender package for controlling runaway
 * threads.
 * 
 * @see Counter
 * @see Thread
 * @author Costin Leau
 * 
 */
public abstract class RunnableTimedExecution {

	/** logger */
	private static final Log log = LogFactory.getLog(RunnableTimedExecution.class);

	private static class MonitoredRunnable implements Runnable {

		private Runnable task;

		private Counter counter;

		public MonitoredRunnable(Runnable task, Counter counter) {
			this.task = task;
			this.counter = counter;
		}

		public void run() {
			try {
				task.run();
			} finally {
				counter.decrement();
			}
		}
	}

	private static class SimpleTaskExecutor implements TaskExecutor, DisposableBean {

		private Thread thread;

		public void execute(Runnable task) {
			thread = new Thread(task);
			thread.setName("Thread for runnable [" + task + "]");
			thread.start();
		}

		public void destroy() throws Exception {
			if (thread != null) {
				thread.interrupt();
			}
		}
	}

	public static boolean execute(Runnable task, long waitTime) {
		return execute(task, waitTime, null);
	}

	public static boolean execute(Runnable task, long waitTime, TaskExecutor taskExecutor) {
		Assert.notNull(task, "task is required");

		Counter counter = new Counter("counter for task: " + task);
		Runnable wrapper = new MonitoredRunnable(task, counter);

		boolean internallyManaged = false;

		if (taskExecutor == null) {
			taskExecutor = new SimpleTaskExecutor();
			internallyManaged = true;
		}

		counter.increment();

		taskExecutor.execute(wrapper);

		if (counter.waitForZero(waitTime)) {
			log.error(task + " did not finish in " + waitTime
					+ "ms; consider taking a snapshot and then shutdown the VM in case the thread still hangs");

			//log.error("Current Thread dump***\n" + ThreadDump.dumpThreads());

			if (internallyManaged) {
				try {
					((DisposableBean) taskExecutor).destroy();
				} catch (Exception e) {
					log.error("Exception thrown while destroying internally managed thread executor", e);
				}
			}
			return true;
		}

		return false;
	}
}