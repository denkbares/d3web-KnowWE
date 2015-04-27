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
import java.util.HashMap;
import java.util.Map;

import de.d3web.strings.Identifier;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.LinkToTermDefinitionProvider;
import de.knowwe.core.utils.PackageCompileLinkToTermDefinitionProvider;
import de.knowwe.rdf2go.Rdf2GoCompiler;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;
import de.knowwe.rdfs.vis.OntoGraphDataBuilder;
import de.knowwe.rdfs.vis.markup.sparql.SparqlVisualizationType;
import de.knowwe.rdfs.vis.util.Utils;

/**
 * @author Johanna Latt
 * @created 13.09.2014
 */
public class GoToDefinitionAction extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		String sectionID = context.getParameter("kdomid");
		String conceptName = context.getParameter("concept");
		Section<?> section = Sections.get(sectionID);

		Rdf2GoCompiler compiler = Compilers.getCompiler(section, Rdf2GoCompiler.class);
		LinkToTermDefinitionProvider uriProvider;

		if (compiler == null) {
			// TODO: completely remove dependency to IncrementalCompiler
			try {
				uriProvider = (LinkToTermDefinitionProvider) Class.forName(
						"de.knowwe.compile.utils.IncrementalCompilerLinkToTermDefinitionProvider")
						.newInstance();
			}
			catch (Exception e) {
				uriProvider = new LinkToTermDefinitionProvider() {
					@Override
					public String getLinkToTermDefinition(Identifier name, String masterArticle) {
						return null;
					}
				};
			}
		} else {
			uriProvider = new PackageCompileLinkToTermDefinitionProvider();
		}

		Map<String, String> parameterMap = new HashMap<>();
		SparqlVisualizationType.readParameterFromAnnotation(SparqlVisualizationType.ANNOTATION_LINK_MODE, section, OntoGraphDataBuilder.LINK_MODE, parameterMap, SparqlVisualizationType.LinkMode.browse
				.name());

		String uri =  Rdf2GoUtils.expandNamespace(compiler.getRdf2GoCore(), conceptName);

		String link = Utils.createConceptURL(conceptName, parameterMap, section, uriProvider, uri);

		context.getOutputStream().write(link.toString().getBytes());
	}
}
