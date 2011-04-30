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

package de.d3web.we.kdom.rendering;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.user.UserContext;

@SuppressWarnings("rawtypes")
public abstract class ConditionalRenderer extends KnowWEDomRenderer {

	protected List<KnowWEDomRenderer> conditionalRenderers = new ArrayList<KnowWEDomRenderer>();

	public void addConditionalRenderer(KnowWEDomRenderer r) {
		conditionalRenderers.add(r);
	}

	@Override
	public void render(KnowWEArticle article, Section sec, UserContext user, StringBuilder string) {
		StringBuilder b = new StringBuilder();
		for (KnowWEDomRenderer r : conditionalRenderers) {
			r.render(article, sec, user, b);
			if (b.length() == 0) {
				return;
			}
		}
		renderDefault(sec, user, string);
	}

	protected abstract void renderDefault(Section sec, UserContext user, StringBuilder string);

}
