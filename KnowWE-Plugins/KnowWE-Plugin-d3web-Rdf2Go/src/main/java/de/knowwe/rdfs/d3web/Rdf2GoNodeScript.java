/*
 * Copyright (C) 2013 denkbares GmbH
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

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;

import com.denkbares.strings.Identifier;
import de.d3web.diaFlux.flow.Node;
import de.d3web.we.kdom.action.ContraIndicationAction;
import de.d3web.we.kdom.action.InstantIndication;
import de.d3web.we.kdom.action.QASetIndicationAction;
import de.d3web.we.kdom.action.RepeatedIndication;
import de.d3web.we.kdom.action.SetQNumFormulaAction;
import de.d3web.we.kdom.action.SetQuestionNumValueAction;
import de.d3web.we.kdom.action.SetQuestionValue;
import de.d3web.we.kdom.action.SolutionValueAssignment;
import de.d3web.we.object.D3webTermReference;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.diaflux.FlowchartSubTreeHandler;
import de.knowwe.diaflux.type.ActionType;
import de.knowwe.diaflux.type.CommentType;
import de.knowwe.diaflux.type.DecisionType;
import de.knowwe.diaflux.type.ExitType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.NodeContentType;
import de.knowwe.diaflux.type.NodeType;
import de.knowwe.diaflux.type.SnapshotType;
import de.knowwe.diaflux.type.StartType;
import de.knowwe.ontology.compile.OntologyCompileScript;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.rdf2go.Rdf2GoCore;
import de.knowwe.rdf2go.utils.Rdf2GoUtils;

/**
 * @author Volker Belli (denkbares GmbH)
 * @created 12.08.2014
 */
public class Rdf2GoNodeScript extends OntologyCompileScript<NodeType> {

	@Override
	public void compile(OntologyCompiler compiler, Section<NodeType> section) throws CompilerMessage {
		Section<FlowchartType> flowSection = Sections.ancestor(section, FlowchartType.class);
		Identifier flowIdentifier = FlowchartType.getFlowchartTermIdentifier(compiler, flowSection);
		String nodeID = FlowchartSubTreeHandler.getNodeID(section);
		Identifier identifier = new Identifier(flowIdentifier, nodeID);

		IRI termIdentifierIRI = Rdf2GoD3webUtils.registerTermDefinition(compiler, section, identifier, Node.class);

		Rdf2GoCore core = compiler.getRdf2GoCore();
		List<Statement> statements = new ArrayList<>();

		// rdf:type
		String nodeClass = getNodeClass(section);
		Rdf2GoUtils.addStatement(core, termIdentifierIRI, RDF.TYPE,
				nodeClass, statements);

		// lns:hasFlow
		Rdf2GoUtils.addStatement(core, termIdentifierIRI, core.createLocalIRI("hasFlow"),
				Rdf2GoD3webUtils.getTermIRI(compiler, flowIdentifier), statements);

		// lns:hasObject
		for (Section<D3webTermReference> reference : Sections.successors(section, D3webTermReference.class)) {
			Rdf2GoUtils.addStatement(core, termIdentifierIRI, core.createLocalIRI("hasObject"),
					Rdf2GoD3webUtils.getTermIRI(compiler, reference), statements);
		}

		core.addStatements(section, Rdf2GoUtils.toArray(statements));
	}

	private String getNodeClass(Section<NodeType> section) {
		Section<?> parent = Sections.successor(section, NodeContentType.class);
		if (parent == null) return "Node";
		if (hasSuccessor(parent, StartType.class)) return "StartNode";
		if (hasSuccessor(parent, ExitType.class)) return "EditNode";
		if (hasSuccessor(parent, CommentType.class)) return "CommentNode";
		if (hasSuccessor(parent, SnapshotType.class)) return "SnapshotNode";
		if (hasSuccessor(parent, DecisionType.class)) return "DecisionNode";

		Section<ActionType> action = Sections.successor(parent, ActionType.class);
		if (action != null) {
			if (hasSuccessor(action, SolutionValueAssignment.class)) return "SolutionNode";
			if (hasSuccessor(action, SetQuestionNumValueAction.class)) return "AssignNode";
			if (hasSuccessor(action, SetQNumFormulaAction.class)) return "AssignNode";
			if (hasSuccessor(action, SetQuestionValue.class)) return "AssignNode";
			if (hasSuccessor(action, ContraIndicationAction.class)) return "AskNode";
			if (hasSuccessor(action, InstantIndication.class)) return "AskNode";
			if (hasSuccessor(action, RepeatedIndication.class)) return "AskNode";
			if (hasSuccessor(action, QASetIndicationAction.class)) return "AskNode";
		}

		return "Node";
	}

	private static boolean hasSuccessor(Section<?> parent, Class<? extends Type> clazz) {
		return Sections.successor(parent, clazz) != null;
	}

	@Override
	public void destroy(OntologyCompiler compiler, Section<NodeType> section) {
		Section<FlowchartType> flowSection = Sections.ancestor(section, FlowchartType.class);
		Identifier flowIdentifier = FlowchartType.getFlowchartTermIdentifier(compiler, flowSection);
		//noinspection ConstantConditions
		Identifier identifier = new Identifier(flowIdentifier, FlowchartSubTreeHandler.getNodeID(section));

		compiler.getRdf2GoCore().removeStatements(section);
		Rdf2GoD3webUtils.unregisterTermDefinition(compiler, section, identifier, Node.class);
	}
}
