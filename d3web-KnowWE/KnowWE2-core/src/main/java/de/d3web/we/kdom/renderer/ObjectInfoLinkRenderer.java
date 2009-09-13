/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.renderer;

import java.net.URLEncoder;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class ObjectInfoLinkRenderer extends KnowWEDomRenderer {

	KnowWEDomRenderer renderer = new DefaultTextRenderer();
	
	public ObjectInfoLinkRenderer(KnowWEDomRenderer renderer) {
		super();		
		this.renderer = renderer;
	}
	
	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {

		StringBuilder b = new StringBuilder();
		renderer.render(sec, user, b);
		
		if (sec.getTitle().equals("ObjectInfoPage")) {			
			string.append(KnowWEEnvironment.maskHTML("<a href=\"Wiki.jsp?page="
								+ sec.getTitle() + "\">"
								+ b.toString()
								+ "</a>"));
		} else {
			// TODO: Maybe make the page name non-hardcoded
			string.append(KnowWEEnvironment.maskHTML(
								"<a href=\"Wiki.jsp?page=ObjectInfoPage&objectname="
								+ URLEncoder.encode(sec.getOriginalText())
								+ "\">"
								+ b.toString()
								+ "</a>"));
		}
	}

}
