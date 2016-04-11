/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package de.knowwe.core.tools;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.renderer.RenderKDOMType;
import de.knowwe.core.user.UserContext;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * @author Veronika Sehne (denkbares GmbH)
 * @created 08.07.15
 */
public class KDOMRendererRevealToolProvider implements ToolProvider {

	@Override
	public Tool[] getTools(Section<?> section, UserContext userContext) {
		if (hasTools(section, userContext)) {
			return new Tool[] { getExpandTool(section, userContext) };
		}
		else {
			return new Tool[] {};
		}
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return Sections.successor(section.getArticle(), RenderKDOMType.class) != null;
	}

	protected Tool getExpandTool(Section<?> section, UserContext userContext) {
		// Tool to expand the RenderKDOM table
		String id = section.getID();
		String jsAction = "KNOWWE.kdomtreetable.revealRenderKDOMTable('" + id + "')";
		return new DefaultTool(
				Icon.EXPAND,
				"Reveal in rendered KDOM",
				"Reveals the corresponding section in the RenderKDOM markup.",
				jsAction, Tool.CATEGORY_LAST);
	}
}
