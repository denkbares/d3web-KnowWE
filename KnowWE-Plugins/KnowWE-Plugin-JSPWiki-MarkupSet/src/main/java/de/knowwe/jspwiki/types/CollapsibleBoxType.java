/*
 * Copyright (C) 2012 denkbares GmbH
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.SectionFinder;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

/**
 * @author Lukas Brehl
 * @created 21.11.2012
 */
public class CollapsibleBoxType extends AbstractType {

	public CollapsibleBoxType() {
		this.setSectionFinder(new CollapsibleBoxSectionFinder());
		this.addChildType(new SectionHeaderType());
		this.addChildType(new SectionHeaderType());
		this.addChildType(new SectionContentType());
	}

	public static class CollapsibleBoxSectionFinder implements SectionFinder {

		private static final Pattern END_SIGNS = Pattern.compile("^\\h*(%%|/%|%)\\h*$");

		@Override
		public List<SectionFinderResult> lookForSections(String text,
														 Section<?> father, Type type) {
			List<SectionFinderResult> results = new ArrayList<>();
			// marker is the index, where i start to look for types
			int marker = 0;
			while (marker != text.length()) {
				int depth = 0;
				int start = text.indexOf("%%collapsebox", marker);
				if (start == -1) {
					return results;
				}
				String[] split = text.substring(start).split("\n");
				if (split.length <= 1) {
					return results;
				}
				depth++;
				for (int i = 1; i < split.length; i++) {
					// if another markup starts
					String token = split[i];
					if (token.matches("%%\\S.*?\\s")) {
						depth++;
					}
					Matcher endSigns = END_SIGNS.matcher(token);
					// if a markup ends
					while (endSigns.find()) {
						depth--;
					}
					marker = start + i + Stream.of(split).limit(i).mapToInt(String::length).sum();
					if (depth == 0) {
						int end = start + i + Stream.of(split).limit(i).mapToInt(String::length).sum() + token.length();
						SectionFinderResult result = new SectionFinderResult(start, end);
						results.add(result);
						marker = end;
						break;
					}
				}
			}
			return null;
		}
	}
}
