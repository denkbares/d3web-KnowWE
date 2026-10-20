package de.knowwe.jspwiki.diff;

import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.apache.wiki.api.core.Context;
import org.apache.wiki.api.core.Engine;
import org.apache.wiki.api.exceptions.NoRequiredPropertyException;
import org.apache.wiki.diff.DiffProvider;

import com.denkbares.knowwe.textdiff.DiffHtmlRenderer;
import com.denkbares.knowwe.textdiff.DiffRenderOptions;
import com.denkbares.knowwe.textdiff.DiffView;
import com.denkbares.knowwe.textdiff.TextDiff;

/**
 * JSPWiki {@link DiffProvider} that renders page-version diffs through KnowWE's
 * {@code <knowwe-text-diff>} web component (see {@code KnowWE-Plugin-TextDiff}). The diff is built in the
 * browser, side by side where the page is wide enough, with the layout and whitespace switches of the component.
 * The choices are remembered for the page history.
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

	private static final DiffRenderOptions OPTIONS = DiffRenderOptions.defaults()
			.withView(DiffView.AUTO)
			.withContext("page-history");

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

		return DiffHtmlRenderer.renderTextDiff(new TextDiff(oldText, newText), OPTIONS);
	}
}
