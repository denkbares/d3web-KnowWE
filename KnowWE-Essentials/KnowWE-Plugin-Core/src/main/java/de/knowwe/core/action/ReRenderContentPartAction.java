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

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * ReRenderContentPartAction. Renders a given section again. Often used in combination with AJAX
 * request, to refresh a certain section of an article due to user interaction.
 *
 * @author smark
 */
public class ReRenderContentPartAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		if (sectionId == null)  sectionId = context.getParameter("KdomNodeId"); // compatibility
 		execute(context, Sections.get(sectionId));
	}

	private static void execute(UserActionContext context, Section<?> section) throws IOException {
		if (section == null) {
			context.sendError(HttpServletResponse.SC_NOT_FOUND,
					"The referenced section was not found. " +
							"Maybe the page content is outdated. Please reload.");
		}
		else if (!KnowWEUtils.canView(section, context)) {
			context.sendError(HttpServletResponse.SC_FORBIDDEN,
					"You are not allowed to view this content.");
		}
		else if (handleStatusChanges(context)) {
			context.sendError(HttpServletResponse.SC_NOT_MODIFIED,
					"No change on the server, no need to update.");
		}
		else {
			String renderResult = reRender(context, section);
			if (renderResult != null && context.getWriter() != null) {
				context.setContentType(HTML);
				JSONObject response = new JSONObject();
				int counter = -1;
				String counterParam = context.getParameter("counter");
				if (counterParam != null) {
					counter = Integer.parseInt(counterParam);
				}
				response.put("counter", counter);
				try {
					response.put("html", renderResult);
					response.put("status", KnowWEUtils.getOverallStatus(context));
					response.write(context.getWriter());
				}
				catch (JSONException e) {
					throw new IOException(e);
				}
			}
		}
	}

	/**
	 * Returns true if a status from the client is given and it is the same as the current server status.
	 */
	private static boolean handleStatusChanges(UserActionContext context) {
		String status = context.getParameter("status");
		//noinspection SimplifiableIfStatement
		if (status == null) {
			// no status was given, we disregard status
			return false;
		}
		else {
			// we have a status from the client and compare it with the status from the server
			return status.equals(KnowWEUtils.getOverallStatus(context));
		}
	}

	private static String reRender(UserActionContext context, Section<?> section) {

		RenderResult result = new RenderResult(context);

		Renderer renderer = section.get().getRenderer();
		if (renderer != null) {
			renderer.render(section, context, result);
		}
		else {
			DelegateRenderer.getInstance().render(section, context, result);
		}

		// If the node is in <pre> than do not
		// render it through the JSPWikiPipeline
		String inPre = context.getParameter("inPre");
		String rawResult = result.toStringRaw();

		if ("false".equals(inPre)) {
			rawResult = Environment.getInstance()
					.getWikiConnector().renderWikiSyntax(rawResult);
		}
		else {
			rawResult = Environment.getInstance().getWikiConnector()
					.renderWikiSyntax(rawResult);
		}

		return RenderResult.unmask(rawResult, context);
	}

}
