/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.renderer;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Renderer that provides a preview of the asynchronously rendered content, e.g. by showing a previous cached result
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 15.11.21
 */
public interface AsyncPreviewRenderer extends Renderer {

	void renderAsyncPreview(Section<?> section, UserContext user, RenderResult result);
}
