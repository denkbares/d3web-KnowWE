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

import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.wikiConnector.KnowWEUserContext;
import de.d3web.we.wikiConnector.KnowWEWikiConnector;

/**
 * Renders the mask for Generating KnowledgeBases
 * from jar-files into the Wiki
 * See also: GenerateKBRenderer
 * 
 * @author Johannes Dienst
 *
 */
public class KnowledgeBasesGeneratorHandler extends AbstractTagHandler {

	public KnowledgeBasesGeneratorHandler () {
		super("KnowledgeBasesUploader");		
	}
	
	@Override
	public String getDescription(KnowWEUserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).
				getString("KnowWE.KnowledgeBasesGenerator.description");
	}
	
	@Override
	public String render(String topic, KnowWEUserContext user, Map<String,String> values, String web) {
		
		ResourceBundle rb = D3webModule.getKwikiBundle_d3web(user);
		
		KnowWEWikiConnector wikiConnector = KnowWEEnvironment.getInstance().getWikiConnector();
	
		// update attachments
		List <String> attchmnts = wikiConnector.getJarAttachments();
		
		StringBuffer html = new StringBuffer();
		
		html.append("<div id=\"KnowledgeBasesGenerator\" class=\"panel\"><h3>"
				+ rb.getString("KnowWE.KnowledgeBasesGenerator.topic")
				+ "</h3>");
		html.append("<form method='post' action=''>");
		html.append("<fieldset>");

		if ((attchmnts != null) && (!attchmnts.isEmpty())) {
			
			ListIterator<String> it = attchmnts.listIterator();
			while (it.hasNext()) {
				
				String a = it.next();
				
				html.append("<div>");
				html.append("<p><img src='KnowWEExtension/images/arrow_right.png' border='0'/> "
						+ rb.getString("KnowWE.KnowledgeBasesGenerator.jarname") + a + "</p>");
				html.append("<label for='" + a + "'>" + "Neuer Name:" + "</label>");
				html.append("<input id='" + a + "' type='text' name='nameTerm' class='field' title=''/>");
				
				html.append("<input type='button' value='"
						+ rb.getString("KnowWE.KnowledgeBasesGenerator.generateButton")
						+ "' name='generate' class='button generate-kb' title='' rel='{jar : \""+a+"\"}'/>");
				
				html.append("</div> \n"); // \n only to avoid hmtl-code being cut by JspWiki (String.length > 10000)

			}
		} else {
			html.append("<div>");
			html.append("<p class='info box'>"
			+ rb.getString("KnowWE.KnowledgeBasesGenerator.nokb")
			+ "</p>");
			html.append("</div>");
			
		}
		
		// div for generating info
		html.append("<div id ='GeneratingInfo'>");
		html.append("</div>");
		
		html.append("</fieldset> ");
		
		html.append("</form>");
		
		html.append("</div>");
		
		return html.toString();
	}
}
