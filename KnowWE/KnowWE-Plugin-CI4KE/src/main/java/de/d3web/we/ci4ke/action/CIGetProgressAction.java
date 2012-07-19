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

	@Override
	public void execute(UserActionContext context) throws IOException {

		String id = context.getParameter("id");
		id = URLDecoder.decode(id, "UTF-8");
		ProgressListener listener = ProgressListenerManager.getInstance().getProgressListener(id);

		float progress = 0;
		String message = "";
		if (listener != null) {
			if (listener instanceof AjaxProgressListener) {
				progress = ((AjaxProgressListener) listener).getCurrentProgress();
				message = ((AjaxProgressListener) listener).getCurrentMessage();
				if (message == null || message.length() == 0) {
					message = "starting build...";
				}
			}
		}

		int progressTwoDigits = (int) (progress * 100);
		String percentString = "" + progressTwoDigits;
		if (progressTwoDigits < 10) {
			percentString = "&nbsp;&nbsp;" + percentString;
		}
		String result = "{\"progress\":\"" + percentString + "\",\"message\":\"" + message + "\"}";

		if (context.getWriter() != null) {
			context.setContentType("text/html; charset=UTF-8");
			context.getWriter().write(result);
		}

	}
}
