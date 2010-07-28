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

import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.DelegateRenderer;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.KnowWEUtils;
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
	public void render(KnowWEArticle article, Section sec, KnowWEUserContext user, StringBuilder string) {
		
		final ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
			
		StringBuilder b = new StringBuilder();
		StringBuilder buffi = new StringBuilder();
		DelegateRenderer.getInstance().render(article, sec, user, b);
		
		buffi.append( getOpeningTag(sec) );
		if (!sec.hasQuickEditModeSet(user.getUsername())) {
			buffi.append(generateQuickEdit(sec.getID(),
					rb.getString("KnowWE.TableContentRenderer.setQE")));
		}
		
		if (sec.hasQuickEditModeSet(user.getUsername())) {

			// adds the buttons for addRow/addCol
			buffi.append("<table style='border:1px solid #999999; float: left' class='wikitable knowwetable' border='1'><tbody>");
			buffi.append(getHeader());
			buffi.append(b.toString());
			buffi.append("</tbody></table>");
			buffi.append("<div id=\"addCol\" class=\"addCol\" title=\"Spalte hinzufügen\" onclick=\"return Testcase.addCol(this)\"></div>");
			buffi.append("<div style=\"width: 50%; clear: left\"></div>");
			buffi.append("<div id=\"addRow\" class=\"addRow\" title=\"Zeile hinzufügen\" onclick=\"return Testcase.addRow(this)\"></div>");

			buffi.append("<input class=\"pointer\" id=\""
					+ sec.getID()
					+ "\" style=\"padding:0 0 0 0; width: 25px; height: 25px; background: #FFF url(KnowWEExtension/images/msg_checkmark.png) no-repeat; border: none; vertical-align:top;\" name=\""
					+ sec.getID() + "_accept\" type=\"submit\" value=\"\" title=\""
					+ rb.getString("KnowWE.TableContentRenderer.accept") + "\">");
			buffi.append("<img class=\"quickedit table pointer\" id=\"" + sec.getID()
					+ "_cancel\" width=\"25\" title=\""
					+ rb.getString("KnowWE.TableContentRenderer.cancel")
					+ "\" src=\"KnowWEExtension/images/msg_cross.png\"/>");

		}
		else {
			buffi.append("<table style='border:1px solid #999999;' class='wikitable knowwetable' border='1'><tbody>");
			buffi.append(getHeader());
			buffi.append(b.toString());
			buffi.append("</tbody></table>");
		}
		

		buffi.append( getClosingTag() );
		
		string.append(KnowWEUtils.maskHTML( buffi.toString() ));
	}
	
	/**
	 * Generates a link used to enable or disable the Quick-Edit-Flag.
	 * 
	 * @see UserSetting, UserSettingsManager, NodeFlagSetting
	 * @param topic     name of the current page
	 * @param id        of the section the flag should assigned to
	 * @return
	 */
	protected String generateQuickEdit(String id, String title) {
		String icon = " <img id='" + id + "_pencil' src='KnowWEExtension/images/pencil.png' title='" + title + "' width='10' class='quickedit table pointer'/>";
		return icon;
	}
	
	protected String getHeader() {
		return "";
	}
	
	protected String getOpeningTag(Section sec) {
		return "<div class=\"table-edit\" id=\"" + sec.getID() + "\">";
	}
	
	protected String getClosingTag() {
		return "</div>";
	}

}
