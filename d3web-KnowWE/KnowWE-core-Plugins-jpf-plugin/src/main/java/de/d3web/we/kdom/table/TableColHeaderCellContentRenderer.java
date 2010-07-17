/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.table;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TableColHeaderCellContentRenderer extends TableCellContentRenderer{



	/**
	 * Wraps the content of the cell (sectionText) with the HTML-Code needed for the table
	 */
	@Override
	protected String wrappContent(String sectionText, Section sec, KnowWEUserContext user) {


		String sectionID = sec.getID();
		StringBuilder html = new StringBuilder();
		html.append("<td>   ");
		generateContent(sectionText, sec, user, sectionID, html);
		html.append( "</td>" );
		return KnowWEEnvironment.maskHTML( html.toString() );
	}

}
