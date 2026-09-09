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
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.denkbares.strings.Strings;
import de.d3web.we.ci4ke.build.CIBuildManager;
import de.d3web.we.ci4ke.build.CIBuildProgressStream;
import de.d3web.we.ci4ke.build.CIBuildStatus;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.sse.ServerSentEventWriter;
import de.knowwe.core.sse.ServerSentEvents;

/**
 * Streams the build progress of the dashboards shown on a page as server-sent events until none of them has a
 * queued or running build any more.
 *
 * @author Jochen Reutelshöfer (denkbares GmbH)
 * @created 18.07.2012
 */
public class CIGetProgressAction extends AbstractAction {
	private static final Logger LOGGER = LoggerFactory.getLogger(CIGetProgressAction.class);

	@Override
	public void execute(UserActionContext context) throws IOException {
		List<String> names = parseNames(context);
		if (names.isEmpty()) {
			fail(context, 400, "Parameter 'names' must be a non-empty JSON array of dashboard names");
			return;
		}
		ArticleManager articleManager = context.getArticleManager();

		ServerSentEventWriter sse = ServerSentEvents.open(context.getResponse());
		try {
			new CIBuildProgressStream().stream(names,
					name -> lookupStatus(articleManager, name),
					name -> renderBubble(context, name),
					CIBuildManager.getBuildChanges(), sse);
		}
		catch (IOException e) {
			// the browser closed the stream, e.g. the page was left while a build was running
			LOGGER.debug("Build progress stream for dashboards {} ended: {}", names, e.getMessage());
		}
	}

	private static CIBuildStatus lookupStatus(ArticleManager articleManager, String name) {
		CIDashboard dashboard = lookupDashboard(articleManager, name);
		return dashboard == null ? null : CIBuildManager.getBuildStatus(dashboard);
	}

	private static String renderBubble(UserActionContext context, String name) {
		CIDashboard dashboard = lookupDashboard(context.getArticleManager(), name);
		return dashboard == null ? null : dashboard.getRenderer().renderStateBubbleHtml(context);
	}

	private static CIDashboard lookupDashboard(ArticleManager articleManager, String name) {
		return CIDashboardManager.getDashboard(articleManager, Strings.decodeURL(name));
	}

	private static List<String> parseNames(UserActionContext context) {
		String parameter = context.getParameter("names");
		List<String> names = new ArrayList<>();
		if (Strings.isBlank(parameter)) return names;
		try {
			JSONArray array = new JSONArray(parameter);
			for (int i = 0; i < array.length(); i++) {
				Object element = array.opt(i);
				if (!(element instanceof String name) || Strings.isBlank(name)) {
					LOGGER.warn("Ignoring dashboard name that is not a string in parameter: {}", parameter);
					continue;
				}
				if (!names.contains(name)) names.add(name);
			}
		}
		catch (JSONException e) {
			LOGGER.warn("Ignoring malformed dashboard names parameter: {}", parameter);
		}
		return names;
	}
}
