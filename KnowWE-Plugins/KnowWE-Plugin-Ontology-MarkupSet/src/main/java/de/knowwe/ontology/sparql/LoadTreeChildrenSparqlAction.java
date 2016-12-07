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

package de.knowwe.ontology.sparql;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Fetches children of a parent in a tree table
 *
 * Created by Jonas Mueller (denkbares GmbH) on 23.11.2016.
 */
public class LoadTreeChildrenSparqlAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String parentNodeID = context.getParameter(Attributes.PARENT_NODE_ID);
		Section<?> section = Sections.get(context.getParameter(Attributes.SECTION_ID));

		RenderResult result = new RenderResult(context);
		SparqlResultRenderer.getInstance()
				.getTreeChildren(Sections.cast(section, SparqlType.class), parentNodeID, context, result);
		context.getWriter().append(result.toString());
	}
}
