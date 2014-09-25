/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.knowwe.rdf2go.sparql;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.rdf2go.model.Statement;

import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.DefaultGlobalCompiler.DefaultGlobalScript;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinder;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.kdom.renderer.AsynchronRenderer;
import de.knowwe.rdf2go.Rdf2GoCore;

public class SparqlContentType extends AbstractType {

	public SparqlContentType() {
		this.setSectionFinder(AllTextFinder.getInstance());
		this.setRenderer(new AsynchronRenderer(new SparqlContentRenderer()));
		this.addCompileScript(new SparqlConstructHandler());
	}

	private class SparqlConstructHandler extends DefaultGlobalScript<SparqlContentType> {

		@Override
		public void compile(DefaultGlobalCompiler compiler, Section<SparqlContentType> section) throws CompilerMessage {
			String sparqlString = section.getText();
			sparqlString = sparqlString.trim();
			sparqlString = sparqlString.replaceAll("\n", "");
			sparqlString = sparqlString.replaceAll("\r", "");
			if (sparqlString.toLowerCase().startsWith("construct")) {
				try {
					ClosableIterable<Statement> sparqlConstruct = Rdf2GoCore.getInstance().sparqlConstruct(
							sparqlString);

					for (Statement aSparqlConstruct : sparqlConstruct) {
						Rdf2GoCore.getInstance().addStatements(section, aSparqlConstruct);
					}
				}
				catch (Exception e) {
					throw CompilerMessage.error(e.getMessage());
				}
			}
		}
	}

}
