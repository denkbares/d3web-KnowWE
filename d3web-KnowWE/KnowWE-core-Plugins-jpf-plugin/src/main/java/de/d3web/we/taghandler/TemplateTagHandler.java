/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.d3web.we.taghandler;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;

import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.Template;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.wikiConnector.KnowWEUserContext;

/**
 * @author Johannes Dienst
 * 
 *         Used to Generate WikiPages out of a TemplateType {@link TemplateType}
 *         {@link TemplateGenerationAction}
 */
public class TemplateTagHandler extends AbstractTagHandler {

	public TemplateTagHandler() {
		super("copytemplate");
	}

	@Override
	public String getDescription(KnowWEUserContext user) {
		return KnowWEEnvironment.getInstance().getKwikiBundle(user).
				getString("KnowWE.TemplateTagHandler.description");
	}

	@Override
	public String render(String topic, KnowWEUserContext user,
			Map<String, String> values, String web) {

		ResourceBundle rb = KnowWEEnvironment.getInstance()
				.getKwikiBundle(user);

		List<Section<Template>> templates = getTemplateTypes(KnowWEEnvironment
				.getInstance().getArticle(web, topic));

		StringBuffer html = new StringBuffer();

		html.append("<div id=\"TemplateTagHandler\" class=\"panel\"><h3>"
				+ rb.getString("KnowWE.TemplateTagHandler.topic") + "</h3>");
		html.append("<form method='post' action=''>");
		html.append("<fieldset>");

		if ((templates != null) && (!templates.isEmpty())) {

			int i = 0;
			String secName = "";
			for (ListIterator<Section<Template>> it = templates.listIterator(); it
					.hasNext(); i++) {

				Section<Template> temp = it.next();
				secName = AbstractXMLObjectType.getAttributeMapFor(temp).get(
						"name");

				html.append("<div>");
				html.append("<p><img src='KnowWEExtension/images/arrow_right.png' border='0'/> "
								+ rb
										.getString("KnowWE.TemplateTagHandler.copy")
								+ " " + secName + "</p>");
				html.append("<label for='" + "Template" + i + "'>"
						+ rb.getString("KnowWE.TemplateTagHandler.newpage")
						+ "</label>");
				html.append("<input id='"
								+ "Template"
								+ i
								+ "' type='text' name='templateTerm' class='field' title=''/>");

				html.append("<input type='button' value='"
								+ rb.getString("KnowWE.TemplateTagHandler.copyButton")
								+ "' name='generate' class='button generate-template' "
								+ "title='' rel='{jar : \"Template"
								+ i + "\"}'/>");

				html.append("</div> \n"); // \n only to avoid hmtl-code being
				// cut by JspWiki (String.length >
				// 10000)

			}
		}
		else {
			html.append("<div>");
			html.append("<p class='info box'>"
					+ rb.getString("KnowWE.TemplateTagHandler.noTemplate")
					+ "</p>");
			html.append("</div>");

		}

		// div for generating info
		html.append("<div id ='TemplateGeneratingInfo'>");
		html.append("</div>");

		html.append("</fieldset> ");

		html.append("</form>");

		html.append("</div>");

		return html.toString();
	}

	/**
	 * Finds all TemplateKnowWEObjectTypes in an article.
	 * 
	 * @param article
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Section<Template>> getTemplateTypes(KnowWEArticle article) {
		ArrayList<Section<? extends KnowWEObjectType>> found =
				new ArrayList<Section<? extends KnowWEObjectType>>();
		article.getSection().getAllNodesPreOrder(found);
		// article.getSection().findSuccessorsOfType(TemplateType.class, found);
		ArrayList<Section<Template>> cleaned = new ArrayList<Section<Template>>();
		for (Section<? extends KnowWEObjectType> s : found) {
			if (s.getObjectType() instanceof Template) cleaned.add((Section<Template>) s);
		}
		return cleaned;
	}
}
