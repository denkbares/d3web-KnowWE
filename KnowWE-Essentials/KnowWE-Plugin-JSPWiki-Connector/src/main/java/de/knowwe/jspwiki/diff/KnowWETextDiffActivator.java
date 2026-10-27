package de.knowwe.jspwiki.diff;

import java.util.Properties;
import java.util.Set;

import org.apache.wiki.api.engine.EngineLifecycleExtension;

/**
 * Activates {@link KnowWETextDiffProvider} as JSPWiki's diff provider without requiring
 * an entry in {@code jspwiki-custom.properties}. Discovered via {@link java.util.ServiceLoader}
 * (see {@code META-INF/services/org.apache.wiki.api.engine.EngineLifecycleExtension}) and
 * runs before the {@code DifferenceManager} reads {@code jspwiki.diffProvider}.
 * <p>
 * The property is overridden when it is unset, still points at the JSPWiki default
 * ({@code SvnStyleDiffProvider}, set by JSPWiki's own {@code jspwiki.properties}), or names
 * one of the providers removed in JSPWiki 3 ({@code TraditionalDiffProvider},
 * {@code ContextualDiffProvider}): JSPWiki would fall back to a provider that shows no diff
 * at all for those. Any other explicit value (e.g. {@code ExternalDiffProvider} or a custom
 * provider) is preserved.
 */
public class KnowWETextDiffActivator implements EngineLifecycleExtension {

	private static final String PROP = "jspwiki.diffProvider";
	private static final String PACKAGE = "org.apache.wiki.diff.";
	private static final Set<String> REPLACED = Set.of(
			"SvnStyleDiffProvider", "TraditionalDiffProvider", "ContextualDiffProvider");

	@Override
	public void onInit(Properties properties) {
		String current = properties.getProperty(PROP, "").trim();
		if (current.startsWith(PACKAGE)) current = current.substring(PACKAGE.length());
		if (current.isEmpty() || REPLACED.contains(current)) {
			properties.setProperty(PROP, KnowWETextDiffProvider.class.getName());
		}
	}
}
