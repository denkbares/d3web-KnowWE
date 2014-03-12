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

import java.math.BigInteger;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.include.export.DocumentBuilder.Style;
import de.knowwe.jspwiki.types.DefinitionType;

/**
 * Class to export definitions as unordered list with bold definition text.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class DefinitionExporter implements Exporter<DefinitionType> {

	@Override
	public boolean canExport(Section<DefinitionType> section) {
		return section.get().getClass() == getSectionType();
	}

	@Override
	public Class<DefinitionType> getSectionType() {
		return DefinitionType.class;
	}

	@Override
	public void export(Section<DefinitionType> section, DocumentBuilder manager) throws ExportException {
		XWPFDocument document = manager.getDocument();
		BigInteger abstractID = ListExporter.getAbstractIdUnordered(document);
		BigInteger numID = document.getNumbering().addNum(abstractID);
		XWPFParagraph paragraph = manager.getNewParagraph(Style.list);
		paragraph.setNumID(numID);
		paragraph.getCTP().getPPr().getNumPr().addNewIlvl().setVal(BigInteger.valueOf(0));

		ListBuilder listBuilder = new ListBuilder(manager);

		listBuilder.setBold(true);
		listBuilder.export(section.get().getHeadSection(section));
		listBuilder.setBold(false);
		manager.append(": ");
		manager.getParagraph().createRun().addCarriageReturn();

		listBuilder.export(section.get().getDataSection(section));
		manager.closeParagraph();
	}
}
