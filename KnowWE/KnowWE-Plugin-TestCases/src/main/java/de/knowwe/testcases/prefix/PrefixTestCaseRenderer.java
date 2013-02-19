package de.knowwe.testcases.prefix;

import java.util.List;
import java.util.Set;

import de.d3web.core.utilities.Triple;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
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
	public void render(Section<?> section, UserContext user, StringBuilder string) {
		refreshPrefixWarning(section);
		renderer.render(section, user, string);
	}

	public static void refreshPrefixWarning(Section<?> section) {
		if (!(section.get() instanceof DefaultMarkupType)) return;
		String prefix = DefaultMarkupType.getAnnotation(section,
				PrefixedTestCaseProvider.PREFIX_ANNOTATION_NAME);
		if (prefix == null) return;
		Set<String> packageNames = section.getPackageNames();
		String[] packages = packageNames.toArray(new String[packageNames.size()]);
		List<Triple<TestCaseProvider, Section<?>, Article>> testCaseProviders = TestCaseUtils.getTestCaseProviders(
				section.getWeb(), packages);
		boolean found = false;
		for (Triple<TestCaseProvider, Section<?>, Article> triple : testCaseProviders) {
			TestCaseProvider provider = triple.getA();
			if (provider.getName().equals(prefix)) {
				found = true;
				break;
			}
		}
		if (found) {
			Messages.clearMessages(null, section, PrefixTestCaseRenderer.class);
		}
		else {
			Message warning = Messages.warning("Prefix testcase '" + prefix
					+ "' does not exist or has errors");
			Messages.storeMessage(null, section, PrefixTestCaseRenderer.class, warning);

		}
	}
}
