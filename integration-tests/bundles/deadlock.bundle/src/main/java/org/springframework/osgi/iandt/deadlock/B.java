/******************************************************************************
 * Copyright (c) 2006, 2010 VMware Inc., Oracle Inc.
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
 *   Oracle Inc.
 *****************************************************************************/

package org.springframework.osgi.iandt.deadlock;

/**
 * @author Hal Hildebrand
 *         Date: Jun 5, 2007
 *         Time: 8:48:51 PM
 */
public class B {
    public static final Object lockB = new Object();


    static {
        Runnable runnable = new Runnable() {

            public void run() {
                while (true) {
                    synchronized (lockB) {
                        synchronized (A.lockA) {
                            try {
                                Thread.sleep(300 * 1000); // five minutes
                            } catch (InterruptedException e) {
                                // We're mean and ignoring the InterruptedException
                            }
                        }
                    }
                }
            }
        };
        Thread t = new Thread(runnable, "deadlock B");
        t.setDaemon(true);
        t.start();
        Thread.yield();
    }


    private A a;


    public void setA(A a) {
        synchronized (lockB) {
            synchronized (A.lockA) {
                this.a = a;
            }
        }
    }


    public String toString() {
        return a.toString();
    }
}
