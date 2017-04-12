/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog.action;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import de.knowwe.dialog.Utils;
import de.knowwe.dialog.action.StartCase.KnowledgeBaseProvider;
import de.knowwe.dialog.action.StartCase.StartInfo;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.notification.NotificationManager;

import static de.knowwe.dialog.SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS;
import static de.knowwe.dialog.action.StartCase.PARAM_RESTART_SESSION;

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
