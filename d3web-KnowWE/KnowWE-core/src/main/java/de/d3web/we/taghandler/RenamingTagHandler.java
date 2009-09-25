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

package de.d3web.we.taghandler;

import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * <p>This class handles the appearance of the ReanmingTool tag.</p>
 */
public class RenamingTagHandler extends AbstractTagHandler {
	
	public RenamingTagHandler() {
		super("RenamingTool");

	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).getString("KnowWE.RenamingTagHandler.description");
	}
	
	/**
	 * <p>Returns a HTML representation of the renaming tool form.</p>
	 */
	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		StringBuffer html = new StringBuffer();
		
		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle(user);
		
		html.append("<div id=\"rename-panel\" class=\"panel\"><h3>" + rb.getString("KnowWE.renamingtool.redefine") + "</h3>");
		
		html.append("<form method='post' action=''>");
		html.append("<fieldset>");
//		html.append("<legend> " + rb.getString("KnowWE.renamingtool.redefine") + " </legend>");
				
		html.append("<div class='left'>");
		html.append("<label for='renameInputField'>" + rb.getString("KnowWE.renamingtool.searchterm") + "</label>");
		html.append("<input id='renameInputField' type='text' name='TargetNamespace' value='' tabindex='1' class='field' title=''/>");
		html.append("</div>");	
		
		html.append("<div class='left'>");
		html.append("<label for='replaceInputField'>" + rb.getString("KnowWE.renamingtool.replace") + "</label>");
		html.append("<input id='replaceInputField' type='text' name='replaceTerm'  tabindex='2' class='field' title=''/>");
		html.append("</div>");
		
		html.append("<div id='search-button'>");
		html.append("<input type='button' value='" + rb.getString("KnowWE.renamingtool.preview") + "' name='submit' tabindex='3' class='button' title='' onclick='sendRenameRequest();'/>");
		html.append("</div>");
		
		html.append("<div style='clear:both'></div>");
		
		html.append("<p id='rename-show-extend' class='pointer extend-panel-down'>");
		html.append(rb.getString("KnowWE.renamingtool.settings") + "</p>");

		html.append("<div id='rename-extend-panel' class='hidden'>");
		
		html.append("<div class='left'>");
		html.append("<label for='renamePreviousInputContext'>" + rb.getString("KnowWE.renamingtool.previous") + "</label>"); 
		html.append("<input id='renamePreviousInputContext' type='text' name='' value='' tabindex='5' class='field'/>");
		html.append("</div>");
		
		html.append("<div class='left'>");
		html.append("<label for='renameAfterInputContext'>" + rb.getString("KnowWE.renamingtool.after") + "</label>");
		html.append("<input id='renameAfterInputContext' type='text' name='' value='' tabindex='6' class='field'/>");
		html.append("</div>");
		
		html.append("<div class='left'>");
	    html.append("<label for='search-sensitive'>" + rb.getString("KnowWE.renamingtool.case") + "</label>");
	    html.append("<input id='search-sensitive' type='checkbox' name='search-sensitive' tabindex='7' checked='checked'/>");
		html.append("</div>");		
		
		html.append("</div>");
		
		html.append("<input type='hidden' value='RenamingRenderer' name='action' />");
		html.append("</fieldset> ");
		html.append("</form>");
		html.append("<div id='rename-result'></div>");
		html.append("</div>");
		
		return html.toString();
	}
}
