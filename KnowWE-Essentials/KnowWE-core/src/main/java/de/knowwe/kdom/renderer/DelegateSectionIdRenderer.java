/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.kdom.renderer;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * Works the same as {@link DelegateRenderer}, but also wraps the content in a span containing the ID of the rendered section.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 2019-10-24
 */
public class DelegateSectionIdRenderer implements Renderer {

	private static DelegateSectionIdRenderer instance;

	public static DelegateSectionIdRenderer getInstance() {
		if (instance == null) {
			instance = new DelegateSectionIdRenderer();
		}
		return instance;
	}

	private DelegateSectionIdRenderer() {
		// singleton
	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult result) {
		result.appendHtmlTag("span", "sectionid", section.getID());
		DelegateRenderer.getInstance().render(section, user, result);
		result.appendHtmlTag("/span");
	}
}
