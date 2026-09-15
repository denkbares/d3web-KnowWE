/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import de.knowwe.dialog.SessionConstants;
import de.d3web.core.knowledge.Resource;
import de.knowwe.core.action.Action.Access;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class GetAvailableKnowledgeBaseFavIcon extends AbstractAction {

	/**
	 * The access is checked by {@link StartCase.KnowledgeBaseProvider#canView(de.knowwe.core.action.UserActionContext)},
	 * which rejects knowledge bases the user may not see.
	 */
	@Override
	public Access requiredAccess() {
		return Access.HELPER;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {

		int index = Integer.parseInt(context.getParameter("index"));

		HttpSession session = context.getSession();
		StartCase.KnowledgeBaseProvider[] providers = (StartCase.KnowledgeBaseProvider[]) session.getAttribute(
				SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS);

		StartCase.KnowledgeBaseProvider provider = providers[index];
		if (!provider.canView(context)) {
			context.sendError(403, "not allowed to access this knowledge base");
			return;
		}
		Resource resource = provider.getFavIcon(context);
		Multimedia.deliverFile(context, resource);
	}

}
