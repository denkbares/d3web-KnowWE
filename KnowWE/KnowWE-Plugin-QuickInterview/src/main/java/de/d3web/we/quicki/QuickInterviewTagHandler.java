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
import de.knowwe.core.ResourceLoader;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.taghandler.AbstractTagHandler;
import de.knowwe.core.user.UserContext;

public class QuickInterviewTagHandler extends AbstractTagHandler {

	/**
	 * Create the TagHandler --> "quickInterview" defines the "name" of the tag,
	 * so the tag is inserted in the wiki page like [KnowWEPlugin
	 * quickInterview]
	 */
	public QuickInterviewTagHandler() {
		super("quickInterview");
		ResourceLoader.getInstance().add("quickiNeutral.css");
		ResourceLoader.getInstance().add("quicki.js");

	}

	@Override
	public String getDescription(UserContext user) {
		return D3webUtils.getD3webBundle(user).getString("KnowWE.quicki.description");
	}

	@Override
	public final void render(Section<?> section, UserContext userContext, Map<String, String> parameters, RenderResult result) {
		userContext.getParameters().putAll(parameters);
		result.appendHtmlTag("div", "id", section.getID());
		result.appendHtmlTag("div", "class", "quickinterview", "sectionId", section.getID(), "id",
				"quickinterview_" + section.getID());
		QuickInterviewRenderer.renderInterview(section, userContext, result);
		result.appendHtmlTag("/div");
		result.appendHtmlTag("/div");

	}
}
