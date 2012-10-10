/*
 * Copyright (C) 2011 denkbares GmbH
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

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * 
 * @author Lukas Brehl, Jochen
 * @created 25.05.2012
 */

public class SectionType extends AbstractType {

	/**
	 * for the plugin framework to call
	 */
	public SectionType() {
		this(3);
	}

	/*
	 * A SectionType can have a SectionHeaderType and a SectionContentType as
	 * children.
	 */
	public SectionType(int count) {
		this.setSectionFinder(new SectionBlockFinder(createMarker(count)));
		this.addChildType(new SectionHeaderType());
		this.addChildType(new SectionContentType(count));
	}

	/**
	 * 
	 * @created 11.07.2012
	 * @param count
	 * @return
	 */
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
			String[] sectionTypes = s.split("(^|\n)" + marker);
			int start = -1;
			int end = -1;
			int correcture = 0;
			// looking for start of a verbatimType
			boolean verbatim = sectionTypes[0].replaceAll(
					"\\{\\{\\{(.|\r\n)*?\\}\\}\\}", "").contains("{{{");
			if (verbatim) {
				correcture--;
			}
			for (int i = 1; i < sectionTypes.length; i++) {
				if (verbatim) {
					// looking for end of a verbatimType
					if (sectionTypes[i].replaceAll(
							"\\{\\{\\{(.|\r\n)*?\\}\\}\\}", "").contains("}}}")) {
						// missing \n from splitting is added to length
						if (i != sectionTypes.length - 1) {
							correcture++;
						}
						verbatim = false;
						if (start != -1) {
							end = text.indexOf(sectionTypes[i])
									+ sectionTypes[i].length();
							SectionFinderResult result = new SectionFinderResult(
									start, end + correcture);
							correcture = 0;
							results.add(result);
						}
						// looking for start of a verbatimType
						if (sectionTypes[i].replaceAll(
								"\\{\\{\\{(.|\r\n)*?\\}\\}\\}", "").contains(
								"{{{")) {
							start = text.indexOf(sectionTypes[i])
									- marker.length();
							verbatim = true;
						}
					}
					continue;
				}
				// looking for start of a verbatimType
				if (sectionTypes[i].replaceAll("\\{\\{\\{(.|\r\n)*?\\}\\}\\}",
						"").contains("{{{")) {
					start = text.indexOf(sectionTypes[i]) - marker.length();
					verbatim = true;
					continue;
				}
				// missing \n from splitting is added to length
				if (i != sectionTypes.length - 1) {
					correcture++;
				}
				start = text.indexOf(sectionTypes[i]) - marker.length();
				end = text.indexOf(sectionTypes[i]) + sectionTypes[i].length();
				SectionFinderResult result = new SectionFinderResult(start, end
						+ correcture);
				correcture = 0;
				results.add(result);
			}
			return results;
		}

	}

}
