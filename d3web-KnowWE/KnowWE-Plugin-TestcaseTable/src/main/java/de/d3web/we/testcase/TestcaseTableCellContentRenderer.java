/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.testcase;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableCellContentRenderer;
import de.d3web.we.kdom.table.TableUtils;
import de.d3web.we.utils.KnowWEUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * 
 * @author Florian Ziegler / Sebastian Furth
 * @created 10.08.2010
 */
public class TestcaseTableCellContentRenderer extends TableCellContentRenderer {

	/**
	 * Wraps the content of the cell (sectionText) with the HTML-Code needed for
	 * the table
	 */
	@Override
	protected String wrappContent(String sectionText, Section<TableCellContent> sec, KnowWEUserContext user) {

		int col = TableCellContent.getCol(sec);
		int row = TableCellContent.getRow(sec);

		// Check if there is a valid TimeStamp
		if (col == 0 && row > 0) {

			Section<TimeStampType> timestamp = sec.findSuccessor(TimeStampType.class);
			boolean sort = TableUtils.sortTest(sec);
			boolean validTimeStamp = false;

			if (TimeStampType.isValid(timestamp.getOriginalText())) {
				validTimeStamp = true;
			}

			StringBuilder html = new StringBuilder();

			if (sort) {
				html.append("<th class=\"sort\">");
			}
			else if (validTimeStamp) {
				html.append("<td><div class=\"startTestcase\" onclick=\"return Testcase.runTestcase(this)\"></div>");

			}
			else {
				html.append("<td class=\"invalidTimeStamp\"><div class=\"invalidTimeStamp\">Ungültiger Timestamp</div>");
			}

			generateContent(sectionText, sec, user, sec.getID(), html);

			if (sort) {
				html.append("</th>");
			}

			else {
				html.append("</td>");
				return KnowWEUtils.maskHTML(html.toString());
			}
		}
		
		// No TimeStamp Cell -> Normal Rendering!
		return super.wrappContent(sectionText, sec, user);

	}

}
