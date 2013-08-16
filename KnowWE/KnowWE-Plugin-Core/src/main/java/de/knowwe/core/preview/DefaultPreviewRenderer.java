package de.knowwe.core.preview;

import java.util.Collection;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

public class DefaultPreviewRenderer implements PreviewRenderer {

	public DefaultPreviewRenderer() {
	}

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		DelegateRenderer.getRenderer(section, user).render(section, user, result);
	}
}