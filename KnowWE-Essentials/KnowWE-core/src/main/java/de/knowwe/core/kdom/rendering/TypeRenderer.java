/*
 * Copyright (C) 2021 denkbares GmbH. All rights reserved.
 */

package de.knowwe.core.kdom.rendering;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;

/**
 * Same as the ordinary {@link Renderer}, but the section has the specified type.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 09.12.21
 */
public interface TypeRenderer<T extends Type> extends Renderer {

	/**
	 * All rendered Sections will be appended to string.
	 *
	 * @param section the section to be rendered
	 * @param user    the user context to be used for rendering
	 * @param result  the result buffer to render the output to
	 */
	void renderType(Section<T> section, UserContext user, RenderResult result);

	@Override
	default void render(Section<?> section, UserContext user, RenderResult result) {
		//noinspection unchecked
		renderType((Section<T>) section, user, result);
	}
}
