package de.d3web.we.kdom.rules;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Renders the different RuleContainers.
 *
 * Created by Albrecht Striffler (denkbares GmbH) on 22.03.2015.
 */
public class RuleContainerRenderer implements Renderer {

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		result.appendHtmlTag("span", "class", section.get().getName().replace("Container", ""));
		DelegateRenderer.getInstance().render(section, user, result);
		result.appendHtmlTag("/span");
	}
}
