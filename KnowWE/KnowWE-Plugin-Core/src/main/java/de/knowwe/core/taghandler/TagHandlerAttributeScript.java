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

import java.util.HashMap;
import java.util.Map;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Reads out the attributes of TagHandlerTypeContent and stores them in
 * KnowWESectionStore.
 */
public class TagHandlerAttributeScript extends DefaultGlobalScript<TagHandlerTypeContent> {

	public static final String ATTRIBUTE_MAP = "TagHandler.attributeMap";

	@Override
	public void compile(DefaultGlobalCompiler compiler, Section<TagHandlerTypeContent> section) {
		String text = section.getText();

		Map<String, String> values = new HashMap<String, String>();
		values.put("_cmdline", text);

		// attribute parsen und einfügen
		String[] tmpSecSplit = text.split(",");
		for (int i = 0; i < tmpSecSplit.length; i++) {
			String tag = tmpSecSplit[i].split("=")[0];
			String value = "";
			if (tmpSecSplit[i].contains("=")) {
				String[] splitted = tmpSecSplit[i].split("=");
				if (splitted.length == 2) {
					value = splitted[1];
				}
			}
			values.put(tag.trim(), value.trim());
		}
		section.getParent().storeObject(ATTRIBUTE_MAP, values);

	}
}
