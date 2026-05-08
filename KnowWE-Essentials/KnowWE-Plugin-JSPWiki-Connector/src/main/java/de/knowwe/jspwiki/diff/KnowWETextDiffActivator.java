package de.knowwe.jspwiki.diff;

import java.util.Properties;

import org.apache.wiki.api.engine.EngineLifecycleExtension;

/**
 * Activates {@link KnowWETextDiffProvider} as JSPWiki's diff provider without requiring
 * an entry in {@code jspwiki-custom.properties}. Discovered via {@link java.util.ServiceLoader}
 * (see {@code META-INF/services/org.apache.wiki.api.engine.EngineLifecycleExtension}) and
 * runs before the {@code DifferenceManager} reads {@code jspwiki.diffProvider}.
 * <p>
 * To stay friendly with existing setups, the property is only overridden when it is unset
 * or still points at the JSPWiki default ({@code TraditionalDiffProvider}). An explicit
 * value (e.g. {@code ContextualDiffProvider} or any custom provider) is preserved.
 */
public class KnowWETextDiffActivator implements EngineLifecycleExtension {

	private static final String PROP = "jspwiki.diffProvider";
	private static final String DEFAULT_SHORT = "TraditionalDiffProvider";
	private static final String DEFAULT_FQN = "org.apache.wiki.diff.TraditionalDiffProvider";

	@Override
	public void onInit(Properties properties) {
		String current = properties.getProperty(PROP, "").trim();
		if (current.isEmpty() || DEFAULT_SHORT.equals(current) || DEFAULT_FQN.equals(current)) {
			properties.setProperty(PROP, KnowWETextDiffProvider.class.getName());
		}
	}
}
