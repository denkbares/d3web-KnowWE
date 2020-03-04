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

import org.json.JSONException;
import org.json.JSONObject;

import com.denkbares.strings.Strings;
import com.denkbares.utils.Log;
import de.d3web.we.ci4ke.build.CIBuildManager;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.utils.progress.DefaultAjaxProgressListener;

/**
 * This action handles the ajax upate request of the ci-build progress bar on
 * the dashboard.
 *
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 18.07.2012
 */
public class CIGetProgressAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String name = Strings.decodeURL(context.getParameter("name"));
		CIDashboard dashboard = CIDashboardManager.getDashboard(KnowWEUtils.getArticleManager(context.getWeb()), name);
		DefaultAjaxProgressListener listener = CIBuildManager.getProgress(dashboard);

		float progress;
		String message;
		if (listener == null) {
			// build done, progress listener no longer available
			progress = 1;
			message = "Finished";
		}
		else {
			progress = listener.getProgress();
			message = listener.getMessage();
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
