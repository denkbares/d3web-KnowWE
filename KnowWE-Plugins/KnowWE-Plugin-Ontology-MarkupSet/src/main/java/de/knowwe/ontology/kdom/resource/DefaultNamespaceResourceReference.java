/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.kdom.resource;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.objects.SimpleReference;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.ontology.compile.OntologyCompiler;

/**
 * Type for a resource reference that stands independent of its namespace. As namespace the default namespace of the
 * ontology is used. If no default namespace is set, we use namespace lns.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 04.10.18
 */
public class DefaultNamespaceResourceReference extends SimpleReference {

	public DefaultNamespaceResourceReference(Class<?> termClass) {
		super(OntologyCompiler.class, termClass, Priority.LOWEST);
		this.setSectionFinder(new AllTextFinderTrimmed());
		this.setRenderer(StyleRenderer.Question);
	}

	@Override
	public Identifier getTermIdentifier(TermCompiler compiler, Section<? extends Term> section) {
		return DefaultNamespaceResourceDefinition.getDefaultNamespaceTermIdentifier(section);
	}

	@Override
	public String getSectionTextAfterRename(Section<? extends RenamableTerm> section, Identifier oldIdentifier, Identifier newIdentifier) {
		// we don't want resource to be quoted by interface's default implementation
		return newIdentifier.getLastPathElement();
	}
}
