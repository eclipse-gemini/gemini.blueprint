package org.eclipse.gemini.blueprint.iandt.context;

import org.eclipse.gemini.blueprint.iandt.BaseIntegrationTest;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.container.Converter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests that the application context contains the standard blueprint environment beans as specified by Blueprint spec,
 * 121.11.1 Environment Managers.
 */
public class StandardBlueprintEnvironmentRegistrationTest extends BaseIntegrationTest {
    protected String getManifestLocation() {
        return null;
    }

    public void testBlueprintEnvironmentIsRegistered() {
        assertThat(this.applicationContext.getBeanDefinitionNames())
                .contains(
                        "blueprintBundle",
                        "blueprintBundleContext",
                        "blueprintContainer",
                        "blueprintConverter");

        assertThat(this.applicationContext.getBean("blueprintBundle")).isInstanceOf(Bundle.class);
        assertThat(this.applicationContext.getBean("blueprintBundleContext")).isInstanceOf(BundleContext.class);
        assertThat(this.applicationContext.getBean("blueprintContainer")).isInstanceOf(BlueprintContainer.class);
        assertThat(this.applicationContext.getBean("blueprintConverter")).isInstanceOf(Converter.class);
    }

    @Override
    protected String[] getTestBundlesNames() {
        return new String[]{
                "org.eclipse.gemini.blueprint.iandt, dependent, " + getGeminiBlueprintVersion()
        };
    }
}
