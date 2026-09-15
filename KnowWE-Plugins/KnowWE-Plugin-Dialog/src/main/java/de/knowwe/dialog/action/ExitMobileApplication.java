/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.IOException;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.Action.Access;
import de.knowwe.core.action.UserActionContext;

public class ExitMobileApplication extends AbstractAction {

	/**
	 * Ends the mobile application, which does nothing on a server and touches no wiki content.
	 */
	@Override
	public Access requiredAccess() {
		return Access.NONE;
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		// TODO: MobileApplication.getInstance().exit();
	}

}
