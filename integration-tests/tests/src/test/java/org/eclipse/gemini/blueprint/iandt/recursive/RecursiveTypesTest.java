package org.eclipse.gemini.blueprint.iandt.recursive;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.Bundle;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;


public class RecursiveTypesTest extends BaseIntegrationTest {

	protected String getManifestLocation() {
		return null;
	}

	protected String[] getTestBundlesNames() {
		return null;
	}

	public void testBeanReference() throws Exception {

		Bundle bundle = bundleContext.installBundle(getLocator().locateArtifact(
			"org.eclipse.gemini.blueprint.iandt", "recursive", getGeminiBlueprintVersion()).getURL().toExternalForm());
		bundle.start();
		waitOnContextCreation(bundle.getSymbolicName());
		System.out.println("started bundle [" + OsgiStringUtils.nullSafeSymbolicName(bundle) + "]");
	}
}
