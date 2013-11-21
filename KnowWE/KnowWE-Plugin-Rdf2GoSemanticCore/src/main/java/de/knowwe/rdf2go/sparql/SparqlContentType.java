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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.Statement;

import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextSectionFinder;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.renderer.AsynchronRenderer;
import de.knowwe.rdf2go.RDF2GoSubtreeHandler;
import de.knowwe.rdf2go.Rdf2GoCore;

public class SparqlContentType extends AbstractType {

	public SparqlContentType() {
		this.setSectionFinder(AllTextSectionFinder.getInstance());
		this.setRenderer(new AsynchronRenderer(new SparqlMarkupRenderer()));
		this.addSubtreeHandler(new SparqlConstructHandler());
	}

	private class SparqlConstructHandler extends RDF2GoSubtreeHandler<SparqlContentType> {

		@Override
		public Collection<Message> create(Article article, Section<SparqlContentType> section) {
			List<Message> msgs = new ArrayList<Message>(1);
			String sparqlString = section.getText();
			sparqlString = sparqlString.trim();
			sparqlString = sparqlString.replaceAll("\n", "");
			sparqlString = sparqlString.replaceAll("\r", "");
			if (sparqlString.toLowerCase().startsWith("construct")) {
				try {
					ClosableIterable<Statement> sparqlConstruct = Rdf2GoCore.getInstance().sparqlConstruct(
							sparqlString);
					ClosableIterator<Statement> statementIterator = sparqlConstruct.iterator();

					while (statementIterator.hasNext()) {
						Rdf2GoCore.getInstance().addStatements(section, statementIterator.next());
					}
				}
				catch (Exception e) {
					msgs.add(Messages.error(e.getMessage()));
				}
			}
			return msgs;
		}
	}

}
