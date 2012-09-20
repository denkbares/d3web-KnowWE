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

package de.d3web.we.ci4ke.action;

import java.io.IOException;
import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;

import de.d3web.core.io.progress.AjaxProgressListener;
import de.d3web.core.io.progress.ProgressListener;
import de.d3web.core.io.progress.ProgressListenerManager;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * This action handles the ajax upate request of the ci-build progress bar on
 * the dashboard.
 * 
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 18.07.2012
 */
public class CIGetProgressAction extends AbstractAction {

	String FINISHED = "Finished";

	@Override
	public void execute(UserActionContext context) throws IOException {

		String name = context.getParameter("name");
		name = URLDecoder.decode(name, "UTF-8");
		ProgressListener listener = ProgressListenerManager.getInstance().getProgressListener(name);

		float progress = 0;
		// finished will be returned in case build is finished and
		// ProgressListener already deregistered
		String message = FINISHED;
		if (listener != null) {
			if (listener instanceof AjaxProgressListener) {
				progress = ((AjaxProgressListener) listener).getCurrentProgress();
				message = ((AjaxProgressListener) listener).getCurrentMessage();
				if (message == null || message.isEmpty()) {
					message = "Initializing...";
				}
			}
		}
		if (message.equals(FINISHED)) {
			progress = 1;
		}

		int progressTwoDigits = (int) (progress * 100);
		String percentString = "" + progressTwoDigits;
		if (progressTwoDigits < 10) {
			percentString = " " + percentString;
		}
		JSONObject result = new JSONObject();
		try {
			result.put("progress", percentString);
			result.put("message", message);
			result.write(context.getWriter());
		}
		catch (JSONException e) {
			java.util.logging.Logger.getLogger(this.getClass().getName()).severe(
					"Error while writing JSON message: " + e.getMessage());
		}
	}
}
