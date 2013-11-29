package de.knowwe.core.action;

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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.Attributes;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.taghandler.ObjectInfoTagHandler;

/**
 * Renders a given set of section as preview to be shown in a wiki page.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 29.11.2013
 */
public class RenderPreviewAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String nodeIDs = context.getParameter(Attributes.SECTION_ID);
		String[] ids = nodeIDs.split(",");
		List<Section<?>> sections = new LinkedList<Section<?>>();
		for (String sectionID : ids) {
			Section<? extends Type> section = Sections.getSection(sectionID);
			if (section == null) {
				context.sendError(
						HttpServletResponse.SC_BAD_REQUEST,
						"The specified article sections are not available, maybe the page has been changed by an other user. Please reload this page.");
				return;
			}
			sections.add(section);
		}

		RenderResult result = new RenderResult(context);
		ObjectInfoTagHandler.renderTermReferencesPreviews(sections, context, result);
		// String page =
		// Environment.getInstance().getWikiConnector().renderWikiSyntax(
		// result.toStringRaw(), context.getRequest());
		// String html = RenderResult.unmask(page, context);
		// context.getWriter().append(html);
		context.getWriter().append(result.toString());
		context.setContentType("text/html; charset=UTF-8");
	}
}
