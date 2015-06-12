/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

import java.util.LinkedList;
import java.util.List;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTR;

import de.d3web.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.VerbatimType;

/**
 * Class to export to-do Markup. Unfortunately we do not have access to that markup here, cause it is not open source
 * yet. To avoid any conflicts we handle this as a special kind of formatted default markup.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class CodeExporter implements Exporter<VerbatimType> {

	private final List<String> users = new LinkedList<>();

	@Override
	public Class<VerbatimType> getSectionType() {
		return VerbatimType.class;
	}

	@Override
	public void export(Section<VerbatimType> section, DocumentBuilder manager) throws ExportException {
		// preformatted code: make each line a paragraph
		String[] lines = Strings.trim(section.getText()).split("\n\r?");
		for (String line : lines) {
			XWPFParagraph paragraph = manager.getNewParagraph(DocumentBuilder.Style.code);
			CTR ctr = paragraph.getCTP().addNewR();
			/*CTRPr ctrPr =*/ ctr.addNewRPr();
			ctr.addNewT().setStringValue(line + "\n\r");
			manager.closeParagraph();
		}
	}
}
