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

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * This is a generic class for resetting d3web-Sessions using KnowWE's action
 * mechanism. Please use this action wherever possible to avoid duplicate code.
 * 
 * For a reset of a session the underlying knowledge base is necessary. For
 * getting a knowledge base the name of the compiling master article is needed.
 * This action will use the value of
 * 
 * <pre>
 * UserActionContext.getTitle()
 * </pre>
 * 
 * except the parameter
 * 
 * <pre>
 * SessionResetAction.KBARTICLE
 * </pre>
 * 
 * is explicitly defined.
 * 
 * @author Sebastian Furth
 * @created 14.03.2012
 */
public class SessionResetAction extends AbstractAction {

	private static final String KBARTICLE = "kbarticle";

	@Override
	public void execute(UserActionContext context) throws IOException {

		// get knowledge base
		String kbArticle = context.getParameter(KBARTICLE);
		if (kbArticle == null || kbArticle.isEmpty()) {
			kbArticle = context.getTitle();
		}
		KnowledgeBase base = D3webUtils.getKnowledgeBase(context.getWeb(), kbArticle);

		// reset session
		SessionProvider provider = SessionProvider.getSessionProvider(context);
		provider.removeSession(base);
		provider.createSession(base);
	}
}
