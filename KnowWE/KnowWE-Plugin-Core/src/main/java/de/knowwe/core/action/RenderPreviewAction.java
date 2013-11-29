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

import org.json.JSONArray;
import org.json.JSONException;

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

	private static final String OUTDATED = "<i>The specified article sections are not available, maybe the page has been changed by an other user. Please reload this page.</i>";

	@Override
	public void execute(UserActionContext context) throws IOException {

		RenderResult result = null;
		String jsonString = context.getParameter(Attributes.JSON_DATA);
		String nodeIDs = context.getParameter(Attributes.SECTION_ID);
		if (jsonString != null) {
			result = executeJSON(context, jsonString);
		}
		else if (nodeIDs != null) {
			result = executePlain(context, nodeIDs);
		}

		if (result == null) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing data");
		}
		else {
			context.setContentType("text/plain; charset=UTF-8");
			context.getWriter().append(result.toString());
		}
	}

	private RenderResult executePlain(UserActionContext context, String nodeIDs) throws IOException {
		RenderResult result = new RenderResult(context);
		renderItem(context, nodeIDs, result);
		return result;
	}

	private void renderItem(UserActionContext context, String nodeIDs, RenderResult result) {
		String[] ids = nodeIDs.split(",");
		List<Section<?>> sections = new LinkedList<Section<?>>();
		for (String sectionID : ids) {
			Section<? extends Type> section = Sections.getSection(sectionID);
			if (section == null) {
				result.append(OUTDATED);
				return;
			}
			sections.add(section);
		}
		ObjectInfoTagHandler.renderTermReferencesPreviews(sections, context, result);
	}

	private RenderResult executeJSON(UserActionContext context, String jsonText) throws IOException {
		RenderResult result = new RenderResult(context);
		try {
			JSONArray object = new JSONArray(jsonText);
			for (int i = 0; i < object.length(); i++) {
				String ids = object.getString(i);
				result.appendHtml("<div>");
				renderItem(context, ids, result);
				result.appendHtml("</div>");
				result.append("\n");
			}
			return result;
		}
		catch (JSONException e) {
			throw new IOException("wrong arguments: " + e.getMessage());
		}
	}
}
