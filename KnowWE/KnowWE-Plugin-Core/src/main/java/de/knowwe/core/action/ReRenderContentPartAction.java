package de.knowwe.core.action;

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

import java.io.IOException;

import de.knowwe.core.KnowWEArticleManager;
import de.knowwe.core.KnowWEEnvironment;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.DelegateRenderer;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.kdom.rendering.RendererManager;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * ReRenderContentPartAction. Renders a given section again. Often used in
 * combination with AJAX request, to refresh a certain section of an article due
 * to user interaction.
 * 
 * @author smark
 */
public class ReRenderContentPartAction extends AbstractAction {

	private String perform(UserActionContext context) {

		String web = context.getWeb();
		String nodeID = context.getParameter("KdomNodeId");

		KnowWEArticleManager mgr = KnowWEEnvironment.getInstance().getArticleManager(web);

		Section<? extends Type> secWithNodeID = Sections.getSection(nodeID);

		String title = secWithNodeID.getTitle();

		KnowWEArticle article = mgr.getArticle(title);

		if (secWithNodeID != null) {
			StringBuilder b = new StringBuilder();

			Type type = secWithNodeID.get();
			Renderer renderer = RendererManager.getInstance().getRenderer(type,
					context.getUserName(), title);

			if (renderer != null) {
				renderer.render(secWithNodeID, context, b);
			}
			else {
				renderer = type.getRenderer();
				if (renderer != null) {
					renderer.render(secWithNodeID, context, b);
				}
				else {
					DelegateRenderer.getInstance().render(secWithNodeID, context, b);
				}
			}

			// If the node is in <pre> than do not
			// render it through the JSPWikiPipeline
			String inPre = context.getParameter("inPre");
			String pagedata = b.toString();

			if (inPre == null) pagedata = KnowWEEnvironment.getInstance().getWikiConnector()
						.renderWikiSyntax(pagedata, context);
			if (inPre != null) if (inPre.equals("false")) pagedata = KnowWEEnvironment.getInstance()
							.getWikiConnector().renderWikiSyntax(pagedata, context);

			return KnowWEUtils.unmaskHTML(pagedata);
		}
		return null;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		String result = perform(context);
		if (result != null && context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}

	}
}
