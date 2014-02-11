/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.include.export;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.include.export.DocumentBuilder.Style;
import de.knowwe.jspwiki.types.HeaderType;

/**
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class HeaderExporter implements Exporter<HeaderType> {

	@Override
	public Class<HeaderType> getSectionType() {
		return HeaderType.class;
	}

	@Override
	public boolean canExport(Section<HeaderType> section) {
		return true;
	}

	@Override
	public void export(Section<HeaderType> section, DocumentBuilder manager) throws ExportException {
		int marks = section.get().getMarkerCount();
		export(section.get().getHeaderText(section), 4 - marks, manager);
	}

	public static void export(String headerText, int headingLevel, DocumentBuilder manager) {
		Style style = Style.heading(headingLevel);
		manager.getNewParagraph(style);
		manager.append(headerText);
		manager.closeParagraph();
	}
}
