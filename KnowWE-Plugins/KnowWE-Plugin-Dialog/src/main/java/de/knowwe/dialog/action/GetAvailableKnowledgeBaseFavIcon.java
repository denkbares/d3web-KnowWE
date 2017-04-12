/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import de.knowwe.dialog.SessionConstants;
import de.d3web.core.knowledge.Resource;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class GetAvailableKnowledgeBaseFavIcon extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		int index = Integer.parseInt(context.getParameter("index"));

		HttpSession session = context.getSession();
		StartCase.KnowledgeBaseProvider[] providers = (StartCase.KnowledgeBaseProvider[]) session.getAttribute(
				SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS);

		StartCase.KnowledgeBaseProvider provider = providers[index];
		Resource resource = provider.getFavIcon();
		Multimedia.deliverFile(context, resource);
	}

}
