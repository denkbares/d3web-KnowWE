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
import de.knowwe.tools.ToolSet;
import de.knowwe.tools.ToolUtils;

/**
 * Returns the HTML of tool menu for a certain section.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 02.10.2013
 */
public class GetToolMenuAction extends AbstractAction {

	public static final String IDENTIFIER = "identifier";
	protected static final DefaultMarkupRenderer defaultMarkupRenderer =
			new DefaultMarkupRenderer();

	@Override
	public void execute(UserActionContext context) throws IOException {

		String identifier = context.getParameter(IDENTIFIER);

		Section<? extends Type> section = getSection(context, identifier);
		ToolSet tools = getTools(context, section);
		if (!tools.hasTools()) return;

		RenderResult string = new RenderResult(context);
		defaultMarkupRenderer.appendMenu(tools, identifier, context, string);

		writeResponse(context, section.getID(), string);
	}

	public void writeResponse(UserActionContext context, String sectionId, RenderResult string) throws IOException {
		JSONObject response = new JSONObject();
		try {
			response.accumulate("menuHTML", string.toString());
			response.accumulate("sectionId", sectionId);
			if (context.getWriter() != null) {
				context.setContentType(HTML);
				response.write(context.getWriter());
			}
		}
		catch (JSONException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Returns the Tools to be available for the specified identifier (witch
	 * usually identifies a particular section). You may return null if there
	 * are no such tools available. In this case you may write some response
	 * error to the context.
	 * <p/>
	 * This method may be overwritten to provide tools for some identifier that
	 * do NOT denote a section.
	 *
	 * @param context the user context of the request
	 * @param section the section of which we want the tools
	 * @return the Tools for the identifier
	 * @created 10.11.2013
	 */
	protected ToolSet getTools(UserActionContext context, Section<? extends Type> section) throws IOException {
		if (section == null) {
			context.sendError(409, "Section could not be found, "
					+ "possibly because somebody else"
					+ " has edited the page.");
			return ToolUtils.emptyTools();
		}
		return ToolUtils.getTools(section, context);
	}

	/**
	 * Returns the section that is denoted by a particular identifier. You may
	 * override this method if a custom method to find the section by the
	 * identifier is required.
	 *
	 * @param context    the context of the user
	 * @param identifier the identifier to get the section for
	 * @return the section for that identifier
	 * @created 10.11.2013
	 */
	protected Section<? extends Type> getSection(UserActionContext context, String identifier) throws IOException {
		return Sections.get(identifier);
	}

}
