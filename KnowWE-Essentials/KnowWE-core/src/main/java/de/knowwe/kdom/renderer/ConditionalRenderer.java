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

package de.knowwe.kdom.renderer;

import java.util.ArrayList;
import java.util.List;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

@Deprecated
@SuppressWarnings("rawtypes")
public abstract class ConditionalRenderer implements Renderer {

	protected List<Renderer> conditionalRenderers = new ArrayList<>();

	public void addConditionalRenderer(Renderer r) {
		conditionalRenderers.add(r);
	}

	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {
		RenderResult b = new RenderResult(user);
		for (Renderer r : conditionalRenderers) {
			r.render(sec, user, b);
			if (b.length() == 0) {
				return;
			}
		}
		renderDefault(sec, user, string);
	}

	protected abstract void renderDefault(Section sec, UserContext user, RenderResult string);

}
