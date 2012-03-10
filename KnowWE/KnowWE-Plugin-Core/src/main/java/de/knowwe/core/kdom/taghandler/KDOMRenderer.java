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

package de.knowwe.core.kdom.taghandler;

import java.util.Map;

import de.knowwe.core.Environment;
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
		return Environment.getInstance().getMessageBundle(user).getString(
				"KnowWE.KDOMRenderer.description");
	}

	@Override
	public String renderHTML(String topic, UserContext user, Map<String, String> values, String web) {
		RenderKDOMVisitor v = new RenderKDOMVisitor();
		v.visit(Environment.getInstance().getArticle(web, topic)
				.getSection());
		String data = "<div><h3>KDOM:</h3><tt>"
				+ v.getRenderedKDOM() + "</tt></div>";
		return data;
	}

}
