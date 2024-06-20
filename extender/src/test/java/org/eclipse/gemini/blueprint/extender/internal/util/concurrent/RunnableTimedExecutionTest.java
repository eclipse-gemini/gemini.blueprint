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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

/**
 * 
 * @author Costin Leau
 * 
 */
public class RunnableTimedExecutionTest {

	private long wait = 5 * 1000;

	@Test
	public void testExecute() {
		RunnableTimedExecution.execute(new Runnable() {

			public void run() {
				assertTrue(true);
			}

		}, wait);
	}

	@Test
	public void testDestroy() {
		RunnableTimedExecution.execute(new Runnable() {

			public void run() {
				try {
					Thread.sleep(wait * 5);
					fail("should have been interrupted");
				}
				catch (InterruptedException ie) {
					// expected
				}

			}

		}, 10);
	}
}
