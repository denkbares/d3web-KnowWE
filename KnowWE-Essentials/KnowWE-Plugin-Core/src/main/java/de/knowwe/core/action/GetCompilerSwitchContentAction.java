/*
 * Copyright (C) 2020 denkbares GmbH, Germany
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
 *
 */

package de.knowwe.core.action;

import java.io.IOException;
import java.util.Collection;

import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.GroupingCompiler;
import de.knowwe.core.utils.KnowWEUtils;

/**
 * Action which returns the content for the compiler switch dropdown menu
 *
 * @author Veronika Sehne (denkbares GmbH)
 * @created 09.11.20
 */
public class GetCompilerSwitchContentAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		Collection<GroupingCompiler> compilers = Compilers.getCompilers(KnowWEUtils.getArticleManager(context.getWeb()), GroupingCompiler.class);
		if (compilers.isEmpty()) return;

		StringBuilder builderLink = new StringBuilder();
		StringBuilder builderList = new StringBuilder();
		builderLink.append("<span class=\"far fa-microchip\"></span>");
		String defaultName = null;
		for (GroupingCompiler compiler : compilers) {
			String name = Compilers.getCompilerName(compiler);
			if (Compilers.isDefaultCompiler(context, compiler)) {
				// sets the current selected compiler name
				defaultName = name;
			}
		}

		for (GroupingCompiler compiler : (compilers)) {
			String name = Compilers.getCompilerName(compiler);
			if (defaultName == null) {
				defaultName = name;
			}
			if (defaultName.equals(name)) {
				defaultName = name;
				builderLink.append("<span>").append(name).append("</span>");
			}
			else {
				// add all other compilers to the list
				builderList.append("<li>")
						.append("<a onclick=\"KNOWWE.core.plugin.switchCompiler.setDefaultCompiler('")
						.append(name)
						.append("')\">")
						.append(name)
						.append("</a>")
						.append("</li>");
			}
		}
		if (compilers.size() > 1) {
			// only add caret if there are more than one selectable compilers
			builderLink.append("<span class=\"caret\"></span>\n");
		}

		// send the inner html back to the client
		JSONObject response = new JSONObject();
		try {
			response.put("link", builderLink.toString());
			response.put("list", builderList.toString());
			response.write(context.getWriter());
		}
		catch (JSONException e) {
			throw new IOException(e);
		}
	}
}
