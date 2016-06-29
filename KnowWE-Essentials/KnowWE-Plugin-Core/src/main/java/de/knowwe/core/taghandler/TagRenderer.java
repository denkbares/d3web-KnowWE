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

package de.knowwe.core.taghandler;

import java.util.Map;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.report.Messages;
import de.knowwe.core.user.UserContext;

public class TagRenderer implements Renderer {

	@SuppressWarnings("unchecked")
	@Override
	public void render(Section<?> sec, UserContext user, RenderResult string) {
		Section<TagHandlerTypeContent> attrContent = Sections.child(sec,
				TagHandlerTypeContent.class);
		if (attrContent == null) {
			string.appendHtml("<div><p class='info box'>");
			string.append(Messages.getMessageBundle(user).getString(
					"KnowWE.Taghandler.notFoundError"));
			string.append(" '"
					+ sec.getChildren().get(1).getText()
					+ "'");
			string.appendHtml("</p></div>");
			return;
		}

		Type type = attrContent.get();

		if (type instanceof TagHandlerTypeContent) {
			Map<String, String> attValues = null; // ((TagHandlerTypeContent)type).getValuesForSections().get(attrContent);
			String id = sec.getID();
			Object storedValues = sec.getObject(
					TagHandlerAttributeScript.ATTRIBUTE_MAP);
			if (storedValues != null) {
				if (storedValues instanceof Map) {
					attValues = (Map<String, String>) storedValues;
				}
			}

			if (attValues != null) {
				attValues.put("kdomid", id);
				for (String elem : attValues.keySet()) {
					Map<String, TagHandler> defaultTagHandlers = Environment.getInstance().getDefaultTagHandlers();
					String key = elem.toLowerCase();
					if (defaultTagHandlers.containsKey(key)) {
						TagHandler handler = defaultTagHandlers.get(key);
						handler.render(sec, user, attValues, string);

					}
				}

			}
		}
	}

}
