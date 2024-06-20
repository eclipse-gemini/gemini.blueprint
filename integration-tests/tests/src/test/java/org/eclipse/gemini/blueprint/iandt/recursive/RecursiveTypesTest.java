package org.eclipse.gemini.blueprint.iandt.recursive;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.junit.Test;
import org.osgi.framework.Bundle;


public class RecursiveTypesTest extends BaseIntegrationTest {

	protected String getManifestLocation() {
		return null;
	}

	protected String[] getTestBundlesNames() {
		return null;
	}

	@Test
	public void testBeanReference() throws Exception {

		Bundle bundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.eclipse.gemini.blueprint.iandt", "recursive", getSpringDMVersion()).getURL().toExternalForm());
		bundle.start();
		waitOnContextCreation(bundle.getSymbolicName());
		System.out.println("started bundle [" + OsgiStringUtils.nullSafeSymbolicName(bundle) + "]");
	}
}
