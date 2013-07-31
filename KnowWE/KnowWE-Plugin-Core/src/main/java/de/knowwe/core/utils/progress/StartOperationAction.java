/*
 * Copyright (C) 2012 denkbares GmbH
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

package de.knowwe.core.utils.progress;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;

/**
 * This action handles the ajax upate request of the ci-build progress bar on
 * the dashboard.
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 18.07.2012
 */
public class StartOperationAction extends OperationAction {

	@Override
	public void execute(final UserActionContext context, Section<?> section, final LongOperation operation) throws IOException {

		if (ProgressListenerManager.getInstance().isRunning(operation)) {
			context.sendError(412, "Operation still running, please terminate first");
			return;
		}

		final AjaxProgressListener listener =
				ProgressListenerManager.getInstance().createProgressListener(context, operation);

		new Thread("long-operation-worker") {

			public void run() {
				try {
					operation.execute(context, listener);
				}
				catch (IOException e) {
					Logger.getLogger(getClass().getName()).log(Level.WARNING,
							"cannot complete operation", e);
					listener.setError("Error occured: " + e.getMessage() + ".");
				}
				catch (InterruptedException e) {
					Logger.getLogger(getClass().getName()).log(Level.INFO,
							"operation canceled by user");
					listener.setError("Canceled by user.");
				}
				catch (Throwable e) {
					Logger.getLogger(getClass().getName()).log(Level.SEVERE,
							"cannot complete operation, unexpected internal error", e);
					listener.setError("Unexpected internal error: " + e.getMessage() + ".");
				}
				finally {
					listener.setRunning(false);
				}
			}
		}.start();
	}

}
