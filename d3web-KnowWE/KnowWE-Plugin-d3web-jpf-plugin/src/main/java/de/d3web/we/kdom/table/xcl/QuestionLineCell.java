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

package de.d3web.we.kdom.table.xcl;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.contexts.Context;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.table.TableCellContent;
import de.d3web.we.kdom.table.TableCellContentRenderer;
import de.d3web.we.kdom.table.Table;
import de.d3web.we.kdom.table.TableUtils;
import de.d3web.we.utils.KnowWEObjectTypeUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author jochen
 * 
 * A type for table-cells in lines, which have a question in the first column (headerCol)
 *
 */
public class QuestionLineCell extends TableCellContent {
	
	private static QuestionLineCell instance;
	
	public static QuestionLineCell getInstance() {
		if (instance == null) {
			instance = new QuestionLineCell();
		}

		return instance;
	}

	@Override
	public KnowWEDomRenderer getRenderer() {
		return QuestionLineCellRenderer.getInstance();
	}
	
	
	
}
class QuestionLineCellRenderer extends TableCellContentRenderer {

	/**
	 * Wraps the content of the cell (sectionText) with the HTML-Code needed for the table
	 */
	@Override
	protected String wrappContent(String sectionText, Section sec, KnowWEUserContext user) {

		int col = TableUtils.getColumn(sec);
		
		Context context  = ContextManager.getInstance().getContext(sec,KnowWEObjectTypeUtils.getAncestorOfType(sec,Table.class).getID()+"_col"+col);
		
		String sectionID = sec.getID();
		StringBuilder html = new StringBuilder();
		html.append( "<td style='background-color:#EEEEEE;'>   " );
		generateContent(sectionText, sec, user, sectionID, html);
		html.append( "</td>" );
		return KnowWEEnvironment.maskHTML( html.toString() );
	}
	
	private static QuestionLineCellRenderer instance = null;
	
	public static QuestionLineCellRenderer getInstance() {
		if(instance == null)
			instance = new QuestionLineCellRenderer();
		return instance;
	}
}