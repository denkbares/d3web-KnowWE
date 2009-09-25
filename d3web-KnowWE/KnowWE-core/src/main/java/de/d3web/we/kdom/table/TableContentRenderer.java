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
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * This is a renderer for the TableContent. It wraps the <code>Table</code>
 * tag into an own DIV and delegates the rendering of each <code>TableCellContent</code> 
 * to its own renderer.
 * 
 * @author smark
 */
public class TableContentRenderer extends KnowWEDomRenderer {

	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		
		StringBuilder b = new StringBuilder();
		StringBuilder buffi = new StringBuilder();
		DelegateRenderer.getInstance().render(sec, user, b);
		
		buffi.append( getOpeningTag(sec) );
		buffi.append( generateQuickEdit(sec.getId()));
		
		buffi.append( "<table style='border:1px solid #999999;' class='wikitable knowwetable' border='1'><tbody>" );
		buffi.append(getHeader());
		buffi.append( b.toString() );
		buffi.append( "</tbody></table>" );
		
		if ( sec.hasQuickEditModeSet( user.getUsername() ) ) {
			buffi.append( "<input id=\"" + sec.getId() + "\" type=\"submit\" value=\"save\"/>" );
		}
		
		buffi.append( getClosingTag() );
		
		string.append(KnowWEEnvironment.maskHTML( buffi.toString() ));
	}
	
	/**
	 * Generates a link used to enable or disable the Quick-Edit-Flag.
	 * 
	 * @see UserSetting, UserSettingsManager, NodeFlagSetting
	 * @param topic     name of the current page
	 * @param id        of the section the flag should assigned to
	 * @return
	 */
	protected String generateQuickEdit(String id) {
		String icon = " <img src='KnowWEExtension/images/pencil.png' title='Set QuickEdit-Mode'/>";
		return "<a href=\"javascript:QuickEdit.doTable('" + id + "')\">" + icon + "</a>";
	}
	
	protected String getHeader() {
		return "";
	}
	
	protected String getOpeningTag(Section sec) {
		return "<div class=\"table-edit\" id=\"" + sec.getId() + "\">";
	}
	
	protected String getClosingTag() {
		return "</div>";
	}

}
