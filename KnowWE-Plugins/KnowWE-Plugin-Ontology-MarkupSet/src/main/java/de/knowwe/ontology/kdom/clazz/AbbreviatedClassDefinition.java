/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.ontology.kdom.clazz;

import java.util.Collection;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.compile.OntologyHandler;
import de.knowwe.ontology.kdom.resource.AbbreviatedResourceDefinition;
import de.knowwe.rdf2go.Rdf2GoCore;

import static org.eclipse.rdf4j.model.vocabulary.RDF.*;
import static org.eclipse.rdf4j.model.vocabulary.RDFS.*;

public class AbbreviatedClassDefinition extends AbbreviatedResourceDefinition {

	public AbbreviatedClassDefinition() {
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.addCompileScript(new AbbreviatedClassHandler());
	}

	public org.eclipse.rdf4j.model.URI getClassNameURI(Rdf2GoCore core, Section<AbbreviatedClassDefinition> section) {
		return super.getResourceURI(core, section);
	}

	private class AbbreviatedClassHandler extends OntologyHandler<AbbreviatedClassDefinition> {

		@Override
		public Collection<Message> create(OntologyCompiler compiler, Section<AbbreviatedClassDefinition> section) {

			Rdf2GoCore core = Rdf2GoCore.getInstance(compiler);

			org.eclipse.rdf4j.model.URI classNameURI = getClassNameURI(core, section);

			org.eclipse.rdf4j.model.Statement classStatement = core.createStatement(classNameURI, TYPE, CLASS);
			core.addStatements(section, classStatement);

			return Messages.noMessage();
		}

		@Override
		public void destroy(OntologyCompiler compiler, Section<AbbreviatedClassDefinition> section) {
			Rdf2GoCore.getInstance(compiler).removeStatements(section);
		}

	}

}
