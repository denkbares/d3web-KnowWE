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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.objectinfo.ObjectInfoRenderer;
import de.knowwe.core.preview.PreviewManager;
import de.knowwe.core.preview.PreviewRenderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Renders a given set of section as preview to be shown in a wiki page.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 29.11.2013
 */
public class RenderPreviewAction extends AbstractAction {

	private static final String OUTDATED = "<i>The specified article sections are not available, maybe the page has been changed by an other user. Please reload this page.</i>\n";
	public static final String ATTR_MODE = "mode";

	public static enum Mode {
		plain, list
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		JSONArray result = null;
		String jsonString = context.getParameter(Attributes.JSON_DATA);
		String nodeIDs = context.getParameter(Attributes.SECTION_ID);
		Mode mode = Mode.valueOf(context.getParameter(ATTR_MODE, Mode.list.name()));

		if (jsonString != null) {
			result = executeJSON(context, jsonString, mode);
		}
		else if (nodeIDs != null) {
			result = executePlain(context, nodeIDs, mode);
		}

		if (result == null) {
			context.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing data");
		}
		else {
			context.setContentType("text/plain; charset=UTF-8");
			try {
				result.write(context.getWriter());
			}
			catch (JSONException e) {
				throw new IOException("Wrong json arguments: " + e.getMessage());
			}
		}
	}

	private static String handleWikiSyntax(UserContext context, RenderResult result) {
		String markup = Environment.getInstance().getWikiConnector()
				.renderWikiSyntax(result.toStringRaw().replaceAll("@!!!", "@\n!!!"),
						context.getRequest());
		return RenderResult.unmask(markup, context);
	}

	private JSONArray executePlain(UserActionContext context, String nodeIDs, Mode mode) throws IOException {
		JSONArray result = new JSONArray();
		renderItem(context, nodeIDs, mode, result);
		return result;
	}

	private void renderItem(UserActionContext context, String nodeIDs, Mode mode, JSONArray result) {
		String[] ids = nodeIDs.split(",");
		List<Section<?>> sections = new LinkedList<Section<?>>();
		for (String sectionID : ids) {
			Section<? extends Type> section = Sections.get(sectionID);
			if (section == null) {
				result.put(OUTDATED);
				return;
			}
			sections.add(section);
		}
		if (mode == Mode.list) {
			RenderResult temp = new RenderResult(context);
			ObjectInfoRenderer.renderTermReferencesPreviews(sections, context, temp);
			temp.append("\n");
			result.put(handleWikiSyntax(context, temp));
		}
		else {
			renderPlainPreviews(sections, context, result);
		}
	}

	/**
	 * Renders the specified list of term references (usually of one specific
	 * article). The method renders the previews of the specified sections,
	 * grouped by their preview. Each preview may render one or multiple of the
	 * specified sections.
	 * 
	 * @created 29.11.2013
	 * @param sections the section to be rendered in their previews
	 * @param user the user context
	 * @param result the buffer to render into
	 */
	public static void renderPlainPreviews(List<Section<?>> sections, UserContext user, JSONArray result) {

		RenderResult temp = new RenderResult(user);
		if (KnowWEUtils.canView(sections, user)) {
			Map<Section<?>, Collection<Section<?>>> groupedByPreview =
					PreviewManager.getInstance().groupByPreview(sections);
			for (Entry<Section<?>, Collection<Section<?>>> entry : groupedByPreview.entrySet()) {
				Section<?> previewSection = entry.getKey();
				Collection<Section<?>> group = entry.getValue();
				renderPlainPreview(previewSection, group, user, temp);
			}
		}
		else {
			temp.appendHtml("<i>You are not allowed to view this article.</i>");
		}
		if (temp.length() == 0) {
			temp.appendHtml("<i>No preview available.</i>");
		}
		result.put(handleWikiSyntax(user, temp));
	}

	private static void renderPlainPreview(Section<?> previewSection, Collection<Section<?>> relevantSubSections, UserContext user, RenderResult result) {
		PreviewManager previewManager = PreviewManager.getInstance();
		PreviewRenderer renderer = previewManager.getPreviewRenderer(previewSection);
		if (renderer == null) return;

		result.appendHtml("<div class='preview reference'>");
		result.appendHtml("<div class='type_")
				.append(previewSection.get().getName()).appendHtml("'>");
		renderer.render(previewSection, relevantSubSections, user, result);
		result.appendHtml("</div>");
		result.appendHtml("</div>");
	}

	private JSONArray executeJSON(UserActionContext context, String jsonText, Mode mode) throws IOException {
		JSONArray result = new JSONArray();
		try {
			JSONArray object = new JSONArray(jsonText);
			for (int i = 0; i < object.length(); i++) {
				String ids = object.getString(i);
				renderItem(context, ids, mode, result);
			}
			return result;
		}
		catch (JSONException e) {
			throw new IOException("Wrong json arguments: " + e.getMessage());
		}
	}
}
