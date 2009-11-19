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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.SectionFinder;
import de.d3web.we.kdom.sectionFinder.SectionFinderResult;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author Jochen
 * 
 *         A DashTree-Subtree containing the subtree-content of a
 *         (root-)element. This type is defined recursively - meaning it has
 *         itself as a child (allowing any depth of parsing).
 * 
 *         General Structure of this dashTree: SubTree always has 1 child which
 *         is an Element, which is the root of the subtree. Then it contains
 *         0..* subtrees as further children.
 * 
 */
public class SubTree extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		this.sectionFinder = new SubTreeFinder();

		this.childrenTypes.add(new DashTreeElement());
		// rescursive type definition
		this.childrenTypes.add(this);
	}

	/**
	 * Delivers the (dash-) level of a SubTree-section by asking its
	 * Root-Element (which is (KDOM-)child of the SubTree)
	 * 
	 * @param s
	 * @return
	 */
	public static int getLevel(Section s) {
//		if (s.getObjectType() instanceof SubTree)
//			return -1;
		Section root = s.findChildOfType(DashTreeElement.class);
		if (root == null)
			return 0;
		return DashTreeElement.getLevel(root) + 1;
	}

	/**
	 * @author Jochen
	 * 
	 *         finds the subtrees for a given (dash-) level the level is
	 *         retrieved from father.
	 * 
	 */
	class SubTreeFinder extends SectionFinder {

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section father) {

			int level = 0;

			KnowWEObjectType fatherType = father.getObjectType();
			if (fatherType instanceof SubTree) {
				level = getLevel(father);
			}

			String dashesPrefix = "";
			for (int i = 0; i < level; i++) {
				dashesPrefix += "-";
			}
			Matcher m = null;
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			if (KnowWEUtils.isEmpty(text))
				return result;
			try {
				m = Pattern.compile("^" + dashesPrefix + "[^-]+",
						Pattern.MULTILINE).matcher(text);
			} catch (StackOverflowError e) {
				e.printStackTrace();
				return result;
			}
			int lastStart = -1;
			while (m.find()) {
				if (lastStart > -1) {
					result.add(new SectionFinderResult(lastStart, m.start()));
				}
				lastStart = m.start();

			}
			if (lastStart > -1) {
				result.add(new SectionFinderResult(lastStart, text.length()));
			}
			return result;

		}

	}

}
