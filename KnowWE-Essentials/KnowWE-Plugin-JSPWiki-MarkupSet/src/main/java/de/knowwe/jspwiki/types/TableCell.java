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

import java.util.regex.Pattern;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.basicType.KeywordType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DefaultTextRenderer;
import de.knowwe.core.kdom.sectionFinder.RegexSectionFinder;

/**
 * 
 * @author Lukas Brehl
 * @created 26.09.2012
 */
public class TableCell extends AbstractType {

	private static class Declaration extends KeywordType {

		public Declaration() {
			super(Pattern.compile("^\\s*\\|\\|?"));
			setRenderer(DefaultTextRenderer.getInstance());
		}
	}

	public TableCell() {
		this.setSectionFinder(new RegexSectionFinder("\\|\\|?((\\[[^\\]]*\\])|[^|\\[]+)+"));
		this.addChildType(new Declaration());
		this.addChildType(new ParagraphTypeForLists());
	}

	/**
	 * Returns if the specified cell is a header cell.
	 * 
	 * @created 10.02.2014
	 * @param section the section to check to be a header cell
	 * @return true if the cell is a header cell
	 */
	public boolean isHeader(Section<TableCell> section) {
		return Sections.successor(section, Declaration.class).getText().contains("||");
	}
}
