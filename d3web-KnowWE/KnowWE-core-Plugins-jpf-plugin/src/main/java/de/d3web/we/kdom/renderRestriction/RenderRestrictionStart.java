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
package de.d3web.we.kdom.renderRestriction;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkup;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * Start marker of the group-rendering-restriction
 * 
 * @author Jochen
 * 
 */
public class RenderRestrictionStart extends DefaultMarkupType {

	public RenderRestrictionStart(DefaultMarkup markup) {
		super(markup);
		this.setCustomRenderer(new RestrictionRendererStart());
	}

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("RenderRestrictionStart");
		m.addContentType(new GroupDeclaration());

	}

	public RenderRestrictionStart() {
		super(m);
		this.setCustomRenderer(new RestrictionRendererStart());
	}

	public static String createString(String group) {
		return "RenderRestrictionStart: " + group + "\n";
	}

	class RestrictionRendererStart extends KnowWEDomRenderer<RenderRestrictionStart> {

		@Override
		public void render(KnowWEArticle article, Section<RenderRestrictionStart> sec, KnowWEUserContext user, StringBuilder string) {
			Section<GroupDeclaration> grSec = sec.findSuccessor(GroupDeclaration.class);
			string.append(RenderRestrictionStart.createString(grSec.get().getGroup(grSec)));

		}

	}

}
