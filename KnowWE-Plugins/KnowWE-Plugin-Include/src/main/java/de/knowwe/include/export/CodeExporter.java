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
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.PrettifyType;
import de.knowwe.jspwiki.types.VerbatimType;

/**
 * Class to export to-do Markup. Unfortunately we do not have access to that markup here, cause it
 * is not open source yet. To avoid any conflicts we handle this as a special kind of formatted
 * default markup.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class CodeExporter implements Exporter<Type> {

	public static final CodeExporter VERBATIM = new CodeExporter(
			VerbatimType.class, "^\\{\\{\\{", "\\}\\}\\}$");

	public static final CodeExporter PRETTIFY = new CodeExporter(
			PrettifyType.class, "(?im)^%%prettify[\\s\\r\\n]*\\{\\{\\{", "(?im)\\}\\}\\}[\\s\\r\\n]*/?%$");

	private final List<String> users = new LinkedList<>();
	private final Class<Type> type;
	private final String prefix;
	private final String postfix;

	private <T extends Type> CodeExporter(Class<T> type, String prefix, String postfix) {
		//noinspection unchecked
		this.type = (Class) type;
		this.prefix = prefix;
		this.postfix = postfix;
	}

	@Override
	public Class<Type> getSectionType() {
		return type;
	}

	@Override
	public void export(Section<Type> section, DocumentBuilder manager) throws ExportException {
		// preformatted code: make each line a paragraph
		String text = Strings.trim(section.getText())
				.replaceAll(prefix, "").replaceAll(postfix, "");
		String[] lines = Strings.trimBlankLinesAndTrailingLineBreak(text).split("\n\r?");
		for (String line : lines) {
			XWPFParagraph paragraph = manager.getNewParagraph(DocumentBuilder.Style.code);
			CTR ctr = paragraph.getCTP().addNewR();
			ctr.addNewRPr();
			ctr.addNewT().setStringValue(line + "\n\r");
			manager.closeParagraph();
		}
	}
}
