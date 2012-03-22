/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.d3web.action;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Checks whether the session for a specified knowledge base uses the latest
 * version of the knowledge base or not. In case that the session is out dated,
 * i. e. this action will write "true" to the output stream.
 * 
 * @author Sebastian Furth
 * @created 22.03.2012
 */
public class CheckForOutDatedSessionAction extends AbstractAction {

	private static final String KBARTICLE = "kbarticle";

	@Override
	public void execute(UserActionContext context) throws IOException {

		// get knowledge base
		String kbArticle = context.getParameter(KBARTICLE);
		if (kbArticle == null || kbArticle.isEmpty()) {
			kbArticle = context.getTitle();
		}
		KnowledgeBase base = D3webUtils.getKnowledgeBase(context.getWeb(), kbArticle);

		// check if session is out dated
		if (base != null && SessionProvider.hasOutDatedSession(context, base)) {
			context.sendError(HttpServletResponse.SC_EXPECTATION_FAILED,
					"The session runs an out dated knowledge base!");
		}
	}

}
