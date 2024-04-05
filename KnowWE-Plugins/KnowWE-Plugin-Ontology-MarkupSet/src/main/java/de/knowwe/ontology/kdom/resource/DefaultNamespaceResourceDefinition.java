/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.kdom.resource;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleDefinition;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;
import de.knowwe.ontology.kdom.OntologyUtils;
import de.knowwe.ontology.kdom.namespace.Namespace;

/**
 * Type for a resource definition that stands independent of its namespace. As namespace the default namespace of the
 * ontology is used. If no default namespace is set, we use namespace lns.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 04.10.18
 */
public class DefaultNamespaceResourceDefinition extends SimpleDefinition {

	protected static final String IDENTIFIER_KEY = "identifierKey";

	public DefaultNamespaceResourceDefinition(Class<?> termClass) {
		super(OntologyCompiler.class, termClass, Priority.LOWEST);
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.setRenderer(StyleRenderer.QUESTION);
	}

	public static Identifier getDefaultNamespaceTermIdentifier(TermCompiler compiler, Section<? extends Term> section) {
		Identifier identifier = (Identifier) section.getObject(IDENTIFIER_KEY);
		if (identifier == null) {
			Namespace defaultNamespace = null;
			if (compiler instanceof OntologyCompiler) {
				defaultNamespace = OntologyUtils.getDefaultNamespace((OntologyCompiler) compiler);
			}

			String abbreviation = defaultNamespace == null ? "lns" : defaultNamespace.getAbbreviation();
			identifier = new Identifier(abbreviation, section.get().getTermName(section));
			section.storeObject(IDENTIFIER_KEY, identifier);
		}
		return identifier;
	}

	@Override
	public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> section) {
		return getDefaultNamespaceTermIdentifier(compiler, section);
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		// we don't want resource to be quoted by interface's default implementation
		return newIdentifier.getLastPathElement();
	}
}
