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
package de.d3web.we.kdom.xcl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.utils.Patterns;

/**
 * A Markup for MMInfos.
 * 
 * @author Reinhard Hatko Created on: 03.12.2009
 */
public class DCPropertyType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		super.init();
		childrenTypes.add(new DCPropertyNameType());
		childrenTypes.add(new DCPropertyContentType());

		setSectionFinder(new RegexSectionFinder(Patterns.DCPROPERTY, Pattern.MULTILINE));
		addSubtreeHandler(new DCPropertySubtreeHandler());
	}

	public static void main(String[] args) {
		Pattern p =
				Pattern.compile(
						"\\{([^\\{\\}]*)\\}",
						Pattern.MULTILINE);

		String test = "@Info {\r\n" +
				"Aktuellen Druckabfall der Wasserfalle prüfen und Wasserfalle tauschen:\r\n" +
				"- Hinweis: Sampleline abnehmen, Nullung durchführen, \r\n" +
				"  Pumpe einschalten, Druckdifferenz messen\r\n" +
				"- Wasserfalle tauschen\r\n" +
				"Zusätzliche Hinweise:\r\n" +
				"- Der Selftest Primus ist nicht durchgelaufen\r\n" +
				"- Nullungsfehler trat am nächsten Morgen auf\r\n" +
				"}";
		Matcher matcher = p.matcher(test);

		// System.out.println("look:" + matcher.lookingAt());
		// System.out.println("matches:" + matcher.matches());

		int i = 0;
		while (matcher.find()) {
			System.out.println(i++);
			System.out.println(test.substring(matcher.start(), matcher.end()));

			for (int j = 0; j <= matcher.groupCount(); j++) {
				System.out.println("Group " + j + ": " + matcher.group(j));
				System.out.println("*");
			}
		}
		System.out.println();
		System.out.println();

	}

}
