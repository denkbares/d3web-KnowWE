/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */
package de.knowwe.d3web.ontology.bridge;

import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.TerminologyManager;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.CompilerMessage;
import de.knowwe.core.report.Messages;
import de.knowwe.ontology.compile.OntologyCompiler;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 08.02.2012
 */
public class BridgedReferenceRegistrationScript extends SimpleReferenceRegistrationScript<D3webCompiler> {

	private final Priority definitionPriority;

	/**
	 * Creates a new compile script for the given compiler, automatically validating that the term is defined in the
	 * bridged ontology of the d3web compiler.
	 */
	public BridgedReferenceRegistrationScript() {
		this(null);
	}

	/**
	 * Creates a new compile script for the given compiler. If validate is set to false, the script will register
	 * without checking for the validity of the reference. Otherwise it validates that the term is defined in the
	 * bridged ontology of the d3web compiler.
	 */
	public BridgedReferenceRegistrationScript(boolean validate) {
		this(null, validate);
	}

	/**
	 * Creates a new compile script for the given compiler, automatically validating that the term is defined in the
	 * bridged ontology of the d3web compiler.
	 */
	public BridgedReferenceRegistrationScript(Priority definitionPriority) {
		super(D3webCompiler.class);
		this.definitionPriority = definitionPriority;
	}

	/**
	 * Creates a new compile script for the given compiler. If validate is set to false, the script will register
	 * without checking for the validity of the reference. Otherwise it validates that the term is defined in the
	 * bridged ontology of the d3web compiler.
	 */
	public BridgedReferenceRegistrationScript(Priority definitionPriority, boolean validate) {
		super(D3webCompiler.class, validate);
		this.definitionPriority = definitionPriority;
	}

	@Override
	public void compile(D3webCompiler compiler, Section<Term> section) throws CompilerMessage {
		// apply some explicit error handling if the ontology is not properly linked
		if (!OntologyBridge.hasOntology(compiler)) {
			throw CompilerMessage.error("No ontology linked to the given d3web compiler");
		}
		if (definitionPriority != null) {
			try {
				OntologyCompiler ontology = OntologyBridge.getOntology(compiler);
				compiler.getCompilerManager().awaitCompilePriorityCompleted(ontology, definitionPriority);
			}
			catch (InterruptedException e) {
				throw new CompilerMessage(Messages.error(e));
			}
		}
		super.compile(compiler, section);
	}

	@Override
	protected TerminologyManager getTerminologyManager(D3webCompiler compiler) {
		OntologyCompiler ontology = OntologyBridge.getOntology(compiler);
		return (ontology == null) ? null : ontology.getTerminologyManager();
	}
}
