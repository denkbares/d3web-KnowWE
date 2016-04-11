/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.core.kdom.rendering;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;

/**
 * The AnchorKDOMRender prefixes a section with an HTML anchor. This anchor can
 * be used to link from other articles to the section. Also due the fact that
 * the anchor is unique it can be used to address the section itself. E.g. for
 * AJAX interaction.
 * 
 * @author Jochen, smark
 * @since 2009/10/19
 */
public abstract class AnchorKDOMRender implements Renderer {

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {
		String head = "<a name=\"" + sec.getID() + "\" id=\"" + sec.getID() + "\"></a>";
		string.append(head);

		renderContent(sec, user, string);
	}

	public abstract void renderContent(Section<?> sec, UserContext user, RenderResult string);
}
