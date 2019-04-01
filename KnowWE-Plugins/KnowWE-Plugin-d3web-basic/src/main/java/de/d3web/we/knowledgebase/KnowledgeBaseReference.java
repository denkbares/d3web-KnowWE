/*
 * Copyright (C) 2019 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.knowledgebase;

import org.jetbrains.annotations.Nullable;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.we.object.D3webTerm;
import de.d3web.we.utils.D3webUtils;
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

/**
 * Class that references a named knowledge base within the wiki.
 *
 * @author Volker Belli (denkbares GmbH)
 * @created 01.04.2019
 */
public class KnowledgeBaseReference extends AbstractType implements TermReference, D3webTerm<KnowledgeBase>, RenamableTerm {

	public KnowledgeBaseReference() {
		setSectionFinder(new AllTextFinderTrimmed());
		setRenderer(StyleRenderer.CONSTANT.withToolMenu());
		addCompileScript(Priority.HIGH, new SimpleReferenceRegistrationScript<>(DefaultGlobalCompiler.class));
	}

	@Override
	public Class<?> getTermObjectClass(@Nullable TermCompiler compiler, Section<? extends Term> section) {
		return KnowledgeBase.class;
	}

	@Override
	public KnowledgeBase getTermObject(D3webCompiler compiler, Section<? extends D3webTerm<KnowledgeBase>> section) {
		return D3webUtils.getKnowledgeBase(compiler);
	}

	public Sections<KnowledgeBaseDefinition> getDefinition(Section<? extends KnowledgeBaseReference> self) {
		DefaultGlobalCompiler compiler = Compilers.getGlobalCompiler(self);
		return Sections.definitions(compiler, self).filter(KnowledgeBaseDefinition.class);
	}
}
