/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.jspwiki.types;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * 
 * @author Lukas Brehl, Jochen
 * @created 25.05.2012
 */

public class HeaderType extends AbstractType {

	/**
	 * for the plugin framework to call
	 */
	public HeaderType() {
		this(3);
	}

	/*
	 * A SectionType can have a SectionHeaderType and a SectionContentType as
	 * children.
	 */
	public HeaderType(int count) {
		this.setSectionFinder(new RegexSectionFinder("^" + createMarker(count)
				+ ".*?(?=^|\\z)", Pattern.MULTILINE + Pattern.DOTALL));
		addChildType(new BoldType());
		addChildType(new ItalicType());
		addChildType(new TTType());
		addChildType(new LinkType());
	}

	private static String createMarker(int count) {
		String marker = "";
		for (int i = 0; i < count; i++) {
			marker += "!";
		}
		return marker;
	}

	class SectionBlockFinder implements SectionFinder {

		String marker;

		public SectionBlockFinder(String marker) {
			this.marker = marker;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {
			List<SectionFinderResult> results = new ArrayList<SectionFinderResult>();
			String s = text;
			String[] sectionTypes = s.split("(^|\n)");
			for (int i = 1; i < sectionTypes.length; i++) {
				if (sectionTypes[i].length() >= marker.length()
						&& sectionTypes[i].substring(0, marker.length())
								.equals(marker)) {
					int start = text.indexOf(sectionTypes[i]);
					int end = start + sectionTypes[i].length();
					SectionFinderResult result = new SectionFinderResult(start,
							end + 1);
					results.add(result);
				}
			}
			return results;
		}

	}

}
