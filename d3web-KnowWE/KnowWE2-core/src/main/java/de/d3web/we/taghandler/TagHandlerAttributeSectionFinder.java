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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEUtils;

public class TagHandlerAttributeSectionFinder extends SectionFinder {

	public static final String ATTRIBUTE_MAP = "TagHandler.attributeMap";
	
	@Override
	public List<SectionFinderResult> lookForSections(String text, Section father) {
				
		Map<String, String> values = new HashMap<String, String>();

		//attribute parsen und einfügen
		String[] tmpSecSplit = text.split(",");
		for (int i = 0; i < tmpSecSplit.length; i++) {
			String tag = tmpSecSplit[i].split("=")[0];
			String value = "";
			if (tmpSecSplit[i].contains("=")) {
				String[] splitted = tmpSecSplit[i].split("=");
				if(splitted.length == 2) {
					value = splitted[1];	
				}else {
					value = "";
				}
			}
			values.put(tag.trim(), value.trim());
		}
		
		KnowWEUtils.storeSectionInfo(father.getWeb(),father.getTitle(), father.getId(), ATTRIBUTE_MAP, values);
		
		List<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
		result.add(new SectionFinderResult(0, text.length()));
		return result;
	}
}
