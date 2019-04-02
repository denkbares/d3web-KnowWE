/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.SimpleReferenceRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermReference;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * Class that references a named knowledge base within the wiki.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 01.04.2019
 */
public class OntologyReference extends AbstractType implements TermReference, RenamableTerm {

	public OntologyReference() {
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		setRenderer(StyleRenderer.CONSTANT.withToolMenu());
		addCompileScript(Priority.HIGH, new SimpleReferenceRegistrationScript<>(DefaultGlobalCompiler.class));
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return Rdf2GoCore.class;
	}

	public Sections<OntologyDefinition> getDefinition(Section<? extends OntologyReference> self) {
		DefaultGlobalCompiler compiler = Compilers.getGlobalCompiler(self);
		return Sections.definitions(compiler, self).filter(OntologyDefinition.class);
	}
}
