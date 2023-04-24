/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.knowwe.jspwiki.administration;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.Action;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.jspwiki.readOnly.ReadOnlyManager;
import de.knowwe.tools.AsynchronousActionTool;
import de.knowwe.tools.DefaultTool;
import de.knowwe.tools.Tool;
import de.knowwe.tools.ToolProvider;
import de.knowwe.util.Icon;

/**
 * Tools for %%Admin markup
 *
 * @author Tim Abler
 * @created 09.10.2018
 */
public class AdministrationToolProvider extends AbstractAction implements ToolProvider {

	public static final String THREAD_DUMP = "thread-dump";

	@Override
	public Tool[] getTools(Section<?> section, UserContext user) {
		if (user.userIsAdmin()) {
			boolean readonly = ReadOnlyManager.isReadOnly();

			String js = "javascript:KNOWWE.plugin.jspwikiConnector.setReadOnly(" + readonly + ")";

			Tool readOnlyTool;
			if (readonly) {
				readOnlyTool = new DefaultTool(
						Icon.TOGGLE_ON,
						"Deactivate ReadOnly Mode",
						"Disclaimer: This is a purely administrative feature and should not be used for security purposes, because it is not secure.",
						js,
						Tool.CATEGORY_LAST);
			}
			else {
				readOnlyTool = new DefaultTool(
						Icon.TOGGLE_OFF,
						"Activate ReadOnly Mode",
						"Disclaimer: This is a purely administrative feature and should not be used for security purposes, because it is not secure.",
						js,
						Tool.CATEGORY_LAST);
			}
			DefaultTool threadDumpTool = new DefaultTool(Icon.COPY_TO_CLIPBOARD,
					"Copy thread dump to clipboard",
					"Create thread dump and copy it to the clipboard",
					AsynchronousActionTool.buildJsAction(getClass(), section,
							"jq$('#" + section.getID() + "').copyToClipboard(response);"
									+ "KNOWWE.editCommons.hideAjaxLoader();"
									+ "KNOWWE.notification.success(null, 'Copied thread dump to clipboard', 'thread-dump.copy', 3000);",
							Map.of("type", THREAD_DUMP)),
					Tool.ActionType.ONCLICK, Tool.CATEGORY_EDIT);
			return new Tool[] { readOnlyTool, threadDumpTool };
		}
		else {
			return null;
		}
	}

	@Override
	public boolean hasTools(Section<?> section, UserContext userContext) {
		return userContext.userIsAdmin();
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		if (!context.userIsAdmin()) {
			fail(context, HttpServletResponse.SC_FORBIDDEN, "This method is only available for administrators");
		}

		context.setContentType(Action.PLAIN_TEXT);
		Writer writer = context.getWriter();

		String type = context.getParameter("type");
		if (THREAD_DUMP.equals(type)) {
			writer.append(KnowWEUtils.getThreadDump());
			writer.close();
		} else {
			failUnexpected(context, "Unknown tool type: " + type);
		}
	}

}
