/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action.sync;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

public class StartUpdate extends AbstractAction {

	public static String PARAM_SERVER_URL = CheckUpdates.PARAM_SERVER_URL;
	public static String PARAM_VERSION_ALIAS = "alias";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String url = context.getParameter(PARAM_SERVER_URL);
		String alias = context.getParameter(PARAM_VERSION_ALIAS);

		SyncClientContext.getInstance().startSync(url, alias);
	}

}
