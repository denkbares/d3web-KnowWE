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

package de.d3web.we.taghandler;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TagRenderer extends KnowWEDomRenderer {

	@Override
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		Section attrContent = sec.findChildOfType(TagHandlerTypeContent.class);
		if (attrContent == null) {
			string.append(KnowWEUtils
					.maskHTML("<div><p class='info box'>"));
			string.append(KnowWEUtils.maskHTML(KnowWEEnvironment
					.getInstance().getKwikiBundle(user).getString(
							"KnowWE.Taghandler.notFoundError")
					+ "</p></div>"));
			return;
		}

		StringBuilder buffi = new StringBuilder();

		KnowWEObjectType type = attrContent.getObjectType();

		if (type instanceof TagHandlerTypeContent) {
			Map<String, String> attValues = null; // ((TagHandlerTypeContent)type).getValuesForSections().get(attrContent);
			String id = sec.getID();
			Object storedValues = KnowWEEnvironment.getInstance().getArticleManager(sec.getWeb()).getTypeStore().getStoredObject(
					sec.getTitle(), id, TagHandlerAttributeSubTreeHandler.ATTRIBUTE_MAP);
			if (storedValues != null) {
				if (storedValues instanceof Map) {
					attValues = (Map<String, String>) storedValues;
				}
			}

			if (attValues != null) {
				attValues.put("kdomid", id);
				for (String elem : attValues.keySet()) {
					HashMap<String, TagHandler> defaultTagHandlers = KnowWEEnvironment.getInstance().getDefaultTagHandlers();
					String key = elem.toLowerCase();
					if (defaultTagHandlers.containsKey(key)) {
						TagHandler handler = defaultTagHandlers.get(key);
						boolean autoUpdate = handler.requiresAutoUpdate();
						if (autoUpdate) {
							// buffi.append(KnowWEUtils.maskHTML(
							// "<span class=\"ReRenderSectionMarker\"" +
							// " id=\"" + id + "\"" +
							// " rel=\"{id:'" + id +
							// "'}\"" +
							// ">"));
						}
						buffi.append(KnowWEUtils.maskHTML("<div id=\"" + key + "\">"));
						String resultText =
								handler.render(article, sec, user, attValues);
						buffi.append(resultText).append(" \n");
						buffi.append(KnowWEUtils.maskHTML("</div>"));
						if (autoUpdate) {
							// buffi.append(KnowWEUtils.maskHTML("</span>"));
						}
					}
				}

			}
			string.append(buffi.toString());
		}
	}

}
