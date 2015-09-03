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
package de.knowwe.kdom.dashtree;

import java.util.ArrayList;
import java.util.List;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.kdom.sectionFinder.LineSectionFinder;

/**
 * @author Jochen
 * 
 *         A DashTree-element (one line with dashes), which is always the
 *         root-element of the SubTree which comes after it.
 * 
 */
public class DashTreeElement extends AbstractType {

	private Type elementContent;

	public DashTreeElement(char key) {
		this.setSectionFinder(new RootFinder());
		this.addChildType(new DashesPrefix(key));
		this.addChildType(new LineEndComment());
		elementContent = new DashTreeElementContent();
		this.addChildType(elementContent);

	}

	public void setDashTreeElementContent(Type newType) {
		this.replaceChildType(elementContent.getClass(), newType);
		elementContent = newType;
	}

	/**
	 * Looks for the first non-empty line in the SubTree to make it
	 * (root-)Element
	 * 
	 * @author Jochen
	 * 
	 */
	class RootFinder implements SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {

			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

			List<SectionFinderResult> lookForSections = LineSectionFinder
					.getInstance().lookForSections(text, father, type);
			if (lookForSections != null && lookForSections.size() > 0) {
				int index = 0;

				// Search for first non-empty line
				while (index < lookForSections.size()) {
					SectionFinderResult sectionFinderResult = lookForSections
							.get(index);
					index++;
					int start = sectionFinderResult.getStart();
					int end = sectionFinderResult.getEnd();
					String finding = text.substring(start, end);
					if (!Strings.isBlank(finding)) {
						result.add(sectionFinderResult);
						break;
					}
				}
			}
			return result;
		}

	}

}
