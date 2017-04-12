/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action.sync.server;

import java.io.File;
import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class AddRepositoryFile extends AbstractAction {

	public static String PARAM_PATHNAME = "path";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String path = context.getParameter(PARAM_PATHNAME);

		SyncServerContext syncContext = SyncServerContext.getInstance();
		File file = new File(path);
		syncContext.getRepository().addArchive(file);
	}

}
