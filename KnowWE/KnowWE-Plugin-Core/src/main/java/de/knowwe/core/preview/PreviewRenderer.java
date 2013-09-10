package de.knowwe.core.preview;

import java.util.Collection;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Render interface for preview rendering. In addition to the "normal"
 * {@link Renderer} the preview renderer gets also a number of relevant
 * sub-sections that are the things the user requested to be shown. These
 * sub-sections shall be definitely included while rendering, maybe highlighted.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 16.08.2013
 */
public interface PreviewRenderer {

	void render(Section<?> section, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result);
}