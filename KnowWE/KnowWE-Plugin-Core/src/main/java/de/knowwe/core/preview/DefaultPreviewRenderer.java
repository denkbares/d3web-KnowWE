package de.knowwe.core.preview;

import java.util.Collection;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class DefaultPreviewRenderer extends AbstractPreviewRenderer {

	public DefaultPreviewRenderer() {
	}

	@Override
	public void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		if (section.get() instanceof DefaultMarkupType) {
			// render all sub sections, but not the default markup itself
			DefaultMarkupPreviewRenderer.renderSections(section.getChildren(), user, result);
		}
		else {
			// or render the full markup
			result.append(section, user);
		}
	}
}