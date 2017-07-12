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

package de.knowwe.core.kdom.sectionFinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.denkbares.utils.Log;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

public class RegexSectionFinder implements SectionFinder {

	private final Pattern pattern;
	private final int group;

	public RegexSectionFinder(String p) {
		this(p, 0);
	}

	public RegexSectionFinder(String p, int patternmod) {
		this(p, patternmod, 0);
	}

	/**
	 * creates sections that reflect the content of the group <code>group</code>
	 * .
	 */
	public RegexSectionFinder(String p, int patternmod, int group) {
		this(Pattern.compile(p, patternmod), group);
	}

	public RegexSectionFinder(Pattern pattern) {
		this(pattern, 0);
	}

	public RegexSectionFinder(Pattern pattern, int group) {
		this.pattern = pattern;
		this.group = group;
	}

	@Override
	public List<SectionFinderResult>
	lookForSections(String text, Section<?> father, Type type) {
		ArrayList<SectionFinderResult> result = new ArrayList<>();
		Matcher m = pattern.matcher(text);
		/*
		 * vb: changed this behavior. It now proceeds matching after the last
		 * character USED, instead of the last character MATCHED by the regexp.
		 * This is essential if your mark-up terminates at the beginning of the
		 * next mark-up with no explicit termination expression, because in this
		 * case, the start of the next expression is part of the regular
		 * expression.
		 * 
		 * Example: "#foo: bla #goo: blub"
		 * 
		 * There the end of the "#foo:"-Block will be detected due to the
		 * beginning of the "#goo:"-Block. Therefore "#" may be in the
		 * expression to match the "#foo:"-Block. Nevertheless, the next
		 * matching must start after "#foo: bla" and NOT after ""#foo: bla #".
		 * 
		 * Behavior is identical for "this.group == 0". It is also identical for
		 * the existing instances with "this.group > 0".
		 */
		int index = 0;
		try {

			while (m.find(index)) {
				if (m.group(group) != null) {
					result.add(createSectionFinderResult(m));
				}
				int next = m.end(group);
				// avoid endless iterations with "wrong" expressions
				if (next <= index) break;
				// detect if we reached the end,
				// otherwise we get an IndexOutOfBoundsException from "m.find(...)"
				if (next >= text.length()) break;
				index = next;
			}
			return result;
		}
		catch (StackOverflowError e) {
			String message = "Stack overflow in regex for type " + type.getClass()
					.getSimpleName() + (father == null ? "" : " on article '" + father.getTitle() + "'");
			Log.severe(message);
			return Collections.emptyList();
		}

	}

	protected SectionFinderResult createSectionFinderResult(Matcher m) {
		return new SectionFinderResult(m.start(group), m.end(group));
	}

}
