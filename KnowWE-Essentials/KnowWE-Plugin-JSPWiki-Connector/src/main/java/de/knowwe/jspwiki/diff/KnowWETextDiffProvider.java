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
import com.denkbares.knowwe.textdiff.DiffRenderOptions;
import com.denkbares.knowwe.textdiff.DiffRenderer;
import com.denkbares.knowwe.textdiff.DiffView;
import com.denkbares.knowwe.textdiff.FileChange;

/**
 * JSPWiki {@link DiffProvider} that renders page-version diffs through KnowWE's
 * {@code <knowwe-text-diff>} web component (see {@code KnowWE-Plugin-TextDiff}). The diff is built in the
 * browser, side by side where the page is wide enough, inside a {@code <knowwe-file-change>} frame whose header
 * carries the line counts and the layout and whitespace switches. The choices are remembered for the page history.
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

	private static final String CONTEXT = "page-history";
	private static final DiffRenderOptions OPTIONS = DiffRenderOptions.defaults()
			.withRenderer(DiffRenderer.CODEMIRROR)
			.withView(DiffView.AUTO);

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

		FileChange change = FileChange.modified(context.getPage().getName(), oldText, newText);
		String html = DiffHtmlRenderer.renderFileChange(change, OPTIONS);
		String theme = resolveTheme(context);
		return html.replaceFirst("<knowwe-text-diff ",
				"<knowwe-text-diff data-theme=\"" + theme + "\" data-context=\"" + CONTEXT + "\" ");
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
