/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.taghandler;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TagRenderer extends KnowWEDomRenderer{

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		Section attrContent = sec.findChildOfType(TagHandlerTypeContent.class);
		KnowWEObjectType type = attrContent.getObjectType();
		
		StringBuilder buffi = new StringBuilder();
		
		if(type instanceof TagHandlerTypeContent) {
			Map<String,String> attValues = null; //((TagHandlerTypeContent)type).getValuesForSections().get(attrContent);
			Object storedValues = KnowWEEnvironment.getInstance().getArticleManager(sec.getWeb()).getTypeStore().getStoredObject(sec.getTitle(), sec.getId(), TagHandlerAttributeSectionFinder.ATTRIBUTE_MAP);
			if(storedValues != null) {
				if(storedValues instanceof Map) {
					attValues = (Map<String,String>) storedValues;
				}
			}

			if (attValues != null) {
				for (String elem: attValues.keySet()) {
					HashMap<String, TagHandler> defaultTagHandlers = KnowWEEnvironment.getInstance().getDefaultTagHandlers();
					if (defaultTagHandlers.containsKey(elem.toLowerCase())) {			
						buffi.append(defaultTagHandlers.get(elem.toLowerCase()).render(sec.getTitle(), user, attValues, sec.getWeb()) + " \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)
					}
				}
				if (buffi.length() == 0) {
					buffi.append("tag not found");
				}
			}
			string.append(KnowWEEnvironment.maskHTML(buffi.toString()));
		}
	}

}
