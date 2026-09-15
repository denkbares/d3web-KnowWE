/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action.sync.server;

import java.io.File;
import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.Action.Access;
import de.knowwe.core.action.UserActionContext;

public class AddRepositoryFile extends AbstractAction {

	/**
	 * Adds a file of the server to the update repository, which is repository administration and is offered by the
	 * administration pages of the dialog only.
	 */
	@Override
	public Access requiredAccess() {
		return Access.ADMIN;
	}

	public static String PARAM_PATHNAME = "path";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String path = context.getParameter(PARAM_PATHNAME);

		SyncServerContext syncContext = SyncServerContext.getInstance();
		File file = new File(path);
		syncContext.getRepository().addArchive(file);
	}

}
