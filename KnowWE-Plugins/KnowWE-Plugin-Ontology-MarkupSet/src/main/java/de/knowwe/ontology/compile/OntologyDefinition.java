/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.compile.PackageRegistrationCompiler;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.terminology.RenamableTerm;
import de.knowwe.core.compile.terminology.TermCompiler;
import de.knowwe.core.kdom.AbstractType;
import de.knowwe.core.kdom.objects.SimpleDefinitionRegistrationScript;
import de.knowwe.core.kdom.objects.Term;
import de.knowwe.core.kdom.objects.TermDefinition;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.kdom.renderer.StyleRenderer;
import de.knowwe.rdf2go.Rdf2GoCore;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Volker Belli (denkbares GmbH)
 * @created 01.04.2019
 */
public class OntologyDefinition extends AbstractType implements TermDefinition, RenamableTerm {

	public OntologyDefinition() {
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		setRenderer(StyleRenderer.CONSTANT.withToolMenu());
		addCompileScript(Priority.HIGHER, new SimpleDefinitionRegistrationScript<>(PackageRegistrationCompiler.class));
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return Rdf2GoCore.class;
	}

	@NotNull
	public OntologyCompiler getCompiler(Section<? extends OntologyDefinition> self) {
		return Objects.requireNonNull($(self).ancestor(OntologyType.class).mapFirst(OntologyType::getCompiler));
	}

	@Override
	public Identifier getTermIdentifier(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return new Identifier(getTermObjectClass(compiler, section).getSimpleName(), getTermName(section));
	}
}
