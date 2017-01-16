package de.knowwe.testcases.prefix;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.testcases.TestCaseProvider;
import de.knowwe.testcases.TestCaseUtils;

public class PrefixTestCaseRenderer implements Renderer {

	private final Renderer renderer;

	public PrefixTestCaseRenderer(Renderer actualRenderer) {
		this.renderer = actualRenderer;
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {
		if (!user.isRenderingPreview()) {
			refreshPrefixWarning(section);
		}
		renderer.render(section, user, string);
	}

	public static void refreshPrefixWarning(Section<?> section) {
		if (!(section.get() instanceof DefaultMarkupType)) return;
		String prefix = DefaultMarkupType.getAnnotation(section,
				PrefixedTestCaseProvider.PREFIX_ANNOTATION_NAME);
		List<TestCaseProvider> found = (prefix == null)
				? Collections.<TestCaseProvider>emptyList()
				: TestCaseUtils.getTestCaseProviders(
				section.getWeb(), section.getPackageNames(),
				"^" + Pattern.quote(prefix) + "$");
		if (prefix != null && found.isEmpty()) {
			Message warning = Messages.warning("Prefix testcase '" + prefix
					+ "' does not exist or has errors");
			Messages.storeMessage(section, PrefixTestCaseRenderer.class, warning);
		} else {
			Messages.clearMessages(section, PrefixTestCaseRenderer.class);
		}
	}
}
