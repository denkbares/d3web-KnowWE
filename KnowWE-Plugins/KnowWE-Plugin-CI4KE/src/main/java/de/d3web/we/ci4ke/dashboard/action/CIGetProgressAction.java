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

package de.d3web.we.ci4ke.dashboard.action;

import java.io.IOException;
import java.net.URLDecoder;

import org.json.JSONException;
import org.json.JSONObject;

import de.d3web.utils.Log;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.progress.AjaxProgressListener;
import de.knowwe.core.utils.progress.ProgressListenerManager;

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
		String web = context.getWeb();
		CIDashboard dashboard = CIDashboardManager.getDashboard(
				KnowWEUtils.getArticleManager(web),
				name);
		AjaxProgressListener listener = ProgressListenerManager.getInstance().getProgressListener(
				Integer.toString(dashboard.hashCode()));

		float progress = 0;
		// finished will be returned in case build is finished and
		// ProgressListener already deregistered
		String message = FINISHED;
		if (listener != null) {
			progress = listener.getProgress();
			message = listener.getMessage();
			if (message == null || message.isEmpty()) {
				message = "Initializing...";
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
			Log.severe("Error while writing JSON message", e);
		}
	}
}
