/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.d3web.property;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.knowwe.core.compile.IncrementalConstraint;
import de.knowwe.core.compile.IncrementalMarker;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.Patterns;

/**
 * One Property definition inside the PropertyMarkup.
 * 
 * @author Markus Friedrich, Albrecht Striffler (denkbares GmbH)
 * @created 10.11.2010
 */
public class PropertyDeclarationType extends AbstractType implements IncrementalMarker, IncrementalConstraint<PropertyDeclarationType> {

	public static final String QUOTED_NAME = Patterns.quoted;
	public static final String UNQUOTED_NAME = "[^\".=#\\n\\r]*";
	public static final String NAME = "(?:" + QUOTED_NAME + "|" + UNQUOTED_NAME + ")";

	public PropertyDeclarationType() {

		String tripleQuotes = "\"\"\"";
		String noTripleQuotes = "(?!" + tripleQuotes + ")";
		String anyCharButNoTripleQuotes = "(?:" + noTripleQuotes + ".)";

		String singleLinePropertyDeclaration = anyCharButNoTripleQuotes + "+?$\\s*"
						+ "(?!\\s*" + tripleQuotes + ")";
		// the singleLinePropertyDeclaration is a line that does not contain
		// triple quotes and also is not followed by a line that starts (maybe
		// after some white spaces) with a triple quote

		String multiLinePropertyDeclaration = ".+?" + tripleQuotes + ".+?" + tripleQuotes;

		String propertyDeclaration = "^\\s*("
				+ singleLinePropertyDeclaration + "|"
				+ multiLinePropertyDeclaration + ")\\s*?(^|\\z)";

		Pattern p = Pattern.compile(propertyDeclaration, Pattern.MULTILINE + Pattern.DOTALL);
		setSectionFinder(new RegexSectionFinder(p, 1));

		this.childrenTypes.add(new NamedObjectReference());

		this.childrenTypes.add(new PropertyType());
		this.childrenTypes.add(new LocaleType());
		this.childrenTypes.add(new PropertyContentType());

		addSubtreeHandler(Priority.LOW, new PropertyDeclarationHandler());
	}

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<PropertyDeclarationType> s) {
		return KnowWEUtils.getTerminologyHandler(
				article.getWeb()).areTermDefinitionsModifiedFor(article);
	}

	public static void main(String[] args) {
		String test = "Year of birth.date_format = yyyy\n"
				+
				"Date of operation that caused the incisional hernia.date_format = dd.MM.yyyy OR MM.yyyy OR yyyy\n"
				+
				"Date of the last repair.date_format = dd.MM.yyyy OR MM.yyyy OR yyyy\n\n"
				+
				"Operation date.date_format = dd.MM.yyyy\n\n"
				+
				"\"Start of operation (first incision)\".date_format = \n \"\"\"\n HH:mm:ss OR\nHH:mm\n\"\"\"\n"
				+
				"\"End of operation (last skin suture)\".date_format = HH:mm:ss OR HH:mm\n"
				+
				"Date of discharge.date_format = dd.MM.yyyy\n" +
				"Hour of discharge.date_format = HH\n" +
				"\"Date of death (Follow up 1)\".date_format = dd.MM.yyyy\n" +
				"\"Date of follow up (Follow up 1)\".date_format = dd.MM.yyyy\n";

		String trippleQuotes = "\"\"\"";
		String noTrippleQuotes = "(?!" + trippleQuotes + ")";
		String noTrippleQuotesFollowedByAnyChar = "(?:" + noTrippleQuotes + ".)";

		// and also the next line does not start with the ESCAPE pattern
		String singleLinePropertyDeclaration = "^"
				+ noTrippleQuotesFollowedByAnyChar + "+?$"
				+ "(?!\\s*" + trippleQuotes + ")";

		// at the start of the line there is first some stuff till there is some
		// more stuff between two ESCAPE patterns... after the second ESCAPE
		// pattern, only white spaces are allowed till the line end
		String multiLinePropertyDeclaration = "^"
				+ noTrippleQuotesFollowedByAnyChar + "+?"
				+ trippleQuotes
				+ noTrippleQuotesFollowedByAnyChar + "+?"
				+ trippleQuotes + "\\s*$";

		// either single or multi line
		String propertyDeclaration = singleLinePropertyDeclaration + "|"
				+ multiLinePropertyDeclaration;
		Pattern p = Pattern.compile(propertyDeclaration, Pattern.MULTILINE + Pattern.DOTALL);
		System.out.println(test);
		System.out.println(noTrippleQuotesFollowedByAnyChar);
		System.out.println(propertyDeclaration);
		Matcher matcher = p.matcher(test);
		while (matcher.find()) {
			System.out.println(matcher.group());
			System.out.println("--");
		}

	}
}
