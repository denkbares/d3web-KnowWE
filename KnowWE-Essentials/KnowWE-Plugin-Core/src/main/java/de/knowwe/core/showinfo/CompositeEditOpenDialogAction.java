/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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
package de.knowwe.core.showinfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.denkbares.strings.Identifier;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Renders the "Show Info" dialog of one or several terms (elements), see {@link ShowInfoRenderer}. The terms are given
 * by their external form, as parameter <code>termIdentifier</code> for one term, or as a JSON array in parameter
 * <code>termIdentifiers</code>, optionally with the section the dialog is opened for (<code>contextSection</code>, e.g.
 * the one of the clicked tool menu, see {@link ShowInfoRenderer#renderLabel(Identifier, RenderResult)}). The response is a JSON object with the title of the dialog (<code>header</code>), its
 * content (<code>result</code>), whether the user may edit it (<code>canWriteAll</code>), the shown elements
 * (<code>elements</code>, each with <code>term</code>, <code>name</code>, <code>type</code> and for several elements
 * <code>label</code>, the element rendered as in the wiki) and the elements by reference section
 * (<code>referenceElements</code>, see {@link ShowInfoRenderer#getReferenceElements()}).
 *
 * @author stefan
 * @created 12.12.2013
 */
public class CompositeEditOpenDialogAction extends AbstractAction {

	public static final String TITLE = "Show Info";
	private static final String TERM_IDENTIFIER = "termIdentifier";
	private static final String TERM_IDENTIFIERS = "termIdentifiers";
	private static final String CONTEXT_SECTION = "contextSection";

	@Override
	public void execute(UserActionContext context) throws IOException {
		List<Identifier> identifiers = getIdentifiers(context);
		if (identifiers.isEmpty()) {
			context.sendError(400, "No term given");
			return;
		}
		// the section the dialog is opened for, e.g. the one of the clicked tool menu
		String sectionId = context.getParameter(CONTEXT_SECTION);
		Section<?> section = sectionId == null ? null : Sections.get(context, sectionId);
		if (section != null && !KnowWEUtils.canView(section, context)) section = null;
		ShowInfoRenderer renderer = new ShowInfoRenderer(identifiers, section, context);

		RenderResult header = new RenderResult(context);
		renderer.renderHeader(header);
		RenderResult result = new RenderResult(context);
		renderer.renderContent(result);

		try {
			JSONObject response = new JSONObject();
			response.put("header", toHtml(context, header));
			response.put("result", toHtml(context, result));
			response.put("canWriteAll", KnowWEUtils.canWrite(renderer.getEditableSections(), context));
			JSONArray elements = new JSONArray();
			for (Identifier identifier : renderer.getIdentifiers()) {
				JSONObject element = new JSONObject();
				element.put("term", identifier.toExternalForm());
				element.put("name", identifier.toPrettyPrint());
				element.put("type", renderer.getTypeName(identifier));
				if (renderer.isMultiple()) {
					RenderResult label = new RenderResult(context);
					renderer.renderLabel(identifier, label);
					element.put("label", toHtml(context, label));
				}
				elements.put(element);
			}
			response.put("elements", elements);
			response.put("referenceElements", new JSONObject(renderer.getReferenceElements()));
			context.setContentType(JSON);
			response.write(context.getWriter());
		}
		catch (JSONException e) {
			throw new IOException(e);
		}
	}

	private static List<Identifier> getIdentifiers(UserActionContext context) throws IOException {
		List<Identifier> identifiers = new ArrayList<>();
		String json = context.getParameter(TERM_IDENTIFIERS);
		if (json != null) {
			try {
				JSONArray array = new JSONArray(json);
				for (int i = 0; i < array.length(); i++) {
					identifiers.add(Identifier.fromExternalForm(array.getString(i)));
				}
			}
			catch (JSONException e) {
				throw new IOException("Invalid parameter " + TERM_IDENTIFIERS, e);
			}
		}
		String single = context.getParameter(TERM_IDENTIFIER);
		if (single != null) identifiers.add(Identifier.fromExternalForm(single));
		return identifiers;
	}

	private String toHtml(UserActionContext context, RenderResult result) {
		String html = context.getEnvironment().getWikiConnector().renderWikiSyntax(result.toStringRaw());
		return RenderResult.unmask(html, context);
	}
}
