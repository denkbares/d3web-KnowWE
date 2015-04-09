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

import java.util.regex.Pattern;

import de.d3web.we.kdom.rules.Indent;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.rendering.AnchorRenderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;
import de.knowwe.core.utils.Patterns;

/**
 * One Property definition inside the PropertyMarkup.
 * 
 * @author Markus Friedrich, Albrecht Striffler (denkbares GmbH)
 * @created 10.11.2010
 */
public class PropertyDeclarationType extends AbstractType {

	public static final String QUOTED_NAME = Patterns.QUOTED;
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

		this.addChildType(new PropertyObjectReference());

		this.addChildType(new PropertyType());
		this.addChildType(new LocaleType());
		this.addChildType(new PropertyContentType());
		this.addChildType(new Indent());

		addCompileScript(new PropertyDeclarationHandler());
		this.setRenderer(AnchorRenderer.getDelegateInstance());
	}

}
