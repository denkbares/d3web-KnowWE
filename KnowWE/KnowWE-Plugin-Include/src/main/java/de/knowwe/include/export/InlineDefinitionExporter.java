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

import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.InlineDefinitionType;

/**
 * Exporter for exporting links. They will be exported in the same style as the
 * surrounding text.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class InlineDefinitionExporter implements Exporter<InlineDefinitionType> {

	/**
	 * Create a new exporter that exports inline definitions.
	 */
	public InlineDefinitionExporter() {
	}

	@Override
	public boolean canExport(Section<InlineDefinitionType> section) {
		return true;
	}

	@Override
	public Class<InlineDefinitionType> getSectionType() {
		return InlineDefinitionType.class;
	}

	@Override
	public void export(Section<InlineDefinitionType> section, DocumentBuilder manager) throws ExportException {
		ExportUtils.addRequiredSpace(manager);
		String refID = HeaderExporter.getCrossReferenceID(section);
		XWPFRun run = HeaderExporter.createCrossReferenceRun(refID, manager);
		run.setColor("606060");
		run.setFontSize(7);

		run.setText("(");
		run.setText(section.get().getHeadText(section));
		run.setText(": ");

		manager.export(section.get().getDataSection(section));

		run = manager.getParagraph().createRun();
		run.setColor("606060");
		run.setFontSize(7);
		run.setText(")");

		run = manager.getParagraph().createRun();
		run.setText(" ");
	}
}
