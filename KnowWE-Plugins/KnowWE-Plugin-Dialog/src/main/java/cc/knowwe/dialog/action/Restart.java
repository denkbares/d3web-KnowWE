/*
 * Copyright (C) 2011 denkbares GmbH
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
package cc.knowwe.dialog.action;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import cc.knowwe.dialog.Utils;
import cc.knowwe.dialog.action.StartCase.KnowledgeBaseProvider;
import cc.knowwe.dialog.action.StartCase.StartInfo;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.notification.NotificationManager;

import static cc.knowwe.dialog.SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS;
import static cc.knowwe.dialog.action.StartCase.PARAM_RESTART_SESSION;

/**
 * @author Markus Friedrich (denkbares GmbH)
 * @created 25.05.2011
 */
public class Restart extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		HttpSession httpSession = context.getSession();
		KnowledgeBaseProvider[] providers = (KnowledgeBaseProvider[])
				httpSession.getAttribute(ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS);
		if (providers.length == 1) {
			KnowledgeBaseProvider provider = providers[0];
			if (provider instanceof InitWiki.WikiProvider) {
				// Remove {@link OutDatedSessionNotification} notification
				InitWiki.WikiProvider wikiProvider = (InitWiki.WikiProvider) provider;
				NotificationManager.removeNotification(context, wikiProvider.getSectionId());
			}
			StartCase cmd = (StartCase) Utils.getAction(StartCase.class.getSimpleName());
			httpSession.setAttribute(PARAM_RESTART_SESSION, new StartInfo(true));
			cmd.startCase(context, provider);
		}
		else {
			String language = context.getParameter(StartCase.PARAM_LANGUAGE);
			context.sendRedirect("Resource/ui.zip/html/selectBase.html?" +
					StartCase.PARAM_LANGUAGE + "=" + language);
		}
	}
}
