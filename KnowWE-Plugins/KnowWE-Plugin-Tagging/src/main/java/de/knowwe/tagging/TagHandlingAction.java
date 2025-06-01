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

package de.knowwe.tagging;

import java.io.IOException;

import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class TagHandlingAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String result = perform(context);
		if (result != null && context.getWriter() != null) {
			context.setContentType(HTML);
			context.getWriter().write(result);
		}

	}

	private String perform(UserActionContext context) throws IOException {
		// String web = parameterMap.getWeb();
		String topic = context.getTitle();
		String tagaction = context.getParameter(Attributes.TAGGING_ACTION);
		String tag = context.getParameter(Attributes.TAGGING_TAG);

		if (tagaction.equals("pagesearch")) {
			String query = context.getParameter(Attributes.TAGGING_QUERY);
			return TaggingMangler.getInstance().getResultPanel(query);
		}

		if (topic == null) {
			return "error! null topic";
		}
		boolean b = Environment.getInstance().getWikiConnector().userCanEditArticle(topic,
				context);

		if (b == false) {
			return "Your are not allowed to edit page";
		}

		TaggingMangler tm = TaggingMangler.getInstance();
		if (tagaction.equals("add")) {
			tm.addTag(topic, tag, context);
		}
		else if (tagaction.equals("del")) {
			tm.removeTag(topic, tag, context);
		}
		else if (tagaction.equals("set")) {
			tm.setTags(topic, tag, context);
		}

		return tag;
	}

}
