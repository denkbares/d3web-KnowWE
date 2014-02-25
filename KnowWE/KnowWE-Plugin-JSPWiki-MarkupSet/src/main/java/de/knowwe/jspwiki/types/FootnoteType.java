/*
 * Copyright (C) 2011 denkbares GmbH
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

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * Type matching footnote definitions. The references to footnotes are still
 * inner-wiki links. Thus they are represented by section of type the
 * {@link LinkType}. These sections will return <code>true</code> for
 * {@link LinkType#isFootnote(de.knowwe.core.kdom.parsing.Section)}
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 19.02.2014
 */
public class FootnoteType extends AbstractType {

	private static final String INTRO = "^\\s*\\[#\\d+\\]\\s*";

	private static class FootnoteIntro extends AbstractType {

		public FootnoteIntro() {
			setSectionFinder(new RegexSectionFinder(INTRO));
			addChildType(new KeywordType("[#", true));
			addChildType(new KeywordType("]", true));
			addChildType(new FootnoteID());
		}
	}

	private static class FootnoteID extends AbstractType {

		public FootnoteID() {
			setSectionFinder(new RegexSectionFinder("\\d+"));
		}
	}

	public FootnoteType() {
		this.setSectionFinder(new RegexSectionFinder(INTRO + ".*$", Pattern.MULTILINE));

		this.addChildType(new FootnoteIntro());
		this.addChildType(new ParagraphTypeForLists());
	}

	public String getFootnoteID(Section<FootnoteType> section) {
		return Sections.successor(section, FootnoteID.class).getText();
	}
}
