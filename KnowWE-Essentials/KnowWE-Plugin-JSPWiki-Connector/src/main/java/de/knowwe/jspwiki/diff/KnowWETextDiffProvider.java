package de.knowwe.jspwiki.diff;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.diff.DiffProvider;
import org.apache.wiki.preferences.Preferences;

import com.denkbares.knowwe.textdiff.DiffHtmlRenderer;
import com.denkbares.knowwe.textdiff.TextDiff;

/**
 * JSPWiki {@link DiffProvider} that renders page-version diffs through KnowWE's
 * {@code <knowwe-text-diff>} web component (see {@code KnowWE-Plugin-TextDiff}).
 * <p>
 * Activate by setting in {@code jspwiki-custom.properties}:
 * <pre>
 *   jspwiki.diffProvider = de.knowwe.jspwiki.diff.KnowWETextDiffProvider
 * </pre>
 * <p>
 * The component's CSS and JS are already registered globally via
 * {@code KnowWEPlugin.includeDOMResources(...)} in {@code commonheader.jsp}, so no
 * additional resource wiring is required.
 */
public class KnowWETextDiffProvider implements DiffProvider {

	@Override
	public void initialize(Engine engine, Properties properties) throws NoRequiredPropertyException, IOException {
	}

	@Override
	public String getProviderInfo() {
		return "KnowWETextDiffProvider";
	}

	@Override
	public String makeDiffHtml(Context context, String oldWikiText, String newWikiText) {
		String oldText = oldWikiText == null ? "" : oldWikiText;
		String newText = newWikiText == null ? "" : newWikiText;
		if (Objects.equals(oldText, newText)) return "";

		String html = DiffHtmlRenderer.renderTextDiff(new TextDiff(oldText, newText));
		String theme = resolveTheme(context);
		return html.replaceFirst("<knowwe-text-diff ", "<knowwe-text-diff data-theme=\"" + theme + "\" ");
	}

	// Mirrors DefaultLogoAction#getLogoPath: derive light/dark from the user's "DisplayMode"
	// preference, defaulting to light when no preference is available.
	private static String resolveTheme(Context context) {
		HttpServletRequest request = context == null ? null : context.getHttpRequest();
		if (request != null && request.getSession(false) != null) {
			Object prefs = request.getSession().getAttribute(Preferences.SESSIONPREFS);
			if (prefs instanceof java.util.Map<?, ?> map) {
				Object mode = map.get("DisplayMode");
				if (mode != null && "dark-mode".equals(mode.toString())) return "dark";
			}
		}
		return "light";
	}
}
