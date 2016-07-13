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

import com.denkbares.strings.Strings;
import de.d3web.we.ci4ke.build.CIBuildManager;
import de.d3web.we.ci4ke.dashboard.CIDashboard;
import de.d3web.we.ci4ke.dashboard.CIDashboardManager;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.utils.KnowWEUtils;

public class CIStopBuildAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String dashboardName = Strings.decodeURL(context.getParameter("name"));
		String web = context.getWeb();
		CIDashboard dashboard = CIDashboardManager.getDashboard(KnowWEUtils.getArticleManager(web),
				dashboardName);
		CIBuildManager.getInstance().shutDownNow(dashboard);
	}

}
