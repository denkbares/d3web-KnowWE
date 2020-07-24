/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.knowledgebase;

import java.util.Collection;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.denkbares.strings.Identifier;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.object.D3webTerm;
import de.d3web.we.utils.D3webUtils;
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

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * Class that references a named knowledge base within the wiki.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 01.04.2019
 */
public class KnowledgeBaseReference extends AbstractType implements TermReference, D3webTerm<KnowledgeBase>, RenamableTerm {

	public static final Class<KnowledgeBase> TERM_CLASS = KnowledgeBase.class;

	public KnowledgeBaseReference() {
		setSectionFinder(AllTextFinderTrimmed.getInstance());
		setRenderer(StyleRenderer.CONSTANT.withToolMenu());
		addCompileScript(Priority.HIGH, new SimpleReferenceRegistrationScript<>(PackageRegistrationCompiler.class));
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return TERM_CLASS;
	}

	@NotNull
	private static Identifier toIdentifier(String termName) {
		return new Identifier(TERM_CLASS.getSimpleName(), termName); // to avoid conflicts, we add class name
	}

	@Override
	public KnowledgeBase getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<KnowledgeBase>> section) {
		return D3webUtils.getKnowledgeBase(compiler);
	}

	public Sections<KnowledgeBaseDefinition> getDefinition(Section<? extends KnowledgeBaseReference> self) {
		DefaultGlobalCompiler compiler = Compilers.getPackageRegistrationCompiler(self);
		return Sections.definitions(compiler, self).filter(KnowledgeBaseDefinition.class);
	}

	public static Sections<KnowledgeBaseDefinition> getDefinition(ArticleManager manager, String knowledgeBaseName) {
		PackageRegistrationCompiler compiler = Compilers.getPackageRegistrationCompiler(manager);
		Collection<Section<?>> termDefiningSections = compiler.getTerminologyManager()
				.getTermDefiningSections(toIdentifier(knowledgeBaseName));
		return $(Stream.concat($(termDefiningSections).filter(KnowledgeBaseDefinition.class).stream(),
				$(manager.getArticle(knowledgeBaseName)).successor(KnowledgeBaseDefinition.class).stream()));
	}

	@Override
	public Identifier getTermIdentifier(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return toIdentifier(getTermName(section));
	}

}
