/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog.action;

import java.io.IOException;

import javax.servlet.http.HttpSession;

import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.Action.Access;
import de.knowwe.core.action.UserActionContext;

/**
 * Returns a list of all available knowledge bases calculated by any init method
 * before. It is used to display a user selectable list of bases before starting
 * a case.
 * 
 * @author volker.beli
 * @created 16.04.2011
 */
public class SelectAvailableKnowledgeBase extends AbstractAction {

	public static final String PARAM_INDEX = "index";

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

		int index = Integer.parseInt(context.getParameter(PARAM_INDEX));

		HttpSession session = context.getSession();
		StartCase.KnowledgeBaseProvider[] providers = (StartCase.KnowledgeBaseProvider[]) session.getAttribute(
				SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS);

		StartCase.KnowledgeBaseProvider provider = providers[index];
		if (!provider.canView(context)) {
			context.sendError(403, "not allowed to access this knowledge base");
			return;
		}

		StartCase cmd = (StartCase) Utils.getAction(StartCase.class.getSimpleName());
		cmd.startCase(context, provider, null);
	}

}
