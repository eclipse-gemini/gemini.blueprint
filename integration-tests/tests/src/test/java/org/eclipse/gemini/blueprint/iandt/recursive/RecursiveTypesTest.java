package org.eclipse.gemini.blueprint.iandt.recursive;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.springframework.context.ConfigurableApplicationContext;
import org.eclipse.gemini.blueprint.util.OsgiServiceReferenceUtils;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.eclipse.gemini.blueprint.context.ConfigurableOsgiBundleApplicationContext;


public class RecursiveTypesTest extends BaseIntegrationTest {

	protected String getManifestLocation() {
		return null;
	}

	protected String[] getTestBundlesNames() {
		return null;
	}

	public void testBeanReference() throws Exception {

		Bundle bundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.eclipse.gemini.blueprint.iandt", "recursive", getSpringDMVersion()).getURL().toExternalForm());
		bundle.start();
		waitOnContextCreation(bundle.getSymbolicName());
		System.out.println("started bundle [" + OsgiStringUtils.nullSafeSymbolicName(bundle) + "]");
	}
}
