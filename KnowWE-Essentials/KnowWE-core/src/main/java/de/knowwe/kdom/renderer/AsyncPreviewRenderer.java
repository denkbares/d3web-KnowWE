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

	/**
	 * Renders a fast preview of the actual result to be shown until the actual result is available
	 *
	 * @param section the section to be rendered
	 * @param user    the user context to be used for rendering
	 * @param result  the result buffer to render the output to
	 */
	void renderAsyncPreview(Section<?> section, UserContext user, RenderResult result);

	/**
	 * In some cases, the full/actual result of the renderer is immediately available (e.g. due to caching). In this
	 * case, this method can return false to indicate, that the asynchronous rendering is not needed. This can help
	 * reduce flickering and moving elements while loading the page.
	 *
	 * @param section the section to be rendered
	 * @param user    the user context to be used for rendering
	 * @return true if the async rendering should proceed, false otherwise (e.g. because full result immediately available)
	 */
	default boolean shouldRenderAsynchronous(Section<?> section, UserContext user) {
		return true;
	}

	/**
	 * Util method for decorating async preview render that just want to check the decorated renderer for the result
	 *
	 * @param decoratedRenderer the decorated renderer to check for the result of {@link #shouldRenderAsynchronous(Section, UserContext)}
	 * @param section           the section to be rendered
	 * @param user              the user context to be used for rendering
	 * @return true if the async rendering should proceed, false otherwise (e.g. because full result immediately available)
	 */
	static boolean shouldRenderAsynchronous(Renderer decoratedRenderer, Section<?> section, UserContext user) {
		return !(decoratedRenderer instanceof AsyncPreviewRenderer) || ((AsyncPreviewRenderer) decoratedRenderer).shouldRenderAsynchronous(section, user);
	}
}
