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

package org.eclipse.gemini.blueprint.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLDecoder;

import org.eclipse.gemini.blueprint.io.OsgiBundleResourceLoader;
import org.eclipse.gemini.blueprint.test.internal.holder.OsgiTestInfoHolder;
import org.eclipse.gemini.blueprint.test.junit4.ConditionalTestCase;
import org.eclipse.gemini.blueprint.test.platform.OsgiPlatform;
import org.eclipse.gemini.blueprint.util.OsgiBundleUtils;
import org.eclipse.gemini.blueprint.util.OsgiPlatformDetector;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * Base test for OSGi environments. Takes care of configuring the chosen OSGi platform, starting it, installing a number
 * of bundles and delegating the test execution to a test copy that runs inside OSGi.
 *
 * @author Costin Leau
 */
public abstract class AbstractOsgiTests extends AbstractOptionalDependencyInjectionTests {

    private static final String UTF_8_CHARSET = "UTF-8";

    // JVM shutdown hook
    private static Thread shutdownHook;

    // the OSGi fixture
    private static OsgiPlatform osgiPlatform;

    // OsgiPlatform bundle context
    private static BundleContext platformContext;

    // JUnit Service
    private static Object service;

    // JUnitService trigger
    private static Method serviceTrigger;

    // OsgiResourceLoader
    protected ResourceLoader resourceLoader;

    /**
     * Hook for JUnit infrastructures which can't reuse this class hierarchy. This instance represents the test which
     * will be executed by AbstractOsgiTests & co.
     */
    private ConditionalTestCase osgiJUnitTest = this;

    private static final String ACTIVATOR_REFERENCE = "org.eclipse.gemini.blueprint.test.JUnitTestActivator";

    /**
     * Default constructor. Constructs a new <code>AbstractOsgiTests</code> instance.
     */
    public AbstractOsgiTests() {
        super();
    }

    /**
     * Constructs a new <code>AbstractOsgiTests</code> instance.
     *
     * @param name test name
     */
    public AbstractOsgiTests(String name) {
        super(name);
    }

    /**
     * Returns the test framework bundles (part of the test setup). Used by the test infrastructure. Override this
     * method <i>only</i> if you want to change the jars used by default, by the testing infrastructure.
     * <p/>
     * User subclasses should use {@link #getTestBundles()} instead.
     *
     * @return the array of test framework bundle resources
     */
    protected abstract Resource[] getTestFrameworkBundles();

    /**
     * Returns the bundles required for the test execution.
     *
     * @return the array of bundles to install
     */
    protected abstract Resource[] getTestBundles();

    /**
     * Creates (and configures) the OSGi platform.
     *
     * @return OSGi platform instance
     * @throws Exception if the platform creation fails
     */
    protected abstract OsgiPlatform createPlatform() throws Exception;

    /**
     * Pre-processes the bundle context. This call back gives access to the platform bundle context before any bundles
     * have been installed. The method is invoked <b>after</b> starting the OSGi environment but <b>before</b> any
     * bundles are installed in the OSGi framework.
     * <p/>
     * <p/> Normally, this method is called only once during the lifecycle of a test suite.
     *
     * @param platformBundleContext the platform bundle context
     * @throws Exception if processing the bundle context fails
     * @see #postProcessBundleContext(BundleContext)
     */
    protected void preProcessBundleContext(BundleContext platformBundleContext) throws Exception {
    }

    /**
     * Post-processes the bundle context. This call back gives access to the platform bundle context after the critical
     * test infrastructure bundles have been installed and started. The method is invoked <b>after</b> preparing the
     * OSGi environment for the test execution but <b>before</b> any test is executed.
     * <p/>
     * The given <code>BundleContext</code> belongs to the underlying OSGi framework.
     * <p/>
     * <p/> Normally, this method is called only one during the lifecycle of a test suite.
     *
     * @param platformBundleContext the platform bundle context
     * @see #preProcessBundleContext(BundleContext)
     */
    protected void postProcessBundleContext(BundleContext platformBundleContext) throws Exception {
    }

    //
    // JUnit overridden methods.
    //

    @Test
    public void test() throws Throwable {
        // add ConditionalTestCase behaviour

        // getName will return the name of the method being run
        if (isDisabledInThisEnvironment(getName())) {
            recordDisabled();
            logger.warn("**** " + getClass().getName() + "." + getName() + " disabled in this environment: "
                    + "Total disabled tests=" + getDisabledTestCount());
        } else {
            prepareTestExecution();
            // invoke OSGi test run
            invokeOSGiTestExecution();
        }
    }

    //
    // OSGi testing infrastructure setup.
    //

    /**
     * Starts the OSGi platform and install/start the bundles (happens once for the all test runs)
     *
     * @throws Exception
     */
    private void startup() throws Exception {
        if (osgiPlatform == null) {

            boolean debug = logger.isDebugEnabled();

            // make sure the platform is closed properly
            registerShutdownHook();

            osgiPlatform = createPlatform();

            // start platform
            if (debug) {
                logger.debug("About to start " + osgiPlatform);
            }

            osgiPlatform.start();

            // platform context
            platformContext = osgiPlatform.getBundleContext();

            // log platform name and version
            logPlatformInfo(platformContext);

            // hook before the OSGi platform is setup but right after is has been started
            preProcessBundleContext(platformContext);

            // install bundles (from the local system/classpath)
            Resource[] bundleResources = locateBundles();

            Bundle[] bundles = new Bundle[bundleResources.length];
            for (int i = 0; i < bundles.length; i++) {
                bundles[i] = installBundle(bundleResources[i]);
            }

            // start bundles
            for (Bundle bundle : bundles) {
                startBundle(bundle);
            }

            // hook after the OSGi platform has been setup
            postProcessBundleContext(platformContext);

            initializeServiceRunnerInvocationMethods();
        }
    }

    // concatenate bundles to install
    private Resource[] locateBundles() {
        Resource[] testFrameworkBundles = getTestFrameworkBundles();
        Resource[] testBundles = getTestBundles();

        if (testFrameworkBundles == null) {
            testFrameworkBundles = new Resource[0];
        }
        if (testBundles == null) {
            testBundles = new Resource[0];
        }

        Resource[] allBundles = new Resource[testFrameworkBundles.length + testBundles.length];
        System.arraycopy(testFrameworkBundles, 0, allBundles, 0, testFrameworkBundles.length);
        System.arraycopy(testBundles, 0, allBundles, testFrameworkBundles.length, testBundles.length);
        return allBundles;
    }

    /**
     * Logs the underlying OSGi information (which can be tricky).
     */
    private void logPlatformInfo(BundleContext context) {
        StringBuilder platformInfo = new StringBuilder();

        // add platform information
        platformInfo.append(osgiPlatform);
        platformInfo.append(" [");
        // Version
        platformInfo.append(OsgiPlatformDetector.getVersion(context));
        platformInfo.append("]");
        logger.info(platformInfo + " started");
    }

    /**
     * Installs an OSGi bundle from the given location.
     *
     * @param location - location bundle to install
     * @return bundle instance
     * @throws Exception
     */
    private Bundle installBundle(Resource location) throws Exception {
        Assert.notNull(platformContext, "the OSGi platform is not set");
        Assert.notNull(location, "cannot install from a null location");
        if (logger.isDebugEnabled())
            logger.debug("Installing bundle from location " + location.getDescription());

        String bundleLocation;

        try {
            bundleLocation = URLDecoder.decode(location.getURL().toExternalForm(), UTF_8_CHARSET);
        } catch (Exception ex) {
            // the URL cannot be created, fall back to the description
            bundleLocation = location.getDescription();
        }

        return platformContext.installBundle(bundleLocation, location.getInputStream());
    }

    /**
     * Starts a bundle and prints a nice logging message in case of failure.
     *
     * @param bundle to start
     * @throws BundleException
     */
    private void startBundle(Bundle bundle) throws BundleException {
        boolean debug = logger.isDebugEnabled();
        String info = "[" + OsgiStringUtils.nullSafeNameAndSymName(bundle) + "|" + bundle.getLocation() + "]";

        if (!OsgiBundleUtils.isFragment(bundle)) {
            if (debug)
                logger.debug("Starting " + info);
            try {
                bundle.start();
            } catch (BundleException ex) {
                logger.error("cannot start bundle " + info, ex);
                throw ex;
            }
        } else {
//			if (!OsgiBundleUtils.isBundleResolved(bundle)) {
//				logger.error("fragment not resolved: " + info);
//				throw new BundleException("Unable to resolve fragment: " + info);
//			} else if (debug)
            logger.debug(info + " is a fragment; start not invoked");
        }
    }

    //
    // Delegation methods for OSGi execution and initialization
    //

    // runs outside OSGi

    /**
     * Prepares test execution - the OSGi platform will be started (if needed) and cached for the test suite execution.
     */
    private void prepareTestExecution() throws Exception {

        if (getName() == null) {
            throw new IllegalArgumentException("no test specified");
        }

        // clear test results
        OsgiTestInfoHolder.INSTANCE.clearResults();
        // set test class
        OsgiTestInfoHolder.INSTANCE.setTestClassName(osgiJUnitTest.getClass().getName());

        // start OSGi platform (the caching is done inside the method).
        try {
            startup();
        } catch (Exception e) {
            logger.debug("Caught exception starting up", e);
            throw e;
        }

        if (logger.isTraceEnabled())
            logger.trace("Writing test name [" + getName() + "] to OSGi");

        // write test name to OSGi
        // set test method name
        OsgiTestInfoHolder.INSTANCE.setTestMethodName(getName());
    }

    /**
     * Delegates the test execution to the OSGi copy.
     *
     * @throws Exception
     */
    private void invokeOSGiTestExecution() throws Exception {
        Assert.notNull(serviceTrigger, "no executeTest() method found on: " + service.getClass());
        try {
            serviceTrigger.invoke(service);
        } catch (InvocationTargetException ex) {
            Throwable th = ex.getCause();
            if (th instanceof Exception) {
                throw ((Exception) th);
            } else {
                throw ((Error) th);
            }
        }
    }

    /**
     * Determines through reflection the methods used for invoking the TestRunnerService.
     *
     * @throws Exception
     */
    private void initializeServiceRunnerInvocationMethods() throws Exception {
        // get JUnit test service reference
        // this is a loose reference - update it if the JUnitTestActivator class is changed.

        BundleContext ctx = getRuntimeBundleContext();

        ServiceReference reference = ctx.getServiceReference(ACTIVATOR_REFERENCE);
        Assert.notNull(reference, "no OSGi service reference found at " + ACTIVATOR_REFERENCE);

        service = ctx.getService(reference);
        Assert.notNull(service, "no service found for reference: " + reference);

        serviceTrigger = service.getClass().getDeclaredMethod("executeTest", new Class[0]);
        ReflectionUtils.makeAccessible(serviceTrigger);
        Assert.notNull(serviceTrigger, "no executeTest() method found on: " + service.getClass());
    }

    /**
     * Tries to get the bundle context for test-support bundle. This is useful on platform where the
     * platformContext or system BundleContext doesn't behave like a normal context.
     * <p/>
     * Will fallback to {@link #platformContext}.
     *
     * @return BundleContext of test-support bundle
     */
    private BundleContext getRuntimeBundleContext() {

        // read test bundle id property
        Long id = OsgiTestInfoHolder.INSTANCE.getTestBundleId();

        BundleContext ctx = null;
        if (id != null) {
            try {
                ctx = OsgiBundleUtils.getBundleContext(platformContext.getBundle(id));
            } catch (RuntimeException ex) {
                logger.trace("cannot determine bundle context for bundle " + id, ex);
            }
        }

        return (ctx == null ? platformContext : ctx);
    }

    /**
     * Special shutdown hook.
     */
    private void registerShutdownHook() {
        if (shutdownHook == null) {
            // No shutdown hook registered yet.
            shutdownHook = new Thread() {

                public void run() {
                    shutdownTest();
                }
            };
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }
    }

    /**
     * Cleanup for the test suite.
     */
    private void shutdownTest() {
        logger.info("Shutting down OSGi platform");
        if (osgiPlatform != null) {
            try {
                osgiPlatform.stop();
            } catch (Exception ex) {
                // swallow
                logger.warn("Shutdown procedure threw exception " + ex);
            }
            osgiPlatform = null;
        }
    }

    //
    // OsgiJUnitTest execution hooks. Used by the test framework.
    //

    /**
     * Set the bundle context to be used by this test.
     * <p/>
     * <p/> This method is called automatically by the test infrastructure after the OSGi platform is being setup.
     */
    private void injectBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        // instantiate ResourceLoader
        this.resourceLoader = new OsgiBundleResourceLoader(bundleContext.getBundle());
    }

    /**
     * Set the underlying OsgiJUnitTest used for the test delegation.
     * <p/>
     * <p/> This method is called automatically by the test infrastructure after the OSGi platform is being setup.
     *
     * @param test
     */
    private void injectOsgiJUnitTest(ConditionalTestCase test) {
        this.osgiJUnitTest = test;
    }

    /**
     * the setUp version for the OSGi environment.
     *
     * @throws Exception
     */
    private void osgiSetUp() throws Exception {
        // call the normal onSetUp
        setUp();
    }

    private void osgiTearDown() throws Exception {
        // call the normal tearDown
        tearDown();
    }

    /**
     * Actual test execution (delegates to the superclass implementation).
     *
     * @throws Throwable
     */
    private void osgiRunTest() throws Throwable {
        test();
    }

}
