/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolUtils;

/**
 * Returns the HTML of tool menu for a certain section.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.10.2013
 */
public class GetToolMenuAction extends AbstractAction {

	private static DefaultMarkupRenderer defaultMarkupRenderer =
			new DefaultMarkupRenderer();

	@Override
	public void execute(UserActionContext context) throws IOException {

		String sectionID = context.getParameter("sectionID");

		Section<? extends Type> sec = Sections.getSection(sectionID);

		if (sec == null) {
			context.sendError(409, "Section '" + sectionID
					+ "' could not be found, possibly because somebody else"
					+ " has edited the page.");
			return;
		}
		Tool[] tools = ToolUtils.getTools(sec, context);
		RenderResult string = new RenderResult(context);
		defaultMarkupRenderer.appendMenu(tools, sec.getID(), context, string);

		JSONObject response = new JSONObject();
		try {
			response.accumulate("menuHTML", string.toString());
			if (context.getWriter() != null) {
				context.setContentType("text/html; charset=UTF-8");
				response.write(context.getWriter());
			}
		}
		catch (JSONException e) {
			new IOException(e);
		}

	}

}
