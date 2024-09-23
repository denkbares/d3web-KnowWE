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
package de.knowwe.core.kdom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;

import de.knowwe.core.Environment;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.PageTitleTermCompileScript;
import de.knowwe.core.kdom.basicType.PlainText;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

/**
 * Root of every page/article/kdom...
 *
 * @author Reinhard Hatko Created on: 17.12.2009
 */
public class RootType extends AbstractType {

	private static final int CONTEXT_WINDOWS = 50;
	private static final String TARGET = "↯";

	public RootType() {
		this.addCompileScript(Priority.HIGHEST, new PageTitleTermCompileScript());
		this.addCompileScript(new DefaultGlobalCompiler.DefaultGlobalScript<RootType>() {
			@Override
			public void compile(DefaultGlobalCompiler compiler, Section<RootType> section) throws CompilerMessage {
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

			private @NotNull String getContext(Section<RootType> section, Matcher matcher) {
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
		this.setSectionFinder(AllTextFinder.getInstance());
		this.setRenderer((section, user, string) -> {
			Map<de.knowwe.core.compile.Compiler, Collection<Message>> messages = Messages.getMessagesMap(section);
			for (Entry<de.knowwe.core.compile.Compiler, Collection<Message>> entry : messages.entrySet()) {
				for (Message message : entry.getValue()) {
					String tag = (message.getType() == Message.Type.ERROR)
							? "error"
							: (message.getType() == Message.Type.WARNING)
							? "warning"
							: "information";
					string.append("\n%%").append(tag).append("\n");
					string.append(message.getVerbalization());
					string.append("\n/%\n\n");
				}
			}
			DelegateRenderer.getInstance().render(section, user, string);
		});
	}

	public static RootType getInstance() {
		if (Environment.getInstance() != null) {
			return Environment.getInstance().getRootType();
		}
		else {
			// is a dummy object for testing only
			return new RootType();
		}
	}
}
