/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.knowwe.kdom.defaultMarkup;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.AnchorRenderer;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

public final class ContentType extends AbstractType {

	private final DefaultMarkup markup;

	public ContentType(DefaultMarkup markup) {
		this.markup = markup;
		this.setSectionFinder(new ContentFinder(markup));
		this.setRenderer(AnchorRenderer.getDelegateInstance());
		Type[] types = this.markup.getTypes();
		for (Type type : types) {
			this.addChildType(type);
		}
	}

	/**
	 * Return the name of the content type section.
	 */
	@Override
	public String getName() {
		return "content";
	}

	private static class ContentFinder implements SectionFinder {

		private final static int FLAGS =
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL;

		private final static String START_TAG =
				"^\\p{Blank}*%%$NAME$\\p{Blank}*[:=]?\\h*?(?:\\r?\\n)?+";

		private final static String CONTENT =
				"(?:(?!$LINESTART$\\h*@$IGNORES$\\w+).)*?";

		private final static String END_TAG =
				"(?:^\\p{Blank}*[/%]?%\\p{Blank}*$|\\z|$LINESTART$\\h*@$IGNORES$\\w+$INLINEENDTAG$)";

		private final static String REGEX_TEMPLATE = START_TAG + "(" + CONTENT + ")" + END_TAG;

		private final Pattern startPattern;

		private final AdaptiveMarkupFinder adaptiveFinder;

		public ContentFinder(DefaultMarkup markup) {

			String regex = REGEX_TEMPLATE;
			if (markup.isInline()) {
				regex = regex.replace("$INLINEENDTAG$", "|\\h*%%|\\h*/%").replace("$LINESTART$", "^|");
			}
			else {
				regex = regex.replace("$INLINEENDTAG$", "");
			}
			Set<String> ignoredAnnotations = markup.getIgnoredAnnotations();
			if (ignoredAnnotations.isEmpty()) {
				regex = regex.replace("$IGNORES$", "");
			}
			else {
				regex = regex.replace("$IGNORES$", "(?!" + Strings.concat("|", ignoredAnnotations) + ")");
			}
			adaptiveFinder = new AdaptiveMarkupFinder(markup.getName(), regex, FLAGS, 1, true);
			startPattern = Pattern.compile(START_TAG.replace("$NAME$", markup.getName()), FLAGS);
		}

		@Override
		public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
			List<SectionFinderResult> results = adaptiveFinder.lookForSections(text, father, type);
			if (results.isEmpty()) {
				// if no content was found, we try to create a zero length
				// result after the start tag
				Matcher matcher = startPattern.matcher(text);
				if (matcher.find()) {
					SectionFinderResult result =
							new SectionFinderResult(matcher.end(), matcher.end());
					results.add(result);
				}
			}
			return results;
		}
	}

}
