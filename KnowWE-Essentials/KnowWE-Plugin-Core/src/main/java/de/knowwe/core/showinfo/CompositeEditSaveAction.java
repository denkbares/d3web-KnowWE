/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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
package de.knowwe.core.showinfo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Sections;

/**
 * Saves the changed sections made in Composite Edit
 *
 * @author Stefan Plehn
 * @created 09.01.2014
 */
public class CompositeEditSaveAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {

		String replaceSectionsString = context.getParameter("replaceSections");
		try {
			JSONArray replaceSections = new JSONArray(replaceSectionsString);

			// errors and security are handled inside
			// replaceKDOMNodesSaveAndBuild
			Map<String, String> nodesMap = new HashMap<>();
			for (int i = 0; i < replaceSections.length(); i++) {
				JSONObject explrObject = replaceSections.getJSONObject(i);
				String id = explrObject.get("id").toString();
				String text = explrObject.get("text").toString();
				nodesMap.put(id, text);
			}

			//
			Sections.replace(context, nodesMap).sendErrors(context);
			Compilers.awaitTermination(context.getArticleManager().getCompilerManager());
		}
		catch (JSONException e) {
			// TODO Auto-generated catch block
			throw new IOException(e);
		}

	}

}