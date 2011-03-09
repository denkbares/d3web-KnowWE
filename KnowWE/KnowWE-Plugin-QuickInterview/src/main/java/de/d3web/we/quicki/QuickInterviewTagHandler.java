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

import de.d3web.we.basic.D3webModule;
import de.d3web.we.core.KnowWERessourceLoader;
import de.d3web.we.taghandler.AbstractHTMLTagHandler;
import de.d3web.we.user.UserContext;

public class QuickInterviewTagHandler extends AbstractHTMLTagHandler {

	/**
	 * Create the TagHandler --> "quickInterview" defines the "name" of the tag,
	 * so the tag is inserted in the wiki page like [KnowWEPlugin
	 * quickInterview]
	 */
	public QuickInterviewTagHandler() {
		super("quickInterview");
		KnowWERessourceLoader.getInstance().add("quickiNeutral.css",
				KnowWERessourceLoader.RESOURCE_STYLESHEET);
		KnowWERessourceLoader.getInstance().add("quicki.js",
				KnowWERessourceLoader.RESOURCE_SCRIPT);

	}

	@Override
	public String getDescription(UserContext user) {
		return D3webModule.getKwikiBundle_d3web(user).getString("KnowWE.quicki.description");
	}

	/*
	 * calls the appropriate Action that is responsible for creating the
	 * session, knowledge etc and then in turn calls the interview renderer that
	 * returns the interview-HTML-String
	 */
	@Override
	public String renderHTML(String topic, UserContext user, Map<String, String> values, String web) {

		if (topic.equalsIgnoreCase("LeftMenu")) {
			topic = user.getParameters().get("page");
		}

		String iv = QuickInterviewAction.callQuickInterviewRenderer(topic, user.getUserName(),
				user.getRequest(), web, user);
		if (iv == null) return null;

		return iv;
	}
}
