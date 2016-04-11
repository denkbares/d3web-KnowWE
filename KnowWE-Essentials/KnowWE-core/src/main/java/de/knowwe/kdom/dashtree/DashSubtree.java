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
import java.util.regex.PatternSyntaxException;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.basicType.CommentLineType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.AnchorRenderer;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * A Dash-Subtree containing the subtree-content of a (root-)element. This type
 * is defined recursively - meaning it has itself as a child (allowing any depth
 * of parsing).
 * 
 * General Structure of this dashTree: Subtree always has 1 child which is an
 * Element, which is the root of the subtree. Then it contains 0..* subtrees as
 * further children.
 * 
 * @author Jochen
 */
public class DashSubtree extends AbstractType {

	public char getKey() {
		return key;
	}

	private final char key;

	public DashSubtree(char keyCharacter, int startLevel) {
		this.key = keyCharacter;
		this.setSectionFinder(new SubtreeFinder(keyCharacter, startLevel));

		this.addChildType(new DashTreeElement(key));
		// rescursive type definition
		this.addChildType(this);
		this.addChildType(new OverdashedElement(keyCharacter));
		this.addChildType(new CommentLineType());

		setRenderer(AnchorRenderer.getDelegateInstance());
	}

	public DashSubtree(char keyCharacter) {
		this(keyCharacter, 0);
	}

	/**
	 * Finds the subtrees for a given (dash-) level the level is retrieved from
	 * father.
	 * 
	 * @author Jochen
	 */
	class SubtreeFinder implements SectionFinder {

		/**
		 * Determines with how many dashes the top level of the dash tree should
		 * have (usually either zero or one).
		 */
		private final int startLevel;

		/**
		 * Which key-character will be expected (might differ from dash ('-')).
		 */
		private final char key;

		/**
		 * Contains the key char as String to be used in regex, escaped if
		 * regex-meta-character
		 */
		private String keyString;

		/**
		 * Pattern for dash tree level 0
		 */
		private final Pattern p0;

		/**
		 * Pattern for dash tree level 1
		 */
		private Pattern p1;

		/**
		 * The pattern can easily also be generated on demand. However, for
		 * performance reasons the first couple of patterns are precompiled and
		 * reused.
		 */
		private final Pattern p2;
		private final Pattern p3;
		private final Pattern p4;

		public SubtreeFinder(char c, int startLevel) {
			this.startLevel = startLevel;
			key = c;
			p0 = Pattern.compile("^\\s*[\\w\"ÜÖÄüöäß]+.*$",
					Pattern.MULTILINE);

			keyString = "" + c;

			// just to increase speed by reuse of precompiled patterns

			try {
				// check if its a meta-character for regex
				p1 = Pattern.compile(getRegex(keyString, 1),
						Pattern.MULTILINE);
			}
			catch (PatternSyntaxException e) {
				// escape meta-character
				keyString = "\\" + c;
				p1 = Pattern.compile(getRegex(keyString, 1),
						Pattern.MULTILINE);
			}
			p2 = Pattern.compile(getRegex(keyString, 2),
					Pattern.MULTILINE);

			p3 = Pattern.compile(getRegex(keyString, 3),
					Pattern.MULTILINE);

			p4 = Pattern.compile(getRegex(keyString, 4),
					Pattern.MULTILINE);
		}

		private String getRegex(String keyString, int level) {
			return "^\\s*" + keyString + "{" + level + "}" + "[^" + keyString + "]";
		}

		public char getKey() {
			return key;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text,
				Section<?> father, Type type) {

			// if there is no dashTree-father the first level of dashes to be
			// looked for is startLevel
			int level = startLevel;

			Type fatherType = father.get();
			if (fatherType instanceof DashSubtree) {
				level = DashTreeUtils.getDashLevel(father) + 1;
			}

			Matcher m = null;
			ArrayList<SectionFinderResult> result = new ArrayList<SectionFinderResult>();
			if (Strings.isBlank(text)) return result;
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
						m = Pattern.compile(
								"^\\s*" + keyString + "{" + level + "}" + "[^" + keyString + "]",
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
