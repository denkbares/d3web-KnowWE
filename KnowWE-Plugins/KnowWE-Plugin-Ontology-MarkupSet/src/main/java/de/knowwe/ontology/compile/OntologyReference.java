/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.knowwe.ontology.compile;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.knowwe.core.ArticleManager;
import de.knowwe.core.compile.Compilers;
import de.knowwe.core.compile.DefaultGlobalCompiler;
import de.knowwe.core.compile.PackageRegistrationCompiler;
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

import static de.knowwe.core.kdom.parsing.Sections.$;

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
		addCompileScript(Priority.HIGH, new SimpleReferenceRegistrationScript<>(PackageRegistrationCompiler.class));
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return Rdf2GoCore.class;
	}

	public Sections<OntologyDefinition> getDefinition(Section<? extends OntologyReference> self) {
		DefaultGlobalCompiler compiler = Compilers.getPackageRegistrationCompiler(self);
		return Sections.definitions(compiler, self).filter(OntologyDefinition.class);
	}

	public static Sections<OntologyDefinition> getDefinition(ArticleManager manager, String ontologyName) {
		PackageRegistrationCompiler compiler = Compilers.getPackageRegistrationCompiler(manager);
		Collection<Section<?>> termDefiningSections = compiler.getTerminologyManager()
				.getTermDefiningSections(toIdentifier(ontologyName));
		return $(termDefiningSections).filter(OntologyDefinition.class);
	}

	@Override
	public Identifier getTermIdentifier(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return toIdentifier(getTermName(section));
	}

	@NotNull
	private static Identifier toIdentifier(String termName) {
		return new Identifier(Rdf2GoCore.class.getSimpleName(), termName); // to avoid conflicts, we add class name
	}
}
