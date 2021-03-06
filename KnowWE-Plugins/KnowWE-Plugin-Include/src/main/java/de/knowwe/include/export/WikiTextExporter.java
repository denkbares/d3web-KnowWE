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

import com.denkbares.strings.Strings;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.jspwiki.types.WikiTextType;

/**
 * @author Volker Belli (denkbares GmbH)
 * @created 07.02.2014
 */
public class WikiTextExporter implements Exporter<WikiTextType> {

	@Override
	public boolean canExport(Section<WikiTextType> section) {
		return true;
	}

	@Override
	public Class<WikiTextType> getSectionType() {
		return WikiTextType.class;
	}

	@Override
	public void export(Section<WikiTextType> section, DocumentBuilder manager) throws ExportException {
		String text = Strings.trimBlankLinesAndTrailingLineBreak(section.getText());
		if (Strings.isBlank(text)) return;

		// Split lines by two or more '\' characters
		// Also ignore trailing and leading whitespaces surrounding the returns
		int from = 0;
		while (from <= text.length()) {
			// append line break after the first line
			if (from > 0) manager.appendLineBreak();

			// find next manual line break, or the end of text if no such line break is available
			int to = text.indexOf("\\\\", from);
			if (to == -1) to = text.length();

			// append the line
			// replace (multiple) tab/spaces/returns by one space
			// this can be done, because multiple paragraphs are NOT (!) included in one WikiTextType section
			// but the wiki allows single line breaks as floating text
			String line = text.substring(from, to);

			// remove spaces between the manual line break "\\" and the text
			if (from > 0) line = Strings.trimLeft(line);
			if (to < text.length()) line = Strings.trimRight(line);
			// unescape JSPWiki escape character "~" and potentially used HTML entities
			line = line.replaceAll("~([_'|~\\\\])", "$1");
			line = Strings.decodeHtml(line);
			// and also remove multiple space characters, which are ignored in HTML but not in target format
			manager.append(line.replaceAll("\\s+", " "));

			// proceed to next line, skipping all '\\' (at least two
			from = to + 2;
			while (from < text.length() && text.charAt(from) == '\\') from++;
		}
	}
}
