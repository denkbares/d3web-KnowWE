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

package de.knowwe.rdfs.vis.edit;

import java.io.IOException;
import java.util.Iterator;

import org.json.JSONArray;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;

import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdfs.vis.util.Utils;

/**
 * @author Johanna Latt
 * @created 27.11.2014
 */
public class FindLiteralsForConceptAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String sectionID = context.getParameter("kdomid");
		String conceptName = context.getParameter("concept");
		Section<?> section = Sections.get(sectionID);

		JSONArray literals = new JSONArray();

		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		if (compiler == null) throw new IOException("No compiler found");

		Rdf2GoCore rdfRepository = compiler.getRdf2GoCore();

		if (!conceptName.contains("ONTOVIS-LITERAL")) {
			String query = "SELECT ?y ?z WHERE { " + conceptName + " ?y ?z. FILTER isLiteral(?z) }";
			Iterator<BindingSet> result =
					rdfRepository.sparqlSelect(query).getBindingSets().iterator();
			while (result.hasNext()) {
				BindingSet row = result.next();
				Value yNode = row.getValue("y");
				Value zNode = row.getValue("z");

				String rel = Utils.getConceptName(yNode, rdfRepository);
				String literal = zNode.stringValue();

				if (literal.contains("@")) {
					String[] labelAndLanguage = literal.split("@");
					literal = labelAndLanguage[0];
					rel += " (" + labelAndLanguage[1] + ")";
				}

				JSONArray array = new JSONArray();
				array.put(rel);
				array.put(literal);

				literals.put(array);
			}
		}

		context.getOutputStream().write(literals.toString().getBytes());
	}
}
