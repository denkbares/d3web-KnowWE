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

package de.knowwe.core.kdom.renderer;

import java.util.Map;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.report.Messages;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.visitor.RenderKDOMVisitor;

public class KDOMRenderer extends AbstractHTMLTagHandler {

	public KDOMRenderer() {
		super("renderKDOM");
		this.setMaskJSPWikiSyntax(true);
	}

	@Override
	public String getDescription(UserContext user) {
		return Messages.getMessageBundle(user).getString(
				"KnowWE.KDOMRenderer.description");
	}

	@Override
	public void renderHTML(String web, String title, UserContext user, Map<String, String> parameters, RenderResult result) {
		Article article = Environment.getInstance().getArticle(web, title);
		renderHTML(article, user, result);
	}

	public static void renderHTML(Article article, UserContext user, RenderResult result) {
		Section<?> section = article.getRootSection();
		renderHTML(section, user, result);
	}

	public static void renderHTML(Section<?> section, UserContext user, RenderResult result) {
		RenderKDOMVisitor v = new RenderKDOMVisitor(user);
		v.visit(section);
		result.appendHtml("<div><h3>KDOM:</h3><tt>");
		result.append(v.getRenderedKDOMRaw());
		result.appendHtml("</tt></div>");
	}

	public static String renderPlain(Article article, UserContext user) {
		return renderPlain(article.getRootSection(), user);
	}

	public static String renderPlain(Section<?> section, UserContext user) {
		RenderKDOMVisitor v = new RenderKDOMVisitor(user);
		v.visit(section);
		return v.getRenderedKDOMHtml().replaceAll("<[^>]*>", "").replace("&quot;", "\"");
	}
}
