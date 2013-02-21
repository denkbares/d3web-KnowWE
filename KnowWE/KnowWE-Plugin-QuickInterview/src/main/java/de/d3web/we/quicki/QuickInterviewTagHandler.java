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

package de.d3web.we.quicki;

import java.util.Map;

import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.RessourceLoader;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.taghandler.AbstractHTMLTagHandler;
import de.knowwe.core.user.UserContext;

public class QuickInterviewTagHandler extends AbstractHTMLTagHandler {

	/**
	 * Create the TagHandler --> "quickInterview" defines the "name" of the tag,
	 * so the tag is inserted in the wiki page like [KnowWEPlugin
	 * quickInterview]
	 */
	public QuickInterviewTagHandler() {
		super("quickInterview");
		RessourceLoader.getInstance().add("quickiNeutral.css",
				RessourceLoader.RESOURCE_STYLESHEET);
		RessourceLoader.getInstance().add("quicki.js",
				RessourceLoader.RESOURCE_SCRIPT);

	}

	@Override
	public String getDescription(UserContext user) {
		return D3webUtils.getD3webBundle(user).getString("KnowWE.quicki.description");
	}

	/*
	 * calls the appropriate Action that is responsible for creating the
	 * session, knowledge etc and then in turn calls the interview renderer that
	 * returns the interview-HTML-String
	 */
	@Override
	public void renderHTML(String web, String topic, UserContext user, Map<String, String> values, RenderResult result) {
		if (topic.equalsIgnoreCase("LeftMenu")) {
			topic = user.getParameters().get("page");
		}
		user.getParameters().putAll(values);
		String iv = QuickInterviewAction.callQuickInterviewRenderer(user);
		if (iv == null) return;

		result.appendHTML("<div id='quickinterview'>");
		result.appendHTML(iv);
		result.appendHTML("</div>");

	}
}
