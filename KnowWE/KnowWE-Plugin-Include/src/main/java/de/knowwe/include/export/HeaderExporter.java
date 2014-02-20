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

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBookmark;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.include.export.DocumentBuilder.Style;
import de.knowwe.jspwiki.types.HeaderType;

/**
 * Exports header of the document for any level.
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
		String headerText = section.get().getHeaderText(section);
		int marks = section.get().getMarkerCount();
		export(headerText, 4 - marks, getCrossReferenceID(section), manager);
	}

	public static void export(String headerText, int headingLevel, String refID, DocumentBuilder manager) {
		Style style = Style.heading(headingLevel);
		manager.getNewParagraph(style);
		createCrossReferenceRun(refID, manager).setText(headerText);
		manager.closeParagraph();
	}

	public static XWPFRun createCrossReferenceRun(String refID, DocumentBuilder builder) {
		XWPFParagraph paragraph = builder.getParagraph();
		if (refID == null) {
			return paragraph.createRun();
		}
		else {
			CTP ctp = paragraph.getCTP();
			CTBookmark start = ctp.addNewBookmarkStart();
			start.setName(refID);
			XWPFRun run = paragraph.createRun();
			ctp.addNewBookmarkEnd();
			return run;
		}
	}

	public static String getCrossReferenceID(Section<?> headerOrRootSection) {
		if (headerOrRootSection == null) return null;
		return "_Ref" + Long.parseLong(headerOrRootSection.getID(), 16);
	}
}
