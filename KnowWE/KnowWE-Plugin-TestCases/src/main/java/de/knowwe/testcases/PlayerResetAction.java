/*
 * Copyright (C) 2012 denkbares GmbH
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
package de.knowwe.testcases;

import java.io.IOException;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.basic.SessionProvider;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 10.02.2012
 */
public class PlayerResetAction extends AbstractAction {

	private static final String KBARTICLE = "kbid";

	@Override
	public void execute(UserActionContext context) throws IOException {
		SessionProvider provider = SessionProvider.getSessionProvider(context);
		String kbid = context.getParameter(KBARTICLE);
		KnowledgeBase base = D3webUtils.getKnowledgeBase(context.getWeb(), kbid);

		// remove session
		provider.removeSession(base);

		// add new session
		provider.createSession(base);

	}
}
