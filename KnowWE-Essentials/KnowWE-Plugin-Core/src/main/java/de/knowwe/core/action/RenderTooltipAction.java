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

package de.knowwe.core.action;

import java.io.IOException;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.kdom.renderer.TooltipRenderer;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 14.09.15
 */
public class RenderTooltipAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		Section<?> section = getSection(context);
		Renderer renderer = section.get().getRenderer();
		if (renderer instanceof TooltipRenderer) {
			TooltipRenderer tooltipRenderer = (TooltipRenderer) renderer;
			boolean hasTooltip = tooltipRenderer.hasTooltip(section, context);
			if (hasTooltip) {
				String tooltip = tooltipRenderer.getTooltip(section, context);
				context.setContentType("text/html; charset=UTF-8");
				context.getWriter().append(tooltip);
			}
		}
	}
}
