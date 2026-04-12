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

package de.knowwe.rdfs.d3web;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.CompileScript;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DestroyScript;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.dashtree.DashTreeTermRelationScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * This {@link CompileScript} adds all relations defined by dash trees to the triple store.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 12.03.2014
 */
public abstract class Rdf2GoDashTreeTermRelationScript extends DashTreeTermRelationScript<OntologyCompiler> implements DestroyScript<OntologyCompiler, TermDefinition> {

	@Override
	protected void createObjectRelations(Section<TermDefinition> parentSection, OntologyCompiler compiler, Identifier parentIdentifier, List<Identifier> childrenIdentifier) {

		// since we also have to destroy all defining sections, we also have to compile all defining sections
		compiler.getTerminologyManager()
				.getTermDefiningSections(parentSection.get().getTermIdentifier(compiler, parentSection))
				.forEach(definition -> Compilers.recompileSection(compiler, definition, this.getClass()));

		Rdf2GoCore core = compiler.getRdf2GoCore();
		IRI parentIRI = core.createLocalIRI(Rdf2GoUtils.getCleanedExternalForm(parentIdentifier));
		IRI hasChildIRI = core.createLocalIRI("hasChild");
		List<Statement> statements = new ArrayList<>();
		boolean hasParent = Rdf2GoD3webUtils.hasParentDashTreeElement(compiler, parentIdentifier);
		if (!hasParent) {
			IRI rootIRI = getRootIRI(core);
			Rdf2GoUtils.addStatement(core, parentIRI, RDFS.SUBCLASSOF, rootIRI, statements);
			Rdf2GoUtils.addStatement(core, rootIRI, hasChildIRI, parentIRI, statements);
		}
		int index = 0;
		for (Identifier childIdentifier : childrenIdentifier) {
			IRI childIRI = core.createLocalIRI(Rdf2GoUtils.getCleanedExternalForm(childIdentifier));
			Rdf2GoUtils.addStatement(core, childIRI, RDFS.SUBCLASSOF, parentIRI, statements);
			Rdf2GoUtils.addStatement(core, parentIRI, hasChildIRI, childIRI, statements);

			BNode indexNode = core.createBlankNode();
			IRI hasIndexInfoIRI = core.createLocalIRI("hasIndexInfo");
			Rdf2GoUtils.addStatement(core, childIRI, hasIndexInfoIRI, indexNode, statements);
			IRI hasIndexIRI = core.createLocalIRI("hasIndex");
			Literal indexLiteral = core.createDatatypeLiteral(Integer.toString(index++), XMLSchema.INTEGER);
			Rdf2GoUtils.addStatement(core, indexNode, hasIndexIRI, indexLiteral, statements);
			IRI indexOfIRI = core.createLocalIRI("isIndexOf");
			Rdf2GoUtils.addStatement(core, indexNode, indexOfIRI, parentIRI, statements);
		}
		core.addStatements(parentSection, Rdf2GoUtils.toArray(statements));

	}

	protected abstract IRI getRootIRI(Rdf2GoCore core);

	@Override
	public Class<OntologyCompiler> getCompilerClass() {
		return OntologyCompiler.class;
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<TermDefinition> section) {
		compiler.getRdf2GoCore().removeStatements(section);
		if (section.getObject(compiler, RELATIONS_ADDED) == null) return;
		// we don't exactly know where the relations were added, so we destroy all defining sections
		Sections.definitions(compiler, section).forEach(definition -> {
			compiler.addSectionToDestroy(definition, this.getClass());
			definition.storeObject(compiler, RELATIONS_ADDED, null);
		});
	}
}
