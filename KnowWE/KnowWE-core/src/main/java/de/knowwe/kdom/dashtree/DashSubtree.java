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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.CommentLineType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * @author Jochen
 * 
 *         A Dash-Subtree containing the subtree-content of a (root-)element.
 *         This type is defined recursively - meaning it has itself as a child
 *         (allowing any depth of parsing).
 * 
 *         General Structure of this dashTree: Subtree always has 1 child which
 *         is an Element, which is the root of the subtree. Then it contains
 *         0..* subtrees as further children.
 * 
 */
public class DashSubtree extends AbstractType {

	public DashSubtree() {
		this.sectionFinder = new SubtreeFinder();

		this.childrenTypes.add(new DashTreeElement());
		// rescursive type definition
		this.childrenTypes.add(this);
		this.childrenTypes.add(new OverdashedElement());
		this.childrenTypes.add(new CommentLineType());
	}

	/**
	 * @author Jochen
	 * 
	 *         finds the subtrees for a given (dash-) level the level is
	 *         retrieved from father.
	 * 
	 */
	class SubtreeFinder implements SectionFinder {

		Pattern p0 = Pattern.compile("^\\s*[\\w\"]+.*$",
				Pattern.MULTILINE);

		Pattern p1 = Pattern.compile("^\\s*" + "-{1}" + "[^-]",
				Pattern.MULTILINE);

		Pattern p2 = Pattern.compile("^\\s*" + "-{2}" + "[^-]",
				Pattern.MULTILINE);

		Pattern p3 = Pattern.compile("^\\s*" + "-{3}" + "[^-]",
				Pattern.MULTILINE);

		Pattern p4 = Pattern.compile("^\\s*" + "-{4}" + "[^-]",
				Pattern.MULTILINE);

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {

			int level = 0;

			Type fatherType = father.get();
			if (fatherType instanceof DashSubtree) {
				level = DashTreeUtils.getDashLevel(father) + 1;
			}

			Matcher m = null;
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			if (KnowWEUtils.isEmpty(text)) return result;
			try {
				// Searches for line-beginning with correct count of dashes
				// (starting with 0)
				// Exceptions: One additional dash, linebreak (ie. empty lines)
				// and comment lines (starting with '/')
				if (level > 0) {
					// just to increase speed by reuse of precompiled patterns
					if (level == 1) {
						m = p1.matcher(text);
					}
					else if (level == 2) {
						m = p2.matcher(text);
					}
					else if (level == 3) {
						m = p3.matcher(text);
					}
					else if (level == 4) {
						m = p4.matcher(text);
					}
					else {
						m = Pattern.compile("^\\s*" + "-{" + level + "}" + "[^-]",
								Pattern.MULTILINE).matcher(text);
					}

				}
				else {
					m = p0.matcher(text);
				}
			}
			catch (StackOverflowError e) {
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
