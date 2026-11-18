/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 */

package de.knowwe.core.showinfo;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.user.UserContext;

/**
 * Implemented by types whose sections render more than the element itself in the wiki (e.g. a name followed by its
 * prompts). In the "Show Info" dialog of several elements, such a section labels its element (see
 * {@link ShowInfoRenderer#renderLabel}) only with what this renderer renders, otherwise the whole section is rendered
 * as in the wiki.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 18.11.2026
 */
public interface ShowInfoLabelRenderer {

	/**
	 * Renders the given section as label of its element, i.e. only the element itself as in the wiki (colors, ...),
	 * without additional content like prompts or hints.
	 */
	void renderShowInfoLabel(Section<?> section, UserContext user, RenderResult result);
}
