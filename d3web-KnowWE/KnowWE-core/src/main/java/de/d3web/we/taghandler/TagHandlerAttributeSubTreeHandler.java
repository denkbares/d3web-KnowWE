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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.utils.KnowWEUtils;

/**
 * Reads out the attributes of TagHandlerTypeContent and stores
 * them in KnowWESectionStore.
 */
public class TagHandlerAttributeSubTreeHandler extends SubtreeHandler {

	public static final String ATTRIBUTE_MAP = "TagHandler.attributeMap";
	
	public Collection<KDOMReportMessage> create(KnowWEArticle art, Section sec) {
				
		String text = sec.getOriginalText();
		
		Map<String, String> values = new HashMap<String, String>();
		values.put("_cmdline", text);

		//attribute parsen und einfügen
		String[] tmpSecSplit = text.split(",");
		for (int i = 0; i < tmpSecSplit.length; i++) {
			String tag = tmpSecSplit[i].split("=")[0];
			String value = "";
			if (tmpSecSplit[i].contains("=")) {
				String[] splitted = tmpSecSplit[i].split("=");
				if(splitted.length == 2) {
					value = splitted[1];	
				}
			}
			values.put(tag.trim(), value.trim());
		}
		
		KnowWEUtils.storeSectionInfo(sec.getWeb(),sec.getTitle(), sec.getFather().getID(), ATTRIBUTE_MAP, values);
		return null;
		
	}
}
