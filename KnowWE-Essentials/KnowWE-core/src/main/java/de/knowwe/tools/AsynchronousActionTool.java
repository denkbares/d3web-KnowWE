/*
 * Copyright (C) 2016 denkbares GmbH, Germany
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

package de.knowwe.tools;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.denkbares.strings.Strings;
import de.knowwe.core.action.Action;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.util.Icon;

import static de.knowwe.core.Attributes.*;

/**
 * A tool causing the given action being called asynchronously passing the given section id, waits for action completion
 * and triggers a page reload.
 *
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 24.03.16.
 */
public class AsynchronousActionTool extends DefaultTool {

	public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section) {
		this(icon, title, description, action, section, Collections.emptyMap());
	}

	public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, Map<String, String> params) {
		super(icon, title, description,
				buildJsAction(action, section, params),
				Tool.ActionType.ONCLICK, null);
	}

	public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, Map<String, String> params, String category) {
		super(icon, title, description,
				buildJsAction(action, section, params),
				Tool.ActionType.ONCLICK, category);
	}

	public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, String redirectPage) {
		this(icon, title, description, action, section, redirectPage, null);
	}

	public AsynchronousActionTool(Icon icon, String title, String description, Class<? extends Action> action, Section<?> section, String redirectPage, String category) {
		super(icon, title, description,
				buildJsAction(action, section, "window.location='Wiki.jsp?page=" + redirectPage + "'",
						Collections.singletonMap(REDIRECT_PAGE, redirectPage)),
				Tool.ActionType.ONCLICK, category);
	}

	public static String buildJsAction(Class<? extends Action> action, Section<?> section, Map<String, String> params) {
		return buildJsAction(action, section, "window.location.reload()", params);
	}

	public static String buildJsAction(Class<? extends Action> action, Section<?> section, String successFunction, Map<String, String> params) {
		return "KNOWWE.editCommons.showAjaxLoader(); " +
				"jq$.ajax({url : 'action/" + action.getSimpleName() + "', " +
				"cache : false, " +
				"data : " + createData(section, params) + "," +
				"success : function(response) {" + successFunction + "}," +
				"error: function(xhr) {KNOWWE.notification.error_jqXHR(xhr);KNOWWE.editCommons.hideAjaxLoader();} })";
	}

	private static String createData(Section<?> section, Map<String, String> params) {
		// expand the parameters by section id and article name
		params = new HashMap<>(params);
		params.put(SECTION_ID, section.getID());
		params.put(TITLE, section.getTitle());

		// we create the JSON manually, because we need single quotes
		StringBuilder builder = new StringBuilder(256);
		builder.append("{");
		params.forEach((key, value) -> {
			builder.append(Strings.quote(key, '\'')).append(":")
					.append(Strings.quote(value, '\'')).append(",");
		});
		builder.append("}");
		return builder.toString();
	}
}
