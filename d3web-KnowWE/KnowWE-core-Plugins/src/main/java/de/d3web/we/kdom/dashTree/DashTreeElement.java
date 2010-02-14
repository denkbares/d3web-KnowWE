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
package de.d3web.we.kdom.dashTree;

import java.util.ArrayList;
import java.util.List;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.LineSectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author Jochen
 * 
 * A DashTree-element (one line with dashes), which is always the root-element of the SubTree which comes after it.
 *
 */
public class DashTreeElement extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new RootFinder();
		this.childrenTypes.add(new DashesPrefix());
		this.childrenTypes.add(new DashTreeElementContent());
	}

	/**
	 * Delivers the (dash-)level if the element by counting leading '-'
	 * 
	 * @param s
	 * @return
	 */
	public static int getLevel(Section<? extends DashTreeElement> s) {
		if (s == null)
			return -1;
		if (DashTreeElement.class.isAssignableFrom(s.getObjectType().getClass())) {
			String text = s.getOriginalText().trim();
			if (text.length() == 0)
				return -1;
			int index = 0;
			while (text.charAt(index) == '-') {
				index++;
			}
			return index;
		} else {
			return -1;
		}
	}

	public static Section<? extends DashTreeElement> getDashTreeFather(Section<? extends DashTreeElement> s) {
		if(s.getObjectType().isAssignableFromType(DashTreeElement.class)) {
			Section father = s.getFather();
			if(father != null) {
				Section grandFather = father.getFather();
				if(grandFather != null) {
					return grandFather.findChildOfType(DashTreeElement.class);
				}
			}
		}
		return null;
		
	}
	
	
	public static List<Section<? extends DashTreeElement>> getDashTreeAncestors(Section<? extends DashTreeElement> s) {
		List<Section<? extends DashTreeElement>> ancestors = new ArrayList<Section<? extends DashTreeElement>>();
		List<Section> ancestorSubTrees = new ArrayList<Section>();
		Section father = s.getFather();
		if (father != null && father.getObjectType().isAssignableFromType(SubTree.class)) {
			father = father.getFather();
		}
		while (father != null && father.getObjectType().isAssignableFromType(SubTree.class)) {
			ancestorSubTrees.add(father);
			father = father.getFather();
		}
		for (Section subTree:ancestorSubTrees) {
			ancestors.add(subTree.findChildOfType(DashTreeElement.class));
		}
		return ancestors;
	}
	
	/**
	 * Looks for the first non-empty line in the SubTree to make it (root-)Element
	 * 
	 * @author Jochen
	 *
	 */
	class RootFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {

			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();

			List<SectionFinderResult> lookForSections = LineSectionFinder
					.getInstance().lookForSections(text, father);
			if (lookForSections != null && lookForSections.size() > 0) {
				int index = 0;
				
				//Search for first non-empty line
				while (index < lookForSections.size()) {
					SectionFinderResult sectionFinderResult = lookForSections
							.get(index);
					index++;
					int start = sectionFinderResult.getStart();
					int end = sectionFinderResult.getEnd();
					String finding = text.substring(start, end);
										if (!KnowWEUtils.isEmpty(finding)) {
						result.add(sectionFinderResult);
						break;
					}
				}
			}
			return result;
		}

	}
	
	

}
