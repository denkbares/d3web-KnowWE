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

package de.knowwe.core.kdom.basicType;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.CompilerMessage;

/**
 * This type is the terminal-type of the Knowledge-DOM. All leafs and only the
 * leafs of the KDMO-tree are of this type. If a type has no findings for the
 * allowed children or has no allowed children one son of this type is created
 * to end the recursion.
 *
 * @author Jochen
 */
public class PlainText extends AbstractType {

	private static final int CONTEXT_WINDOWS = 50;
	private static final String TARGET = "↯";
	private static PlainText instance;

	public static synchronized PlainText getInstance() {
		if (instance == null) {
			instance = new PlainText();
		}
		return instance;
	}

	public PlainText() {
		this.setRenderer(DefaultTextRenderer.getInstance());
		this.setSectionFinder(AllTextFinder.getInstance());
		this.addCompileScript(new DefaultGlobalCompiler.DefaultGlobalScript<PlainText>() {
			@Override
			public void compile(DefaultGlobalCompiler compiler, Section<PlainText> section) throws CompilerMessage {
				String text = section.getText();
				String regex = "[\u00A0\u2000-\u200B\u202F\u205F\u2060\u3000\u180E]";

				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(text);

				List<String> foundSpaces = new ArrayList<>();
				while (matcher.find()) {
					String context = getContext(section, matcher);
					foundSpaces.add("Found problematic " + getWhitespaceDescription(matcher.group()) + " in the following text, here shown as " + TARGET + ". Remove the character to get rid of this warning.\n" + context.replaceAll("\n", "\\\\n"));
				}
				if (!foundSpaces.isEmpty()) {
					throw CompilerMessage.warning(String.join("\n", foundSpaces));
				}
			}

			private @NotNull String getContext(Section<PlainText> section, Matcher matcher) {
				int indexStart = section.getOffsetInArticle() + matcher.start();
				int indexEnd = section.getOffsetInArticle() + matcher.end();
				String articleText = section.getArticle()
						.getText();
				String contextBefore = articleText.substring(Math.max(0, indexStart - CONTEXT_WINDOWS), indexStart);
				String contextAfter = articleText.substring(indexEnd, Math.min(articleText.length(), indexEnd + CONTEXT_WINDOWS));
				return "..." + contextBefore + TARGET + contextAfter + "...";
			}

			// Methode zur Beschreibung der gefundenen Whitespaces
			private static String getWhitespaceDescription(String match) {
				return switch (match) {
					case "\u00A0" -> "non-breaking space";
					case "\u200B" -> "zero width space";
					case "\u2003" -> "em space";
					case "\u3000" -> "ideographic space";
					default -> {
						// Für Unicode Whitespaces innerhalb \u2000-\u200A
						if (match.codePointAt(0) >= 0x2000 && match.codePointAt(0) <= 0x200A) {
							yield String.format("unicode whitespace (U+%04X)", (int) match.charAt(0));
						}
						yield "unknown white-space";
					}
				};
			}
		});
	}

	/**
	 * prevent cloning
	 */
	@Override
	public Object clone()
			throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
