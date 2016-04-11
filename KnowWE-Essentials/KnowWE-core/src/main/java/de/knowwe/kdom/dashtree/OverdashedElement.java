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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;
import de.knowwe.core.report.Messages;

/**
 * Type to detect Too-much-dashes-Erros in DashTree-Markup This error is first
 * ignored by the dash-tree parser (leaves it as plaintext/comment) This type
 * catches it to render it highlighted.
 * 
 * 
 * @author Jochen
 * 
 * 
 * 
 */
public class OverdashedElement extends AbstractType {

	public OverdashedElement(char keyCharacter) {

		this.addCompileScript(new OverDashedErrorHandler());

		this.setSectionFinder(new OverDashedSectionFinder(keyCharacter));
	}

	class OverDashedSectionFinder implements SectionFinder {

		private final char keyChar;

		public OverDashedSectionFinder(char c) {
			keyChar = c;
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			// when there is no father, one dash is too much
			int level = 1;

			// IMPORTANT: +2
			if (father.get() instanceof DashSubtree) {
				level = DashTreeUtils.getDashLevel(father) + 2;
			}
			Pattern pattern = null;

			try {
				// check if its a meta-character for regex
				pattern = Pattern.compile(getRegexString("" + keyChar, level),
						Pattern.MULTILINE);
			}
			catch (PatternSyntaxException e) {
				// is a meta-character obviously
				pattern = Pattern.compile(getRegexString("\\" + keyChar, level),
						Pattern.MULTILINE);
			}

			Matcher m = pattern.matcher(text);
			if (m.find()) {

				return SectionFinderResult.singleItemList(new SectionFinderResult(
						m.start(1), m.start(1) + level));
			}
			return null;
		}

		private String getRegexString(String key, int level) {
			return "^\\s*" + "(" + key + "{" + level + "})";
		}

	}

	class OverDashedErrorHandler extends DefaultGlobalScript<OverdashedElement> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<OverdashedElement> section) {
			Messages.storeMessage(compiler, section, getClass(),
					Messages.syntaxError("to many dashes; remove \"-\""));
		}
	}
}
