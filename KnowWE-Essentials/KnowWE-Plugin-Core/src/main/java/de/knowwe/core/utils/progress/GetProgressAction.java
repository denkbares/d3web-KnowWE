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
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.denkbares.utils.Stopwatch;
import de.knowwe.core.Attributes;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;

/**
 * This action handles the ajax update request of the ci-build progress bar on
 * the dashboard.
 *
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 18.07.2012
 */
public class GetProgressAction extends AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(GetProgressAction.class);

	@Override
	public void execute(UserActionContext context) throws IOException {

		String sectionID = context.getParameter(Attributes.SECTION_ID);
		Section<?> section = Sections.get(sectionID);
		if (section == null) {
			context.sendError(404, "no such section");
			return;
		}

		try {
			JSONArray result = new JSONArray();
			for (LongOperation operation : new ArrayList<>(LongOperationUtils.getLongOperations(section))) {
				AjaxProgressListener listener = operation.getProgressListener();
				String timeDisplay;
				long time = listener.getRuntimeMillis();
				if (time <= TimeUnit.MINUTES.toMillis(1)) {
					timeDisplay = Stopwatch.getDisplay(time, TimeUnit.MINUTES);
				}
				else {
					timeDisplay = Stopwatch.getDisplay(time);
				}

				JSONObject progress = new JSONObject();
				progress.put("operationID",
						LongOperationUtils.getRegistrationID(section, operation));
				progress.put("progress", listener.getProgress());
				progress.put("runtime", timeDisplay);
				progress.put("runtimeMillis", time);
				progress.put("message", listener.getMessage());
				progress.put("report", getReport(context, operation));
				progress.put("error", listener.getError());
				progress.put("running", listener.isRunning());
				result.put(progress);
			}
			result.write(context.getWriter());
		}
		catch (JSONException e) {
			LOGGER.error("Error while writing JSON message", e);
		}
	}

	private String getReport(UserActionContext context, LongOperation operation) {
		final RenderResult renderResult = new RenderResult(context);
		operation.renderReport(context, renderResult);
		String rawResult = Environment.getInstance().getWikiConnector()
				.renderWikiSyntax(renderResult.toStringRaw());
		return RenderResult.unmask(rawResult, context);
	}
}
