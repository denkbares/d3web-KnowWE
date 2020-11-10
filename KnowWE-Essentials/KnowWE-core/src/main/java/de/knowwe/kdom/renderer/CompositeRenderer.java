/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
import java.util.Collections;
import java.util.List;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;

/**
 * 
 * @author Jochen Reutelshöfer
 * @created 28.11.2012
 */
public class CompositeRenderer implements Renderer {

	private final List<SurroundingRenderer> renderers = new ArrayList<>();

	private final Renderer mainRenderer;

	/**
	 * 
	 */
	public CompositeRenderer(SurroundingRenderer r) {
		renderers.add(r);
		mainRenderer = DelegateRenderer.getInstance();
	}

	public CompositeRenderer(Renderer r, SurroundingRenderer wrapper) {
		renderers.add(wrapper);
		mainRenderer = r;
	}

	public void addRenderer(SurroundingRenderer r) {
		renderers.add(r);

	}

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {

		// render pre hooks of all surrounding renderers
		for (SurroundingRenderer r : renderers) {
			r.renderPre(section, user, string);
		}

		// render main renderer
		mainRenderer.render(section, user, string);

		// render post hooks of all surrounding renderers (reverse order)
		ArrayList<SurroundingRenderer> reverseList = new ArrayList<>(renderers);
		Collections.reverse(reverseList);
		for (SurroundingRenderer r : reverseList) {
			r.renderPost(section, user, string);
		}

	}
}
