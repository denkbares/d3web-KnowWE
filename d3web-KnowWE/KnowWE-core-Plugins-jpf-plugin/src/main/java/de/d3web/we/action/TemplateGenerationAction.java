/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.action;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.basic.PlainText;
import de.d3web.we.kdom.wikiTemplate.Template;
import de.d3web.we.taghandler.TemplateTagHandler;

/**
 * Creates a new WikiPage out of a Template text:
 * <code> <Template> text </Template> </code>
 *
 * @see TemplateType
 * @see TemplateTagHandler
 *
 *
 * @author Johannes Dienst
 *
 */
public class TemplateGenerationAction extends AbstractAction {

	@Override
	public void execute(ActionContext context) throws IOException {

		KnowWEParameterMap map = context.getKnowWEParameterMap();
		ResourceBundle rb = KnowWEEnvironment.getInstance().getKwikiBundle();
		context.setContentType("text/html; charset=UTF-8");

		try {

			int templateNum = Integer.parseInt(map.get(
					KnowWEAttributes.TEMPLATE_NAME).replace("Template", ""));
			String pageName = map.get(KnowWEAttributes.NEW_PAGE_NAME);

			List<Section<Template>> temps = TemplateTagHandler
					.getTemplateTypes(KnowWEEnvironment.getInstance()
							.getArticle(map.getWeb(), map.getTopic()));

			if (pageName == null || pageName == ""
					|| templateNum >= temps.size()) {
				context.getWriter()
						.write(
								"<p class='error box'>"
										+ rb.getString(
												"KnowWE.TemplateTagHandler.generatingError")
										+ "</p>");
				return;
			}

			Section<Template> renderMe = temps.get(templateNum);

			if (KnowWEEnvironment.getInstance().getArticle(map.getWeb(),
					pageName) != null) {
				context.getWriter()
						.write(
								"<p class='error box'>"
										+ rb.getString(
												"KnowWE.TemplateTagHandler.alreadyExists")
										+ "</p>");
				return;
			}

			Section<PlainText> text = renderMe
					.findChildOfType(PlainText.class);
			KnowWEEnvironment.getInstance().getWikiConnector().createWikiPage(
					pageName, text.getOriginalText(), map.getUser());

			String baseUrl = KnowWEEnvironment.getInstance().getWikiConnector()
					.getBaseUrl();

			context.getWriter()
					.write(
							"<p class='info box'>"
									+ rb.getString("KnowWE.TemplateTagHandler.pageCreated")
									+ " <a href=" + baseUrl + "Wiki.jsp?page="
									+ pageName + ">" + pageName + "</a>"
									+ "</p>");
			return;

		}
		catch (Exception e) {
			Logger.getLogger(this.getClass().getName()).severe(
					"Problem generating page from template:" + e.getMessage());
		}

		context.getWriter()
				.write(
						"<p class='error box'>"
								+ rb.getString("KnowWE.TemplateTagHandler.generatingError")
								+ "</p>");
	}
}
