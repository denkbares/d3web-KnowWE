/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action.sync.server;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.Action.Access;
import de.knowwe.core.action.UserActionContext;

public class GetRepositoryInfo extends AbstractAction {

	/**
	 * Serves the repository description the dialog application needs to find its update, before it could sign in
	 * anywhere. It reveals nothing but the versions the repository offers.
	 */
	@Override
	public Access requiredAccess() {
		return Access.NONE;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		SyncServerContext serverContext = SyncServerContext.getInstance();
		context.getWriter().append(serverContext.getRepository().getConfigXML());
		context.setContentType("text/xml");
	}

}
