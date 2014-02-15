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

import java.util.regex.Pattern;

import org.apache.poi.xwpf.usermodel.XWPFRun;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.jspwiki.types.DefinitionType;
import de.knowwe.jspwiki.types.DefinitionType.DefinitionData;
import de.knowwe.jspwiki.types.DefinitionType.DefinitionHead;
import de.knowwe.jspwiki.types.InlineDefinitionType;

/**
 * Exporter for exporting links. They will be exported in the same style as the
 * surrounding text.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class InlineDefinitionExporter implements Exporter<Type> {

	private final Pattern pattern;

	/**
	 * Create a new exporter that exports inline definitions.
	 */
	public InlineDefinitionExporter() {
		this.pattern = null;
	}

	/**
	 * Create a new exporter that exports normal definitions as inline
	 * definitions if they match a special expression
	 * 
	 * @param regex the expression to be matched
	 */
	public InlineDefinitionExporter(String regex) {
		this.pattern = Pattern.compile(regex);
	}

	@Override
	public boolean canExport(Section<Type> section) {
		if (pattern == null) {
			return (section.get() instanceof InlineDefinitionType);
		}
		else if (section.get() instanceof DefinitionType) {
			Section<DefinitionHead> head = Sections.successor(section, DefinitionHead.class);
			return pattern.matcher(head.getText()).matches();
		}
		else {
			return false;
		}
	}

	@Override
	public Class<Type> getSectionType() {
		return Type.class;
	}

	@Override
	public void export(Section<Type> section, DocumentBuilder manager) throws ExportException {
		ExportUtils.addRequiredSpace(manager);
		XWPFRun run = manager.getParagraph().createRun();
		run.setColor("606060");
		run.setFontSize(7);
		// run.setSubscript(VerticalAlign.SUBSCRIPT);

		Section<DefinitionHead> head = Sections.successor(section, DefinitionHead.class);
		run.setText("(");
		run.setText(head.getText());
		run.setText(": ");

		run = manager.getParagraph().createRun();

		Section<DefinitionData> data = Sections.successor(section, DefinitionData.class);
		run.setText(data.getText());

		run = manager.getParagraph().createRun();
		run.setColor("606060");
		run.setFontSize(7);
		// run.setSubscript(VerticalAlign.SUBSCRIPT);

		run.setText(")");

		run = manager.getParagraph().createRun();

		run.setText(" ");
	}
}
