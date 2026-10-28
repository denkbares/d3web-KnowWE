package de.knowwe.jspwiki;

import java.util.Properties;

import org.apache.wiki.api.engine.EngineLifecycleExtension;

/**
 * Makes KnowWE's CodeMirror editor the default editor without requiring an entry in
 * {@code jspwiki-custom.properties}. Discovered via {@link java.util.ServiceLoader} (see
 * {@code META-INF/services/org.apache.wiki.api.engine.EngineLifecycleExtension}) and runs before the preferences read
 * {@code jspwiki.defaultprefs.template.editor}.
 * <p>
 * The property is only overridden when it is unset or still the JSPWiki default {@code plain} (set by JSPWiki's own
 * {@code jspwiki.properties}), any other explicit value is preserved. Users who chose an editor keep their choice.
 */
public class KnowWEDefaultEditorActivator implements EngineLifecycleExtension {

	private static final String PROP = "jspwiki.defaultprefs.template.editor";
	private static final String JSPWIKI_DEFAULT = "plain";
	private static final String EDITOR = "codemirror";

	@Override
	public void onInit(Properties properties) {
		String current = properties.getProperty(PROP, "").trim();
		if (current.isEmpty() || current.equalsIgnoreCase(JSPWIKI_DEFAULT)) {
			properties.setProperty(PROP, EDITOR);
		}
	}
}
