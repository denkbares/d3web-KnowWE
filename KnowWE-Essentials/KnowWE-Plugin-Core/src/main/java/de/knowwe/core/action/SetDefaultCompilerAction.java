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

/**
 * Action to set the grouping compiler as default compiler
 *
 * @author Veronika Sehne (denkbares GmbH)
 * @created 11.11.20
 */
public class SetDefaultCompilerAction extends AbstractAction {
	@Override
	public void execute(UserActionContext context) throws IOException {
		String name = context.getParameter("name");
		Collection<GroupingCompiler> compilers = Compilers.getCompilers(context.getArticleManager(), GroupingCompiler.class);
		compilers.stream()
				.filter(c -> Compilers.getCompilerName(c).equals(name))
				.findFirst()
				.ifPresent(compiler -> Compilers.markSelfAndChildrenCompilersAsDefault(context, compiler));

		JSONObject response = new JSONObject();
		try {
			response.write(context.getWriter());
		}
		catch (JSONException e) {
			throw new IOException(e);
		}
	}
}
