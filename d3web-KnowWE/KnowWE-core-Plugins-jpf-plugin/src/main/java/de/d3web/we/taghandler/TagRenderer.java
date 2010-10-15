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
		KnowWEObjectType type = attrContent.getObjectType();

		StringBuilder buffi = new StringBuilder();

		if (type instanceof TagHandlerTypeContent) {
			Map<String, String> attValues = null; // ((TagHandlerTypeContent)type).getValuesForSections().get(attrContent);
			Object storedValues = KnowWEEnvironment.getInstance().getArticleManager(sec.getWeb()).getTypeStore().getStoredObject(
					sec.getTitle(), sec.getID(), TagHandlerAttributeSubTreeHandler.ATTRIBUTE_MAP);
			if (storedValues != null) {
				if (storedValues instanceof Map) {
					attValues = (Map<String, String>) storedValues;
				}
			}

			if (attValues != null) {
				attValues.put("kdomid", sec.getID());
				for (String elem : attValues.keySet()) {
					HashMap<String, TagHandler> defaultTagHandlers = KnowWEEnvironment.getInstance().getDefaultTagHandlers();
					if (defaultTagHandlers.containsKey(elem.toLowerCase())) {
						buffi.append(KnowWEUtils.maskHTML("<div id=\"" + elem.toLowerCase() + "\">"));
						TagHandler handler = defaultTagHandlers.get(elem.toLowerCase());
						String resultText =
								handler.render(sec.getWeb(), sec.getTitle(), user, attValues);
						buffi.append(resultText).append(" \n");
						buffi.append(KnowWEUtils.maskHTML("</div>"));
					}
				}
				if (buffi.length() == 0) {
					buffi.append(KnowWEUtils
							.maskHTML("<div><p class='info box'>"));
					buffi.append(KnowWEUtils.maskHTML(KnowWEEnvironment
							.getInstance().getKwikiBundle(user).getString(
									"KnowWE.Taghandler.notFoundError")
							+ "</p></div>"));
				}
			}
			string.append(buffi.toString());
		}
	}

}
